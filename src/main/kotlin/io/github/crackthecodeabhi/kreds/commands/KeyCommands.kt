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
import io.github.crackthecodeabhi.kreds.commands.KeyCommand.*
import io.github.crackthecodeabhi.kreds.protocol.*

internal enum class KeyCommand(override val subCommand: Command? = null) : Command {
    DEL, COPY, DUMP, EXISTS, EXPIRE, EXPIREAT, EXPIRETIME,
    KEYS, MOVE, PERSIST, PEXPIRE, PEXPIREAT, PEXPIRETIME,
    PTTL, RANDOMKEY, RENAME, RENAMENX, TOUCH, TTL, TYPE, UNLINK,
    SCAN;

    override val string = name
}

internal interface BaseKeyCommands {
    fun _del(vararg keys: String) = CommandExecution(DEL, IntegerCommandProcessor, *keys.toArguments())
    fun _copy(source: String, destination: String, destinationDb: String?, replace: Boolean?): CommandExecution<Long> {
        val args =
            createArguments(
                source,
                destination,
                destinationDb?.let { KeyValueArgument("DB", it) },
                replace?.let { KeyOnlyArgument("replace") })
        return CommandExecution(COPY, IntegerCommandProcessor, *args)
    }

    fun _dump(key: String) = CommandExecution(DUMP, BulkStringCommandProcessor, key.toArgument())
    fun _exists(vararg keys: String) = CommandExecution(EXISTS, IntegerCommandProcessor, *keys.toArguments())
    fun _expire(key: String, seconds: ULong, expireOption: ExpireOption?) =
        CommandExecution(EXPIRE, IntegerCommandProcessor, *createArguments(key, seconds, expireOption))

    fun _expireAt(key: String, timestamp: ULong, expireOption: ExpireOption?) =
        CommandExecution(EXPIREAT, IntegerCommandProcessor, *createArguments(key, timestamp, expireOption))

    fun _expireTime(key: String) = CommandExecution(EXPIRETIME, IntegerCommandProcessor, key.toArgument())

    fun _keys(pattern: String) = CommandExecution(KEYS, ArrayCommandProcessor, pattern.toArgument())

    fun _move(key: String, db: String) =
        CommandExecution(MOVE, IntegerCommandProcessor, key.toArgument(), db.toArgument())

    fun _persist(key: String) = CommandExecution(PERSIST, IntegerCommandProcessor, key.toArgument())

    fun _pexpire(key: String, milliseconds: ULong, expireOption: PExpireOption?) =
        CommandExecution(PEXPIRE, IntegerCommandProcessor, *createArguments(key, milliseconds, expireOption))

    fun _pexpireat(key: String, millisecondsTimestamp: ULong, expireOption: PExpireOption?) =
        CommandExecution(PEXPIREAT, IntegerCommandProcessor, *createArguments(key, millisecondsTimestamp, expireOption))

    fun _pexpiretime(key: String) = CommandExecution(PEXPIRETIME, IntegerCommandProcessor, key.toArgument())

    fun _pttl(key: String) = CommandExecution(PTTL, IntegerCommandProcessor, key.toArgument())

    fun _randomKey() = CommandExecution(RANDOMKEY, BulkStringCommandProcessor)

    fun _rename(key: String, newKey: String) =
        CommandExecution(RENAME, SimpleStringCommandProcessor, *createArguments(key, newKey))

    fun _renamenx(key: String, newKey: String) =
        CommandExecution(RENAMENX, IntegerCommandProcessor, *createArguments(key, newKey))

    fun _touch(vararg keys: String) = CommandExecution(TOUCH, IntegerCommandProcessor, *createArguments(keys))

    fun _ttl(key: String) = CommandExecution(TTL, IntegerCommandProcessor, key.toArgument())

    fun _type(key: String) = CommandExecution(TYPE, SimpleStringCommandProcessor, key.toArgument())

    fun _unlink(vararg keys: String) = CommandExecution(UNLINK, IntegerCommandProcessor, *createArguments(keys))

    fun _scan(cursor: Long, matchPattern: String?, count: Long?, type: String?) =
        CommandExecution(SCAN, ScanResultProcessor, *createArguments(
            cursor,
            matchPattern?.let { KeyValueArgument("MATCH", it) },
            count?.let { KeyValueArgument("COUNT", it.toString(10)) },
            type?.let { KeyValueArgument("TYPE", it) }
        ))
}

public interface KeyCommands {
    /**
     * ### `DEL key [key ...]`
     * Removes the specified keys. A key is ignored if it does not exist.
     * [Doc](https://redis.io/commands/del)
     *
     * @return The number of keys that were removed.
     * @since  1.0.0.
     */
    public suspend fun del(vararg keys: String): Long

    /**
     * ### `COPY source destination [DB destination-db] [REPLACE]`
     * This command copies the value stored at the source key to the destination key.
     * By default, the destination key is created in the logical database used by the connection.
     * The DB option allows specifying an alternative logical database index for the destination key.
     * The command returns an error when the destination key already exists.
     * The REPLACE option removes the destination key before copying the value to it.
     *
     * [Doc](https://redis.io/commands/copy)
     * @return 1 if source was copied, else 0
     * @since 6.2.0
     */
    public suspend fun copy(
        source: String,
        destination: String,
        destinationDb: String? = null,
        replace: Boolean? = null
    ): Long

    /**
     * ### `DUMP key`
     * Serialize the value stored at key in a Redis-specific format and return it to the user.
     *
     * [Doc](https://redis.io/commands/dump)
     * @since 2.6.0
     * @return If key does not exist a null is returned else serialized value
     */
    public suspend fun dump(key: String): String?

    /**
     * ### ` EXISTS key [key ...]`
     * Returns if key exists.
     *
     * [Doc](https://redis.io/commands/exists)
     * @since 1.0.0
     * @return 1 if the key exists else 0
     */
    public suspend fun exists(vararg keys: String): Long

    /**
     * ### `EXPIRE key seconds [NX|XX|GT|LT]`
     * Set a timeout on key. After the timeout has expired, the key will automatically be deleted.
     * A key with an associated timeout is often said to be volatile in Redis terminology.
     *
     * [Doc](https://redis.io/commands/expire)
     * @since 1.0.0, >= 7.0: Added options: NX, XX, GT and LT.
     * @return 1 if the timeout was set else 0,e.g. key doesn't exist, or operation skipped due to the provided arguments.
     */
    public suspend fun expire(key: String, seconds: ULong, expireOption: ExpireOption? = null): Long

    /**
     * ### `EXPIREAT key timestamp [NX|XX|GT|LT]`
     *
     * EXPIREAT has the same effect and semantic as EXPIRE, but instead of specifying the number of seconds representing
     * the TTL (time to live), it takes an absolute Unix timestamp (seconds since January 1, 1970).
     *
     * A timestamp in the past will delete the key immediately.
     *
     * [Doc](https://redis.io/commands/expireat)
     * @since 1.2.0
     * @return 1 if the timeout was set else 0. e.g. key doesn't exist, or operation skipped due to the provided arguments.
     */
    public suspend fun expireAt(key: String, timestamp: ULong, expireOption: ExpireOption? = null): Long

    /**
     * ###  EXPIRETIME key
     *
     * Returns the absolute Unix timestamp (since January 1, 1970) in seconds at which the given key will expire.
     * [Doc](https://redis.io/commands/expiretime)
     * @since 7.0.0
     * @return Expiration Unix timestamp in seconds, or a negative value in order to signal an error
     * The command returns -1 if the key exists but has no associated expiration time.
     * The command returns -2 if the key does not exist.
     */
    public suspend fun expireTime(key: String): Long

    /**
     * ###  KEYS pattern
     *
     * Returns all keys matching pattern.
     *
     * [Doc](https://redis.io/commands/keys)
     * @since 1.0.0
     * @return list of keys matching pattern.
     */
    public suspend fun keys(pattern: String): List<String>

    /**
     * ###  MOVE key db
     *
     * Move key from the currently selected database (see SELECT) to the specified destination database.
     * When key already exists in the destination database, or it does not exist in the source database, it does nothing.
     * It is possible to use MOVE as a locking primitive because of this.
     *
     * [Doc](https://redis.io/commands/move)
     * @since 1.0.0
     * @return 1 if key was moved else 0
     */
    public suspend fun move(key: String, db: String): Long

    /**
     * ###  PERSIST key
     *
     * Remove the existing timeout on key, turning the key from volatile (a key with an expire set)
     * to persistent (a key that will never expire as no timeout is associated).
     *
     * [Doc](https://redis.io/commands/persist)
     * @since 2.2.0
     * @return 1 if the timeout was removed, 0 if key does not exist or does not have an associated timeout.
     */
    public suspend fun persist(key: String): Long

    /**
     * ###  `PEXPIRE key milliseconds [NX|XX|GT|LT]`
     *
     * This command works exactly like EXPIRE but the time to live of the key is specified in milliseconds instead of seconds.
     *
     * [Doc](https://redis.io/commands/pexpire)
     * @since 2.6.0
     * @return 1 if the timeout was set.
     * 0 if the timeout was not set. e.g. key doesn't exist, or operation skipped due to the provided arguments.
     */
    public suspend fun pexpire(key: String, milliseconds: ULong, expireOption: PExpireOption? = null): Long

    /**
     * ### `PEXPIREAT key milliseconds-timestamp [NX|XX|GT|LT] `
     *
     * PEXPIREAT has the same effect and semantic as EXPIREAT,
     * but the Unix time at which the key will expire is specified in milliseconds instead of seconds.
     *
     * [Doc](https://redis.io/commands/pexpireat)
     * @since 2.6.0
     * @return 1 if the timeout was set. 0 if the timeout was not set. e.g. key doesn't exist, or operation skipped due to the provided arguments.
     */
    public suspend fun pexpireat(
        key: String,
        millisecondsTimestamp: ULong,
        expireOption: PExpireOption? = null /* = org.kreds.ExpireOption? */
    ): Long

    /**
     * ###  PEXPIRETIME key
     *
     * PEXPIRETIME has the same semantic as EXPIRETIME, but returns the absolute Unix expiration timestamp in milliseconds instead of seconds.
     *
     * [Doc](https://redis.io/commands/pexpiretime)
     * @since 7.0.0
     * @return Expiration Unix timestamp in milliseconds, or a negative value in order to signal an error (see the description below). * The command returns -1 if the key exists but has no associated expiration time. * The command returns -2 if the key does not exist.
     */
    public suspend fun pexpiretime(key: String): Long

    /**
     * ###  PTTL key
     *
     * Like TTL this command returns the remaining time to live of a key that has an expire set, with the sole difference that TTL returns the amount of remaining time in seconds while PTTL returns it in milliseconds.
     * In Redis 2.6 or older the command returns -1 if the key does not exist or if the key exist but has no associated expire.
     * Starting with Redis 2.8 the return value in case of error changed:
     * The command returns -2 if the key does not exist.
     * The command returns -1 if the key exists but has no associated expire.
     *
     * [Doc](https://redis.io/commands/pttl)
     * @since 2.6.0
     * @return TTL in milliseconds, or a negative value in order to signal an error (see the description above).
     */
    public suspend fun pttl(key: String): Long


    /**
     * ### RANDOMKEY
     *
     * Return a random key from the currently selected database.
     *
     * [Doc](https://redis.io/commands/randomkey)
     * @since 1.0.0
     * @return the random key, or null when the database is empty.
     */
    public suspend fun randomKey(): String?

    /**
     * ##  RENAME key newkey
     *
     * Renames key to newkey.
     *
     * [Doc](https://redis.io/commands/rename)
     * @since 1.0.0
     * @return OK
     */
    public suspend fun rename(key: String, newKey: String): String

    /**
     * ###  RENAMENX key newkey
     *
     * Renames key to newkey if newkey does not yet exist. It returns an error when key does not exist.
     *
     * [Doc](https://redis.io/commands/renamenx)
     * @since 1.0.0
     * @return 1 if key was renamed to newkey.
     * 0 if newkey already exists.
     */
    public suspend fun renamenx(key: String, newKey: String): Long

    /**
     * ### ` TOUCH key [key ...] `
     *
     * Alters the last access time of a key(s). A key is ignored if it does not exist.
     *
     * [Doc](https://redis.io/commands/touch)
     * @since 3.2.1
     * @return The number of keys that were touched.
     */
    public suspend fun touch(vararg keys: String): Long

    /**
     * ### TTL key
     *
     * Returns the remaining time to live of a key that has a timeout.
     *
     * [Doc](https://redis.io/commands/ttl)
     * @since 1.0.0
     * @return TTL in seconds, or a negative value in order to signal an error (see the description above).
     */
    public suspend fun ttl(key: String): Long

    /**
     * ###  TYPE key
     *
     * Returns the string representation of the type of the value stored at key.
     * The different types that can be returned are: string, list, set, zset, hash and stream.
     *
     * [Doc](https://redis.io/commands/type)
     * @since 1.0.0
     * @return type of key, or none when key does not exist.
     */
    public suspend fun type(key: String): String

    /**
     * ### ` UNLINK key [key ...] `
     *
     * This command is very similar to DEL: it removes the specified keys. Just like DEL a key is ignored if it does not exist.
     *
     * [Doc](https://redis.io/commands/unlink)
     * @since 4.0.0
     * @return The number of keys that were unlinked.
     */
    public suspend fun unlink(vararg keys: String): Long

    /**
     * ### `SCAN cursor [MATCH pattern] [COUNT count] [TYPE type]`
     *
     * [Doc](https://redis.io/commands/scan)
     * @since 2.8.0
     * @return [ScanResult]
     */
    public suspend fun scan(
        cursor: Long,
        matchPattern: String? = null,
        count: Long? = null,
        type: String? = null
    ): IScanResult<String>
}

internal interface KeyCommandExecutor : CommandExecutor, KeyCommands, BaseKeyCommands {

    override suspend fun del(vararg keys: String): Long = execute(_del(*keys))

    override suspend fun copy(source: String, destination: String, destinationDb: String?, replace: Boolean?): Long =
        execute(_copy(source, destination, destinationDb, replace))

    override suspend fun dump(key: String): String? = execute(_dump(key))

    override suspend fun exists(vararg keys: String): Long = execute(_exists(*keys))

    override suspend fun expire(key: String, seconds: ULong, expireOption: ExpireOption?): Long =
        execute(_expire(key, seconds, expireOption))

    override suspend fun expireAt(key: String, timestamp: ULong, expireOption: ExpireOption?): Long =
        execute(_expireAt(key, timestamp, expireOption))

    override suspend fun expireTime(key: String): Long = execute(_expireTime(key))

    override suspend fun keys(pattern: String): List<String> =
        execute(_keys(pattern)).responseTo("keys")

    override suspend fun move(key: String, db: String): Long = execute(_move(key, db))

    override suspend fun persist(key: String): Long = execute(_persist(key))

    override suspend fun pexpire(key: String, milliseconds: ULong, expireOption: PExpireOption?): Long =
        execute(_pexpire(key, milliseconds, expireOption))

    override suspend fun pexpireat(key: String, millisecondsTimestamp: ULong, expireOption: PExpireOption?): Long =
        execute(_pexpireat(key, millisecondsTimestamp, expireOption))

    override suspend fun pexpiretime(key: String): Long = execute(_pexpiretime(key))

    override suspend fun pttl(key: String): Long = execute(_pttl(key))

    override suspend fun randomKey(): String? = execute(_randomKey())

    override suspend fun rename(key: String, newKey: String): String = execute(_rename(key, newKey))

    override suspend fun renamenx(key: String, newKey: String): Long = execute(_renamenx(key, newKey))

    override suspend fun touch(vararg keys: String): Long = execute(_touch(*keys))

    override suspend fun ttl(key: String): Long = execute(_ttl(key))

    override suspend fun type(key: String): String = execute(_type(key))

    override suspend fun unlink(vararg keys: String): Long = execute(_unlink(*keys))

    override suspend fun scan(cursor: Long, matchPattern: String?, count: Long?, type: String?): IScanResult<String> =
        execute(_scan(cursor, matchPattern, count, type))
}