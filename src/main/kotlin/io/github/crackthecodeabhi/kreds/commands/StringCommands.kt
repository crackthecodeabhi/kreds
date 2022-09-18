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
import io.github.crackthecodeabhi.kreds.commands.StringCommand.*
import io.github.crackthecodeabhi.kreds.protocol.*
import java.math.BigDecimal

internal interface BaseStringCommands {
    fun _append(key: String, value: String) =
        CommandExecution(APPEND, IntegerCommandProcessor, key.toArgument(), value.toArgument())

    fun _get(key: String) = CommandExecution(GET, BulkStringCommandProcessor, key.toArgument())

    fun _decr(key: String) = CommandExecution(DECR, IntegerCommandProcessor, key.toArgument())

    fun _decrBy(key: String, decrement: Long) =
        CommandExecution(DECRBY, IntegerCommandProcessor, key.toArgument(), decrement.toArgument())

    fun _getDel(key: String) = CommandExecution(GETDEL, BulkStringCommandProcessor, key.toArgument())

    fun _getRange(key: String, start: Int, end: Int) =
        CommandExecution(GETRANGE, BulkStringCommandProcessor, *createArguments(key, start, end))

    fun _getSet(key: String, value: String) =
        CommandExecution(GETSET, BulkStringCommandProcessor, key.toArgument(), value.toArgument())

    fun _incr(key: String) = CommandExecution(INCR, IntegerCommandProcessor, key.toArgument())

    fun _incrBy(key: String, increment: Long) =
        CommandExecution(INCRBY, IntegerCommandProcessor, key.toArgument(), increment.toArgument())

    fun _incrByFloat(key: String, increment: BigDecimal) =
        CommandExecution(INCRBYFLOAT, BulkStringCommandProcessor, key.toArgument(), increment.toArgument())

    fun _mget(vararg keys: String) = CommandExecution(MGET, ArrayCommandProcessor, *createArguments(*keys))

    fun _mset(vararg keyValues: Pair<String, String>) =
        CommandExecution(MSET, SimpleStringCommandProcessor, *createArguments(*keyValues))

    fun _set(key: String, value: String, setOption: SetOption?) =
        CommandExecution(SET, SimpleAndBulkStringCommandProcessor, *createArguments(
            key, value,
            setOption?.exSeconds?.let { Pair("EX", it.toString(10)) },
            setOption?.pxMilliseconds?.let { Pair("PX", it.toString(10)) },
            setOption?.exatTimestamp?.let { Pair("EXAT", it.toString(10)) },
            setOption?.pxatMillisecondTimestamp?.let { Pair("PXAT", it.toString(10)) },
            setOption?.keepTTL?.let { KeyOnlyArgument("KEEPTTL") },
            setOption?.nx?.let { KeyOnlyArgument("NX") },
            setOption?.xx?.let { KeyOnlyArgument("XX") },
            setOption?.get?.let { KeyOnlyArgument("GET") }
        ))

    fun _getEx(key: String, getExOption: GetExOption? = null) =
        CommandExecution(GETEX, BulkStringCommandProcessor, *createArguments(
            key,
            getExOption?.exSeconds?.let { Pair("EX", it.toString(10)) },
            getExOption?.pxMilliseconds?.let { Pair("PX", it.toString(10)) },
            getExOption?.exatTimestamp?.let { Pair("EXAT", it.toString(10)) },
            getExOption?.pxatMillisecondTimestamp?.let { Pair("PXAT", it.toString(10)) },
            getExOption?.persist?.let { KeyOnlyArgument("PERSIST") }
        ))

    fun _strlen(key: String) = CommandExecution(STRLEN, IntegerCommandProcessor, key.toArgument())
}


public interface StringCommands {
    /**
     * ###  APPEND key value
     *
     * If key already exists and is a string, this command appends the value at the end of the string. If key does not exist it is created and set as an empty string, so APPEND will be similar to SET in this special case.
     *
     * [Doc](https://redis.io/commands/append)
     *
     * @since 2.0.0
     * @return the length of the string after the append operation.
     */
    public suspend fun append(key: String, value: String): Long

    /**
     * ###   DECR key
     *
     * Decrements the number stored at key by one. If the key does not exist, it is set to 0 before performing the operation. An error is returned if the key contains a value of the wrong type or contains a string that can not be represented as integer. This operation is limited to 64 bit signed integers.
     *
     * [Doc](https://redis.io/commands/decr)
     * @since 1.0.0
     * @return the value of key after the decrement
     */
    public suspend fun decr(key: String): Long

    /**
     * ###  DECRBY key decrement
     *
     * Decrements the number stored at key by decrement. If the key does not exist, it is set to 0 before performing the operation. An error is returned if the key contains a value of the wrong type or contains a string that can not be represented as integer. This operation is limited to 64 bit signed integers.
     *
     * [Doc](https://redis.io/commands/decrby)
     * @since 1.0.0
     * @return the value of key after the decrement
     */
    public suspend fun decrBy(key: String, decrement: Long): Long

    /**
     * ###  GET key
     *
     * Get the value of key. If the key does not exist the special value nil is returned. An error is returned if the value stored at key is not a string, because GET only handles string values.
     *
     * [Doc](https://redis.io/commands/get)
     * @since 1.0.0
     * @return the value of key, or null when key does not exist.
     */
    public suspend fun get(key: String): String?

    /**
     * ###  GETDEL key
     *
     * Get the value of key and delete the key. This command is similar to GET, except for the fact that it also deletes the key on success (if and only if the key's value type is a string).
     *
     * [Doc](https://redis.io/commands/getdel)
     * @since 6.2.0
     * @return the value of key, null when key does not exist, or an error if the key's value type isn't a string.
     */
    public suspend fun getDel(key: String): String?

    /**
     * ###  GETEX key [EX seconds|PX milliseconds|EXAT timestamp|PXAT milliseconds-timestamp|PERSIST]
     *
     * Get the value of key and optionally set its expiration. GETEX is similar to GET, but is a write command with additional options
     *
     * [Doc](https://redis.io/commands/getex)
     * @since 6.2.0
     * @return the value of key, or nil when key does not exist.
     */
    public suspend fun getEx(key: String, getExOption: GetExOption? = null): String?

    /**
     * ###  GETRANGE key start end
     *
     * Returns the substring of the string value stored at key, determined by the offsets start and end (both are inclusive). Negative offsets can be used in order to provide an offset starting from the end of the string. So -1 means the last character, -2 the penultimate and so forth
     *
     * [Doc](https://redis.io/commands/getrange)
     * @since 2.4.0
     * @return string value or null
     */
    public suspend fun getRange(key: String, start: Int, end: Int): String?

    /**
     * ###  GETSET key value
     *
     * Atomically sets key to value and returns the old value stored at key. Returns an error when key exists but does not hold a string value. Any previous time to live associated with the key is discarded on successful SET operation.
     *
     * [Doc](https://redis.io/commands/getset)
     * @since 1.0.0
     * @return the old value stored at key, or null when key did not exist.
     */
    public suspend fun getSet(key: String, value: String): String?

    /**
     * ###  INCR key
     *
     * Increments the number stored at key by one. If the key does not exist, it is set to 0 before performing the operation. An error is returned if the key contains a value of the wrong type or contains a string that can not be represented as integer. This operation is limited to 64 bit signed integers.
     *
     * [Doc](https://redis.io/commands/incr)
     * @since 1.0.0
     * @return  the value of key after the increment
     */
    public suspend fun incr(key: String): Long

    /**
     * ###  INCRBY key increment
     *
     * Increments the number stored at key by increment. If the key does not exist, it is set to 0 before performing the operation. An error is returned if the key contains a value of the wrong type or contains a string that can not be represented as integer. This operation is limited to 64 bit signed integers.
     *
     * [Doc](https://redis.io/commands/incrby)
     * @since 1.0.0
     * @return the value of key after the increment
     */
    public suspend fun incrBy(key: String, increment: Long): Long

    /**
     * ###  INCRBYFLOAT key increment
     *
     * Increment the string representing a floating point number stored at key by the specified increment
     *
     * [Doc](https://redis.io/commands/incrbyfloat)
     * @since 2.6.0
     * @return the value of key after the increment.
     */
    public suspend fun incrByFloat(key: String, increment: BigDecimal): String?

    /**
     * ### ` MGET key [key ...]`
     *
     * Returns the values of all specified keys.
     * For every key that does not hold a string value or does not exist, the special value null is returned.
     * Because of this, the operation never fails.
     *
     * [Doc](https://redis.io/commands/mget)
     * @since 1.0.0
     * @return list of values at the specified keys.
     */
    public suspend fun mget(vararg keys: String): List<String?>

    /**
     * ### `MSET key value [key value ...]`
     *
     * Sets the given keys to their respective values
     *
     * [Doc](https://redis.io/commands/mset)
     * @since 1.0.1
     * @return OK
     */
    public suspend fun mset(vararg keyValues: Pair<String, String>): String


    /**
     * ### ` SET key value [EX seconds|PX milliseconds|EXAT timestamp|PXAT milliseconds-timestamp|KEEPTTL] [NX|XX] [GET] `
     *
     * Set key to hold the string value. If key already holds a value, it is overwritten, regardless of its type. Any previous time to live associated with the key is discarded on successful SET operation.
     *
     *
     * ###Options
     * The SET command supports a set of options that modify its behavior:
     * - EX seconds -- Set the specified expire time, in seconds.
     * - PX milliseconds -- Set the specified expire time, in milliseconds.
     * - EXAT timestamp-seconds -- Set the specified Unix time at which the key will expire, in seconds.
     * - PXAT timestamp-milliseconds -- Set the specified Unix time at which the key will expire, in milliseconds.
     * - NX -- Only set the key if it does not already exist.
     * - XX -- Only set the key if it already exist.
     * - KEEPTTL -- Retain the time to live associated with the key.
     * - GET -- Return the old string stored at key, or nil if key did not exist. An error is returned and SET aborted if the value stored at key is not a string.
     *
     * [Doc](https://redis.io/commands/set)
     * @since 1.0.0
     * @return
     * - OK if SET was executed correctly.
     * - the old string value stored at key if GET option set.
     * - Null if the key did not exist.
     * - Null if the SET operation was not performed because the user specified the NX or XX option but the condition was not met.
     */
    public suspend fun set(key: String, value: String, setOption: SetOption? = null): String?

    /**
     * ###  STRLEN key
     *
     * Returns the length of the string value stored at key. An error is returned when key holds a non-string value.
     *
     * [Doc](https://redis.io/commands/strlen)
     * @since 2.2.0
     * @return the length of the string at key, or 0 when key does not exist.
     */
    public suspend fun strlen(key: String): Long
}

internal enum class StringCommand(override val subCommand: Command? = null) : Command {
    APPEND, DECR, DECRBY, GET, GETDEL, GETRANGE, GETSET, INCR, INCRBY, INCRBYFLOAT, MGET, MSET, SET, GETEX,
    STRLEN;

    override val string = name
}

internal interface StringCommandsExecutor : CommandExecutor, StringCommands, BaseStringCommands {
    override suspend fun append(key: String, value: String): Long = execute(_append(key, value))

    override suspend fun decr(key: String): Long = execute(_decr(key))

    override suspend fun decrBy(key: String, decrement: Long): Long = execute(_decrBy(key, decrement))

    override suspend fun get(key: String): String? = execute(_get(key))

    override suspend fun getDel(key: String): String? = execute(_getDel(key))

    override suspend fun getRange(key: String, start: Int, end: Int): String? = execute(_getRange(key, start, end))

    override suspend fun getSet(key: String, value: String): String? = execute(_getSet(key, value))

    override suspend fun incr(key: String): Long = execute(_incr(key))

    override suspend fun incrBy(key: String, increment: Long): Long = execute(_incrBy(key, increment))

    override suspend fun incrByFloat(key: String, increment: BigDecimal): String? =
        execute(_incrByFloat(key, increment))

    override suspend fun mget(vararg keys: String): List<String?> = execute(_mget(*keys)).responseTo("mget")

    override suspend fun mset(vararg keyValues: Pair<String, String>): String = execute(_mset(*keyValues))

    override suspend fun set(key: String, value: String, setOption: SetOption?): String? =
        execute(_set(key, value, setOption))

    override suspend fun getEx(key: String, getExOption: GetExOption?): String? = execute(_getEx(key, getExOption))

    override suspend fun strlen(key: String): Long = execute(_strlen(key))
}