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
import io.github.crackthecodeabhi.kreds.commands.*
import io.github.crackthecodeabhi.kreds.connection.DefaultKredsClient
import io.github.crackthecodeabhi.kreds.protocol.KredsRedisDataException
import io.github.crackthecodeabhi.kreds.withReentrantLock
import io.netty.handler.codec.redis.ErrorRedisMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex


public class Response<out T> internal constructor(
    private val responseFlow: Flow<List<Any?>>,
    private val index: Int,
    private val nullable: Boolean = true
) {

    @Suppress("UNCHECKED_CAST")
    @Throws(KredsException::class, KredsRedisDataException::class)
    public suspend operator fun invoke(): T =
        when (val value = responseFlow.first().ifEmpty { throw KredsException("Operation was cancelled.") }[index]) {
            is KredsException -> throw value
            null -> if (nullable) null as T else throw KredsRedisDataException("Received null from server.")
            else -> value as T
        }

    @Throws(KredsException::class, KredsRedisDataException::class)
    public suspend fun get(): T = invoke()
}

internal interface QueuedCommand {
    suspend fun <T> add(commandExecution: CommandExecution<T>, nullable: Boolean = true): Response<T>
}

public interface Pipeline : PipelineStringCommands, PipelineKeyCommands, PipelineHashCommands, PipelineSetCommands,
    PipelineListCommands, PipelineHyperLogLogCommands, PipelineZSetCommands {
    public suspend fun execute()
}


internal class PipelineImpl(private val client: DefaultKredsClient) : ExclusiveObject, Pipeline,
    PipelineStringCommandsExecutor, PipelineKeyCommandExecutor, PipelineHashCommandExecutor, PipelineSetCommandExecutor,
    PipelineListCommandExecutor, PipelineHyperLogLogCommandExecutor, PipelineZSetCommandExecutor {

    override val mutex: Mutex = Mutex()

    override val key: ReentrantMutexContextKey = ReentrantMutexContextKey(mutex)

    private var done = false

    private val responseFlow = MutableSharedFlow<List<Any?>>(1)

    private val sharedResponseFlow: Flow<List<Any?>> = responseFlow.asSharedFlow()

    private val commands = mutableListOf<CommandExecution<*>>()
    private val commandResponse = mutableListOf<Any?>()

    override suspend fun <T> add(commandExecution: CommandExecution<T>, nullable: Boolean): Response<T> =
        withReentrantLock {
            commands.add(commandExecution)
            Response(sharedResponseFlow, commands.lastIndex, nullable)
        }

    private suspend fun executePipeline(commands: List<CommandExecution<*>>): List<Any?> = withReentrantLock {
        val responseList = mutableListOf<Any?>()
        val responseMessages = client.executeCommands(commands)
        //TODO: check this
        responseMessages.onEachIndexed { idx, msg ->
            if (msg is ErrorRedisMessage)
                responseList.add(
                    KredsRedisDataException(
                        msg.content(),
                        null,
                        enableSuppression = true,
                        writableStackTrace = false
                    )
                )
            else responseList.add(commands[idx].processor.decode(msg))
        }
        responseList
    }

    override suspend fun execute(): Unit = withReentrantLock {
        if (!done) {
            commandResponse.addAll(executePipeline(commands))
            done = true
            responseFlow.tryEmit(commandResponse.toMutableList())
        }
    }
}
