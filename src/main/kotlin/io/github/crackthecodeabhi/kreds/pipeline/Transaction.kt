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

import io.github.crackthecodeabhi.kreds.ExclusiveObject
import io.github.crackthecodeabhi.kreds.KredsException
import io.github.crackthecodeabhi.kreds.args.createArguments
import io.github.crackthecodeabhi.kreds.commands.*
import io.github.crackthecodeabhi.kreds.connection.DefaultKredsClient
import io.github.crackthecodeabhi.kreds.lockByCoroutineJob
import io.github.crackthecodeabhi.kreds.pipeline.TransactionCommand.*
import io.github.crackthecodeabhi.kreds.protocol.ArrayCommandProcessor
import io.github.crackthecodeabhi.kreds.protocol.ArrayHandler
import io.github.crackthecodeabhi.kreds.protocol.KredsRedisDataException
import io.github.crackthecodeabhi.kreds.protocol.SimpleStringCommandProcessor
import io.netty.handler.codec.redis.ArrayRedisMessage
import io.netty.handler.codec.redis.ErrorRedisMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex

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
    public suspend fun exec(): Unit

    /**
     * ### MULTI
     *
     * Marks the start of a transaction block. Subsequent commands will be queued for atomic execution using EXEC.
     *
     * [Doc](https://redis.io/commands/multi)
     * @since 1.2.0
     */
    public suspend fun multi(): Unit

    /**
     * ### `WATCH key [key ...] `
     *
     * Marks the given keys to be watched for conditional execution of a transaction.
     *
     * [Doc](https://redis.io/commands/watch)
     * @since 2.2.0
     */
    public suspend fun watch(key: String, vararg keys: String): Unit

    /**
     * ### UNWATCH
     *
     * Flushes all the previously watched keys for a transaction.
     * If you call EXEC or DISCARD, there's no need to manually call UNWATCH.
     *
     * [Doc](https://redis.io/commands/unwatch)
     * @since 2.2.0
     */
    public suspend fun unwatch(): Unit

}

internal class TransactionImpl(private val client: DefaultKredsClient) : ExclusiveObject, Transaction,
    PipelineStringCommandsExecutor, PipelineKeyCommandExecutor, PipelineHashCommandExecutor,
    PipelineListCommandExecutor, PipelineHyperLogLogCommandExecutor, PipelineSetCommandExecutor {

    override val mutex: Mutex = Mutex()

    /**
     * Guarded by mutex.
     */
    private var done = false

    /**
     * Guarded by mutex.
     */
    private var transactionStarted = false

    private val responseFlow = MutableSharedFlow<List<Any?>>(1)

    /**
     * Guarded by mutex.
     */
    private val commands = mutableListOf<CommandExecution<*>>()

    private val commandResponse = mutableListOf<Any?>()

    override suspend fun <T> add(commandExecution: CommandExecution<T>, nullable: Boolean): Response<T> =
        lockByCoroutineJob {
            if (!transactionStarted) {
                with(commandExecution) {
                    if (command != WATCH || command != UNWATCH || command != MULTI)
                        throw KredsTransactionException(
                            "Only WATCH, UNWATCH, MULTI commands are allowed " +
                                    "before starting the transaction with MULTI."
                        )
                }
            }

            commands.add(commandExecution)

            Response(responseFlow, commands.lastIndex, nullable)
        }

    override suspend fun multi(): Unit = lockByCoroutineJob {
        if (transactionStarted) throw KredsTransactionException("Cannot nest multi command inside a transaction.")
        else {
            transactionStarted = true
            add(CommandExecution(MULTI, SimpleStringCommandProcessor))
        }
    }

    override suspend fun watch(key: String, vararg keys: String): Unit = lockByCoroutineJob {
        commands.add(CommandExecution(WATCH, SimpleStringCommandProcessor, *createArguments(key, *keys)))
    }

    override suspend fun unwatch(): Unit = lockByCoroutineJob {
        commands.add(CommandExecution(UNWATCH, SimpleStringCommandProcessor))
    }

    private suspend fun executeTransaction(commands: List<CommandExecution<*>>): List<*>? = lockByCoroutineJob {
        val responseList = mutableListOf<Any?>()
        val responseMessages = client.executeCommands(commands)

        return when (val execResult = responseMessages.last()) {
            is ErrorRedisMessage -> throw KredsRedisDataException(execResult.content())
            is ArrayRedisMessage -> {
                val results = ArrayHandler.doHandle(execResult) ?: return null
                responseList.add(results)
                responseList
            }
            else -> throw KredsTransactionException("Invalid data received from Redis.")
        }
    }

    override suspend fun exec() = lockByCoroutineJob {
        if (!transactionStarted) throw KredsTransactionException("Start transaction with multi() before exec()")
        if (!done) {
            try {
                commands.add(CommandExecution(EXEC, ArrayCommandProcessor))
                val response = executeTransaction(commands) ?: throw KredsTransactionException("Transaction aborted.")
                commandResponse.addAll(response)
                done = true
                responseFlow.tryEmit(commandResponse)
            } catch (ex: KredsException) {
                // failed to execute transaction. emit empty list to signal operation cancelled.
                responseFlow.tryEmit(emptyList())
                throw ex
            }
        }
    }
}
