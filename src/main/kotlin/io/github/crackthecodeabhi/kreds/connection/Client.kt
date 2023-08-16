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

package io.github.crackthecodeabhi.kreds.connection

import io.github.crackthecodeabhi.kreds.ReentrantMutexContextKey
import io.github.crackthecodeabhi.kreds.args.Argument
import io.github.crackthecodeabhi.kreds.commands.*
import io.github.crackthecodeabhi.kreds.pipeline.Pipeline
import io.github.crackthecodeabhi.kreds.pipeline.PipelineImpl
import io.github.crackthecodeabhi.kreds.pipeline.Transaction
import io.github.crackthecodeabhi.kreds.pipeline.TransactionImpl
import io.github.crackthecodeabhi.kreds.protocol.CommandExecutor
import io.github.crackthecodeabhi.kreds.protocol.ICommandProcessor
import io.github.crackthecodeabhi.kreds.withReentrantLock
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.handler.codec.redis.RedisMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex


//TODO: INCRBY can be negative also! check that in api, should accept long, not ulong

private val eventLoopGroup = NioEventLoopGroup()

public fun CoroutineScope.newSubscriberClient(
    endpoint: Endpoint,
    handler: KredsSubscriber,
    config: KredsClientConfig = defaultSubscriberClientConfig
): KredsSubscriberClient =
    DefaultKredsSubscriberClient(endpoint, eventLoopGroup, coroutineContext, handler, config)

public fun newClient(endpoint: Endpoint, config: KredsClientConfig = defaultClientConfig): KredsClient =
    DefaultKredsClient(endpoint, eventLoopGroup, config)

public fun newBlockingClient(
    endpoint: Endpoint,
    config: KredsClientConfig = defaultBlockingKredsClientConfig
): BlockingKredsClient =
    DefaultKredsClient(endpoint, eventLoopGroup, config)

public suspend fun shutdown() {
    eventLoopGroup.shutdownGracefully().suspendableAwait()
}

public interface KredsClient : AutoCloseable, KeyCommands, StringCommands, ConnectionCommands, PublisherCommands,
    HashCommands, SetCommands,
    ListCommands, HyperLogLogCommands, ServerCommands, ZSetCommands, JsonCommands, ScriptingCommands, FunctionCommands {
    public fun pipelined(): Pipeline
    public fun transaction(): Transaction
}

internal interface InternalKredsClient : KredsClient {
    val endpoint: Endpoint
}

public interface BlockingKredsClient : AutoCloseable, BlockingListCommands, BlockingZSetCommands

internal abstract class AbstractKredsClient(
    endpoint: Endpoint,
    eventLoopGroup: EventLoopGroup,
    config: KredsClientConfig
) :
    KonnectionImpl(endpoint, eventLoopGroup, config), CommandExecutor {

    override suspend fun <T> execute(command: Command, processor: ICommandProcessor<T>, vararg args: Argument): T =
         withReentrantLock {
            connectWriteAndFlush(processor.encode(command, *args))
            processor.decode(read())
        }

    override suspend fun <T> execute(commandExecution: CommandExecution<T>): T = withReentrantLock {
        with(commandExecution) {
            connectWriteAndFlush(processor.encode(command, *args))
            processor.decode(read())
        }
    }

    override suspend fun executeCommands(commands: List<CommandExecution<*>>): List<RedisMessage> = withReentrantLock {
        connect()
        commands.forEach {
            with(it) {
                write(processor.encode(command, *args))
            }
        }
        flush()
        // collect the response messages.
        val responseList = mutableListOf<RedisMessage>()
        repeat(commands.size) {
            responseList.add(it, read())
        }
        responseList
    }
}

internal class DefaultKredsClient(
    override val endpoint: Endpoint,
    eventLoopGroup: EventLoopGroup,
    config: KredsClientConfig
) :
    AbstractKredsClient(endpoint, eventLoopGroup, config), KredsClient, InternalKredsClient, KeyCommandExecutor,
    StringCommandsExecutor, ConnectionCommandsExecutor, PublishCommandExecutor, HashCommandsExecutor,
    SetCommandExecutor, ListCommandExecutor, HyperLogLogCommandExecutor, ServerCommandExecutor, BlockingKredsClient,
    ZSetCommandExecutor, JsonCommandExecutor, ScriptingCommandExecutor, FunctionCommandExecutor {

    override val mutex: Mutex = Mutex()

    override val key: ReentrantMutexContextKey = ReentrantMutexContextKey(mutex)

    override fun pipelined(): Pipeline = PipelineImpl(this)

    override fun transaction(): Transaction = TransactionImpl(this)

    override fun close() {
        runBlocking {
             withReentrantLock {
                disconnect()
            }
        }
    }
}
