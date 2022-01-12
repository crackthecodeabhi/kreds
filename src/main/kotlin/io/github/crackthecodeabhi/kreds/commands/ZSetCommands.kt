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
import io.github.crackthecodeabhi.kreds.commands.ZSetCommand.*
import io.github.crackthecodeabhi.kreds.protocol.BulkStringCommandProcessor
import io.github.crackthecodeabhi.kreds.protocol.CommandExecutor
import io.github.crackthecodeabhi.kreds.protocol.IntegerCommandProcessor

internal enum class ZSetCommand(override val subCommand: Command? = null) : Command {
    ZADD, ZCARD, ZCOUNT, ZDIFF, ZDIFFSTORE, ZINCRBY, ZINTER, ZINTERCARD, ZINTERSTORE,
    ZLEXCOUNT, ZMPOP, ZMSCORE, ZPOPMAX, ZPOPMIN, ZRANDMEMBER, ZRANGE,
    ZRANGEBYLEX, ZRANGEBYSCORE, ZRANGESTORE, ZRANK, ZREM, ZREMRANGEBYLEX,
    ZREMRANGEBYRANK, ZREMRANGEBYSCORE, ZREVRANGE, ZREVRANGEBYLEX, ZREVRANGEBYSCORE,
    ZREVRANK, ZSCAN, ZSCORE, ZUNION, ZUNIONSTORE,
    BZMPOP, BZPOPMAX, BZPOPMIN;

    override val string: String = name
}

internal interface BaseZSetCommands {
    fun _zcard(key: String) =
        CommandExecution(ZCARD, IntegerCommandProcessor, key.toArgument())

    fun _zcount(key: String, min: Int, max: Int) =
        CommandExecution(ZCOUNT, IntegerCommandProcessor, key.toArgument(), min.toArgument(), max.toArgument())

    fun _zincrBy(key: String, increment: Int, member: String) =
        CommandExecution(
            ZINCRBY,
            BulkStringCommandProcessor,
            key.toArgument(),
            increment.toArgument(),
            member.toArgument()
        )

    fun _zlexcount(key: String, min: Int, max: Int) =
        CommandExecution(ZLEXCOUNT, IntegerCommandProcessor, key.toArgument(), min.toArgument(), max.toArgument())


    fun _zrangestore(
        dst: String,
        src: String,
        min: Int,
        max: Int,
        by: ZRangeStoreBy? = null,
        rev: Boolean? = null,
        limit: Pair<Int, Int>? = null
    ) =
        CommandExecution(
            ZRANGESTORE, IntegerCommandProcessor,
            *createArguments(
                dst,
                src,
                min,
                max,
                by,
                if (rev != null) KeyOnlyArgument("REV") else null,
                if (limit != null) KeyValueArgument("LIMIT", "${limit.first} ${limit.second}{}") else null
            )
        )
}

public interface BlockingZSetCommands : BlockingOperation {

}

public interface ZSetCommands {
    /**
     * ###  ZCARD key
     *
     * Returns the sorted set cardinality (number of elements) of the sorted set stored at key.
     *
     * [Doc](https://redis.io/commands/zcard)
     * @since 1.2.0
     * @return the cardinality (number of elements) of the sorted set, or 0 if key does not exist.
     */
    public suspend fun zcard(key: String): Long

    /**
     * ###  ZCOUNT key min max
     *
     * Returns the number of elements in the sorted set at key with a score between min and max.
     *
     * [Doc](https://redis.io/commands/zcount)
     * @since 2.0.0
     * @return the number of elements in the specified score range
     */
    public suspend fun zcount(key: String, min: Int, max: Int): Long

    /**
     * ###  ZINCRBY key increment member
     *
     * [Doc](https://redis.io/commands/zincrby)
     * @since 1.2.0
     * @return the new score of member (a double precision floating point number), represented as string.
     */
    public suspend fun zincrBy(key: String, increment: Int, member: String): String?

    /**
     * ###  ZLEXCOUNT key min max
     *
     * [Doc](https://redis.io/commands/zlexcount)
     * @since 2.8.9
     * @return the number of elements in the specified score range.
     */
    public suspend fun zlexcount(key: String, min: Int, max: Int): Long


    /**
     * ### ` ZRANGESTORE dst src min max [BYSCORE|BYLEX] [REV] [LIMIT offset count] `
     *
     * This command is like ZRANGE, but stores the result in the <dst> destination key.
     *
     * [Doc](https://redis.io/commands/zrangestore)
     * @since 6.2.0
     * @param limit a pair of offset and count.
     * @return the number of elements in the resulting sorted set.
     */
    public suspend fun zrangestore(
        dst: String,
        src: String,
        min: Int,
        max: Int,
        by: ZRangeStoreBy? = null,
        rev: Boolean? = null,
        limit: Pair<Int, Int>? = null
    ): Long
}

internal interface ZSetCommandExecutor : BaseZSetCommands, CommandExecutor, BlockingZSetCommands, ZSetCommands {
    override suspend fun zcard(key: String): Long =
        execute(_zcard(key))

    override suspend fun zcount(key: String, min: Int, max: Int): Long =
        execute(_zcount(key, min, max))

    override suspend fun zincrBy(key: String, increment: Int, member: String): String? =
        execute(_zincrBy(key, increment, member))

    override suspend fun zlexcount(key: String, min: Int, max: Int): Long =
        execute(_zlexcount(key, min, max))

    override suspend fun zrangestore(
        dst: String,
        src: String,
        min: Int,
        max: Int,
        by: ZRangeStoreBy?,
        rev: Boolean?,
        limit: Pair<Int, Int>?
    ): Long =
        execute(_zrangestore(dst, src, min, max, by, rev, limit))
}

