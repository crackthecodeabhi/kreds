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

import io.github.crackthecodeabhi.kreds.args.BeforeAfterOption
import io.github.crackthecodeabhi.kreds.args.LeftRightOption
import io.github.crackthecodeabhi.kreds.pipeline.QueuedCommand
import io.github.crackthecodeabhi.kreds.pipeline.Response

public interface PipelineListCommands {

    /**
     * @see [ListCommands.lindex]
     */
    public suspend fun lindex(key: String, index: Int): Response<String?>

    /**
     * @see [ListCommands.linsert]
     */
    public suspend fun linsert(
        key: String,
        beforeAfterOption: BeforeAfterOption,
        pivot: String,
        element: String
    ): Response<Long>

    /**
     * @see [ListCommands.llen]
     */
    public suspend fun llen(key: String): Response<Long>

    /**
     * @see [ListCommands.lmove]
     */
    public suspend fun lmove(
        source: String,
        destination: String,
        whereFrom: LeftRightOption,
        whereTo: LeftRightOption
    ): Response<String?>

    /**
     * @see [ListCommands.lmpop]
     */
    public suspend fun lmpop(
        numkeys: Long,
        key: String,
        vararg keys: String,
        leftRight: LeftRightOption,
        count: Long?
    ): Response<LMPOPResult?>

    /**
     * @see [ListCommands.lpop]
     */
    public suspend fun lpop(key: String): Response<String?>

    /**
     * @see [ListCommands.lpop]
     */
    public suspend fun lpop(key: String, count: Long): Response<List<String>?>

    /**
     * @see [ListCommands.lpush]
     */
    public suspend fun lpush(key: String, element: String, vararg elements: String): Response<Long>

    /**
     * @see [ListCommands.lpushx]
     */
    public suspend fun lpushx(key: String, element: String, vararg elements: String): Response<Long>

    /**
     * @see [ListCommands.lrange]
     */
    public suspend fun lrange(key: String, start: Int, stop: Int): Response<List<String>>

    /**
     * @see [ListCommands.lrem]
     */
    public suspend fun lrem(key: String, count: Int, element: String): Response<Long>

    /**
     * @see [ListCommands.lset]
     */
    public suspend fun lset(key: String, index: Int, element: String): Response<String>

    /**
     * @see [ListCommands.ltrim]
     */
    public suspend fun ltrim(key: String, start: Int, stop: Int): Response<String>

    /**
     * @see [ListCommands.rpop]
     */
    public suspend fun rpop(key: String): Response<String?>

    /**
     * @see [ListCommands.rpop]
     */
    public suspend fun rpop(key: String, count: Int): Response<List<String>?>

    /**
     * @see [ListCommands.rpush]
     */
    public suspend fun rpush(key: String, element: String, vararg elements: String): Response<Long>

    /**
     * @see [ListCommands.rpushx]
     */
    public suspend fun rpushx(key: String, element: String, vararg elements: String): Response<Long>
}

internal interface PipelineListCommandExecutor : PipelineListCommands, QueuedCommand, BaseListCommands {
    override suspend fun lindex(key: String, index: Int): Response<String?> = add(_lindex(key, index))

    override suspend fun linsert(
        key: String,
        beforeAfterOption: BeforeAfterOption,
        pivot: String,
        element: String
    ): Response<Long> = add(_linsert(key, beforeAfterOption, pivot, element))

    override suspend fun llen(key: String): Response<Long> = add(_llen(key))

    override suspend fun lmove(
        source: String,
        destination: String,
        whereFrom: LeftRightOption,
        whereTo: LeftRightOption
    ): Response<String?> = add(_lmove(source, destination, whereFrom, whereTo))

    override suspend fun lmpop(
        numkeys: Long,
        key: String,
        vararg keys: String,
        leftRight: LeftRightOption,
        count: Long?
    ): Response<LMPOPResult?> = add(_lmpop(numkeys, key, *keys, leftRight = leftRight, count = count))

    override suspend fun lpop(key: String): Response<String?> = add(_lpop(key))

    override suspend fun lpop(key: String, count: Long): Response<List<String>?> =
        add(_lpop(key, count)).asReturnType()

    override suspend fun lpush(key: String, element: String, vararg elements: String): Response<Long> =
        add(_lpush(key, element, elements))

    override suspend fun lpushx(key: String, element: String, vararg elements: String): Response<Long> =
        add(_lpushx(key, element, *elements))

    override suspend fun lrange(key: String, start: Int, stop: Int): Response<List<String>> =
        add(_lrange(key, start, stop), false).asReturnType()

    override suspend fun lrem(key: String, count: Int, element: String): Response<Long> =
        add(_lrem(key, count, element))

    override suspend fun lset(key: String, index: Int, element: String): Response<String> =
        add(_lset(key, index, element))

    override suspend fun ltrim(key: String, start: Int, stop: Int): Response<String> =
        add(_ltrim(key, start, stop))

    override suspend fun rpop(key: String): Response<String?> =
        add(_rpop(key))

    override suspend fun rpop(key: String, count: Int): Response<List<String>?> =
        add(_rpop(key, count)).asReturnType()

    override suspend fun rpush(key: String, element: String, vararg elements: String): Response<Long> =
        add(_rpush(key, element, *elements))

    override suspend fun rpushx(key: String, element: String, vararg elements: String): Response<Long> =
        add(_rpushx(key, element, *elements))
}