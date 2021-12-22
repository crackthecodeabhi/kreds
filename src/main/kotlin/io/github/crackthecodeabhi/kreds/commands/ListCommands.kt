package io.github.crackthecodeabhi.kreds.commands

import io.netty.handler.codec.redis.RedisMessage
import io.github.crackthecodeabhi.kreds.args.*

import io.github.crackthecodeabhi.kreds.commands.ListCommand.*
import io.github.crackthecodeabhi.kreds.protocol.*
import io.github.crackthecodeabhi.kreds.second
import java.lang.ClassCastException

internal enum class ListCommand(override val subCommand: Command? = null) : Command {
    BLMOVE, BLMPOP, BLPOP, BRPOP, BRPOPLPUSH, LINDEX, LINSERT, LLEN,
    LMOVE, LMPOP, LPOP, LPOS, LPUSH, LPUSHX, LRANGE, LREM, LSET, LTRIM,
    RPOP, RPOPLPUSH, RPUSH, RPUSHX;

    override val string = name
}

internal interface BaseListCommands {

    fun _lindex(key: String, index: Long) =
        CommandExecution(LINDEX, BulkStringCommandProcessor,key.toArgument(),index.toArgument())

    fun _linsert(key: String, beforeAfterOption: BeforeAfterOption, pivot: String, element: String) =
        CommandExecution(LINSERT, IntegerCommandProcessor,*createArguments(
            key,
            beforeAfterOption,
            pivot,
            element
        ))

    fun _llen(key: String)= CommandExecution(LLEN, IntegerCommandProcessor,key.toArgument())

    fun _lmove(
        source: String,
        destination: String,
        leftRightOption1: LeftRightOption,
        leftRightOption2: LeftRightOption
    ) = CommandExecution(LMOVE, BulkStringCommandProcessor,source.toArgument(),destination.toArgument(),leftRightOption1,leftRightOption2)

    fun _lmpop(numkeys: Long, key: String, vararg keys: String, leftRight: LeftRightOption, count: Long?) =
        CommandExecution(LMPOP,LMPopResultProcessor,*createArguments(
            numkeys,
            key,
            *keys,
            leftRight,
            count?.let { KeyOnlyArgument("COUNT") }
        ))

    fun _lpop(key: String) =
        CommandExecution(LPOP, BulkStringCommandProcessor,key.toArgument())

    fun _lpop(key: String, count: Long) =
        CommandExecution(LPOP,CommandProcessor(BulkStringHandler,ArrayHandler),key.toArgument(),count.toArgument())
}

public data class LMPOPResult(val key: String, val elements: List<String>)

@Suppress("UNCHECKED_CAST")
internal object LMPopResultProcessor : CommandProcessor(ArrayHandler, BulkStringHandler) {
    override fun <T> decode(message: RedisMessage): T {
        val reply: List<Any>? = super.decode(message)
        reply ?: return null as T
        if (reply.size != 2) throw KredsRedisDataException("Invalid response received for LMPOP command from server.")
        try {
            return LMPOPResult(reply.first() as String, reply.second() as List<String>) as T
        } catch (ex: Throwable) {
            when (ex) {
                is ClassCastException -> throw KredsRedisDataException("Invalid response received for LMPOP command from server.")
                else -> throw ex
            }
        }
    }
}

public interface ListCommands {

    /**
     * ###  LINDEX key index
     *
     * Returns the element at index index in the list stored at key. The index is zero-based.
     *
     * [Doc](https://redis.io/commands/lindex)
     * @since 1.0.0
     * @return the requested element, or null when index is out of range.
     */
    public suspend fun lindex(key: String, index: Long): String?

    /**
     * ### ` LINSERT key BEFORE|AFTER pivot element `
     *
     * Inserts element in the list stored at key either before or after the reference value pivot.
     *
     * [Doc](https://redis.io/commands/linsert)
     * @since 2.2.0
     * @return the length of the list after the insert operation, or -1 when the value pivot was not found
     */
    public suspend fun linsert(key: String, beforeAfterOption: BeforeAfterOption, pivot: String, element: String): Long

    /**
     * ### LLEN key
     *
     * [Doc](https://redis.io/commands/llen)
     * @since 1.0.0
     * @return the length of the list at key.
     */
    public suspend fun llen(key: String): Long

    /**
     * ### ` LMOVE source destination LEFT|RIGHT LEFT|RIGHT `
     *
     * [Doc](https://redis.io/commands/lmove)
     * @since 6.2.0
     * @return the element being popped and pushed.
     */
    public suspend fun lmove(
        source: String,
        destination: String,
        leftRightOption1: LeftRightOption,
        leftRightOption2: LeftRightOption
    ): String

    /**
     * ### ` LMPOP numkeys key [key ...] LEFT|RIGHT [COUNT count] `
     *
     * Pops one or more elements from the first non-empty list key from the list of provided key names.
     *
     * [Doc](https://redis.io/commands/lmpop)
     * @since 7.0.0
     * @return A null when no element could be popped.
     * A two-element array with the first element being the name of the key from which elements were popped, and the second element is an array of elements.
     */
    public suspend fun lmpop(
        numkeys: Long,
        key: String,
        vararg keys: String,
        leftRight: LeftRightOption,
        count: Long?
    ): LMPOPResult?

    /**
     * ### ` LPOP key`
     *
     * Removes and returns the first elements of the list stored at key.
     *
     * [Doc](https://redis.io/commands/lpop)
     * @since 1.0.0
     * @return the value of the first element, or null when key does not exist.
     */
    public suspend fun lpop(key: String): String?

    /**
     * ### ` LPOP key [count]`
     *
     * Removes and returns the first elements of the list stored at key.
     *
     * [Doc](https://redis.io/commands/lpop)
     * @since 1.0.0
     * @return list of popped elements, or null when key does not exist.
     */
    public suspend fun lpop(key: String, count: Long): List<String>?

}

internal interface ListCommandExecutor : ListCommands, CommandExecutor, BaseListCommands {
    override suspend fun lindex(key: String, index: Long): String? =
        execute(_lindex(key, index))

    override suspend fun linsert(
        key: String,
        beforeAfterOption: BeforeAfterOption,
        pivot: String,
        element: String
    ): Long = execute(_linsert(key, beforeAfterOption, pivot, element))

    override suspend fun llen(key: String): Long =
        execute(_llen(key))

    override suspend fun lmove(
        source: String,
        destination: String,
        leftRightOption1: LeftRightOption,
        leftRightOption2: LeftRightOption
    ): String =
        execute(_lmove(source, destination, leftRightOption1, leftRightOption2))

    override suspend fun lmpop(
        numkeys: Long,
        key: String,
        vararg keys: String,
        leftRight: LeftRightOption,
        count: Long?
    ): LMPOPResult? = execute(_lmpop(numkeys, key, *keys, leftRight = leftRight, count = count))

    override suspend fun lpop(key: String): String? = execute(_lpop(key))

    override suspend fun lpop(key: String, count: Long): List<String>? = execute(_lpop(key, count))
}