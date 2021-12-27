/*
 *  Copyright (C) 2021 Abhijith Shivaswamy
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

import io.github.crackthecodeabhi.kreds.args.SyncOption
import io.github.crackthecodeabhi.kreds.commands.ServerCommand.*
import io.github.crackthecodeabhi.kreds.protocol.CommandExecutor
import io.github.crackthecodeabhi.kreds.protocol.SimpleStringCommandProcessor

internal enum class ServerCommand(override val subCommand: Command? = null) : Command {
    FLUSHALL, FLUSHDB;

    override val string: String = name
}

internal interface BaseServerCommands {
    fun _flushAll(syncOption: SyncOption? = null) =
        syncOption?.let {
            CommandExecution(FLUSHALL, SimpleStringCommandProcessor, it)
        } ?: CommandExecution(FLUSHALL, SimpleStringCommandProcessor)

    fun _flushDb(syncOption: SyncOption? = null) =
        syncOption?.let {
            CommandExecution(FLUSHDB, SimpleStringCommandProcessor, it)
        } ?: CommandExecution(FLUSHDB, SimpleStringCommandProcessor)
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
}

internal interface ServerCommandExecutor : BaseServerCommands, ServerCommands, CommandExecutor {
    override suspend fun flushAll(syncOption: SyncOption?): String =
        execute(_flushAll(syncOption))

    override suspend fun flushDb(syncOption: SyncOption?): String =
        execute(_flushDb(syncOption))
}