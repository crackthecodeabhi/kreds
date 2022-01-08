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

import io.github.crackthecodeabhi.kreds.pipeline.QueuedCommand
import io.github.crackthecodeabhi.kreds.pipeline.Response

public interface PipelineSetCommands {
    /**
     * @see [SetCommands.sadd]
     */
    public suspend fun sadd(key: String, member: String, vararg members: String): Response<Long>

    /**
     * @see [SetCommands.scard]
     */
    public suspend fun scard(key: String): Response<Long>

    /**
     * @see [SetCommands.sdiff]
     */
    public suspend fun sdiff(key: String, vararg keys: String): Response<List<String>>

    /**
     * @see [SetCommands.sdiffstore]
     */
    public suspend fun sdiffstore(destination: String, key: String, vararg keys: String): Response<Long>

    /**
     * @see [SetCommands.sinter]
     */
    public suspend fun sinter(key: String, vararg keys: String): Response<List<String>>

    /**
     * @see [SetCommands.sintercard]
     */
    public suspend fun sintercard(numkeys: Int, key: String, vararg keys: String, limit: Long?): Response<Long>

    /**
     * @see [SetCommands.sinterstore]
     */
    public suspend fun sinterstore(destination: String, key: String, vararg keys: String): Response<Long>

    /**
     * @see [SetCommands.sismember]
     */
    public suspend fun sismember(key: String, member: String): Response<Long>

    /**
     * @see [SetCommands.smembers]
     */
    public suspend fun smembers(key: String): Response<List<String>>

    /**
     * @see [SetCommands.smismember]
     */
    public suspend fun smismember(key: String, member: String, vararg members: String): Response<List<Long>>

    /**
     * @see [SetCommands.smove]
     */
    public suspend fun smove(source: String, destination: String, member: String): Response<Long>

    /**
     * @see [SetCommands.spop]
     */
    public suspend fun spop(key: String): Response<String?>

    /**
     * @see [SetCommands.spop]
     */
    public suspend fun spop(key: String, count: Int): Response<List<String>>

    /**
     * @see [SetCommands.srandmember]
     */
    public suspend fun srandmember(key: String): Response<String?>

    /**
     * @see [SetCommands.srandmember]
     */
    public suspend fun srandmember(key: String, count: Int): Response<List<String>>

    /**
     * @see [SetCommands.srem]
     */
    public suspend fun srem(key: String, member: String, vararg members: String): Response<Long>

    /**
     * @see [SetCommands.sunion]
     */
    public suspend fun sunion(key: String, vararg keys: String): Response<List<String>>

    /**
     * @see [SetCommands.sunionstore]
     */
    public suspend fun sunionstore(destination: String, key: String, vararg keys: String): Response<Long>

}

internal interface PipelineSetCommandExecutor : BaseSetCommands, PipelineSetCommands, QueuedCommand {
    override suspend fun sadd(key: String, member: String, vararg members: String): Response<Long> =
        add(_sadd(key, member, *members))

    override suspend fun scard(key: String): Response<Long> =
        add(_scard(key))

    override suspend fun sdiff(key: String, vararg keys: String): Response<List<String>> =
        add(_sdiff(key, *keys), false).asReturnType()

    override suspend fun sdiffstore(destination: String, key: String, vararg keys: String): Response<Long> =
        add(_sdiffstore(destination, key, *keys))

    override suspend fun sinter(key: String, vararg keys: String): Response<List<String>> =
        add(_sinter(key, *keys), false).asReturnType()

    override suspend fun sintercard(numkeys: Int, key: String, vararg keys: String, limit: Long?): Response<Long> =
        add(_sintercard(numkeys, key, *keys, limit = limit))

    override suspend fun sinterstore(destination: String, key: String, vararg keys: String): Response<Long> =
        add(_sinterstore(destination, key, *keys))

    override suspend fun sismember(key: String, member: String): Response<Long> =
        add(_sismember(key, member))

    override suspend fun smembers(key: String): Response<List<String>> =
        add(_smembers(key), false).asReturnType()

    override suspend fun smismember(key: String, member: String, vararg members: String): Response<List<Long>> =
        add(_smismember(key, member, *members), false).asReturnType()

    override suspend fun smove(source: String, destination: String, member: String): Response<Long> =
        add(_smove(source, destination, member))

    override suspend fun spop(key: String): Response<String?> =
        add((_spop(key)))

    override suspend fun spop(key: String, count: Int): Response<List<String>> =
        add(_spop(key, count), false).asReturnType()

    override suspend fun srandmember(key: String): Response<String?> =
        add(_srandmember(key))

    override suspend fun srandmember(key: String, count: Int): Response<List<String>> =
        add(_srandmember(key, count), false).asReturnType()

    override suspend fun srem(key: String, member: String, vararg members: String): Response<Long> =
        add(_srem(key, member, *members))

    override suspend fun sunion(key: String, vararg keys: String): Response<List<String>> =
        add(_sunion(key, *keys), false).asReturnType()

    override suspend fun sunionstore(destination: String, key: String, vararg keys: String): Response<Long> =
        add(_sunionstore(destination, key, *keys))
}