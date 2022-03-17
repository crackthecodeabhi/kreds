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

import io.github.crackthecodeabhi.kreds.*
import io.github.crackthecodeabhi.kreds.args.EmptyArgument
import io.github.crackthecodeabhi.kreds.args.createArguments
import io.github.crackthecodeabhi.kreds.args.toArgument
import io.github.crackthecodeabhi.kreds.commands.Command
import io.github.crackthecodeabhi.kreds.commands.CommandExecution
import io.github.crackthecodeabhi.kreds.commands.ConnectionCommand
import io.github.crackthecodeabhi.kreds.commands.responseTo
import io.github.crackthecodeabhi.kreds.connection.PubSubCommand.*
import io.github.crackthecodeabhi.kreds.protocol.*
import io.netty.channel.EventLoopGroup
import io.netty.handler.codec.redis.ArrayRedisMessage
import io.netty.handler.codec.redis.FullBulkStringRedisMessage
import io.netty.handler.codec.redis.RedisMessage
import io.netty.handler.codec.redis.SimpleStringRedisMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import mu.KotlinLogging
import kotlin.coroutines.CoroutineContext

public class KredsPubSubException : KredsException {
    internal companion object {
        @JvmStatic
        val serialVersionUID = -942312382149778098L
    }

    internal constructor(message: String) : super(message)
    internal constructor(throwable: Throwable) : super(throwable)
    internal constructor(message: String, throwable: Throwable) : super(message, throwable)
}

internal enum class PubSubCommand(override val subCommand: Command? = null, commandString: String? = null) : Command {
    PSUBSCRIBE, PUBLISH, PUNSUBSCRIBE, SUBSCRIBE, UNSUBSCRIBE,

    CHANNELS, NUMPAT, NUMSUB, HELP,

    PUBSUB_CHANNELS(CHANNELS, "PUBSUB"),
    PUBSUB_NUMPAT(NUMPAT, "PUBSUB"),
    PUBSUB_NUMSUB(NUMSUB, "PUBSUB"),
    PUBSUB_HELP(HELP, "PUBSUB");

    override val string = commandString ?: name
}

public interface KredsSubscriber {
    public fun onMessage(channel: String, message: String)
    public fun onPMessage(pattern: String, channel: String, message: String)
    public fun onSubscribe(channel: String, subscribedChannels: Long)
    public fun onUnsubscribe(channel: String, subscribedChannels: Long)
    public fun onPUnsubscribe(pattern: String, subscribedChannels: Long)
    public fun onPSubscribe(pattern: String, subscribedChannels: Long)
    public fun onException(ex: Throwable)
}

/**
 * Clients override the required methods to process those events else they are discarded by default.
 * @see KredsSubscriber
 */
public abstract class AbstractKredsSubscriber : KredsSubscriber {

    override fun onMessage(channel: String, message: String) {}

    override fun onPMessage(pattern: String, channel: String, message: String) {}

    override fun onSubscribe(channel: String, subscribedChannels: Long) {}

    override fun onUnsubscribe(channel: String, subscribedChannels: Long) {}

    override fun onPUnsubscribe(pattern: String, subscribedChannels: Long) {}

    override fun onPSubscribe(pattern: String, subscribedChannels: Long) {}

}

public interface PublisherCommands {
    /**
     * ###  PUBLISH channel message
     *
     * Posts a message to the given channel.
     *
     * [Doc](https://redis.io/commands/publish)
     * @since 2.0.0
     * @return the number of clients that received the message. Note that in a Redis Cluster, only clients that are connected to the same node as the publishing client are included in the count
     */
    public suspend fun publish(channel: String, message: String): Long

    /**
     * ###  PUBSUB CHANNELS [pattern]
     *
     * Lists the currently active channels.
     *
     * [Doc](https://redis.io/commands/pubsub-channels)
     * @since 2.8.0
     * @return a list of active channels, optionally matching the specified pattern.
     */
    public suspend fun pubsubChannels(pattern: String? = null): List<String>

    /**
     * ### PUBSUB NUMPAT
     *
     * Returns the number of unique patterns that are subscribed to by clients (that are performed using the PSUBSCRIBE command).
     *
     * [Doc](https://redis.io/commands/pubsub-numpat)
     * @since 2.8.0
     * @return the number of patterns all the clients are subscribed to.
     */
    public suspend fun pubsubNumpat(): Long

    /**
     * ###  `PUBSUB NUMSUB [channel [channel ...]]`
     *
     * Returns the number of subscribers (exclusive of clients subscribed to patterns) for the specified channels.
     *
     * [Doc](https://redis.io/commands/pubsub-numsub)
     * @since 2.8.0
     * @return  a list of channels and number of subscribers for every channel.
     * The format is channel, count, channel, count, ..., so the list is flat. The order in which the channels are listed is the same as the order of the channels specified in the command call.
     */
    public suspend fun pubsubNumsub(vararg channels: String): List<Any>

    /**
     * ### PUBSUB HELP
     *
     * The PUBSUB HELP command returns a helpful text describing the different subcommands.
     *
     * [Doc](https://redis.io/commands/pubsub-help)
     * @since 6.2.0
     * @return a list of subcommands and their descriptions
     */
    public suspend fun pubsubHelp(): List<String>

}

internal interface PublishCommandExecutor : PublisherCommands, CommandExecutor {
    override suspend fun publish(channel: String, message: String): Long =
        execute(PUBLISH, IntegerCommandProcessor, channel.toArgument(), message.toArgument())

    override suspend fun pubsubChannels(pattern: String?): List<String> =
        execute(PUBSUB_CHANNELS, ArrayCommandProcessor, *createArguments(pattern)).responseTo("pubsub channels")

    override suspend fun pubsubNumpat(): Long =
        execute(PUBSUB_NUMPAT, IntegerCommandProcessor)

    override suspend fun pubsubNumsub(vararg channels: String): List<Any> =
        execute(PUBSUB_NUMSUB, ArrayCommandProcessor, *createArguments(*channels)).responseTo("pubsub numsub")

    override suspend fun pubsubHelp(): List<String> =
        execute(PUBSUB_HELP, ArrayCommandProcessor).responseTo("pubsub help")
}

public interface SubscriberCommands {
    /**
     * ###  `SUBSCRIBE channel [channel ...]`
     *
     * Subscribes the client to the specified channels.
     *
     * [Doc](https://redis.io/commands/subscribe)
     * @since 2.0.0
     */
    public suspend fun subscribe(vararg channels: String)

    /**
     * ### `PSUBSCRIBE pattern [pattern ...]`
     *
     * Subscribes the client to the given patterns.
     *
     * [Doc](https://redis.io/commands/psubscribe)
     * @since 2.0.0
     */
    public suspend fun pSubscribe(vararg patterns: String)

    /**
     * ### PUNSUBSCRIBE [pattern [pattern ...]]
     *
     * Unsubscribes the client from the given patterns, or from all of them if none is given.
     *
     * [Doc](https://redis.io/commands/punsubscribe)
     * @since 2.0.0
     */
    public suspend fun pUnsubscribe(vararg patterns: String)

    /**
     * ## `PING [message]`
     *
     * Returns PONG if no argument is provided, otherwise return a copy of the argument
     *
     * [Doc](https://redis.io/commands/ping)
     * @since 1.0.0
     * @return PONG or message
     */
    public suspend fun ping(message: String? = null): String?

    /**
     * ### RESET
     *
     * This command performs a full reset of the connection's server-side context, mimicking the effect of disconnecting and reconnecting again.
     *
     * [Doc](https://redis.io/commands/reset)
     * @since 6.2
     * @return RESET
     */
    public suspend fun reset(): String

    /**
     * ### QUIT
     *
     * Ask the server to close the connection. The connection is closed as soon as all pending replies have been written to the client.
     *
     * [Doc](https://redis.io/commands/quit)
     * @since 1.0.0
     * @return OK
     */
    public suspend fun quit(): String

    /**
     * ### `UNSUBSCRIBE [channel [channel ...]]`
     *
     * Unsubscribes the client from the given channels, or from all of them if none is given
     *
     * [Doc](https://redis.io/commands/unsubscribe)
     * @since 2.0.0
     */
    public suspend fun unsubscribe(vararg channels: String)
}

public interface KredsSubscriberClient : AutoCloseable, SubscriberCommands {

}

/**
 * [Doc](https://redis.io/topics/pubsub)
 */
private val logger = KotlinLogging.logger {}

internal class DefaultKredsSubscriberClient(
    endpoint: Endpoint,
    eventLoopGroup: EventLoopGroup,
    context: CoroutineContext,
    kredsSubscriber: KredsSubscriber,
    config: KredsClientConfig
) : KredsSubscriberClient, AbstractKredsClient(endpoint, eventLoopGroup, config) {

    private val scope = CoroutineScope(context + SupervisorJob(context.job))

    override val mutex: Mutex = Mutex()

    override val key: ReentrantMutexContextKey = ReentrantMutexContextKey(mutex)

    private val reader = Reader(kredsSubscriber)

    private val writer = Writer(mutex, key)

    inner class Reader(private val kredsSubscriber: KredsSubscriber) : ExclusiveObject {
        override val mutex = Mutex()
        override val key: ReentrantMutexContextKey = ReentrantMutexContextKey(mutex)

        private var job: Job? = null
        val readChannel = Channel<RedisMessage>(Channel.UNLIMITED)

        suspend fun <R> preemptRead(writeOp: suspend () -> R) = withReentrantLock {
            try {
                stop()
                writeOp()
            } finally {
                start()
            }
        }

        suspend fun close() = withReentrantLock {
            stop()
        }

        private fun stop(): Boolean {
            return if (job != null) {
                job!!.cancel()
                job = null
                true
            } else false
        }

        private fun start(): Boolean {
            return if (job == null) {
                job = scope.launch { read() }
                true
            } else false
        }

        private suspend fun read() {
            try {
                while (true) {
                    if (this@DefaultKredsSubscriberClient.isConnected()) {
                        val msg = this@DefaultKredsSubscriberClient.read()
                        if (msg is ArrayRedisMessage) {
                            val reply = ArrayCommandProcessor.decode(msg)
                            if (reply != null) {
                                if (isValidPubSubReply(reply))
                                    processPubSubReply(reply)
                                else
                                    handlePubSubSpecialCase(reply)
                            } else
                                dispatchPubSubEvent {
                                    kredsSubscriber.onException(KredsPubSubException("Received null reply from server."))
                                }

                        } else
                            readChannel.trySend(msg)
                    }
                }
            } catch (ex: CancellationException) {
                logger.trace { "Reader was cancelled." }
                throw ex
            }
        }

        private inline fun dispatchPubSubEvent(crossinline action: () -> Unit) {
            scope.launch { action() }
        }

        private fun handlePubSubSpecialCase(reply: List<Any?>) {
            val first = reply.firstOrNull()
            if (first is String && first.lowercase() == "pong") {
                if (reply.size == 2)
                    readChannel.trySend(FullBulkStringRedisMessage((reply.second() as String).toByteBuf()))
                else
                    readChannel.trySend(SimpleStringRedisMessage("PONG"))
            } else
                dispatchPubSubEvent {
                    kredsSubscriber.onException(KredsPubSubException("Received invalid subscription message."))
                }
        }

        /**
         * Each subscription message has 3 elements, except pmessage, which has 4
         */
        private fun isValidPubSubReply(reply: List<*>): Boolean =
            with(reply) { isNotEmpty() && (size in 3..4) }

        private inline fun <reified R> kind(reply: List<Any?>): R = reply.getAs(0)

        private inline fun <reified R> channelOrPattern(reply: List<Any?>): R = reply.getAs(1)

        private inline fun <reified R> messageOrChannel(reply: List<Any?>): R = reply.getAs(2)

        private inline fun <reified R> subscribedChannels(reply: List<Any?>): R = messageOrChannel(reply)

        private inline fun <reified R> pmessage(reply: List<Any?>): R = reply.getAs(3)

        /**
         * Only valid reply is passed, the validity function [isValidPubSubReply] is used to validate.
         */
        private fun processPubSubReply(reply: List<Any?>) {
            val kind: String = kind(reply)
            val channelOrPattern: String = channelOrPattern(reply)
            when (kind) {
                "subscribe" -> dispatchPubSubEvent {
                    kredsSubscriber.onSubscribe(
                        channelOrPattern,
                        subscribedChannels(reply)
                    )
                }

                "unsubscribe" -> dispatchPubSubEvent {
                    kredsSubscriber.onUnsubscribe(
                        channelOrPattern,
                        subscribedChannels(reply)
                    )
                }

                "psubscribe" -> dispatchPubSubEvent {
                    kredsSubscriber.onPSubscribe(
                        channelOrPattern,
                        subscribedChannels(reply)
                    )
                }

                "punsubscribe" -> dispatchPubSubEvent {
                    kredsSubscriber.onPUnsubscribe(
                        channelOrPattern,
                        subscribedChannels(reply)
                    )
                }

                "message" -> dispatchPubSubEvent {
                    kredsSubscriber.onMessage(
                        channelOrPattern,
                        messageOrChannel(reply)
                    )
                }

                "pmessage" -> dispatchPubSubEvent {
                    kredsSubscriber.onPMessage(
                        channelOrPattern,
                        messageOrChannel(reply),
                        pmessage(reply)
                    )
                }

                else -> dispatchPubSubEvent { kredsSubscriber.onException(KredsPubSubException("Unknown pubsub message kind $kind")) }
            }
        }
    }

    inner class Writer(override val mutex: Mutex, override val key: ReentrantMutexContextKey) : ExclusiveObject {
        suspend fun write(execution: CommandExecution<*>) = withReentrantLock {
            with(execution) {
                connectWriteAndFlush(processor.encode(command, *args))
            }
        }
    }

    override suspend fun subscribe(vararg channels: String) {
        reader.preemptRead {
            writer.write(CommandExecution(SUBSCRIBE, ArrayCommandProcessor, *createArguments(*channels)))
        }
    }

    override suspend fun unsubscribe(vararg channels: String) {
        reader.preemptRead {
            writer.write(CommandExecution(UNSUBSCRIBE, ArrayCommandProcessor, *createArguments(*channels)))
        }
    }

    override suspend fun pSubscribe(vararg patterns: String) {
        reader.preemptRead {
            writer.write(CommandExecution(PSUBSCRIBE, ArrayCommandProcessor, *createArguments(*patterns)))
        }
    }

    override suspend fun pUnsubscribe(vararg patterns: String) {
        reader.preemptRead {
            writer.write(CommandExecution(PUNSUBSCRIBE, ArrayCommandProcessor, *createArguments(*patterns)))
        }
    }

    override suspend fun ping(message: String?): String? {
        reader.preemptRead {
            writer.write(
                CommandExecution(
                    ConnectionCommand.PING,
                    SimpleStringCommandProcessor,
                    message?.toArgument() ?: EmptyArgument
                )
            )
        }
        return SimpleAndBulkStringCommandProcessor.decode(reader.readChannel.receive())
    }

    override suspend fun reset(): String {
        reader.preemptRead {
            writer.write(
                CommandExecution(
                    ConnectionCommand.RESET,
                    SimpleStringCommandProcessor
                )
            )
        }
        return SimpleStringCommandProcessor.decode(reader.readChannel.receive())
    }

    override suspend fun quit(): String {
        reader.preemptRead {
            writer.write(
                CommandExecution(
                    ConnectionCommand.QUIT,
                    SimpleStringCommandProcessor
                )
            )
        }
        return SimpleStringCommandProcessor.decode(reader.readChannel.receive())
    }

    override fun close() {
        runBlocking {
            reader.close()
            disconnect()
            if (scope.isActive) scope.cancel()
        }
    }
}