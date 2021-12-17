package org.kreds.protocol

import io.netty.buffer.Unpooled
import io.netty.handler.codec.redis.*
import org.kreds.Argument
import org.kreds.toByteBuf
import org.kreds.toDefaultCharset
import kotlin.jvm.Throws

interface Command{
    /**
     * Command string
     */
    val string: String
}

class CommandExecution(val command: Command,val processor: ICommandProcessor, vararg val args: Argument)

interface ICommandProcessor {
    fun encode(command: Command, vararg args: Argument): RedisMessage
    @Throws(KredsRedisDataException::class)
    fun <T> decode(message: RedisMessage): T
}

interface CommandExecutor {
    suspend fun <T> execute(command: Command, processor: ICommandProcessor, vararg args: Argument): T
    suspend fun <T> execute(commandExecution: CommandExecution): T
}

open class CommandProcessor(private vararg val outputTypeHandlers: MessageHandler<*>): ICommandProcessor {

    override fun encode(command: Command,vararg args: Argument): RedisMessage {
        if(args.isEmpty()) return ArrayRedisMessage(listOf(FullBulkStringRedisMessage(command.string.toByteBuf())))
        val x = mutableListOf(FullBulkStringRedisMessage(command.string.toByteBuf()))
        x.addAll(args.map { FullBulkStringRedisMessage(it.toString().toByteBuf()) })
        return ArrayRedisMessage(x as List<RedisMessage>)
    }

    @Throws(KredsRedisDataException::class)
    @Suppress("UNCHECKED_CAST")
    override fun <T> decode(message:RedisMessage): T {
        if(message is ErrorRedisMessage) throw KredsRedisDataException(message.content())
        val handler = outputTypeHandlers.first { it.canHandle(message) }
        return handler.doHandle(message) as T
    }
}

val IntegerCommandProcessor = CommandProcessor(IntegerHandler)
val BulkStringCommandProcessor = CommandProcessor(BulkStringHandler)
val SimpleStringCommandProcessor = CommandProcessor(SimpleStringHandler)
val ArrayCommandProcessor = CommandProcessor(ArrayHandler)

interface MessageHandler<T> {
    fun canHandle(message: RedisMessage): Boolean
    fun doHandle(message: RedisMessage): T
}

object SimpleStringHandler: MessageHandler<String>{
    override fun canHandle(message: RedisMessage): Boolean = message is SimpleStringRedisMessage

    override fun doHandle(message: RedisMessage): String {
        val msg = message as SimpleStringRedisMessage
        return msg.content()?: throw KredsRedisDataException("Unexpected: received null as RESP Simple String")
    }
}

object IntegerHandler: MessageHandler<Long>{
    override fun canHandle(message: RedisMessage): Boolean = message is IntegerRedisMessage

    override fun doHandle(message: RedisMessage): Long {
        val msg = message as IntegerRedisMessage
        return msg.value()
    }
}

object BulkStringHandler: MessageHandler<String?>{
    override fun canHandle(message: RedisMessage): Boolean = message is FullBulkStringRedisMessage

    override fun doHandle(message: RedisMessage): String? {
        val msg = message as FullBulkStringRedisMessage
        return if(msg.isNull) null
        else if(msg.content() == Unpooled.EMPTY_BUFFER) ""
        else msg.content().toDefaultCharset()
    }
}

object ArrayHandler: MessageHandler<List<Any?>>{
    override fun canHandle(message: RedisMessage): Boolean = message is ArrayRedisMessage

    override fun doHandle(message: RedisMessage): List<Any?> {
        val msg = message as ArrayRedisMessage
        return if(msg.children().isEmpty()) emptyList()
        else {
            msg.children().map {
                when(true){
                    SimpleStringHandler.canHandle(it) -> SimpleStringHandler.doHandle(it)
                    IntegerHandler.canHandle(it) -> IntegerHandler.doHandle(it)
                    BulkStringHandler.canHandle(it) -> BulkStringHandler.doHandle(it) as Any
                    else -> throw KredsRedisDataException("Received unexpected data type from redis server.")
                }
            }
        }
    }
}