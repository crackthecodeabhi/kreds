/*
 *  Copyright (C) 2022 Abhijith Shivaswamy
 *   See the notice.md file distributed with this work for additional
 *   information regarding copyright ownership.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package io.github.crackthecodeabhi.kreds.pipeline

import io.github.crackthecodeabhi.kreds.KredsException
import io.github.crackthecodeabhi.kreds.commands.*
import io.github.crackthecodeabhi.kreds.pipeline.TransactionCommand.*

public class KredsTransactionException internal constructor(message: String) : KredsException(message)

internal enum class TransactionCommand(override val subCommand: Command? = null) : Command {
    MULTI, EXEC, DISCARD, WATCH, UNWATCH;

    override val string = name
}

/**
 * A Pipelined transaction.
 * All the command issued in this transaction are pipelined and executed on [Transaction.exec]
 * Each command returns a [Response] object, which can be queried after transaction execution to retrieve the result
 * of that command.
 */
public interface Transaction : PipelineStringCommands, PipelineKeyCommands, PipelineHashCommands, PipelineSetCommands,
    PipelineListCommands, PipelineHyperLogLogCommands {

    /**
     * ### EXEC
     *
     * Executes all previously queued commands in a transaction and restores the connection state to normal.
     * When using WATCH, EXEC will execute commands only if the watched keys were not modified,
     * allowing for a check-and-set mechanism.
     *
     * [Doc](https://redis.io/commands/exec)
     * @since 1.2.0
     * @throws KredsTransactionException if transaction was aborted when using WATCH or if any command in transaction fails.
     *
     */
    public suspend fun exec()

    /**
     * ### MULTI
     *
     * Marks the start of a transaction block. Subsequent commands will be queued for atomic execution using EXEC.
     *
     * [Doc](https://redis.io/commands/multi)
     * @since 1.2.0
     * @return always OK.
     */
    public suspend fun multi(): Response<String>

    /**
     * ### DISCARD
     *
     * Discards this pipelined transaction. After transaction is discarded, any operation invoked on this transaction
     * throws [KredsTransactionException]
     */
    public suspend fun discard()
}

/*internal class TransactionImpl(private val client: DefaultKredsClient) : ExclusiveObject, Transaction,
    PipelineStringCommandsExecutor, PipelineKeyCommandExecutor, PipelineHashCommandExecutor,
    PipelineListCommandExecutor, PipelineHyperLogLogCommandExecutor, PipelineSetCommandExecutor {

    override val mutex: Mutex = Mutex()

    private var done = false
    private var discarded = false

    private val responseFlow = MutableSharedFlow<List<Any?>>(1)

    private var multiCommandIndex: Int? = null

    private val commands = mutableListOf<CommandExecution>()
    private val commandResponse = mutableListOf<Any?>()

    override suspend fun <T> add(commandExecution: CommandExecution): Response<T> = mutex.withLock {
        if(multiCommandIndex == null) {
            with(commandExecution) {
                if(command != WATCH || command != UNWATCH || command != MULTI)
                    throw KredsTransactionException("Only WATCH, UNWATCH, MULTI commands are allowed " +
                            "before starting the transaction with MULTI command")
            }
        }

        commands.add(commandExecution)

        if(commandExecution.command == MULTI) multiCommandIndex = commands.lastIndex

        Response(responseFlow, commands.lastIndex)
    }
    override suspend fun multi(): Response<String> {
        return if(transactionStarted()) throw KredsTransactionException("Cannot nest multi command inside a transaction.")
        else add(CommandExecution(MULTI, SimpleStringCommandProcessor))
    }

    */
/**
 * Do not call holding [mutex]
 *//*
    private suspend inline fun checkTransactionDiscarded(): Boolean = mutex.withLock {
        if(discarded)  throw KredsTransactionException("Transaction discarded.")
        else false
    }

    */
/**
 * Do not call holding [mutex]
 *//*
    private suspend inline fun transactionStarted(): Boolean = mutex.withLock { multiCommandIndex != null  }

    override suspend fun discard() {
        if(!transactionStarted()) throw KredsTransactionException("Cannot discard transaction which is not started yet.")
        mutex.withLock {
            responseFlow.tryEmit(emptyList())
            discarded = true
        }
    }

    */
/**
 * ### [mutex] should be held while calling this method
 *//*

    private suspend fun executeTransaction(commands: List<CommandExecution>, multiCommandIdx: Int): List<Any?> {
        val responseList = mutableListOf<Any?>()
        val responseMessages = client.executeCommands(commands)
        val beforeMultiCommand = responseMessages.subList(0,multiCommandIdx + 1)
        val afterMultiCommandWithoutExec  = responseMessages.subList(multiCommandIdx+ 1, responseMessages.size).dropLast(1)
        val execResult = responseMessages.last()

        val beforeMultiCommandResponse: List<Any?> = beforeMultiCommand.mapIndexed{ idx, it -> commands[idx].processor.decode(it) }

        val afterMultiCommandResponse: MutableList<Any?> =  afterMultiCommandWithoutExec.map {
            when(it){
                is ErrorRedisMessage -> KredsRedisDataException(it.content(),null,enableSuppression = true, writableStackTrace = false)
                is SimpleStringRedisMessage -> it.content()
                else -> throw KredsTransactionException("Invalid data received from Redis.")
            }
        }.toMutableList()

        return when(execResult) {
            is ErrorRedisMessage -> {
                val execException = KredsRedisDataException(execResult.content(),null, enableSuppression = true, writableStackTrace = false)
                responseList.addAll(beforeMultiCommandResponse)
                responseList.addAll(afterMultiCommandResponse.filter{ it !is KredsRedisDataException }.map { KredsRedisDataException("Transaction failed.",execException, enableSuppression = true,writableStackTrace = false) })
                responseList.add(execException)
                responseList
            }
            is ArrayRedisMessage -> {
                if(execResult.isNull || execResult.children().isEmpty()) throw KredsTransactionException("Invalid data received from Redis.")
                val results = execResult.children()
                responseList.addAll(beforeMultiCommandResponse)
                var idx = 0
                for(i in (multiCommandIdx + 1) until afterMultiCommandResponse.size) {
                    afterMultiCommandResponse[idx] = commands[i].processor.decode(results[idx])
                    ++idx
                }
                responseList.addAll(afterMultiCommandResponse)
                responseList.add(ArrayHandler.doHandle(execResult))
                responseList
            }
            else -> throw KredsTransactionException("Invalid data received from Redis.")
        }
    }

    override suspend fun exec() {
        checkTransactionDiscarded()
        mutex.withLock {
            if(multiCommandIndex == null) throw KredsTransactionException("Start transaction with multi() before exec()")
            if (!done) {
                commands.add(CommandExecution(EXEC, ArrayCommandProcessor))
                commandResponse.addAll(executeTransaction(commands, multiCommandIndex!!))
                done = true
                responseFlow.tryEmit(commandResponse)
            }
        }
    }
}*/
