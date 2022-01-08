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

import io.github.crackthecodeabhi.kreds.FieldValue
import io.github.crackthecodeabhi.kreds.StringFieldValue
import io.github.crackthecodeabhi.kreds.args.*
import io.github.crackthecodeabhi.kreds.commands.HashCommand.*
import io.github.crackthecodeabhi.kreds.protocol.*
import java.math.BigDecimal

internal enum class HashCommand(override val subCommand: Command? = null) : Command {
    HDEL, HEXISTS, HGET, HGETALL, HINCRBY, HINCRBYFLOAT, HKEYS, HLEN, HMGET, HMSET, HRANDFIELD, HSCAN,
    HSET, HSETNX, HSTRLEN, HVALS;

    override val string: String = name
}

internal interface BaseHashCommands {
    fun _hdel(key: String, field: String, vararg moreFields: String) =
        CommandExecution(HDEL, IntegerCommandProcessor, *createArguments(key, field, *moreFields))

    fun _hexists(key: String, field: String) =
        CommandExecution(HEXISTS, IntegerCommandProcessor, key.toArgument(), field.toArgument())

    fun _hget(key: String, field: String) =
        CommandExecution(HGET, BulkStringCommandProcessor, key.toArgument(), field.toArgument())

    fun _hgetAll(key: String) =
        CommandExecution(HGETALL, ArrayCommandProcessor, key.toArgument())

    fun _hincrBy(key: String, field: String, increment: Long) =
        CommandExecution(HINCRBY, IntegerCommandProcessor, *createArguments(key, field, increment))

    fun _hincrByFloat(key: String, field: String, increment: BigDecimal) =
        CommandExecution(HINCRBYFLOAT, BulkStringCommandProcessor, *createArguments(key, field, increment))


    fun _hkeys(key: String) = CommandExecution(HKEYS, ArrayCommandProcessor, key.toArgument())

    fun _hlen(key: String) = CommandExecution(HLEN, IntegerCommandProcessor, key.toArgument())

    fun _hmget(key: String, field: String, vararg fields: String) =
        CommandExecution(HMGET, ArrayCommandProcessor, *createArguments(key, field, *fields))

    fun _hrandfield(key: String) = CommandExecution(HRANDFIELD, BulkStringCommandProcessor, key.toArgument())

    fun _hrandfield(key: String, count: Int, withValues: Boolean? = null) =
        CommandExecution(HRANDFIELD, ArrayCommandProcessor, *createArguments(
            key, count,
            withValues?.let { KeyOnlyArgument("WITHVALUES") }
        ))

    fun _hset(key: String, fieldValuePair: Pair<String, String>, vararg fieldValuePairs: Pair<String, String>) =
        CommandExecution(
            HSET, IntegerCommandProcessor, *createArguments(
                key, fieldValuePair, *fieldValuePairs
            )
        )

    fun _hsetnx(key: String, field: String, value: String) =
        CommandExecution(HSETNX, IntegerCommandProcessor, *createArguments(key, field, value))

    fun _hstrlen(key: String, field: String) =
        CommandExecution(HSTRLEN, IntegerCommandProcessor, key.toArgument(), field.toArgument())

    fun _hvals(key: String) = CommandExecution(HVALS, ArrayCommandProcessor, key.toArgument())

    fun _hscan(
        key: String,
        cursor: Long,
        matchPattern: String?,
        count: Long?
    ): CommandExecution<IScanResult<FieldValue<String, String>>> {
        val args = if (matchPattern != null && count != null)
            createArguments(key, cursor, "MATCH", matchPattern, "COUNT", count)
        else if (matchPattern != null)
            createArguments(key, cursor, "MATCH", matchPattern)
        else if (count != null)
            createArguments(key, cursor, "COUNT", count)
        else
            createArguments(key, cursor)
        return CommandExecution(HSCAN, HScanResultProcessor, *args)
    }
}

//TODO: HSCAN

public interface HashCommands {

    /**
     * ###  `HDEL key field [field ...]`
     *
     * Removes the specified fields from the hash stored at key.
     * Specified fields that do not exist within this hash are ignored.
     * If key does not exist, it is treated as an empty hash and this command returns 0.
     *
     * [Doc](https://redis.io/commands/hdel)
     * @since 2.0.0
     * @return the number of fields that were removed from the hash, not including specified but non existing fields.
     */
    public suspend fun hdel(key: String, field: String, vararg moreFields: String): Long

    /**
     * ###  HEXISTS key field
     *
     * Returns if field is an existing field in the hash stored at key
     *
     * [Doc](https://redis.io/commands/hexists)
     * @since 2.0.0
     * @return 1 if the hash contains field.
     * 0 if the hash does not contain field, or key does not exist.
     */
    public suspend fun hexists(key: String, field: String): Long

    /**
     * ###  HGET key field
     *
     * Returns the value associated with field in the hash stored at key.
     *
     * [Doc](https://redis.io/commands/hget)
     * @since 2.0.0
     * @return the string value associated with field, or null when field is not present in the hash or key does not exist.
     */
    public suspend fun hget(key: String, field: String): String?

    /**
     * ###  HGETALL key
     *
     *  Returns all fields and values of the hash stored at key.
     *  In the returned value, every field name is followed by its value,
     *  so the length of the reply is twice the size of the hash.
     *
     *  [Doc](https://redis.io/commands/hgetall)
     *  @since 2.0.0
     *  @return list of fields and their values stored in the hash, or an empty list when key does not exist.
     */
    public suspend fun hgetAll(key: String): List<String>

    /***
     * ###  HINCRBY key field increment
     *
     * Increments the number stored at field in the hash stored at key by increment.
     * If key does not exist, a new key holding a hash is created.
     * If field does not exist the value is set to 0 before the operation is performed.
     *
     * [Doc](https://redis.io/commands/hincrby)
     * @since 2.0.0
     * @return the value at field after the increment operation.
     */
    public suspend fun hincrBy(key: String, field: String, increment: Long): Long

    /**
     * ### HINCRBYFLOAT key field increment
     *
     * Increment the specified field of a hash stored at key, and representing a floating point number, by the specified increment.
     *
     * [Doc](https://redis.io/commands/hincrbyfloat)
     * @since 2.6.0
     * @return the value of field after the increment.
     */
    public suspend fun hincrByFloat(key: String, field: String, increment: BigDecimal): String

    /**
     * ###  HKEYS key
     *
     * Returns all field names in the hash stored at key.
     *
     * [Doc](https://redis.io/commands/hkeys)
     * @since 2.0.0
     * @return list of fields in the hash, or an empty list when key does not exist.
     */
    public suspend fun hkeys(key: String): List<String>

    /**
     * ###  HLEN key
     *
     * Returns the number of fields contained in the hash stored at key.
     *
     * [Doc](https://redis.io/commands/hlen)
     * @since 2.0.0
     * @return number of fields in the hash, or 0 when key does not exist.
     */
    public suspend fun hlen(key: String): Long

    /**
     * ###  `HMGET key field [field ...]`
     *
     * Returns the values associated with the specified fields in the hash stored at key.
     *
     * [Doc](https://redis.io/commands/hmget)
     * @since 2.0.0
     * @return list of values associated with the given fields, in the same order as they are requested.
     */
    public suspend fun hmget(key: String, field: String, vararg fields: String): List<String?>

    /**
     * ### `HRANDFIELD key`
     *
     *  When called with just the key argument, return a random field from the hash value stored at key.
     *
     *  [Doc](https://redis.io/commands/hrandfield)
     *  @since 6.2.0
     *  @return the command returns a String Reply with the randomly selected field, or null when key does not exist.
     */
    public suspend fun hrandfield(key: String): String?


    /**
     * ### `HRANDFIELD key count [WITHVALUES]`
     *
     *  If the provided count argument is positive, return an array of distinct fields. The array's length is either count or the hash's number of fields (HLEN), whichever is lower.
     *  If called with a negative count, the behavior changes and the command is allowed to return the same field multiple times. In this case, the number of returned fields is the absolute value of the specified count.
     *  The optional WITHVALUES modifier changes the reply so it includes the respective values of the randomly selected hash fields.
     *
     *  [Doc](https://redis.io/commands/hrandfield)
     *  @since 6.2.0
     *  @return when the additional count argument is passed, the command returns an array of fields,
     *  or an empty array when key does not exist.
     *  If the WITHVALUES modifier is used, the reply is a list fields and their values from the hash.
     */
    public suspend fun hrandfield(key: String, count: Int, withValues: Boolean? = null): List<String>


    /**
     * ### ` HSET key field value [field value ...] `
     *
     * Sets field in the hash stored at key to value. If key does not exist, a new key holding a hash is created. If field already exists in the hash, it is overwritten.
     *
     * [Doc](https://redis.io/commands/hset)
     *
     * Example Usage:
     * ```
     * hset(key,field1 toFV value1, field2 toFV value2, .....)
     * ```
     * @since 2.0.0
     * @return The number of fields that were added.
     *
     */
    public suspend fun hset(
        key: String,
        fieldValuePair: Pair<String, String>,
        vararg fieldValuePairs: Pair<String, String>
    ): Long

    /**
     * ###  HSETNX key field value
     *
     * Sets field in the hash stored at key to value, only if field does not yet exist. If key does not exist, a new key holding a hash is created. If field already exists, this operation has no effect.
     *
     * [Doc](https://redis.io/commands/hsetnx)
     * @since 2.0.0
     * @return 1 if field is a new field in the hash and value was set.
     * 0 if field already exists in the hash and no operation was performed.
     */
    public suspend fun hsetnx(key: String, field: String, value: String): Long

    /**
     * ###  HSTRLEN key field
     *
     * Returns the string length of the value associated with field in the hash stored at key. If the key or the field do not exist, 0 is returned.
     *
     * [Doc](https://redis.io/commands/hstrlen)
     * @since 3.2.0
     * @return the string length of the value associated with field, or zero when field is not present in the hash or key does not exist at all.
     */
    public suspend fun hstrlen(key: String, field: String): Long

    /**
     * ### HVALS key
     *
     * Returns all values in the hash stored at key.
     *
     * [Doc](https://redis.io/commands/hvals)
     * @since 2.0.0
     * @return list of values in the hash, or an empty list when key does not exist.
     */
    public suspend fun hvals(key: String): List<String>

    /**
     * ### ` HSCAN key cursor [MATCH pattern] [COUNT count] `
     *
     * [Doc](https://redis.io/commands/hscan)
     * @since 2.8.0
     * @return [HScanResult]
     * @see [IScanResult]
     */
    public suspend fun hscan(
        key: String,
        cursor: Long,
        matchPattern: String? = null,
        count: Long? = null
    ): IScanResult<StringFieldValue>
}

internal interface HashCommandsExecutor : HashCommands, BaseHashCommands, CommandExecutor {
    override suspend fun hdel(key: String, field: String, vararg moreFields: String): Long =
        execute(_hdel(key, field, *moreFields))

    override suspend fun hexists(key: String, field: String): Long =
        execute(_hexists(key, field))

    override suspend fun hget(key: String, field: String): String? =
        execute(_hget(key, field))

    override suspend fun hgetAll(key: String): List<String> =
        execute(_hgetAll(key)).responseTo("hgetAll")

    override suspend fun hincrBy(key: String, field: String, increment: Long): Long =
        execute(_hincrBy(key, field, increment))

    override suspend fun hincrByFloat(key: String, field: String, increment: BigDecimal): String =
        execute(_hincrByFloat(key, field, increment)).responseTo("hincrByFloat")

    override suspend fun hkeys(key: String): List<String> =
        execute(_hkeys(key)).responseTo("hkeys")

    override suspend fun hlen(key: String): Long =
        execute(_hlen(key))

    override suspend fun hmget(key: String, field: String, vararg fields: String): List<String?> =
        execute(_hmget(key, field, *fields)).responseTo("hmget")

    override suspend fun hrandfield(key: String): String? =
        execute(_hrandfield(key))

    override suspend fun hrandfield(key: String, count: Int, withValues: Boolean?): List<String> =
        execute((_hrandfield(key, count, withValues))).responseTo("hrandfield")

    override suspend fun hset(
        key: String,
        fieldValuePair: Pair<String, String>,
        vararg fieldValuePairs: Pair<String, String>
    ): Long = execute(_hset(key, fieldValuePair, *fieldValuePairs))

    override suspend fun hsetnx(key: String, field: String, value: String): Long =
        execute(_hsetnx(key, field, value))

    override suspend fun hstrlen(key: String, field: String): Long =
        execute(_hstrlen(key, field))

    override suspend fun hvals(key: String): List<String> =
        execute(_hvals(key)).responseTo("hvals")

    override suspend fun hscan(
        key: String,
        cursor: Long,
        matchPattern: String?,
        count: Long?
    ): IScanResult<StringFieldValue> =
        execute(_hscan(key, cursor, matchPattern, count))
}