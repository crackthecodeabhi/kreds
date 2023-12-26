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
import io.github.crackthecodeabhi.kreds.ReentrantMutexContextKey
import io.github.crackthecodeabhi.kreds.args.createArguments
import io.github.crackthecodeabhi.kreds.commands.*
import io.github.crackthecodeabhi.kreds.connection.DefaultKredsClient
import io.github.crackthecodeabhi.kreds.pipeline.TransactionCommand.*
import io.github.crackthecodeabhi.kreds.protocol.ArrayCommandProcessor
import io.github.crackthecodeabhi.kreds.protocol.ArrayHandler
import io.github.crackthecodeabhi.kreds.protocol.KredsRedisDataException
import io.github.crackthecodeabhi.kreds.protocol.SimpleStringCommandProcessor
import io.github.crackthecodeabhi.kreds.withReentrantLock
import io.netty.handler.codec.redis.ArrayRedisMessage
import io.netty.handler.codec.redis.ErrorRedisMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex

public class KredsTransactionException internal constructor(message: String) : KredsException(message)

internal enum class TransactionCommand(override val subCommand: Command? = null) : Command {
    MULTI, EXEC, WATCH;

    override val string = name
}

/**
 * A Pipelined transaction.
 * All the command issued in this transaction are pipelined and executed on [Transaction.exec]
 * Only MULTI, WATCH and EXEC are modelled, since it is a pipelined execution, DISCARD does not make sense.
 * UNWATCH is not supported to make implementation simple,
 * For implementation details read implementation doc.
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
     * WATCH cannot be called after MULTI, throws [KredsTransactionException]
     *
     * [Doc](https://redis.io/commands/watch)
     * @since 2.2.0
     */
    public suspend fun watch(key: String, vararg keys: String): Unit

}

/**
 * This implementation assumes that only Queued commands are allowed after MULTI.
 * i.e, once MULTI command is issued, the subsequent commands will result in Queued response.
 * and the last command will be EXEC.
 * Visually:
 *    MULTI
 *    QUEUED 1
 *    QUEUED 2
 *    .
 *    .
 *    QUEUED N
 *    EXEC
 *
 *   This assumption makes the implementation easier and straight forward
 *   Hence, the UNWATCH command is not allowed/implemented.
 *
 *   The result of exec() is expected to result in response to each queued command as it is pipelined execution
 *   and the last message as the actual response to EXEC, as Redis array message.
 *
 *   The response to EXEC is either a ArrayMessage or ErrorMessage.
 *   Error message is thrown as [KredsTransactionException].
 *   The ArrayMessage, can be Null -> When using WATCH, EXEC can return a Null reply if the execution was aborted,
 *                                    a [KredsTransactionException] is thrown.
 *                     can contain array of response containing the response to each Queued command before EXEC command.
 */
internal class TransactionImpl(private val client: DefaultKredsClient) : ExclusiveObject, Transaction,
    PipelineStringCommandsExecutor, PipelineKeyCommandExecutor, PipelineHashCommandExecutor,
    PipelineListCommandExecutor, PipelineHyperLogLogCommandExecutor, PipelineSetCommandExecutor {

    override val mutex: Mutex = Mutex()

    override val key: ReentrantMutexContextKey = ReentrantMutexContextKey(mutex)

    /**
     * Guarded by [mutex].
     */
    private var done = false

    /*
    Guarded by [mutex]
     */
    private var multiIdx = -1

    /**
     * Guarded by [mutex].
     */
    private var transactionStarted = false

    /**
     * Shared, synchronization primitive.
     */
    private val responseFlow = MutableSharedFlow<List<Any?>>(1)

    /**
     * Guarded by [mutex].
     */
    private val commands = mutableListOf<CommandExecution<*>>()

    private suspend fun addTransactionCommand(commandExecution: CommandExecution<String>) = withReentrantLock {
        if (!transactionStarted) {
            with(commandExecution) {
                if (command != WATCH && command != MULTI)
                    throw KredsTransactionException(
                        "Only WATCH, MULTI commands are allowed " +
                                "before starting the transaction with MULTI."
                    )
            }
        } else {
            with(commandExecution) {
                if (command == MULTI || command == WATCH)
                    throw KredsTransactionException("MULTI/WATCH commands are not allowed after starting transaction with MULTI")
            }
        }
        commands.add(commandExecution)

        if (commandExecution.command == MULTI && multiIdx == -1) multiIdx = commands.lastIndex

    }

    override suspend fun <T> add(commandExecution: CommandExecution<T>, nullable: Boolean): Response<T> =
        withReentrantLock {
            commands.add(commandExecution)

            if (multiIdx > -1) {
                val idx = (commands.lastIndex - multiIdx) - 1
                Response(responseFlow, idx, nullable)
            } else {
                throw KredsTransactionException("Invalid Transaction state. Attempted to add non-transaction command before MULTI")
            }
        }

    override suspend fun multi(): Unit = withReentrantLock {
        if (transactionStarted) throw KredsTransactionException("Cannot nest multi command inside a transaction.")
        else {
            addTransactionCommand(CommandExecution(MULTI, SimpleStringCommandProcessor))
            transactionStarted = true
        }
    }

    override suspend fun watch(key: String, vararg keys: String): Unit = withReentrantLock {
        addTransactionCommand(CommandExecution(WATCH, SimpleStringCommandProcessor, *createArguments(key, *keys)))
    }

    private suspend fun executeTransaction(commands: List<CommandExecution<*>>): List<*>? = withReentrantLock {
        val responseMessages = client.executeCommands(commands)

        when (val execResult = responseMessages.last()) {
            is ErrorRedisMessage -> throw KredsRedisDataException(execResult.content())
            is ArrayRedisMessage -> ArrayHandler.doHandleAndRelease(execResult)
            else -> throw KredsTransactionException("Invalid data received from Redis.")
        }
    }

    override suspend fun exec() = withReentrantLock {
        if (!transactionStarted) throw KredsTransactionException("Start transaction with multi() before exec()")
        if (!done) {
            try {
                commands.add(CommandExecution(EXEC, ArrayCommandProcessor))
                val response = executeTransaction(commands) ?: throw KredsTransactionException("Transaction aborted.")
                done = true
                responseFlow.tryEmit(response)
            } catch (ex: KredsException) {
                // failed to execute transaction. emit empty list to signal operation cancelled.
                responseFlow.tryEmit(emptyList())
                throw ex
            }
        }
    }
}
