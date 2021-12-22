package io.github.crackthecodeabhi.kreds.connection

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
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import io.github.crackthecodeabhi.kreds.toByteBuf
import io.github.crackthecodeabhi.kreds.toDefaultCharset
import java.util.concurrent.atomic.AtomicInteger
import io.github.crackthecodeabhi.kreds.lockByCoroutineJob

internal class TestConnectionImpl(endpoint: Endpoint, eventLoopGroup: EventLoopGroup, config: KredsClientConfig): KonnectionImpl(endpoint, eventLoopGroup,config){
    override val mutex: Mutex = Mutex()
}

class KonnectionTest {

    companion object {

        private val eventLoopGroup = NioEventLoopGroup(4)

        @AfterAll
        @JvmStatic
        fun close(){
            eventLoopGroup.shutdownGracefully().get()
        }
    }

    private fun createPing(message: String): ArrayRedisMessage {
        return ArrayRedisMessage(listOf(FullBulkStringRedisMessage("PING".toByteBuf()),
            FullBulkStringRedisMessage(message.toByteBuf())
        ))
    }

    @Test
    fun testConnectionExclusivity(){
        val conn = TestConnectionImpl(Endpoint.from("127.0.0.1:6379"), eventLoopGroup, defaultClientConfig)
        val correctReplyCount = AtomicInteger(0)
        runBlocking {
            coroutineScope {
                withContext(Dispatchers.Default){
                    repeat(10000){
                        launch {
                            val count = it.toString(10)
                            conn.lockByCoroutineJob {
                                conn.connect()
                                conn.writeAndFlush(createPing(count))
                                when(val reply = conn.read()){
                                    !is FullBulkStringRedisMessage -> throw KredsConnectionException("Received invalid response for ping.")
                                    else -> {
                                        val actual = reply.content().toDefaultCharset()
                                        reply.content().release()
                                        if(actual != count) throw KredsConnectionException("Konnection state corrupted! Expected $count, received $actual")
                                        else correctReplyCount.incrementAndGet()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        assert(correctReplyCount.get() == 10000)
        println("Correct Reply count = ${correctReplyCount.get()}")
    }

    @Test
    fun testConnectionFail(){
        val conn = TestConnectionImpl(Endpoint.from("127.0.0.1:6373"), eventLoopGroup, defaultClientConfig)
        val ex = assertThrows<KredsConnectionException> {
            runBlocking {
                conn.lockByCoroutineJob {
                    conn.connect()
                }
            }
        }
        println("cause = ${ex.cause}")
    }

    @Test
    fun testConnectionTimeout(){
        val conn = TestConnectionImpl(Endpoint.from("www.google.com:81"), eventLoopGroup, defaultClientConfig)
        val ex = assertThrows<KredsConnectionException> {
            runBlocking {
                conn.lockByCoroutineJob {
                    conn.connect()
                }
            }
        }
        println("cause = ${ex.cause}")
    }

    @Test
    fun testReadTimeout(){
        val bs = ServerBootstrap()
        val serverEventLoopGroup = NioEventLoopGroup()
        val workerGroup = NioEventLoopGroup()
        val scf = bs.group(serverEventLoopGroup,workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .childHandler(object : ChannelInitializer<SocketChannel>(){
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline().addFirst(object: SimpleChannelInboundHandler<ByteBuf>(){
                        override fun channelRead0(ctx: ChannelHandlerContext, msg: ByteBuf) {
                            println("Received: ${msg.toDefaultCharset()}")
                            // no response, let it timeout
                        }
                    })
                }
            }).bind(8081).sync()

        val conn = TestConnectionImpl(Endpoint.from("127.0.0.1:8081"), eventLoopGroup, defaultClientConfig)
        val cause = assertThrows<KredsConnectionException> {
            runBlocking {
                conn.connect()
                conn.writeAndFlush(createPing("Client: Hello there!"))
                conn.read()
            }
        }
        println("cause = ${cause.cause}")
        scf.channel().close().sync()
        serverEventLoopGroup.shutdownGracefully().get()
    }
}