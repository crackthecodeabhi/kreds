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

import io.github.crackthecodeabhi.kreds.CoroutineSafe
import io.github.crackthecodeabhi.kreds.ExclusiveObject
import io.github.crackthecodeabhi.kreds.withReentrantLock
import io.netty.handler.codec.redis.RedisMessage

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
internal interface Konnection : ExclusiveObject {
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

}


/**
 * Connects to the Redis server, writes the message and flushes.
 * @throws KredsConnectionException
 * @throws KredsTimeoutException
 * @throws KredsNotYetConnectedException
 */
internal suspend inline fun Konnection.connectWriteAndFlush(message: RedisMessage) =  withReentrantLock {
    if (isConnected()) writeAndFlush(message)
    else {
        connect()
        writeAndFlush(message)
    }
}