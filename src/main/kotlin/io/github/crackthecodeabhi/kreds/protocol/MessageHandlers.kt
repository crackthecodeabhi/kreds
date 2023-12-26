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

package io.github.crackthecodeabhi.kreds.protocol

import io.github.crackthecodeabhi.kreds.toDefaultCharset
import io.netty.buffer.Unpooled
import io.netty.handler.codec.redis.*
import io.netty.util.ReferenceCounted

internal interface MessageHandler<T> {
    fun canHandle(message: RedisMessage): Boolean
    /**
     * Handles the message and releases it
     */
    fun doHandleAndRelease(message: RedisMessage): T
}
internal abstract class AbstractMessageHandler<T>: MessageHandler<T> {
    abstract fun doHandle(message: RedisMessage): T
    override fun doHandleAndRelease(message: RedisMessage): T = try {
        doHandle(message)
    } finally {
        if (message is ReferenceCounted) {
            message.release()
        }
    }
}

internal object SimpleStringHandler : AbstractMessageHandler<String>() {
    override fun canHandle(message: RedisMessage): Boolean = message is SimpleStringRedisMessage

    override fun doHandle(message: RedisMessage): String {
        val msg = message as SimpleStringRedisMessage
        return msg.content() ?: throw KredsRedisDataException("Unexpected: received null as RESP Simple String")
    }
}

internal object IntegerHandler : AbstractMessageHandler<Long>() {
    override fun canHandle(message: RedisMessage): Boolean = message is IntegerRedisMessage

    override fun doHandle(message: RedisMessage): Long {
        val msg = message as IntegerRedisMessage
        return msg.value()
    }
}

internal object BulkStringHandler : AbstractMessageHandler<String?>() {
    override fun canHandle(message: RedisMessage): Boolean = message is FullBulkStringRedisMessage

    override fun doHandle(message: RedisMessage): String? {
        val msg = message as FullBulkStringRedisMessage
        return if (msg.isNull) null
        else if (msg.content() == Unpooled.EMPTY_BUFFER) ""
        else msg.content().toDefaultCharset()
    }
}

internal object ArrayHandler : AbstractMessageHandler<List<*>?>() {
    override fun canHandle(message: RedisMessage): Boolean = message is ArrayRedisMessage

    override fun doHandle(message: RedisMessage): List<*>? {
        val msg = message as ArrayRedisMessage
        return if (msg.isNull) null
        else if (msg.children().isEmpty()) emptyList()
        else {
            msg.children().map {
                when (true) {
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