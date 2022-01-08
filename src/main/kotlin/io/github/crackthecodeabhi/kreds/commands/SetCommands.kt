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

import io.github.crackthecodeabhi.kreds.args.KeyValueArgument
import io.github.crackthecodeabhi.kreds.args.createArguments
import io.github.crackthecodeabhi.kreds.args.toArgument
import io.github.crackthecodeabhi.kreds.commands.SetCommand.*
import io.github.crackthecodeabhi.kreds.protocol.ArrayCommandProcessor
import io.github.crackthecodeabhi.kreds.protocol.BulkStringCommandProcessor
import io.github.crackthecodeabhi.kreds.protocol.CommandExecutor
import io.github.crackthecodeabhi.kreds.protocol.IntegerCommandProcessor

internal enum class SetCommand(override val subCommand: Command? = null) : Command {
    SADD, SCARD, SDIFF, SDIFFSTORE, SINTER, SINTERCARD, SINTERSTORE, SISMEMBER, SMISMEMBER,
    SMEMBERS, SMOVE, SPOP, SRANDMEMBER, SREM, SUNION, SUNIONSTORE, SSCAN;

    override val string = name
}

internal interface BaseSetCommands {

    fun _sadd(key: String, member: String, vararg members: String) =
        CommandExecution(
            SADD, IntegerCommandProcessor, *createArguments(
                key,
                member,
                *members
            )
        )

    fun _scard(key: String) = CommandExecution(SCARD, IntegerCommandProcessor, key.toArgument())

    fun _sdiff(key: String, vararg keys: String) = CommandExecution(
        SDIFF, ArrayCommandProcessor, *createArguments(
            key, *keys
        )
    )


    fun _sdiffstore(destination: String, key: String, vararg keys: String) =
        CommandExecution(SDIFFSTORE, IntegerCommandProcessor, *createArguments(destination, key, *keys))

    fun _sinter(key: String, vararg keys: String) =
        CommandExecution(SINTER, ArrayCommandProcessor, *createArguments(key, *keys))


    fun _sintercard(numkeys: Int, key: String, vararg keys: String, limit: Long?) =
        CommandExecution(SINTERCARD, IntegerCommandProcessor, *createArguments(
            numkeys, key, *keys,
            limit?.let { KeyValueArgument("LIMIT", it.toString(10)) }
        ))

    fun _sinterstore(destination: String, key: String, vararg keys: String) =
        CommandExecution(SINTERSTORE, IntegerCommandProcessor, *createArguments(destination, key, *keys))


    fun _sismember(key: String, member: String) =
        CommandExecution(SISMEMBER, IntegerCommandProcessor, key.toArgument(), member.toArgument())

    fun _smembers(key: String) =
        CommandExecution(SMEMBERS, ArrayCommandProcessor, key.toArgument())

    fun _smismember(key: String, member: String, vararg members: String) =
        CommandExecution(SMISMEMBER, ArrayCommandProcessor, *createArguments(key, member, *members))

    fun _smove(source: String, destination: String, member: String) =
        CommandExecution(
            SMOVE,
            IntegerCommandProcessor,
            source.toArgument(),
            destination.toArgument(),
            member.toArgument()
        )

    fun _spop(key: String) =
        CommandExecution(SPOP, BulkStringCommandProcessor, key.toArgument())

    fun _spop(key: String, count: Int) =
        CommandExecution(SPOP, ArrayCommandProcessor, key.toArgument(), count.toArgument())

    fun _srandmember(key: String) =
        CommandExecution(SRANDMEMBER, BulkStringCommandProcessor, key.toArgument())

    fun _srandmember(key: String, count: Int) =
        CommandExecution(SRANDMEMBER, ArrayCommandProcessor, key.toArgument(), count.toArgument())

    fun _srem(key: String, member: String, vararg members: String) =
        CommandExecution(
            SREM, IntegerCommandProcessor, *createArguments(
                key, member, *members
            )
        )


    fun _sunion(key: String, vararg keys: String) =
        CommandExecution(SUNION, ArrayCommandProcessor, *createArguments(key, *keys))

    fun _sunionstore(destination: String, key: String, vararg keys: String) =
        CommandExecution(SUNIONSTORE, IntegerCommandProcessor, *createArguments(destination, key, *keys))

    fun _sscan(key: String, cursor: Long, matchPattern: String?, count: Long?): CommandExecution<IScanResult<String>> {
        val args = if (matchPattern != null && count != null)
            createArguments(key, cursor, "MATCH", matchPattern, "COUNT", count)
        else if (matchPattern != null)
            createArguments(key, cursor, "MATCH", matchPattern)
        else if (count != null)
            createArguments(key, cursor, "COUNT", count)
        else
            createArguments(key, cursor)

        return CommandExecution(
            SSCAN, SScanResultProcessor, *args
        )
    }
}

//TODO: SSCAN

public interface SetCommands {

    /**
     * ### `SADD key member [member ...]`
     *
     * Add the specified members to the set stored at key.
     * Specified members that are already a member of this set are ignored.
     * If key does not exist, a new set is created before adding the specified members.
     * An error is returned when the value stored at key is not a set.
     *
     * [Doc](https://redis.io/commands/sadd)
     * @since 1.0.0
     * @return the number of elements that were added to the set, not including all the elements already present in the set.
     */
    public suspend fun sadd(key: String, member: String, vararg members: String): Long

    /**
     * ###  SCARD key
     *
     * Returns the set cardinality (number of elements) of the set stored at key.
     *
     * [Doc](https://redis.io/commands/scard)
     * @since 1.0.0
     * @return the cardinality (number of elements) of the set, or 0 if key does not exist.
     */
    public suspend fun scard(key: String): Long

    /**
     * ###  `SDIFF key [key ...]`
     *
     * Returns the members of the set resulting from the difference between the first set and all the successive sets.
     *
     * [Doc](https://redis.io/commands/sdiff)
     * @since 1.0.0
     * @return list with members of the resulting set.
     */
    public suspend fun sdiff(key: String, vararg keys: String): List<String>

    /**
     * ### ` SDIFFSTORE destination key [key ...] `
     *
     * This command is equal to SDIFF, but instead of returning the resulting set, it is stored in destination.
     * if destination already exists, it is overwritten
     *
     * [Doc](https://redis.io/commands/sdiffstore)
     * @since 1.0.0
     * @return the number of elements in the resulting set.
     */
    public suspend fun sdiffstore(destination: String, key: String, vararg keys: String): Long

    /**
     * ###  `SINTER key [key ...]`
     *
     * Returns the members of the set resulting from the intersection of all the given sets.
     *
     * [Doc](https://redis.io/commands/sinter)
     * @since 1.0.0
     * @return list with members of the resulting set.
     */
    public suspend fun sinter(key: String, vararg keys: String): List<String>

    /**
     * ### ` SINTERCARD numkeys key [key ...] [LIMIT limit] `
     *
     * [Doc](https://redis.io/commands/sintercard)
     * @since 7.0.0
     * @return the number of elements in the resulting intersection.
     */
    public suspend fun sintercard(numkeys: Int, key: String, vararg keys: String, limit: Long?): Long

    /**
     * ### ` SINTERSTORE destination key [key ...]`
     *
     * This command is equal to SINTER, but instead of returning the resulting set, it is stored in destination.
     * If destination already exists, it is overwritten.
     *
     * [Doc](https://redis.io/commands/sinterstore)
     * @since 1.0.0
     * @return  the number of elements in the resulting set.
     */
    public suspend fun sinterstore(destination: String, key: String, vararg keys: String): Long

    /**
     * ###  SISMEMBER key member
     *
     * Returns if member is a member of the set stored at key.
     *
     * [Doc](https://redis.io/commands/sismember)
     * @since 1.0.0
     * @return 1 if the element is a member of the set.
     * 0 if the element is not a member of the set, or if key does not exist.
     */
    public suspend fun sismember(key: String, member: String): Long

    /**
     * ###  SMEMBERS key
     *
     * Returns all the members of the set value stored at key.
     *
     * [Doc](https://redis.io/commands/smembers)
     * @since 1.0.0
     * @return  all elements of the set.
     */
    public suspend fun smembers(key: String): List<String>


    /**
     * ###  `SMISMEMBER key member [member ...]`
     *
     * Returns whether each member is a member of the set stored at key.
     * For every member, 1 is returned if the value is a member of the set,
     * or 0 if the element is not a member of the set or if key does not exist.
     *
     * [Doc](https://redis.io/commands/smismember)
     * @since 6.2.0
     * @return list representing the membership of the given elements, in the same order as they are requested.
     */
    public suspend fun smismember(key: String, member: String, vararg members: String): List<Long>

    /**
     * ###  SMOVE source destination member
     *
     * Move member from the set at source to the set at destination
     *
     * [Doc](https://redis.io/commands/smove)
     * @since 1.0.0
     * @return 1 if the element is moved.
     * 0 if the element is not a member of source and no operation was performed.
     */
    public suspend fun smove(source: String, destination: String, member: String): Long

    /**
     * ### ` SPOP key`
     *
     * Removes and returns one or more random members from the set value store at key.
     *
     * [Doc](https://redis.io/commands/spop)
     * @since 1.0.0
     * @return the removed member, or nil when key does not exist.
     */
    public suspend fun spop(key: String): String?

    /**
     * ### ` SPOP key count`
     *
     * Removes and returns one or more random members from the set value store at key.
     *
     * [Doc](https://redis.io/commands/spop)
     * @since 3.2.0
     * @return  the removed members, or an empty array when key does not exist.
     */
    public suspend fun spop(key: String, count: Int): List<String>

    /**
     * ###  `SRANDMEMBER key`
     *
     * return a random element from the set value stored at key.
     *
     * [Doc](https://redis.io/commands/srandmember)
     * @since 1.0.0
     * @return the command returns a String Reply with the randomly selected element, or null when key does not exist.
     */
    public suspend fun srandmember(key: String): String?

    /**
     * ###  `SRANDMEMBER key count`
     *
     * return an array of distinct elements.
     *
     * [Doc](https://redis.io/commands/srandmember)
     * @since 2.6.0
     * @return the command returns an array of elements, or an empty array when key does not exist.
     */
    public suspend fun srandmember(key: String, count: Int): List<String>

    /**
     * ### ` SREM key member [member ...] `
     *
     * Remove the specified members from the set stored at key.
     * Specified members that are not a member of this set are ignored.
     * If key does not exist, it is treated as an empty set and this command returns 0.
     *
     * An error is returned when the value stored at key is not a set.
     *
     * [Doc](https://redis.io/commands/srem)
     * @since 2.4.0
     * @return the number of members that were removed from the set, not including non existing members.
     */
    public suspend fun srem(key: String, member: String, vararg members: String): Long


    /**
     * ### ` SUNION key [key ...]`
     *
     * Returns the members of the set resulting from the union of all the given sets.
     *
     * [Doc](https://redis.io/commands/sunion)
     * @since 1.0.0
     * @return list with members of the resulting set.
     */
    public suspend fun sunion(key: String, vararg keys: String): List<String>

    /**
     * ### ` SUNIONSTORE destination key [key ...] `
     *
     * This command is equal to SUNION, but instead of returning the resulting set, it is stored in destination.
     * If destination already exists, it is overwritten.
     *
     * [Doc](https://redis.io/commands/sunionstore)
     * @since 1.0.0
     * @return the number of elements in the resulting set.
     */
    public suspend fun sunionstore(destination: String, key: String, vararg keys: String): Long

    /**
     * ### ` SSCAN key cursor [MATCH pattern] [COUNT count]`
     *
     * [Doc](https://redis.io/commands/sscan)
     * @since 2.8.0
     * @return [SScanResult]
     */
    public suspend fun sscan(
        key: String,
        cursor: Long,
        matchPattern: String? = null,
        count: Long? = null
    ): IScanResult<String>
}

internal interface SetCommandExecutor : BaseSetCommands, SetCommands, CommandExecutor {
    override suspend fun sadd(key: String, member: String, vararg members: String): Long =
        execute(_sadd(key, member, *members))

    override suspend fun scard(key: String): Long =
        execute(_scard(key))

    override suspend fun sdiff(key: String, vararg keys: String): List<String> =
        execute(_sdiff(key, *keys)).responseTo("sdiff")

    override suspend fun sdiffstore(destination: String, key: String, vararg keys: String): Long =
        execute(_sdiffstore(destination, key, *keys))

    override suspend fun sinter(key: String, vararg keys: String): List<String> =
        execute(_sinter(key, *keys)).responseTo("sinter")

    override suspend fun sintercard(numkeys: Int, key: String, vararg keys: String, limit: Long?): Long =
        execute(_sintercard(numkeys, key, *keys, limit = limit))

    override suspend fun sinterstore(destination: String, key: String, vararg keys: String): Long =
        execute(_sinterstore(destination, key, *keys))

    override suspend fun sismember(key: String, member: String): Long =
        execute(_sismember(key, member))

    override suspend fun smembers(key: String): List<String> =
        execute(_smembers(key)).responseTo("smembers")

    override suspend fun smismember(key: String, member: String, vararg members: String): List<Long> =
        execute(_smismember(key, member, *members)).responseTo("smismember")

    override suspend fun smove(source: String, destination: String, member: String): Long =
        execute(_smove(source, destination, member))

    override suspend fun spop(key: String): String? =
        execute(_spop(key))

    override suspend fun spop(key: String, count: Int): List<String> =
        execute(_spop(key, count)).responseTo("spop")

    override suspend fun srandmember(key: String): String? =
        execute(_srandmember(key))

    override suspend fun srandmember(key: String, count: Int): List<String> =
        execute(_srandmember(key, count)).responseTo("srandmember")

    override suspend fun srem(key: String, member: String, vararg members: String): Long =
        execute(_srem(key, member, *members))

    override suspend fun sunion(key: String, vararg keys: String): List<String> =
        execute(_sunion(key, *keys)).responseTo("sunion")

    override suspend fun sunionstore(destination: String, key: String, vararg keys: String): Long =
        execute(_sunionstore(destination, key, *keys))

    override suspend fun sscan(key: String, cursor: Long, matchPattern: String?, count: Long?): IScanResult<String> =
        execute(_sscan(key, cursor, matchPattern, count))
}