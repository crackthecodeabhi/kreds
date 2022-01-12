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

import io.github.crackthecodeabhi.kreds.args.ZRangeStoreBy
import io.github.crackthecodeabhi.kreds.pipeline.QueuedCommand
import io.github.crackthecodeabhi.kreds.pipeline.Response

public interface PipelineZSetCommands {
    /**
     * @see [ZSetCommands.zcard]
     */
    public suspend fun zcard(key: String): Response<Long>

    /**
     * @see [ZSetCommands.zcount]
     */
    public suspend fun zcount(key: String, min: Int, max: Int): Response<Long>

    /**
     * @see [ZSetCommands.zincrBy]
     */
    public suspend fun zincrBy(key: String, increment: Int, member: String): Response<String?>

    /**
     * @see [ZSetCommands.zlexcount]
     */
    public suspend fun zlexcount(key: String, min: Int, max: Int): Response<Long>


    /**
     * @see [ZSetCommands.zrangestore]
     */
    public suspend fun zrangestore(
        dst: String,
        src: String,
        min: Int,
        max: Int,
        by: ZRangeStoreBy? = null,
        rev: Boolean? = null,
        limit: Pair<Int, Int>? = null
    ): Response<Long>

    /**
     * @see [ZSetCommands.zrank]
     */
    public suspend fun zrank(key: String, member: String): Response<Long?>

    /**
     * @see [ZSetCommands.zrem]
     */
    public suspend fun zrem(key: String, member: String, vararg members: String): Response<Long>

    /**
     * @see [ZSetCommands.zremrangebylex]
     */
    public suspend fun zremrangebylex(key: String, min: Int, max: Int): Response<Long>

    /**
     * @see [ZSetCommands.zremrangebyrank]
     */
    public suspend fun zremrangebyrank(key: String, start: Int, stop: Int): Response<Long>

    /**
     * @see [ZSetCommands.zremrangebyscore]
     */
    public suspend fun zremrangebyscore(key: String, min: Int, max: Int): Response<Long>

    /**
     * @see [ZSetCommands.zrevrank]
     */
    public suspend fun zrevrank(key: String, member: String): Response<Long?>

    /**
     * @see [ZSetCommands.zscore]
     */
    public suspend fun zscore(key: String, member: String): Response<String?>
}

internal interface PipelineZSetCommandExecutor : QueuedCommand, PipelineZSetCommands, BaseZSetCommands {
    override suspend fun zcard(key: String): Response<Long> =
        add(_zcard(key))

    override suspend fun zcount(key: String, min: Int, max: Int): Response<Long> =
        add(_zcount(key, min, max))

    override suspend fun zincrBy(key: String, increment: Int, member: String): Response<String?> =
        add(_zincrBy(key, increment, member))

    override suspend fun zlexcount(key: String, min: Int, max: Int): Response<Long> =
        add(_zlexcount(key, min, max))

    override suspend fun zrangestore(
        dst: String,
        src: String,
        min: Int,
        max: Int,
        by: ZRangeStoreBy?,
        rev: Boolean?,
        limit: Pair<Int, Int>?
    ): Response<Long> =
        add(_zrangestore(dst, src, min, max, by, rev, limit))

    override suspend fun zrank(key: String, member: String): Response<Long?> =
        add(_zrank(key, member))

    override suspend fun zrem(key: String, member: String, vararg members: String): Response<Long> =
        add(_zrem(key, member, *members))

    override suspend fun zremrangebylex(key: String, min: Int, max: Int): Response<Long> =
        add(_zremrangebylex(key, min, max))

    override suspend fun zremrangebyrank(key: String, start: Int, stop: Int): Response<Long> =
        add(_zremrangebyrank(key, start, stop))

    override suspend fun zremrangebyscore(key: String, min: Int, max: Int): Response<Long> =
        add(_zremrangebyscore(key, min, max))

    override suspend fun zrevrank(key: String, member: String): Response<Long?> =
        add(_zrevrank(key, member))

    override suspend fun zscore(key: String, member: String): Response<String?> =
        add(_zscore(key, member))
}