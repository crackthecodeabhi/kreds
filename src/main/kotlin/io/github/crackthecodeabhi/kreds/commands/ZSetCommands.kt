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
import io.github.crackthecodeabhi.kreds.protocol.*

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

    fun _zincrBy(key: String, increment: Double, member: String) =
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
        by: ZSetByOption? = null,
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

    fun _zrank(key: String, member: String) =
        CommandExecution(ZRANK, IntegerOrBulkNullStringCommandProcessor, key.toArgument(), member.toArgument())

    fun _zrem(key: String, member: String, vararg members: String) =
        CommandExecution(
            ZREM,
            IntegerCommandProcessor,
            key.toArgument(),
            member.toArgument(),
            *createArguments(*members)
        )

    fun _zremrangebylex(key: String, min: Int, max: Int) =
        CommandExecution(ZREMRANGEBYLEX, IntegerCommandProcessor, key.toArgument(), min.toArgument(), max.toArgument())

    fun _zremrangebyrank(key: String, start: Int, stop: Int) =
        CommandExecution(
            ZREMRANGEBYRANK,
            IntegerCommandProcessor,
            key.toArgument(),
            start.toArgument(),
            stop.toArgument()
        )

    fun _zremrangebyscore(key: String, min: Int, max: Int) =
        CommandExecution(
            ZREMRANGEBYSCORE,
            IntegerCommandProcessor,
            key.toArgument(),
            min.toArgument(),
            max.toArgument()
        )


    fun _zrevrank(key: String, member: String) =
        CommandExecution(ZREVRANK, IntegerOrBulkNullStringCommandProcessor, key.toArgument(), member.toArgument())

    fun _zscan(
        key: String,
        cursor: Long,
        pattern: String? = null,
        count: Int? = null
    ): CommandExecution<IScanResult<Pair<String, Long>>> {
        val args = if (pattern != null && count != null) createArguments(key, cursor, "MATCH", pattern, "COUNT", count)
        else if (pattern != null) createArguments(key, cursor, "MATCH", pattern)
        else if (count != null) createArguments(key, cursor, "COUNT", count)
        else createArguments(key, cursor)
        return CommandExecution(ZSCAN, ZScanResultProcessor, *args)
    }

    fun _zscore(key: String, member: String) =
        CommandExecution(ZSCORE, BulkStringCommandProcessor, key.toArgument(), member.toArgument())

    fun _zunionstore(
        destination: String,
        numKeys: Int,
        key: String,
        vararg keys: String,
        weights: Weights? = null,
        aggregate: AggregateType? = null
    ) =
        CommandExecution(
            ZUNIONSTORE, IntegerCommandProcessor, *createArguments(
                destination,
                numKeys,
                key,
                *keys,
                weights,
                aggregate
            )
        )

    fun _zadd(
        key: String,
        nxOrXX: ZAddNXOrXX? = null,
        gtOrLt: ZAddGTOrLT? = null,
        ch: Boolean? = null,
        scoreMember: Pair<Int, String>,
        vararg scoreMembers: Pair<Int, String>
    ) =
        CommandExecution(
            ZADD, IntegerCommandProcessor, *createArguments(
                key,
                nxOrXX,
                gtOrLt,
                ch?.let { KeyOnlyArgument("CH") },
                scoreMember,
                *scoreMembers
            )
        )

    fun _zadd(
        key: String,
        nxOrXX: ZAddNXOrXX? = null,
        gtOrLt: ZAddGTOrLT? = null,
        ch: Boolean? = null,
        incr: Boolean,
        scoreMember: Pair<Int, String>,
        vararg scoreMembers: Pair<Int, String>
    ) =
        CommandExecution(
            ZADD, BulkStringCommandProcessor, *createArguments(
                key,
                nxOrXX,
                gtOrLt,
                ch?.let { KeyOnlyArgument("CH") },
                incr.let { KeyOnlyArgument("INCR") },
                scoreMember,
                *scoreMembers
            )
        )

    fun _zdiff(numKeys: Int, key: String, vararg keys: String, withScores: Boolean? = null) =
        CommandExecution(ZDIFF, ArrayCommandProcessor, *createArguments(
            numKeys,
            key,
            *keys,
            withScores?.let { if (it) KeyOnlyArgument("WITHSCORES") else null }
        ))

    fun _zdiffstore(destination: String, numKeys: Int, key: String, vararg keys: String) =
        CommandExecution(
            ZDIFFSTORE, IntegerCommandProcessor, *createArguments(
                destination,
                numKeys,
                key,
                *keys
            )
        )

    fun _zinter(
        numKeys: Int,
        key: String,
        vararg keys: String,
        weights: Weights? = null,
        aggregate: AggregateType? = null,
        withScores: Boolean? = null
    ) =
        CommandExecution(ZINTER, ArrayCommandProcessor, *createArguments(
            numKeys,
            key,
            *keys,
            weights,
            aggregate,
            withScores?.let { if (it) KeyOnlyArgument("WITHSCORES") else null }
        ))

    fun _zintercard(numKeys: Int, key: String, vararg keys: String, limit: Boolean? = null) =
        CommandExecution(
            ZINTERCARD, IntegerCommandProcessor, *createArguments(
                numKeys,
                key,
                *keys,
                limit
            )
        )

    fun _zinterstore(
        destination: String,
        numKeys: Int,
        key: String,
        vararg keys: String,
        weights: Weights?,
        aggregate: AggregateType?
    ) = CommandExecution(
        ZINTERSTORE, IntegerCommandProcessor, *createArguments(
            destination,
            numKeys,
            key,
            *keys,
            weights,
            aggregate
        )
    )

    fun _zrange(
        key: String,
        min: Long,
        max: Long,
        by: ZSetByOption?,
        rev: Boolean?,
        limit: Pair<Int, Int>?,
        withScores: Boolean?
    ) = CommandExecution(
        ZRANGE,
        ArrayCommandProcessor,
        *createArguments(
            key,
            min,
            max,
            by,
            rev?.let {
                if (it)
                    KeyOnlyArgument("REV")
                else
                    EmptyArgument
            },
            limit?.let { KeyValueArgument("LIMIT","${it.first} ${it.second}") },
            withScores?.let {
                if (it)
                    KeyOnlyArgument("WITHSCORES")
                else
                    EmptyArgument
            }
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
    public suspend fun zincrBy(key: String, increment: Double, member: String): String?

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
        by: ZSetByOption? = null,
        rev: Boolean? = null,
        limit: Pair<Int, Int>? = null
    ): Long

    /**
     * ### ZRANK key member
     *
     * Returns the rank of member in the sorted set stored at key, with the scores ordered from low to high. The rank (or index) is 0-based, which means that the member with the lowest score has rank 0.
     *
     * [Doc](https://redis.io/commands/zrank)
     * @since 2.0.0
     * @return If member exists in the sorted set, the rank of member,
     * If member does not exist in the sorted set or key does not exist, null
     */
    public suspend fun zrank(key: String, member: String): Long?


    /**
     * ### ` ZREM key member [member ...] `
     *
     * Removes the specified members from the sorted set stored at key. Non existing members are ignored.
     * An error is returned when key exists and does not hold a sorted set
     *
     *[Doc](https://redis.io/commands/zrem)
     * @since * 1.2.0
     * * 2.4.0: Accepts multiple elements.
     * @return The number of members removed from the sorted set, not including non existing members.
     */
    public suspend fun zrem(key: String, member: String, vararg members: String): Long

    /**
     * ###  ZREMRANGEBYLEX key min max
     *
     * When all the elements in a sorted set are inserted with the same score, in order to force lexicographical ordering, this command removes all elements in the sorted set stored at key between the lexicographical range specified by min and max.
     *
     * [Doc](https://redis.io/commands/zremrangebylex)
     * @since 2.8.9
     * @return the number of elements removed.
     */
    public suspend fun zremrangebylex(key: String, min: Int, max: Int): Long

    /**
     * ###  ZREMRANGEBYRANK key start stop
     *
     * Removes all elements in the sorted set stored at key with rank between start and stop. Both start and stop are 0 -based indexes with 0 being the element with the lowest score.
     *
     * [Doc](https://redis.io/commands/zremrangebyrank)
     * @since 2.0.0
     * @return the number of elements removed.
     */
    public suspend fun zremrangebyrank(key: String, start: Int, stop: Int): Long

    /**
     * ###  ZREMRANGEBYSCORE key min max
     *
     * Removes all elements in the sorted set stored at key with a score between min and max (inclusive).
     *
     * [Doc](https://redis.io/commands/zremrangebyscore)
     * @since 1.2.0
     * @return the number of elements removed.
     */
    public suspend fun zremrangebyscore(key: String, min: Int, max: Int): Long


    /**
     * ###  ZREVRANK key member
     *
     * Returns the rank of member in the sorted set stored at key, with the scores ordered from high to low.
     *
     * [Doc](https://redis.io/commands/zrevrank)
     * @since 2.0.0
     * @return If member exists in the sorted set, the rank of member
     * If member does not exist in the sorted set or key does not exist, null
     */
    public suspend fun zrevrank(key: String, member: String): Long?

    /**
     * ### ` ZSCAN key cursor [MATCH pattern] [COUNT count] `
     *
     * [Doc](https://redis.io/commands/zscan)
     * @since 2.8.0
     */
    public suspend fun zscan(
        key: String,
        cursor: Long,
        pattern: String? = null,
        count: Int? = null
    ): IScanResult<Pair<String, Long>>

    /**
     * ###  ZSCORE key member
     *
     * Returns the score of member in the sorted set at key.
     * If member does not exist in the sorted set, or key does not exist, nil is returned.
     *
     * [Doc](https://redis.io/commands/zscore)
     * @since 1.2.0
     * @return the score of member (a double precision floating point number), represented as string.
     */
    public suspend fun zscore(key: String, member: String): String?

    /**
     * ### ` ZUNIONSTORE destination numkeys key [key ...] [WEIGHTS weight [weight ...]] [AGGREGATE SUM|MIN|MAX] `
     *
     * [Doc](https://redis.io/commands/zunionstore)
     * @since 2.0.0
     * @return he number of elements in the resulting sorted set at destination.
     */
    public suspend fun zunionstore(
        destination: String,
        numKeys: Int,
        key: String,
        vararg keys: String,
        weights: Weights? = null,
        aggregate: AggregateType? = null
    ): Long


    /**
     * ### ` ZADD key [NX|XX] [GT|LT] [CH] [INCR] score member [score member ...] `
     *
     * Adds all the specified members with the specified scores to the sorted set stored at key
     *
     * [Doc](https://redis.io/commands/zadd)
     * @since 1.2.0
     * @return * When used without optional arguments, the number of elements added to the sorted set (excluding score updates).
     * * If the CH option is specified, the number of elements that were changed (added or updated).
     */
    public suspend fun zadd(
        key: String,
        nxOrXX: ZAddNXOrXX? = null,
        gtOrLt: ZAddGTOrLT? = null,
        ch: Boolean? = null,
        scoreMember: Pair<Int, String>,
        vararg scoreMembers: Pair<Int, String>
    ): Long

    /**
     * @since 1.2.0
     * @see [ZSetCommands.zadd]
     * @return the new score of member (a double precision floating point number) represented as string, or nil if the operation was aborted (when called with either the XX or the NX option).
     */
    public suspend fun zadd(
        key: String,
        nxOrXX: ZAddNXOrXX? = null,
        gtOrLt: ZAddGTOrLT? = null,
        ch: Boolean? = null,
        incr: Boolean,
        scoreMember: Pair<Int, String>,
        vararg scoreMembers: Pair<Int, String>
    ): String?

    /**
     * ### ` ZDIFF numkeys key [key ...] [WITHSCORES] `
     *
     * This command is similar to ZDIFFSTORE, but instead of storing the resulting sorted set, it is returned to the client.
     *
     * [Doc](https://redis.io/commands/zdiff)
     * @since 6.2.0
     * @return the result of the difference (optionally with their scores, in case the WITHSCORES option is given).
     */
    public suspend fun zdiff(numKeys: Int, key: String, vararg keys: String, withScores: Boolean? = null): List<String>


    /**
     * ### ` ZDIFFSTORE destination numkeys key [key ...] `
     *
     * [Doc](https://redis.io/commands/zdiffstore)
     * @since 6.2.0
     * @return the number of elements in the resulting sorted set at destination.
     */
    public suspend fun zdiffstore(destination: String, numKeys: Int, key: String, vararg keys: String): Long


    /**
     * ### ` ZINTER numkeys key [key ...] [WEIGHTS weight [weight ...]] [AGGREGATE SUM|MIN|MAX] [WITHSCORES] `
     *
     * This command is similar to ZINTERSTORE, but instead of storing the resulting sorted set, it is returned to the client.
     * For a description of the WEIGHTS and AGGREGATE options, see ZUNIONSTORE.
     *
     * [Doc](https://redis.io/commands/zinter)
     * @since 6.2.0
     * @return the result of intersection (optionally with their scores, in case the WITHSCORES option is given).
     */
    public suspend fun zinter(
        numKeys: Int,
        key: String,
        vararg keys: String,
        weights: Weights? = null,
        aggregate: AggregateType? = null,
        withScores: Boolean? = null
    ): List<String>

    /**
     * ### `ZINTERCARD numkeys key [key ...] [LIMIT limit]'
     *
     * This command is similar to ZINTER, but instead of returning the result set, it returns just the cardinality of the result.
     *
     * [Doc](https://redis.io/commands/zintercard)
     * @since 7.0.0
     * @return the number of elements in the resulting intersection.
     */
    public suspend fun zintercard(numKeys: Int, key: String, vararg keys: String, limit: Boolean? = null): Long

    /**
     * ### ` ZINTERSTORE destination numkeys key [key ...] [WEIGHTS weight [weight ...]] [AGGREGATE SUM|MIN|MAX]`
     *
     * [Doc](https://redis.io/commands/zinterstore)
     * @since 2.0.0
     * @return the number of elements in the resulting sorted set at destination.
     */
    public suspend fun zinterstore(
        destination: String,
        numKeys: Int,
        key: String,
        vararg keys: String,
        weights: Weights?,
        aggregate: AggregateType?
    ): Long

    /**
     * ### `ZRANGE key min max [BYSCORE|BYLEX] [REV] [LIMIT offset count] [WITHSCORES]`
     *
     * [Doc](https://redis.io/commands/zrange/)
     * @since 1.2.0
     *  @param limit a pair of offset and count.
     * @return list of elements in the specified range (optionally with their scores, in case the WITHSCORES option is given).
     */
    public suspend fun zrange(
        key: String,
        min: Long,
        max: Long,
        by: ZSetByOption?,
        rev: Boolean?,
        limit: Pair<Int, Int>?,
        withScores: Boolean?
    ): List<String>
}

internal interface ZSetCommandExecutor : BaseZSetCommands, CommandExecutor, BlockingZSetCommands, ZSetCommands {
    override suspend fun zcard(key: String): Long =
        execute(_zcard(key))

    override suspend fun zcount(key: String, min: Int, max: Int): Long =
        execute(_zcount(key, min, max))

    override suspend fun zincrBy(key: String, increment: Double, member: String): String? =
        execute(_zincrBy(key, increment, member))

    override suspend fun zlexcount(key: String, min: Int, max: Int): Long =
        execute(_zlexcount(key, min, max))

    override suspend fun zrangestore(
        dst: String,
        src: String,
        min: Int,
        max: Int,
        by: ZSetByOption?,
        rev: Boolean?,
        limit: Pair<Int, Int>?
    ): Long =
        execute(_zrangestore(dst, src, min, max, by, rev, limit))

    override suspend fun zrank(key: String, member: String): Long? =
        execute(_zrank(key, member))

    override suspend fun zrem(key: String, member: String, vararg members: String): Long =
        execute(_zrem(key, member, *members))

    override suspend fun zremrangebylex(key: String, min: Int, max: Int): Long =
        execute(_zremrangebylex(key, min, max))

    override suspend fun zremrangebyrank(key: String, start: Int, stop: Int): Long =
        execute(_zremrangebyrank(key, start, stop))

    override suspend fun zremrangebyscore(key: String, min: Int, max: Int): Long =
        execute(_zremrangebyscore(key, min, max))

    override suspend fun zrevrank(key: String, member: String): Long? =
        execute(_zrevrank(key, member))

    override suspend fun zscan(
        key: String,
        cursor: Long,
        pattern: String?,
        count: Int?
    ): IScanResult<Pair<String, Long>> =
        execute(_zscan(key, cursor, pattern, count))

    override suspend fun zscore(key: String, member: String): String? =
        execute(_zscore(key, member))

    override suspend fun zunionstore(
        destination: String,
        numKeys: Int,
        key: String,
        vararg keys: String,
        weights: Weights?,
        aggregate: AggregateType?
    ): Long = execute(_zunionstore(destination, numKeys, key, *keys, weights = weights, aggregate = aggregate))

    override suspend fun zadd(
        key: String,
        nxOrXX: ZAddNXOrXX?,
        gtOrLt: ZAddGTOrLT?,
        ch: Boolean?,
        scoreMember: Pair<Int, String>,
        vararg scoreMembers: Pair<Int, String>
    ): Long = execute(_zadd(key, nxOrXX, gtOrLt, ch, scoreMember, scoreMembers = scoreMembers))

    override suspend fun zadd(
        key: String,
        nxOrXX: ZAddNXOrXX?,
        gtOrLt: ZAddGTOrLT?,
        ch: Boolean?,
        incr: Boolean,
        scoreMember: Pair<Int, String>,
        vararg scoreMembers: Pair<Int, String>
    ): String? = execute(_zadd(key, nxOrXX, gtOrLt, ch, incr, scoreMember, scoreMembers = scoreMembers))

    override suspend fun zdiff(numKeys: Int, key: String, vararg keys: String, withScores: Boolean?): List<String> =
        execute(_zdiff(numKeys, key, *keys, withScores = withScores)).responseTo("zdiff")

    override suspend fun zdiffstore(destination: String, numKeys: Int, key: String, vararg keys: String): Long =
        execute(_zdiffstore(destination, numKeys, key, *keys))

    override suspend fun zinter(
        numKeys: Int,
        key: String,
        vararg keys: String,
        weights: Weights?,
        aggregate: AggregateType?,
        withScores: Boolean?
    ): List<String> =
        execute(
            _zinter(
                numKeys,
                key,
                *keys,
                weights = weights,
                aggregate = aggregate,
                withScores = withScores
            )
        ).responseTo("zinter")


    override suspend fun zintercard(numKeys: Int, key: String, vararg keys: String, limit: Boolean?): Long =
        execute(_zintercard(numKeys, key, *keys, limit = limit))

    override suspend fun zinterstore(
        destination: String,
        numKeys: Int,
        key: String,
        vararg keys: String,
        weights: Weights?,
        aggregate: AggregateType?
    ): Long = execute(_zinterstore(destination, numKeys, key, *keys, weights = weights, aggregate = aggregate))

    override suspend fun zrange(
        key: String,
        min: Long,
        max: Long,
        by: ZSetByOption?,
        rev: Boolean?,
        limit: Pair<Int, Int>?,
        withScores: Boolean?
    ): List<String> = execute(_zrange(key, min, max, by, rev, limit, withScores)).responseTo("zrange")
}

