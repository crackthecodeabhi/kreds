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

import io.github.crackthecodeabhi.kreds.args.Argument
import io.github.crackthecodeabhi.kreds.args.EmptyArgument
import io.github.crackthecodeabhi.kreds.commands.Command
import io.github.crackthecodeabhi.kreds.commands.CommandExecution
import io.github.crackthecodeabhi.kreds.toByteBuf
import io.netty.handler.codec.redis.ArrayRedisMessage
import io.netty.handler.codec.redis.ErrorRedisMessage
import io.netty.handler.codec.redis.FullBulkStringRedisMessage
import io.netty.handler.codec.redis.RedisMessage

internal fun Command.toRedisMessageList(): List<FullBulkStringRedisMessage> {
    return if (subCommand != null) {
        listOf(FullBulkStringRedisMessage(string.toByteBuf()), *subCommand!!.toRedisMessageList().toTypedArray())
    } else listOf(FullBulkStringRedisMessage(string.toByteBuf()))
}

internal interface ICommandProcessor<out R> {
    fun encode(command: Command, vararg args: Argument): RedisMessage {
        if (args.isEmpty()) return ArrayRedisMessage(command.toRedisMessageList())
        val x = command.toRedisMessageList().toMutableList()
        x.addAll(args.filter { it !is EmptyArgument }.map { FullBulkStringRedisMessage(it.toString().toByteBuf()) })
        return ArrayRedisMessage(x as List<RedisMessage>)
    }

    @Throws(KredsRedisDataException::class)
    fun decode(message: RedisMessage): R
}

internal interface CommandExecutor {
    suspend fun <T> execute(command: Command, processor: ICommandProcessor<T>, vararg args: Argument): T
    suspend fun <T> execute(commandExecution: CommandExecution<T>): T
    suspend fun executeCommands(commands: List<CommandExecution<*>>): List<RedisMessage>
}

internal val AllCommandProcessor = CommandProcessor<Any?>(SimpleStringHandler, IntegerHandler, BulkStringHandler, ArrayHandler)
internal val IntegerCommandProcessor = CommandProcessor<Long>(IntegerHandler)
internal val BulkStringCommandProcessor = CommandProcessor<String?>(BulkStringHandler)
internal val SimpleStringCommandProcessor = CommandProcessor<String>(SimpleStringHandler)
internal val ArrayCommandProcessor = CommandProcessor<List<*>?>(ArrayHandler)
internal val SimpleAndBulkStringCommandProcessor = CommandProcessor<String?>(SimpleStringHandler, BulkStringHandler)
internal val IntegerOrBulkNullStringCommandProcessor = CommandProcessor<Long?>(IntegerHandler, BulkStringHandler)

internal open class CommandProcessor<R>(private vararg val outputTypeHandlers: MessageHandler<*>) :
    ICommandProcessor<R> {

    @Throws(KredsRedisDataException::class)
    @Suppress("UNCHECKED_CAST")
    override fun decode(message: RedisMessage): R {
        if (message is ErrorRedisMessage) throw KredsRedisDataException(message.content())
        val handler = outputTypeHandlers.first { it.canHandle(message) }
        return handler.doHandleAndRelease(message) as R
    }
}