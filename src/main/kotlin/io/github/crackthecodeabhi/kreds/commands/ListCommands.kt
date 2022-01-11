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

package io.github.crackthecodeabhi.kreds.commands

import io.github.crackthecodeabhi.kreds.args.*
import io.github.crackthecodeabhi.kreds.commands.ListCommand.*
import io.github.crackthecodeabhi.kreds.protocol.*
import io.github.crackthecodeabhi.kreds.second
import io.netty.handler.codec.redis.RedisMessage
import kotlin.time.Duration

internal enum class ListCommand(override val subCommand: Command? = null) : Command {
    BLMOVE, BLMPOP, BLPOP, BRPOP, BRPOPLPUSH, LINDEX, LINSERT, LLEN,
    LMOVE, LMPOP, LPOP, LPOS, LPUSH, LPUSHX, LRANGE, LREM, LSET, LTRIM,
    RPOP, RPUSH, RPUSHX;

    override val string = name
}

internal interface BaseListCommands {

    fun _lindex(key: String, index: Int) =
        CommandExecution(LINDEX, BulkStringCommandProcessor, key.toArgument(), index.toArgument())

    fun _linsert(key: String, beforeAfterOption: BeforeAfterOption, pivot: String, element: String) =
        CommandExecution(
            LINSERT, IntegerCommandProcessor, *createArguments(
                key,
                beforeAfterOption,
                pivot,
                element
            )
        )

    fun _llen(key: String) = CommandExecution(LLEN, IntegerCommandProcessor, key.toArgument())

    fun _lmove(
        source: String,
        destination: String,
        whereFrom: LeftRightOption,
        whereTo: LeftRightOption
    ) = CommandExecution(
        LMOVE,
        BulkStringCommandProcessor,
        source.toArgument(),
        destination.toArgument(),
        whereFrom,
        whereTo
    )

    fun _lmpop(numkeys: Long, key: String, vararg keys: String, leftRight: LeftRightOption, count: Long?) =
        CommandExecution(LMPOP, LMPopResultProcessor, *createArguments(
            numkeys,
            key,
            *keys,
            leftRight,
            count?.let { KeyOnlyArgument("COUNT") }
        ))

    fun _lpop(key: String) =
        CommandExecution(LPOP, BulkStringCommandProcessor, key.toArgument())

    fun _lpop(key: String, count: Long) =
        CommandExecution(LPOP, ArrayCommandProcessor, key.toArgument(), count.toArgument())

    fun _lpush(key: String, element: String, elements: Array<out String>) =
        CommandExecution(
            LPUSH,
            IntegerCommandProcessor,
            key.toArgument(),
            element.toArgument(),
            *createArguments(*elements)
        )

    fun _lpushx(key: String, element: String, vararg elements: String) =
        CommandExecution(
            LPUSHX,
            IntegerCommandProcessor,
            key.toArgument(),
            element.toArgument(),
            *createArguments(*elements)
        )

    fun _lrange(key: String, start: Int, stop: Int) =
        CommandExecution(LRANGE, ArrayCommandProcessor, key.toArgument(), start.toArgument(), stop.toArgument())

    fun _lrem(key: String, count: Int, element: String) =
        CommandExecution(LREM, IntegerCommandProcessor, key.toArgument(), count.toArgument(), element.toArgument())

    fun _lset(key: String, index: Int, element: String) =
        CommandExecution(LSET, SimpleStringCommandProcessor, key.toArgument(), index.toArgument(), element.toArgument())

    fun _ltrim(key: String, start: Int, stop: Int) =
        CommandExecution(LTRIM, SimpleStringCommandProcessor, key.toArgument(), start.toArgument(), stop.toArgument())

    fun _rpop(key: String) = CommandExecution(RPOP, BulkStringCommandProcessor, key.toArgument())

    fun _rpop(key: String, count: Int) =
        CommandExecution(RPOP, ArrayCommandProcessor, key.toArgument(), count.toArgument())

    fun _rpush(key: String, element: String, vararg elements: String) =
        CommandExecution(
            RPUSH,
            IntegerCommandProcessor,
            key.toArgument(),
            element.toArgument(),
            *createArguments(*elements)
        )

    fun _rpushx(key: String, element: String, vararg elements: String) =
        CommandExecution(
            RPUSHX,
            IntegerCommandProcessor,
            key.toArgument(),
            element.toArgument(),
            *createArguments(*elements)
        )

    fun _blpop(key: String, vararg keys: String, timeout: Duration) =
        CommandExecution(BLPOP, FirstTwoArrayElementProcessor, *createArguments(key, *keys, timeout.inWholeSeconds))

    fun _brpop(key: String, vararg keys: String, timeout: Duration) =
        CommandExecution(BRPOP, FirstTwoArrayElementProcessor, *createArguments(key, *keys, timeout.inWholeSeconds))

    fun _blmove(
        source: String,
        destination: String,
        whereFrom: LeftRightOption,
        whereTo: LeftRightOption,
        timeout: Duration
    ) = CommandExecution(
        BLMOVE, BulkStringCommandProcessor, *createArguments(
            source, destination, whereFrom, whereTo, timeout.inWholeSeconds
        )
    )

    fun _blmpop(
        timeout: Duration,
        numKeys: Int,
        key: String,
        vararg keys: String,
        from: LeftRightOption,
        count: Int? = null
    ) = CommandExecution(
        BLMPOP, LMPopResultProcessor, *createArguments(
            timeout.inWholeSeconds, numKeys, key, *keys, from, count ?: EmptyArgument
        )
    )
}

public data class LMPOPResult(val key: String, val elements: List<String>)

@Suppress("UNCHECKED_CAST")
internal object LMPopResultProcessor : ICommandProcessor<LMPOPResult?> {
    override fun decode(message: RedisMessage): LMPOPResult? {
        val reply = ArrayCommandProcessor.decode(message) ?: return null
        if (reply.size != 2) throw KredsRedisDataException("Invalid response received for LMPOP command from server.")
        try {
            return LMPOPResult(reply.first() as String, reply.second() as List<String>)
        } catch (ex: Throwable) {
            when (ex) {
                is ClassCastException -> throw KredsRedisDataException("Invalid response received for LMPOP command from server.")
                else -> throw ex
            }
        }
    }
}

public interface BlockingListCommands : BlockingOperation {

    /**
     * ### `BLPOP key [key ...] timeout`
     *
     * BLPOP is a blocking list pop primitive.
     * It is the blocking version of LPOP because it blocks the connection when there are no elements to pop from any of the given lists.
     * An element is popped from the head of the first list that is non-empty, with the given keys being checked in the order that they are given.
     *
     * [Doc](https://redis.io/commands/blpop)
     * @param timeout maximum number of seconds to block
     * @since 2.0.0
     * @return *  A null when no element could be popped and the timeout expired.
     *  * A pair with the first element being the name of the key where an element was popped and the second element being the value of the popped element.
     */
    public suspend fun blpop(key: String, vararg keys: String, timeout: Duration): Pair<String, String>?

    /**
     * ### ` BRPOP key [key ...] timeout `
     *
     * BRPOP s a blocking list pop primitive.
     * It is the blocking version of RPOP because it blocks the connection when there
     * are no elements to pop from any of the given lists. An element is popped from the tail of the first list that is non-empty,
     * with the given keys being checked in the order that they are given.
     *
     * [Doc](https://redis.io/commands/brpop)
     * @since 2.0.0
     * @return * A null when no element could be popped and the timeout expired.
     * * A pair with the first element being the name of the key where an element was popped and the second element being the value of the popped element.
     */
    public suspend fun brpop(key: String, vararg keys: String, timeout: Duration): Pair<String, String>?

    /**
     * ### ` BLMOVE source destination LEFT|RIGHT LEFT|RIGHT timeout `
     *
     * [Doc](https://redis.io/commands/blmove)
     * @since 6.2.0
     * @return the element being popped from source and pushed to destination. If timeout is reached, a Null reply is returned.
     */
    public suspend fun blmove(
        source: String,
        destination: String,
        whereFrom: LeftRightOption,
        whereTo: LeftRightOption,
        timeout: Duration
    ): String?

    /**
     * ### ` BLMPOP timeout numkeys key [key ...] LEFT|RIGHT [COUNT count] `
     *
     * BLMPOP is the blocking variant of LMPOP.
     *
     * [Doc](https://redis.io/commands/blmpop)
     * @since 7.0.0
     * @return * A null when no element could be popped, and timeout is reached.
     * * A pair with the first element being the name of the key from which elements were popped, and the second element is an array of elements.
     */
    public suspend fun blmpop(
        timeout: Duration,
        numKeys: Int,
        key: String,
        vararg keys: String,
        from: LeftRightOption,
        count: Int? = null
    ): LMPOPResult?
}

/**
 * Works on Array Redis messages
 * Decodes first 2 elements of array to string pair, if array is null, returns null.
 */
internal object FirstTwoArrayElementProcessor : ICommandProcessor<Pair<String, String>?> {
    override fun decode(message: RedisMessage): Pair<String, String>? {
        try {
            @Suppress("UNCHECKED_CAST")
            return when (val resp = ArrayCommandProcessor.decode(message) as List<String>?) {
                null -> null
                else -> Pair(resp.first(), resp.second())
            }
        } catch (ex: ClassCastException) {
            throw KredsRedisDataException("Failed to decode response.", ex)
        }
    }
}

public interface ListCommands {

    /**
     * ###  LINDEX key index
     *
     * Returns the element at index [index] in the list stored at key. The index is zero-based.
     *
     * [Doc](https://redis.io/commands/lindex)
     * @since 1.0.0
     * @return the requested element, or null when index is out of range.
     */
    public suspend fun lindex(key: String, index: Int): String?

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
        whereFrom: LeftRightOption,
        whereTo: LeftRightOption
    ): String?

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

    /**
     * ### ` LPUSH key element [element ...] `
     *
     * Insert all the specified values at the head of the list stored at key.
     * If key does not exist, it is created as empty list before performing the push operations.
     * When key holds a value that is not a list, an error is returned.
     *
     * [Doc](https://redis.io/commands/lpush)
     * @since 1.0.0
     *
     * 2.4.0: Accepts multiple [elements]
     *
     * @return the length of the list after the push operations.
     */
    public suspend fun lpush(key: String, element: String, vararg elements: String): Long

    /**
     * ### ` LPUSHX key element [element ...] `
     *
     * Inserts specified values at the head of the list stored at key,
     * only if key already exists and holds a list.
     * In contrary to `LPUSH`, no operation will be performed when key does not yet exist.
     *
     * [Doc](https://redis.io/commands/lpushx)
     * @since 2.2.0
     *
     * 4.0.0: Accepts multiple [elements]
     *
     * @return the length of the list after the push operation.
     */
    public suspend fun lpushx(key: String, element: String, vararg elements: String): Long

    /**
     * ###  LRANGE key start stop
     *
     * Returns the specified elements of the list stored at key.
     * The offsets start and stop are zero-based indexes,
     * with 0 being the first element of the list (the head of the list),
     * 1 being the next element and so on.
     *
     * [Doc](https://redis.io/commands/lrange)
     *
     * @since 1.0.0
     * @return list of elements in the specified range.
     */
    public suspend fun lrange(key: String, start: Int, stop: Int): List<String>

    /**
     * ###  LREM key count element
     *
     * Removes the first count occurrences of elements equal to element from the list stored at key.
     * The count argument influences the operation in the following ways:
     * * count > 0: Remove elements equal to element moving from head to tail.
     * * count < 0: Remove elements equal to element moving from tail to head.
     * * count = 0: Remove all elements equal to element.
     *
     * [Doc](https://redis.io/commands/lrem)
     * @since 1.0.0
     * @return the number of removed elements.
     */
    public suspend fun lrem(key: String, count: Int, element: String): Long

    /**
     * ###  LSET key index element
     * Sets the list element at index to element.
     * For more information on the index argument, see [lindex].
     *
     * [Doc](https://redis.io/commands/lset)
     * @since 1.0.0
     * @return string reply
     */
    public suspend fun lset(key: String, index: Int, element: String): String

    /**
     * ###  LTRIM key start stop
     *
     * Trim an existing list so that it will contain only the specified range of elements specified.
     *
     * [Doc](https://redis.io/commands/ltrim)
     * @since 1.0.0
     * @return string reply
     */
    public suspend fun ltrim(key: String, start: Int, stop: Int): String

    /**
     * ###  RPOP key
     *
     * Removes and returns the last elements of the list stored at key.
     *
     * [Doc](https://redis.io/commands/rpop)
     * @since 1.0.0
     * @return the value of the last element, or null when key does not exist.
     */
    public suspend fun rpop(key: String): String?

    /**
     * ###  RPOP key
     *
     * Removes and returns the last elements of the list stored at key.
     * The reply will consist of up to count elements, depending on the list's length.
     * [Doc](https://redis.io/commands/rpop)
     * @since 6.2.0: Added the `[count] argument.
     * @return list of popped elements, or null when key does not exist.
     */
    public suspend fun rpop(key: String, count: Int): List<String>?

    /**
     * ### ` RPUSH key element [element ...] `
     *
     * Insert all the specified values at the tail of the list stored at key.
     * If key does not exist, it is created as empty list before performing the push operation.
     * When key holds a value that is not a list, an error is returned.
     *
     * [Doc](https://redis.io/commands/rpush)
     * @since 1.0.0
     *
     *  2.4.0: Accepts multiple [elements] arguments.
     *
     * @return the length of the list after the push operation
     */
    public suspend fun rpush(key: String, element: String, vararg elements: String): Long

    /**
     * ### ` RPUSHX key element [element ...] `
     *
     * Inserts specified values at the tail of the list stored at key,
     * only if key already exists and holds a list.
     * In contrary to RPUSH, no operation will be performed when key does not yet exist.
     *
     * [Doc](https://redis.io/commands/rpushx)
     * @since 2.2.0
     *
     * 4.0.0: Accepts multiple [elements] arguments.
     * @return the length of the list after the push operation.
     */
    public suspend fun rpushx(key: String, element: String, vararg elements: String): Long

}

internal interface ListCommandExecutor : ListCommands, CommandExecutor, BaseListCommands, BlockingListCommands {
    override suspend fun lindex(key: String, index: Int): String? =
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
        whereFrom: LeftRightOption,
        whereTo: LeftRightOption
    ): String? =
        execute(_lmove(source, destination, whereFrom, whereTo))

    override suspend fun lmpop(
        numkeys: Long,
        key: String,
        vararg keys: String,
        leftRight: LeftRightOption,
        count: Long?
    ): LMPOPResult? = execute(_lmpop(numkeys, key, *keys, leftRight = leftRight, count = count))

    override suspend fun lpop(key: String): String? = execute(_lpop(key))

    override suspend fun lpop(key: String, count: Long): List<String>? =
        execute(_lpop(key, count)).responseTo(throwEx = false)

    override suspend fun lpush(key: String, element: String, vararg elements: String): Long =
        execute(_lpush(key, element, elements))

    override suspend fun lpushx(key: String, element: String, vararg elements: String): Long =
        execute(_lpushx(key, element, *elements))

    override suspend fun lrange(key: String, start: Int, stop: Int): List<String> =
        execute(_lrange(key, start, stop)).responseTo("lrange")

    override suspend fun lrem(key: String, count: Int, element: String): Long =
        execute(_lrem(key, count, element))

    override suspend fun lset(key: String, index: Int, element: String): String =
        execute(_lset(key, index, element))

    override suspend fun ltrim(key: String, start: Int, stop: Int): String =
        execute(_ltrim(key, start, stop))

    override suspend fun rpop(key: String): String? =
        execute(_rpop(key))

    override suspend fun rpop(key: String, count: Int): List<String>? =
        execute(_rpop(key, count)).responseTo(throwEx = false)

    override suspend fun rpush(key: String, element: String, vararg elements: String): Long =
        execute(_rpush(key, element, *elements))

    override suspend fun rpushx(key: String, element: String, vararg elements: String): Long =
        execute(_rpushx(key, element, *elements))

    override suspend fun blpop(key: String, vararg keys: String, timeout: Duration): Pair<String, String>? =
        execute(_blpop(key, *keys, timeout = timeout))

    override suspend fun brpop(key: String, vararg keys: String, timeout: Duration): Pair<String, String>? =
        execute(_brpop(key, *keys, timeout = timeout))

    override suspend fun blmove(
        source: String,
        destination: String,
        whereFrom: LeftRightOption,
        whereTo: LeftRightOption,
        timeout: Duration
    ): String? =
        execute(_blmove(source, destination, whereFrom, whereTo, timeout))

    override suspend fun blmpop(
        timeout: Duration,
        numKeys: Int,
        key: String,
        vararg keys: String,
        from: LeftRightOption,
        count: Int?
    ): LMPOPResult? =
        execute(_blmpop(timeout, numKeys, key, *keys, from = from, count = count))
}