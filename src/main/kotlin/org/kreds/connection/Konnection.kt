package org.kreds.connection

import io.netty.handler.codec.redis.RedisMessage
import org.kreds.CoroutineSafe
import org.kreds.ExclusiveObject
import org.kreds.lockByCoroutineJob

/**
 * Interface which models a connection to a redis server.
 *
 * Provides basic connection management and read and write operations of [RedisMessage].
 *
 * Implementations should be coroutine safe.
 *
 * Konnection is coroutine exclusive, i.e, only one coroutine can perform operations on the Konnection
 * while holding the [kotlinx.coroutines.sync.Mutex] defined in [ExclusiveObject]
 */
@CoroutineSafe
internal interface Konnection: ExclusiveObject {
    /**
     * Connects to the Redis server.
     * @throws KredsConnectionException
     * @throws KredsTimeoutException
     */
    suspend fun connect()

    /**
     * Returns true, if connection is alive and active.
     */
    suspend fun isConnected(): Boolean

    /**
     * Disconnects the connection if connected.
     */
    suspend fun disconnect()

    /**
     * Write the given [RedisMessage] and flushes it immediately.
     * @throws KredsConnectionException
     * @throws KredsTimeoutException
     * @throws KredsNotYetConnectedException
     */
    suspend fun writeAndFlush(message: RedisMessage)

    /**
     * Writes the given [RedisMessage], an explicit flush needs to be invoked.
     * @throws KredsConnectionException
     * @throws KredsTimeoutException
     * @throws KredsNotYetConnectedException
     */
    suspend fun write(message: RedisMessage)

    /**
     * Flushes any messages in the channel.
     * @throws KredsNotYetConnectedException
     */
    suspend fun flush()

    /**
     * Reads a [RedisMessage] from the connection.
     * @throws KredsNotYetConnectedException
     * @throws KredsTimeoutException
     * @throws KredsConnectionException
     */
    suspend fun read(): RedisMessage

    /**
     * Reads a [RedisMessage] from the connection if available else returns null.
     * @throws KredsNotYetConnectedException
     * @throws KredsTimeoutException
     * @throws KredsConnectionException
     */
    suspend fun tryRead(): RedisMessage?
}


/**
 * Connects to the Redis server, writes the message and flushes.
 * @throws KredsConnectionException
 * @throws KredsTimeoutException
 * @throws KredsNotYetConnectedException
 */
internal suspend inline fun Konnection.connectWriteAndFlush(message: RedisMessage) = lockByCoroutineJob {
    if(isConnected()) writeAndFlush(message)
    else {
        connect()
        writeAndFlush(message)
    }
}