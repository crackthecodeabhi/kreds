package org.kreds.connection

import io.netty.handler.codec.redis.RedisMessage
import kotlinx.coroutines.channels.Channel

/**
 * Interface which models a connection to a redis server
 * Provides basic connection management and read and write operations
 * read is modeled as a Channel of RedisMessage
 */
interface Konnection {
    suspend fun connect()
    fun isConnected(): Boolean
    suspend fun disconnect()
    suspend fun writeAndFlush(message: RedisMessage)
    suspend fun write(message: RedisMessage)
    val readChannel: Channel<RedisMessage>
}