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

import io.github.crackthecodeabhi.kreds.KredsException
import io.github.crackthecodeabhi.kreds.args.EmptyArgument
import io.github.crackthecodeabhi.kreds.args.ServerInfoSection
import io.github.crackthecodeabhi.kreds.args.SyncOption
import io.github.crackthecodeabhi.kreds.commands.ServerCommand.*
import io.github.crackthecodeabhi.kreds.protocol.BulkStringCommandProcessor
import io.github.crackthecodeabhi.kreds.protocol.CommandExecutor
import io.github.crackthecodeabhi.kreds.protocol.ICommandProcessor
import io.github.crackthecodeabhi.kreds.protocol.SimpleStringCommandProcessor
import io.netty.handler.codec.redis.RedisMessage

internal enum class ServerCommand(override val subCommand: Command? = null) : Command {
    FLUSHALL, FLUSHDB, INFO;

    override val string: String = name
}

internal interface BaseServerCommands {
    fun _flushAll(syncOption: SyncOption? = null) =
        CommandExecution(FLUSHALL, SimpleStringCommandProcessor, syncOption ?: EmptyArgument)

    fun _flushDb(syncOption: SyncOption? = null) =
        CommandExecution(FLUSHDB, SimpleStringCommandProcessor, syncOption ?: EmptyArgument)

    fun _serverVersion() = CommandExecution(INFO, ServerVersionProcessor, ServerInfoSection.server)

    fun _info(section: ServerInfoSection? = null) =
        CommandExecution(INFO, BulkStringCommandProcessor, section ?: EmptyArgument)
}

internal object ServerVersionProcessor : ICommandProcessor<String> {
    private val versionRegex = """redis_version[\s]*:[\s]*([0-9.]*)""".toRegex()
    override fun decode(message: RedisMessage): String {
        val info: String = BulkStringCommandProcessor.decode(message)
            ?: throw KredsException("Failed to retrieve Server Info section.")
        val matchResult = versionRegex.find(info)
        val (versionString) = matchResult?.destructured
            ?: throw KredsException("Failed to find version info from Server Info section.")
        return versionString
    }
}

public interface ServerCommands {

    /**
     * ### ` FLUSHALL [ASYNC|SYNC] `
     *
     * [Doc](https://redis.io/commands/flushall)
     * @since 1.0.0
     * @return String reply.
     */
    public suspend fun flushAll(syncOption: SyncOption? = null): String

    /**
     * ### ` FLUSHDB [ASYNC|SYNC] `
     *
     * [Doc](https://redis.io/commands/flushdb)
     * @since 1.0.0
     * @return String reply
     */
    public suspend fun flushDb(syncOption: SyncOption? = null): String

    /**
     * Parses version info from `INFO server` section and returns the redis version.
     *
     * @throws KredsException on failure to extract the version info.
     * @return Server version string.
     */
    public suspend fun serverVersion(): String

    /**
     * ### ` INFO [section] `
     *
     * [Doc](https://redis.io/commands/info)
     * @since 1.0.0
     * @return a string reply as a collection of text lines.
     */
    public suspend fun info(section: ServerInfoSection? = null): String?
}

internal interface ServerCommandExecutor : BaseServerCommands, ServerCommands, CommandExecutor {
    override suspend fun flushAll(syncOption: SyncOption?): String =
        execute(_flushAll(syncOption))

    override suspend fun flushDb(syncOption: SyncOption?): String =
        execute(_flushDb(syncOption))

    override suspend fun serverVersion(): String =
        execute(_serverVersion())

    override suspend fun info(section: ServerInfoSection?): String? =
        execute(_info(section))
}