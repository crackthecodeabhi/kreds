/*
 *  Copyright (C) 2021 Abhijith Shivaswamy
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
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.redis.ArrayRedisMessage
import io.netty.handler.codec.redis.FullBulkStringRedisMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import io.github.crackthecodeabhi.kreds.toByteBuf
import io.github.crackthecodeabhi.kreds.toDefaultCharset
import java.util.concurrent.atomic.AtomicInteger
import io.github.crackthecodeabhi.kreds.withReentrantLock
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec

internal class TestConnectionImpl(endpoint: Endpoint, eventLoopGroup: EventLoopGroup, config: KredsClientConfig) :
    KonnectionImpl(endpoint, eventLoopGroup, config) {
    override val mutex: Mutex = Mutex()
    override val key: ReentrantMutexContextKey = ReentrantMutexContextKey(mutex)
}

private fun createPing(message: String): ArrayRedisMessage {
    return ArrayRedisMessage(
        listOf(
            FullBulkStringRedisMessage("PING".toByteBuf()),
            FullBulkStringRedisMessage(message.toByteBuf())
        )
    )
}

private lateinit var eventLoopGroup: EventLoopGroup

class KonnectionTest : FunSpec({
    beforeSpec {
        eventLoopGroup = NioEventLoopGroup(4)
    }
    afterSpec {
        eventLoopGroup.shutdownGracefully().suspendableAwait()
    }

    test("Connection Exclusivity") {
        val concurrencyCount = 100
        val conn = TestConnectionImpl(Endpoint.from("127.0.0.1:6379"), eventLoopGroup, defaultClientConfig)
        val correctReplyCount = AtomicInteger(0)
        coroutineScope {
            withContext(Dispatchers.Default) {
                repeat(concurrencyCount) {
                    launch {
                        val count = it.toString(10)
                        conn.withReentrantLock {
                            conn.connect()
                            conn.writeAndFlush(createPing(count))
                            when (val reply = conn.read()) {
                                !is FullBulkStringRedisMessage -> throw KredsConnectionException("Received invalid response for ping.")
                                else -> {
                                    val actual = reply.content().toDefaultCharset()
                                    reply.content().release()
                                    if (actual != count) throw KredsConnectionException("Konnection state corrupted! Expected $count, received $actual")
                                    else correctReplyCount.incrementAndGet()
                                }
                            }
                        }
                    }
                }
            }
        }
        assert(correctReplyCount.get() == concurrencyCount)
        println("Correct Reply count = ${correctReplyCount.get()}")
    }

    test("Connection Fail") {
        val conn = TestConnectionImpl(Endpoint.from("127.0.0.1:6373"), eventLoopGroup, defaultClientConfig)
        val ex = shouldThrow<KredsConnectionException> {
            conn.withReentrantLock {
                conn.connect()
            }
        }
        println("cause = ${ex.cause}")
    }

    test("Connection Timeout") {
        val conn = TestConnectionImpl(Endpoint.from("www.google.com:81"), eventLoopGroup, defaultClientConfig)
        val ex = shouldThrow<KredsConnectionException> {
            conn.withReentrantLock {
                conn.connect()
            }
        }
        println("cause = ${ex.cause}")
    }

    test("Read Timeout") {
        val bs = ServerBootstrap()
        val serverEventLoopGroup = NioEventLoopGroup()
        val workerGroup = NioEventLoopGroup()
        val channel = bs.group(serverEventLoopGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .childHandler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline().addFirst(object : SimpleChannelInboundHandler<ByteBuf>() {
                        override fun channelRead0(ctx: ChannelHandlerContext, msg: ByteBuf) {
                            println("Received: ${msg.toDefaultCharset()}")
                            // no response, let it timeout
                        }
                    })
                }
            }).bind(8081).sync().suspendableAwait()

        val conn =
            TestConnectionImpl(
                Endpoint.from("127.0.0.1:8081"), eventLoopGroup,
                KredsClientConfig.Builder(readTimeoutSeconds = 1).build(defaultClientConfig)
            )
        val cause = shouldThrow<KredsConnectionException> {
            conn.connect()
            conn.writeAndFlush(createPing("Client: Hello there!"))
            conn.read()
        }
        println("cause = ${cause.cause}")
        channel.close().sync().suspendableAwait()
        serverEventLoopGroup.shutdownGracefully().suspendableAwait()
    }
})
