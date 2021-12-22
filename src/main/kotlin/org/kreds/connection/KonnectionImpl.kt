package org.kreds.connection

import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.redis.*
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.TimeoutException
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import java.net.SocketException
import kotlinx.coroutines.channels.Channel as KChannel

/**
 * The state of this Konnection is protected with the abstract [kotlinx.coroutines.sync.Mutex] defined by [ExclusiveObject]
 *
 * The concrete implementations of this class will provide the [kotlinx.coroutines.sync.Mutex] object
 *
 * A Konnection state depends on, hereafter, called as **state**
 * 1. SocketChannel
 * 2. Channel<RedisMessage>
 *
 * When connection is established, the channel and readChannel is set.
 *
 * Assumption: if [isConnected] returns true, state variables are set and live.
 *
 * On any I/O error, connection is closed.
 *
 */
internal abstract class KonnectionImpl(private val endpoint: Endpoint, eventLoopGroup: EventLoopGroup) : Konnection {

    private val bootstrap: Bootstrap = Bootstrap().group(eventLoopGroup)
        .remoteAddress(endpoint.toSocketAddress())
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000) //TODO: client configurable
        .option(ChannelOption.SO_KEEPALIVE,true) //TODO: configurable
        .channel(NioSocketChannel::class.java)

    private var channel: SocketChannel? = null
    private var readChannel: KChannel<RedisMessage>? = null

    private fun createChannelInitializer(readChannel: KChannel<RedisMessage>): ChannelInitializer<SocketChannel> {
        return object : ChannelInitializer<SocketChannel>() {
            override fun initChannel(ch: SocketChannel) {
                val pipeline = ch.pipeline()

                pipeline.addLast(RedisEncoder()) // outbound M
                //pipeline.addLast(WriteTimeoutHandler(10)) // outbound M - 1

                pipeline.addLast(RedisDecoder()) // inbound 1
                pipeline.addLast(RedisBulkStringAggregator()) // inbound 2
                pipeline.addLast(RedisArrayAggregator()) // inbound 3
                pipeline.addLast(ReadTimeoutHandler(10)) // duplex 4 //TODO: not required for Pub sub client
                pipeline.addLast(ResponseHandler(readChannel)) // inbound 5

            }
        }
    }

    /**
     * Creates a new readChannel and tries to connect to remote peer.
     * Returns the connected SocketChannel and ReadChannel as [Pair<SocketChannel,Channel<RedisMessage>>]
     * @throws SocketException
     */
    private suspend fun createNewConnection(): Pair<SocketChannel,KChannel<RedisMessage>> = lockByCoroutineJob {
        val newReadChannel = KChannel<RedisMessage>(KChannel.UNLIMITED)
        return Pair(bootstrap.handler(createChannelInitializer(newReadChannel)).connect().suspendableAwait() as SocketChannel, newReadChannel)
    }

    /**
     * The last inbound handler.
     */
    private class ResponseHandler(val readChannel: KChannel<RedisMessage>) : ChannelInboundHandlerAdapter() {

        override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
            ctx.close()
            readChannel.close(cause)
        }

        override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
            readChannel.trySend(msg as RedisMessage)
        }
    }

    override suspend fun isConnected() = lockByCoroutineJob {
        if(channel == null || readChannel == null) false
        else channel!!.isActive
    }

    private suspend fun writeInternal(message: RedisMessage, flush: Boolean) : Unit = lockByCoroutineJob {
        if(!isConnected()) throw KredsNotYetConnectedException()
        try {
            if (flush) channel!!.writeAndFlush(message).suspendableAwait()
            else channel!!.write(message).suspendableAwait()
        } catch (ex: Throwable) {
            when(ex){
                is TimeoutException -> throw KredsTimeoutException("Write timed out.", ex)
                is SocketException -> throw KredsConnectionException(ex)
                else -> throw ex
            }
        }
    }

    override suspend fun flush(): Unit = lockByCoroutineJob {
        if(!isConnected()) throw KredsNotYetConnectedException()
        else channel!!.flush()
    }

    override suspend fun write(message: RedisMessage): Unit = writeInternal(message,false)

    override suspend fun writeAndFlush(message: RedisMessage): Unit = writeInternal(message,true)

    private suspend fun readInternal(tryRead: Boolean): RedisMessage? = lockByCoroutineJob {
        if(!isConnected()) throw KredsNotYetConnectedException()
        try {
            if(tryRead) {
                val result = readChannel!!.tryReceive()
                if(result.isClosed) throw ClosedReceiveChannelException("Channel closed fro receive.")
                else result.getOrNull()
            } else {
                readChannel!!.receive()
            }
        } catch (ex: Throwable) {
            when(ex) {
                is ClosedReceiveChannelException -> throw KredsConnectionException("Connection closed.")
                is TimeoutException -> throw KredsTimeoutException("Read timed out.", ex)
                is SocketException -> throw KredsConnectionException(ex)
                else -> throw ex
            }
        }
    }

    override suspend fun tryRead(): RedisMessage? = readInternal(true)

    override suspend fun read(): RedisMessage = readInternal(false)!!

    override suspend fun connect(): Unit = lockByCoroutineJob {
        if (!isConnected()) {
            try {
                val (channel, readChannel) = createNewConnection()
                this.channel = channel
                this.readChannel = readChannel
            } catch (ex: SocketException) {
                throw KredsConnectionException("Failed to connect to $endpoint", ex)
            }
        }
    }

    override suspend fun disconnect(): Unit = lockByCoroutineJob {
        if (isConnected()) {
            readChannel!!.close()
            channel!!.close().suspendableAwait()
        }
    }
}

