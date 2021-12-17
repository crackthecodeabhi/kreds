package org.kreds.connection

import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.redis.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import org.kreds.KredsContext
import kotlinx.coroutines.channels.Channel as KChannel

abstract class KonnectionImpl(endpoint: Endpoint, eventLoopGroup: EventLoopGroup): ExclusiveObject(), Konnection {

    private var channel: SocketChannel? = null
    private val cScope = CoroutineScope(KredsContext.context)
    private val bootstrap: Bootstrap

    override val readChannel = KChannel<RedisMessage>()

    init {
        bootstrap = Bootstrap()
            .group(eventLoopGroup)
            .remoteAddress(endpoint.toSocketAddress())
            .channel(NioSocketChannel::class.java)
            .handler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    val pipeline = ch.pipeline()
                    pipeline.addFirst(RedisEncoder())
                    pipeline.addFirst(responseHandler)
                    pipeline.addFirst(RedisArrayAggregator())
                    pipeline.addFirst(RedisBulkStringAggregator())
                    pipeline.addFirst(RedisDecoder())
                }
            })
    }

    private val responseHandler = object : ChannelInboundHandlerAdapter(){

        override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
            cScope.launch {
                readChannel.close(cause)
            }
        }

        override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
            cScope.launch {
                readChannel.send(msg as RedisMessage)
            }
        }
    }

    override fun isConnected() = channel?.isActive ?: false

    override suspend fun writeAndFlush(message: RedisMessage){
        connect()
        channel!!.writeAndFlush(message).suspendableAwait()
    }

    override suspend fun write(message: RedisMessage) {
        connect()
        channel!!.write(message)
    }

    override suspend fun connect(){
        if(!isConnected()) {
            channel = bootstrap.connect().suspendableAwait() as SocketChannel
        }
    }

    override suspend fun disconnect(){
        if(isConnected()){
            cScope.cancel()
            readChannel.close()
            channel!!.close().suspendableAwait()
        }
    }
}

