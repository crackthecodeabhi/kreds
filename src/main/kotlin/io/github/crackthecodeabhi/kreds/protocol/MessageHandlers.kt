package io.github.crackthecodeabhi.kreds.protocol

import io.netty.buffer.Unpooled
import io.netty.handler.codec.redis.*
import io.github.crackthecodeabhi.kreds.toDefaultCharset

internal interface MessageHandler<T> {
    fun canHandle(message: RedisMessage): Boolean
    fun doHandle(message: RedisMessage): T
}

internal object SimpleStringHandler: MessageHandler<String>{
    override fun canHandle(message: RedisMessage): Boolean = message is SimpleStringRedisMessage

    override fun doHandle(message: RedisMessage): String {
        val msg = message as SimpleStringRedisMessage
        return msg.content()?: throw KredsRedisDataException("Unexpected: received null as RESP Simple String")
    }
}

internal object IntegerHandler: MessageHandler<Long>{
    override fun canHandle(message: RedisMessage): Boolean = message is IntegerRedisMessage

    override fun doHandle(message: RedisMessage): Long {
        val msg = message as IntegerRedisMessage
        return msg.value()
    }
}

internal object BulkStringHandler: MessageHandler<String?>{
    override fun canHandle(message: RedisMessage): Boolean = message is FullBulkStringRedisMessage

    override fun doHandle(message: RedisMessage): String? {
        val msg = message as FullBulkStringRedisMessage
        return if(msg.isNull) null
        else if(msg.content() == Unpooled.EMPTY_BUFFER) ""
        else msg.content().toDefaultCharset()
    }
}

internal object ArrayHandler: MessageHandler<List<Any?>>{
    override fun canHandle(message: RedisMessage): Boolean = message is ArrayRedisMessage

    override fun doHandle(message: RedisMessage): List<Any?> {
        val msg = message as ArrayRedisMessage
        return if(msg.children().isEmpty()) emptyList()
        else {
            msg.children().map {
                when(true){
                    SimpleStringHandler.canHandle(it) -> SimpleStringHandler.doHandle(it)
                    IntegerHandler.canHandle(it) -> IntegerHandler.doHandle(it)
                    BulkStringHandler.canHandle(it) -> BulkStringHandler.doHandle(it)
                    canHandle(it) -> doHandle(it)
                    else -> throw KredsRedisDataException("Received unexpected data type from redis server.")
                }
            }
        }
    }
}