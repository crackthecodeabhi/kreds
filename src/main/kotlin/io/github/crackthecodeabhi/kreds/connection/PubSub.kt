package io.github.crackthecodeabhi.kreds.connection

import io.github.crackthecodeabhi.kreds.KredsException
import io.netty.channel.EventLoopGroup
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import io.github.crackthecodeabhi.kreds.commands.Command
import io.github.crackthecodeabhi.kreds.commands.ConnectionCommand
import io.github.crackthecodeabhi.kreds.connection.PubSubCommand.*
import io.github.crackthecodeabhi.kreds.args.*
import io.github.crackthecodeabhi.kreds.lockByCoroutineJob
import io.github.crackthecodeabhi.kreds.protocol.*

class KredsPubSubException : KredsException {
    companion object {
        @JvmStatic
        val serialVersionUID = -942312382149778098L
    }

    constructor(message: String) : super(message)
    constructor(throwable: Throwable) : super(throwable)
    constructor(message: String, throwable: Throwable) : super(message, throwable)
}

enum class PubSubCommand(override val subCommand: Command? = null, commandString: String? = null) : Command {
    PSUBSCRIBE,PUBLISH, PUNSUBSCRIBE, SUBSCRIBE, UNSUBSCRIBE,

    CHANNELS,NUMPAT,NUMSUB,HELP,

    PUBSUB_CHANNELS(CHANNELS,"PUBSUB"),
    PUBSUB_NUMPAT(NUMPAT,"PUBSUB"),
    PUBSUB_NUMSUB(NUMSUB,"PUBSUB"),
    PUBSUB_HELP(HELP,"PUBSUB");

    override val string = commandString ?: name
}

/**
 * New coroutines will be launched in the provided [scope] for each event,
 * clients have full control over context, dispatcher of the coroutines created to fire the events.
 */
interface KredsSubscriber {
    fun onMessage(channel: String, message: String)
    fun onPMessage(pattern: String, channel: String, message: String)
    fun onSubscribe(channel: String, subscribedChannels: Long)
    fun onUnsubscribe(channel: String, subscribedChannels: Long)
    fun onPUnsubscribe(pattern: String, subscribedChannels: Long)
    fun onPSubscribe(pattern: String, subscribedChannels: Long)
    fun onException(ex: Throwable)
    val scope: CoroutineScope
}

/**
 * Clients override the required methods to process those events else they are discarded by default.
 * @see KredsSubscriber
 */
abstract class AbstractKredsSubscriber(override val scope: CoroutineScope) : KredsSubscriber {

    override fun onMessage(channel: String, message: String) {}

    override fun onPMessage(pattern: String, channel: String, message: String) {}

    override fun onSubscribe(channel: String, subscribedChannels: Long) {}

    override fun onUnsubscribe(channel: String, subscribedChannels: Long) {}

    override fun onPUnsubscribe(pattern: String, subscribedChannels: Long) {}

    override fun onPSubscribe(pattern: String, subscribedChannels: Long) {}

}

interface PublisherCommands {
    /**
     * ###  PUBLISH channel message
     *
     * Posts a message to the given channel.
     *
     * [Doc](https://redis.io/commands/publish)
     * @since 2.0.0
     * @return the number of clients that received the message. Note that in a Redis Cluster, only clients that are connected to the same node as the publishing client are included in the count
     */
    suspend fun publish(channel: String, message: String): Long

    /**
     * ###  PUBSUB CHANNELS [pattern]
     *
     * Lists the currently active channels.
     *
     * [Doc](https://redis.io/commands/pubsub-channels)
     * @since 2.8.0
     * @return a list of active channels, optionally matching the specified pattern.
     */
    suspend fun pubsubChannels(pattern: String? = null): List<String>

    /**
     * ### PUBSUB NUMPAT
     *
     * Returns the number of unique patterns that are subscribed to by clients (that are performed using the PSUBSCRIBE command).
     *
     * [Doc](https://redis.io/commands/pubsub-numpat)
     * @since 2.8.0
     * @return the number of patterns all the clients are subscribed to.
     */
    suspend fun pubsubNumpat(): Long

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
    suspend fun pubsubNumsub(vararg channels: String): List<Any>

    /**
     * ### PUBSUB HELP
     *
     * The PUBSUB HELP command returns a helpful text describing the different subcommands.
     *
     * [Doc](https://redis.io/commands/pubsub-help)
     * @since 6.2.0
     * @return a list of subcommands and their descriptions
     */
    suspend fun pubsubHelp(): List<String>

}

interface PublishCommandExecutor : PublisherCommands, CommandExecutor {
    override suspend fun publish(channel: String, message: String): Long =
        execute(PUBLISH, IntegerCommandProcessor, channel.toArgument(), message.toArgument())

    override suspend fun pubsubChannels(pattern: String?): List<String> =
        execute(PUBSUB_CHANNELS, ArrayCommandProcessor, *createArguments(pattern))

    override suspend fun pubsubNumpat(): Long =
        execute(PUBSUB_NUMPAT, IntegerCommandProcessor)

    override suspend fun pubsubNumsub(vararg channels: String): List<Any> =
        execute(PUBSUB_NUMSUB, ArrayCommandProcessor, *createArguments(*channels))

    override suspend fun pubsubHelp(): List<String> =
        execute(PUBSUB_HELP, ArrayCommandProcessor)
}

interface SubscriberCommands {
    /**
     * ###  `SUBSCRIBE channel [channel ...]`
     *
     * Subscribes the client to the specified channels.
     *
     * [Doc](https://redis.io/commands/subscribe)
     * @since 2.0.0
     */
    suspend fun subscribe(vararg channels: String)

    /**
     * ### `PSUBSCRIBE pattern [pattern ...]`
     *
     * Subscribes the client to the given patterns.
     *
     * [Doc](https://redis.io/commands/psubscribe)
     * @since 2.0.0
     */
    suspend fun pSubscribe(vararg patterns: String)

    /**
     * ### PUNSUBSCRIBE [pattern [pattern ...]]
     *
     * Unsubscribes the client from the given patterns, or from all of them if none is given.
     *
     * [Doc](https://redis.io/commands/punsubscribe)
     * @since 2.0.0
     */
    suspend fun pUnsubscribe(vararg patterns: String)

    /**
     * ## `PING [message]`
     *
     * Returns PONG if no argument is provided, otherwise return a copy of the argument
     *
     * [Doc](https://redis.io/commands/ping)
     * @since 1.0.0
     * @return PONG or message
     */
    suspend fun ping(message: String? = null): String

    /**
     * ### RESET
     *
     * This command performs a full reset of the connection's server-side context, mimicking the effect of disconnecting and reconnecting again.
     *
     * [Doc](https://redis.io/commands/reset)
     * @since 6.2
     * @return RESET
     */
    suspend fun reset(): String

    /**
     * ### QUIT
     *
     * Ask the server to close the connection. The connection is closed as soon as all pending replies have been written to the client.
     *
     * [Doc](https://redis.io/commands/quit)
     * @since 1.0.0
     * @return OK
     */
    suspend fun quit(): String

    /**
     * ### `UNSUBSCRIBE [channel [channel ...]]`
     *
     * Unsubscribes the client from the given channels, or from all of them if none is given
     *
     * [Doc](https://redis.io/commands/unsubscribe)
     * @since 2.0.0
     */
    suspend fun unsubscribe(vararg channels: String)
}

interface KredsSubscriberClient : AutoCloseable, SubscriberCommands {

}

/**
 * [Doc](https://redis.io/topics/pubsub)
 */
internal class DefaultKredsSubscriberClient(
    endpoint: Endpoint,
    eventLoopGroup: EventLoopGroup,
    private val kredsSubscriber: KredsSubscriber,
    config: KredsClientConfig
) : KredsSubscriberClient, AbstractKredsClient(endpoint, eventLoopGroup, config) {

    override val mutex: Mutex = Mutex()

    private var subCoroutineJob: Job? = null

    private suspend fun startSubscriptionCoroutine(): Job = CoroutineScope(currentCoroutineContext()).launch {
        while (isActive) {
            val msg = tryRead() ?: continue
            val reply: List<Any?> = ArrayCommandProcessor.decode(msg)
            processPubSubReply(reply)
        }
    }

    override suspend fun subscribe(vararg channels: String) = lockByCoroutineJob {
        connectWriteAndFlush(
            ArrayCommandProcessor.encode(
                SUBSCRIBE,
                *createArguments(*channels)
            )
        ) // do not wait for reply.
        subCoroutineJob = subCoroutineJob ?: startSubscriptionCoroutine()
    }

    override suspend fun unsubscribe(vararg channels: String) = lockByCoroutineJob {
        connectWriteAndFlush(ArrayCommandProcessor.encode(UNSUBSCRIBE, *createArguments(*channels)))
    }

    override suspend fun pSubscribe(vararg patterns: String) = lockByCoroutineJob {
        connectWriteAndFlush(
            ArrayCommandProcessor.encode(
                PSUBSCRIBE,
                *createArguments(*patterns)
            )
        ) //do not wait for reply.
        subCoroutineJob = subCoroutineJob ?: startSubscriptionCoroutine()
    }

    override suspend fun pUnsubscribe(vararg patterns: String) = lockByCoroutineJob {
        connectWriteAndFlush(ArrayCommandProcessor.encode(PUNSUBSCRIBE, *createArguments(*patterns)))
    }

    override suspend fun ping(message: String?): String =
        execute(ConnectionCommand.PING, SimpleStringCommandProcessor, *createArguments(message))

    override suspend fun reset(): String =
        execute(ConnectionCommand.RESET, SimpleStringCommandProcessor)

    override suspend fun quit(): String =
        execute(ConnectionCommand.QUIT, SimpleStringCommandProcessor)

    private inline fun dispatchPubSubEvent(crossinline action: () -> Unit) {
        kredsSubscriber.scope.launch { action() }
    }

    private suspend fun processPubSubReply(reply: List<Any?>) {
        // Each subscription message has 3 elements, except pmessage, which has 4
        if (reply.isEmpty() || (reply.size !in 3..4))
            dispatchPubSubEvent {
                kredsSubscriber.onException(KredsPubSubException("Received invalid subscription message."))
            }
        val kind = reply[0] as String
        val channelOrPattern = reply[1] as String
        val messageOrChannel: () -> String = { reply[2] as String }
        val subscribedChannels: () -> Long = { reply[2] as Long }
        val pmessage: () -> String = { reply[3] as String }
        when (kind) {
            "subscribe" -> dispatchPubSubEvent { kredsSubscriber.onSubscribe(channelOrPattern, subscribedChannels()) }

            "unsubscribe" -> {
                dispatchPubSubEvent { kredsSubscriber.onUnsubscribe(channelOrPattern, subscribedChannels()) }
                subCoroutineJob?.cancel()
            }

            "psubscribe" -> dispatchPubSubEvent { kredsSubscriber.onPSubscribe(channelOrPattern, subscribedChannels()) }

            "punsubscribe" -> {
                dispatchPubSubEvent { kredsSubscriber.onPUnsubscribe(channelOrPattern, subscribedChannels()) }
                subCoroutineJob?.cancel()
            }

            "message" -> dispatchPubSubEvent { kredsSubscriber.onMessage(channelOrPattern, messageOrChannel()) }

            "pmessage" -> dispatchPubSubEvent {
                kredsSubscriber.onPMessage(
                    channelOrPattern,
                    messageOrChannel(),
                    pmessage()
                )
            }

            else -> dispatchPubSubEvent { kredsSubscriber.onException(KredsPubSubException("Unknown pubsub message kind $kind")) }
        }
    }

    override fun close() {
        runBlocking {
            lockByCoroutineJob {
                subCoroutineJob?.cancel()
                disconnect()
            }
        }
    }
}