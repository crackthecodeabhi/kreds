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

import io.github.crackthecodeabhi.kreds.ExclusiveObject
import io.github.crackthecodeabhi.kreds.withReentrantLock
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.redis.*
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.SslHandler
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.TimeoutException
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import mu.KotlinLogging
import java.io.FileInputStream
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
private val logger = KotlinLogging.logger {}

internal abstract class KonnectionImpl(
    private val endpoint: Endpoint,
    eventLoopGroup: EventLoopGroup,
    val config: KredsClientConfig
) : Konnection {

    private val bootstrap: Bootstrap = Bootstrap().group(eventLoopGroup)
        .remoteAddress(endpoint.toSocketAddress())
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.connectTimeOutMillis)
        .option(ChannelOption.SO_KEEPALIVE, config.soKeepAlive)
        .channel(NioSocketChannel::class.java)

    private var channel: SocketChannel? = null
    private var readChannel: KChannel<RedisMessage>? = null

    private fun createChannelInitializer(readChannel: KChannel<RedisMessage>): ChannelInitializer<SocketChannel> {
        return object : ChannelInitializer<SocketChannel>() {
            override fun initChannel(ch: SocketChannel) {
                val pipeline = ch.pipeline()

                if (config.isSslEnabled()) {
                    val sslCtx = SslContextBuilder.forClient()
                        .trustManager(FileInputStream(config.sslTrustManager!!))
                        .build()
                    val sslEngine = sslCtx.newEngine(ch.alloc())
                    pipeline.addLast(SslHandler(sslEngine))
                }

                pipeline.addLast(RedisEncoder()) // outbound M
                //pipeline.addLast(WriteTimeoutHandler(10)) // outbound M - 1

                pipeline.addLast(RedisDecoder()) // inbound 1
                pipeline.addLast(RedisBulkStringAggregator()) // inbound 2
                pipeline.addLast(RedisArrayAggregator()) // inbound 3

                if (config.readTimeoutSeconds != KredsClientConfig.NO_READ_TIMEOUT)
                    pipeline.addLast(ReadTimeoutHandler(config.readTimeoutSeconds)) // duplex 4

                pipeline.addLast(ResponseHandler(readChannel)) // inbound 5

            }
        }
    }

    /**
     * Creates a new readChannel and tries to connect to remote peer.
     * Returns the connected SocketChannel and ReadChannel as [Pair<SocketChannel,Channel<RedisMessage>>]
     * @throws SocketException
     */
    private suspend fun createNewConnection(): Pair<SocketChannel, KChannel<RedisMessage>> =  withReentrantLock {
        val newReadChannel = KChannel<RedisMessage>(KChannel.UNLIMITED)
        Pair(
            bootstrap.handler(createChannelInitializer(newReadChannel)).connect().suspendableAwait() as SocketChannel,
            newReadChannel
        )
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

    override suspend fun isConnected() =  withReentrantLock {
        if (channel == null || readChannel == null) false
        else channel!!.isActive
    }

    private suspend fun writeInternal(message: RedisMessage, flush: Boolean): Unit = withReentrantLock {
        if (!isConnected()) throw KredsNotYetConnectedException()
        try {
            if (flush) channel!!.writeAndFlush(message).suspendableAwait()
            else channel!!.write(message)
        } catch (ex: Throwable) {
            when (ex) {
                is TimeoutException -> throw KredsTimeoutException("Write timed out.", ex)
                is SocketException -> throw KredsConnectionException(ex)
                else -> throw ex
            }
        }
    }

    override suspend fun flush(): Unit = withReentrantLock {
        if (!isConnected()) throw KredsNotYetConnectedException()
        else channel!!.flush()
    }

    override suspend fun write(message: RedisMessage): Unit = writeInternal(message, false)

    override suspend fun writeAndFlush(message: RedisMessage): Unit = writeInternal(message, true)

    override suspend fun read(): RedisMessage = withReentrantLock {
        if (!isConnected()) throw KredsNotYetConnectedException()
        try {
            readChannel!!.receive()
        } catch (ex: Throwable) {
            when (ex) {
                is ClosedReceiveChannelException -> throw KredsConnectionException("Connection closed.")
                is TimeoutException -> throw KredsTimeoutException("Read timed out.", ex)
                is SocketException -> throw KredsConnectionException(ex)
                else -> throw ex
            }
        }
    }

    override suspend fun connect(): Unit = withReentrantLock {
        if (!isConnected()) {
            try {
                val (channel, readChannel) = createNewConnection()
                logger.trace { "New connection created to $endpoint" }
                this.channel = channel
                this.readChannel = readChannel
            } catch (ex: SocketException) {
                throw KredsConnectionException("Failed to connect to $endpoint", ex)
            }
        }
    }

    override suspend fun disconnect(): Unit = withReentrantLock {
        if (isConnected()) {
            readChannel!!.close()
            channel!!.close().suspendableAwait()
        }
    }
}

