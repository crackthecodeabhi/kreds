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

import io.github.crackthecodeabhi.kreds.args.GetExOption
import io.github.crackthecodeabhi.kreds.args.SetOption
import io.github.crackthecodeabhi.kreds.pipeline.QueuedCommand
import io.github.crackthecodeabhi.kreds.pipeline.Response
import java.math.BigDecimal

public interface PipelineStringCommands {
    /**
     * @see [StringCommands.get]
     */
    public suspend fun get(key: String): Response<String?>

    /**
     * @see [StringCommands.set]
     */
    public suspend fun set(key: String, value: String, setOption: SetOption? = null): Response<String?>

    /**
     * @see [StringCommands.append]
     */
    public suspend fun append(key: String, value: String): Response<Long>

    /**
     * @see [StringCommands.decr]
     */
    public suspend fun decr(key: String): Response<Long>

    /**
     * @see [StringCommands.decrBy]
     */
    public suspend fun decrBy(key: String, decrement: Long): Response<Long>

    /**
     * @see [StringCommands.getDel]
     */
    public suspend fun getDel(key: String): Response<String?>

    /**
     * @see [StringCommands.getRange]
     */
    public suspend fun getRange(key: String, start: Int, end: Int): Response<String?>

    /**
     * @see [StringCommands.getSet]
     */
    public suspend fun getSet(key: String, value: String): Response<String?>

    /**
     * @see [StringCommands.incr]
     */
    public suspend fun incr(key: String): Response<Long>

    /**
     * @see [StringCommands.incrBy]
     */
    public suspend fun incrBy(key: String, increment: Long): Response<Long>

    /**
     * @see [StringCommands.incrByFloat]
     */
    public suspend fun incrByFloat(key: String, increment: BigDecimal): Response<String?>

    /**
     * @see [StringCommands.mget]
     */
    public suspend fun mget(vararg keys: String): Response<List<String?>>

    /**
     * @see [StringCommands.mset]
     */
    public suspend fun mset(vararg keyValues: Pair<String, String>): Response<String>

    /**
     * @see [StringCommands.getEx]
     */
    public suspend fun getEx(key: String, getExOption: GetExOption? = null): Response<String?>

    /**
     * @see [StringCommands.strlen]
     */
    public suspend fun strlen(key: String): Response<Long>
}


internal interface PipelineStringCommandsExecutor : PipelineStringCommands, BaseStringCommands, QueuedCommand {
    override suspend fun get(key: String): Response<String?> = add(_get(key))

    override suspend fun set(key: String, value: String, setOption: SetOption?): Response<String?> =
        add(_set(key, value, setOption))

    override suspend fun append(key: String, value: String): Response<Long> = add(_append(key, value))

    override suspend fun decr(key: String): Response<Long> = add(_decr(key))

    override suspend fun decrBy(key: String, decrement: Long): Response<Long> = add(_decrBy(key, decrement))

    override suspend fun getDel(key: String): Response<String?> = add(_getDel(key))

    override suspend fun getRange(key: String, start: Int, end: Int): Response<String?> =
        add(_getRange(key, start, end))

    override suspend fun getSet(key: String, value: String): Response<String?> = add(_getSet(key, value))

    override suspend fun incr(key: String): Response<Long> = add(_incr(key))

    override suspend fun incrBy(key: String, increment: Long): Response<Long> = add(_incrBy(key, increment))

    override suspend fun incrByFloat(key: String, increment: BigDecimal): Response<String?> =
        add(_incrByFloat(key, increment))

    override suspend fun mget(vararg keys: String): Response<List<String?>> = add(_mget(*keys), false).asReturnType()

    override suspend fun mset(vararg keyValues: Pair<String, String>): Response<String> = add(_mset(*keyValues))

    override suspend fun getEx(key: String, getExOption: GetExOption?): Response<String?> =
        add(_getEx(key, getExOption))

    override suspend fun strlen(key: String): Response<Long> = add(_strlen(key))
}
