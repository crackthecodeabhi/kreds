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

import io.github.crackthecodeabhi.kreds.args.ExpireOption
import io.github.crackthecodeabhi.kreds.args.PExpireOption
import io.github.crackthecodeabhi.kreds.pipeline.QueuedCommand
import io.github.crackthecodeabhi.kreds.pipeline.Response

public interface PipelineKeyCommands {
    /**
     * @see [KeyCommands.del]
     */
    public suspend fun del(vararg keys: String): Response<Long>

    /**
     * @see [KeyCommands.copy]
     */
    public suspend fun copy(
        source: String,
        destination: String,
        destinationDb: String? = null,
        replace: Boolean? = null
    ): Response<Long>

    /**
     * @see [KeyCommands.dump]
     */
    public suspend fun dump(key: String): Response<String?>

    /**
     * @see [KeyCommands.exists]
     */
    public suspend fun exists(vararg keys: String): Response<Long>

    /**
     * @see [KeyCommands.expire]
     */
    public suspend fun expire(key: String, seconds: ULong, expireOption: ExpireOption? = null): Response<Long>

    /**
     * @see [KeyCommands.expireAt]
     */
    public suspend fun expireAt(key: String, timestamp: ULong, expireOption: ExpireOption? = null): Response<Long>

    /**
     * @see [KeyCommands.expireTime]
     */
    public suspend fun expireTime(key: String): Response<Long>

    /**
     * @see [KeyCommands.keys]
     */
    public suspend fun keys(pattern: String): Response<List<String>>

    /**
     * @see [KeyCommands.move]
     */
    public suspend fun move(key: String, db: String): Response<Long>

    /**
     * @see [KeyCommands.persist]
     */
    public suspend fun persist(key: String): Response<Long>

    /**
     * @see [KeyCommands.pexpire]
     */
    public suspend fun pexpire(key: String, milliseconds: ULong, expireOption: PExpireOption? = null): Response<Long>

    /**
     * @see [KeyCommands.pexpireat]
     */
    public suspend fun pexpireat(
        key: String,
        millisecondsTimestamp: ULong,
        expireOption: PExpireOption? = null /* = org.kreds.ExpireOption? */
    ): Response<Long>

    /**
     * @see [KeyCommands.pexpiretime]
     */
    public suspend fun pexpiretime(key: String): Response<Long>

    /**
     * @see [KeyCommands.pttl]
     */
    public suspend fun pttl(key: String): Response<Long>

    /**
     * @see [KeyCommands.randomKey]
     */
    public suspend fun randomKey(): Response<String?>

    /**
     * @see [KeyCommands.rename]
     */
    public suspend fun rename(key: String, newKey: String): Response<String>

    /**
     * @see [KeyCommands.renamenx]
     */
    public suspend fun renamenx(key: String, newKey: String): Response<Long>

    /**
     * @see [KeyCommands.touch]
     */
    public suspend fun touch(vararg keys: String): Response<Long>

    /**
     * @see [KeyCommands.ttl]
     */
    public suspend fun ttl(key: String): Response<Long>

    /**
     * @see [KeyCommands.type]
     */
    public suspend fun type(key: String): Response<String>

    /**
     * @see [KeyCommands.unlink]
     */
    public suspend fun unlink(vararg keys: String): Response<Long>

    /**
     * @see [KeyCommands.scan]
     */
    public suspend fun scan(
        cursor: Long,
        matchPattern: String? = null,
        count: Long? = null,
        type: String? = null
    ): Response<IScanResult<String>>
}

internal interface PipelineKeyCommandExecutor : QueuedCommand, PipelineKeyCommands, BaseKeyCommands {

    override suspend fun del(vararg keys: String): Response<Long> = add(_del(*keys))

    override suspend fun copy(
        source: String,
        destination: String,
        destinationDb: String?,
        replace: Boolean?
    ): Response<Long> =
        add(_copy(source, destination, destinationDb, replace))

    override suspend fun dump(key: String): Response<String?> = add(_dump(key))

    override suspend fun exists(vararg keys: String): Response<Long> = add(_exists(*keys))

    override suspend fun expire(key: String, seconds: ULong, expireOption: ExpireOption?): Response<Long> =
        add(_expire(key, seconds, expireOption))

    override suspend fun expireAt(key: String, timestamp: ULong, expireOption: ExpireOption?): Response<Long> =
        add(_expireAt(key, timestamp, expireOption))

    override suspend fun expireTime(key: String): Response<Long> =
        add(_expireTime(key))

    override suspend fun keys(pattern: String): Response<List<String>> =
        add(_keys(pattern), false).asReturnType()

    override suspend fun move(key: String, db: String): Response<Long> =
        add(_move(key, db))

    override suspend fun persist(key: String): Response<Long> =
        add(_persist(key))

    override suspend fun pexpire(key: String, milliseconds: ULong, expireOption: PExpireOption?): Response<Long> =
        add(_pexpire(key, milliseconds, expireOption))

    override suspend fun pexpireat(
        key: String,
        millisecondsTimestamp: ULong,
        expireOption: PExpireOption?
    ): Response<Long> =
        add(_pexpireat(key, millisecondsTimestamp, expireOption))

    override suspend fun pexpiretime(key: String): Response<Long> =
        add(_pexpiretime(key))

    override suspend fun pttl(key: String): Response<Long> =
        add(_pttl(key))

    override suspend fun randomKey(): Response<String?> =
        add(_randomKey())

    override suspend fun rename(key: String, newKey: String): Response<String> =
        add(_rename(key, newKey))

    override suspend fun renamenx(key: String, newKey: String): Response<Long> =
        add(_renamenx(key, newKey))

    override suspend fun touch(vararg keys: String): Response<Long> =
        add(_touch(*keys))

    override suspend fun ttl(key: String): Response<Long> =
        add(_ttl(key))

    override suspend fun type(key: String): Response<String> =
        add(_type(key))

    override suspend fun unlink(vararg keys: String): Response<Long> =
        add(_unlink(*keys))

    override suspend fun scan(
        cursor: Long,
        matchPattern: String?,
        count: Long?,
        type: String?
    ): Response<IScanResult<String>> =
        add(_scan(cursor, matchPattern, count, type))
}