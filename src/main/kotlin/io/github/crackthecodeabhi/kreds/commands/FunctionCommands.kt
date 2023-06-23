/*
 *  Copyright (C) 2023 Abhijith Shivaswamy
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

import io.github.crackthecodeabhi.kreds.args.EmptyArgument
import io.github.crackthecodeabhi.kreds.args.SyncOption
import io.github.crackthecodeabhi.kreds.args.createArguments
import io.github.crackthecodeabhi.kreds.args.toArgument
import io.github.crackthecodeabhi.kreds.commands.FunctionCommand.FUNCTION_DELETE
import io.github.crackthecodeabhi.kreds.commands.FunctionCommand.FUNCTION_FLUSH
import io.github.crackthecodeabhi.kreds.commands.FunctionCommand.FUNCTION_KILL
import io.github.crackthecodeabhi.kreds.commands.FunctionCommand.FUNCTION_LIST
import io.github.crackthecodeabhi.kreds.commands.FunctionSubCommand.DELETE
import io.github.crackthecodeabhi.kreds.commands.FunctionSubCommand.FLUSH
import io.github.crackthecodeabhi.kreds.commands.FunctionSubCommand.KILL
import io.github.crackthecodeabhi.kreds.commands.FunctionSubCommand.LIST
import io.github.crackthecodeabhi.kreds.commands.FunctionSubCommand.LOAD
import io.github.crackthecodeabhi.kreds.protocol.AllCommandProcessor
import io.github.crackthecodeabhi.kreds.protocol.ArrayCommandProcessor
import io.github.crackthecodeabhi.kreds.protocol.BulkStringCommandProcessor
import io.github.crackthecodeabhi.kreds.protocol.CommandExecutor
import io.github.crackthecodeabhi.kreds.protocol.SimpleStringCommandProcessor

internal enum class FunctionCommand(
    override val subCommand: Command?,
    override val string: String = "FUNCTION"
) : Command {
    FCALL(null, "FCALL"),
    FCALL_RO(null, "FCALL_RO"),
    FUNCTION_DELETE(DELETE),
    FUNCTION_FLUSH(FLUSH),
    FUNCTION_KILL(KILL),
    FUNCTION_LIST(LIST),
    FUNCTION_LOAD(LOAD);
}

internal enum class FunctionSubCommand(override val subCommand: Command? = null) : Command {
    DELETE, FLUSH, KILL, LIST, LOAD;

    override val string = name
}

internal interface BaseFunctionCommands {
    fun _fcall(function: String, keys: Array<String>, args: Array<String>, readOnly: Boolean) = CommandExecution(
        if (readOnly) FunctionCommand.FCALL_RO else FunctionCommand.FCALL,
        AllCommandProcessor,
        function.toArgument(),
        keys.size.toArgument(),
        *createArguments(*keys),
        *createArguments(*args)
    )

    fun _functionDelete(libraryName: String) =
        CommandExecution(FUNCTION_DELETE, SimpleStringCommandProcessor, libraryName.toArgument())

    fun _functionFlush(sync: SyncOption) =
        CommandExecution(FUNCTION_FLUSH, SimpleStringCommandProcessor, sync)

    fun _functionKill() = CommandExecution(FUNCTION_KILL, SimpleStringCommandProcessor)

    fun _functionList(libraryName: String?, withCode: Boolean) = CommandExecution(
        FUNCTION_LIST,
        ArrayCommandProcessor,
        libraryName.toArgument(),
        if (withCode) "WITHCODE".toArgument() else EmptyArgument
    )

    fun _functionLoad(replace: Boolean, functionCode: String) = CommandExecution(
        FunctionCommand.FUNCTION_LOAD,
        BulkStringCommandProcessor,
        if (replace) "REPLACE".toArgument() else EmptyArgument,
        functionCode.toArgument()
    )
}

public interface FunctionCommands {
    public suspend fun fcall(
        function: String,
        keys: Array<String>,
        args: Array<String>,
        readOnly: Boolean = false
    ): Any?

    public suspend fun functionDelete(libraryName: String): String

    public suspend fun functionFlush(sync: SyncOption): String

    public suspend fun functionKill(): String

    public suspend fun functionLoad(replace: Boolean, functionCode: String): String
}

internal interface FunctionCommandExecutor : CommandExecutor, FunctionCommands, BaseFunctionCommands {
    override suspend fun fcall(function: String, keys: Array<String>, args: Array<String>, readOnly: Boolean) =
        execute(_fcall(function, keys, args, readOnly))

    override suspend fun functionDelete(libraryName: String): String = execute(_functionDelete(libraryName))

    override suspend fun functionFlush(sync: SyncOption): String = execute(_functionFlush(sync))

    override suspend fun functionKill(): String = execute(_functionKill())

    override suspend fun functionLoad(replace: Boolean, functionCode: String): String =
        execute(_functionLoad(replace, functionCode)).responseTo()
}
