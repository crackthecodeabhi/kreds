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

package io.github.crackthecodeabhi.kreds.protocol

import io.netty.handler.codec.redis.*
import io.github.crackthecodeabhi.kreds.args.*
import io.github.crackthecodeabhi.kreds.commands.*
import io.github.crackthecodeabhi.kreds.toByteBuf
import kotlin.jvm.Throws

internal fun Command.toRedisMessageList(): List<FullBulkStringRedisMessage> {
    return if (subCommand != null) {
        listOf(FullBulkStringRedisMessage(string.toByteBuf()), *subCommand!!.toRedisMessageList().toTypedArray())
    } else listOf(FullBulkStringRedisMessage(string.toByteBuf()))
}

internal interface ICommandProcessor {
    fun encode(command: Command, vararg args: Argument): RedisMessage

    @Throws(KredsRedisDataException::class)
    fun <T> decode(message: RedisMessage): T
}

internal interface CommandExecutor {
    suspend fun <T> execute(command: Command, processor: ICommandProcessor, vararg args: Argument): T
    suspend fun <T> execute(commandExecution: CommandExecution): T
    suspend fun executeCommands(commands: List<CommandExecution>): List<RedisMessage>
}

internal val IntegerCommandProcessor = CommandProcessor(IntegerHandler)
internal val BulkStringCommandProcessor = CommandProcessor(BulkStringHandler)
internal val SimpleStringCommandProcessor = CommandProcessor(SimpleStringHandler)
internal val ArrayCommandProcessor = CommandProcessor(ArrayHandler)

internal open class CommandProcessor(private vararg val outputTypeHandlers: MessageHandler<*>) : ICommandProcessor {

    override fun encode(command: Command, vararg args: Argument): RedisMessage {
        if (args.isEmpty()) return ArrayRedisMessage(command.toRedisMessageList())
        val x = command.toRedisMessageList().toMutableList()
        x.addAll(args.filter { it !is EmptyArgument }.map { FullBulkStringRedisMessage(it.toString().toByteBuf()) })
        return ArrayRedisMessage(x as List<RedisMessage>)
    }

    @Throws(KredsRedisDataException::class)
    @Suppress("UNCHECKED_CAST")
    override fun <T> decode(message: RedisMessage): T {
        if (message is ErrorRedisMessage) throw KredsRedisDataException(message.content())
        val handler = outputTypeHandlers.first { it.canHandle(message) }
        return handler.doHandle(message) as T
    }
}