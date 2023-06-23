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
import io.github.crackthecodeabhi.kreds.commands.FunctionSubCommand.DELETE
import io.github.crackthecodeabhi.kreds.commands.FunctionSubCommand.FLUSH
import io.github.crackthecodeabhi.kreds.commands.FunctionSubCommand.KILL
import io.github.crackthecodeabhi.kreds.commands.FunctionSubCommand.LOAD
import io.github.crackthecodeabhi.kreds.protocol.AllCommandProcessor
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
    FUNCTION_LOAD(LOAD);
}

internal enum class FunctionSubCommand(override val subCommand: Command? = null) : Command {
    DELETE, FLUSH, KILL, LOAD;

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

    fun _functionLoad(replace: Boolean, functionCode: String) = CommandExecution(
        FunctionCommand.FUNCTION_LOAD,
        BulkStringCommandProcessor,
        if (replace) "REPLACE".toArgument() else EmptyArgument,
        functionCode.toArgument()
    )
}

public interface FunctionCommands {
    /**
     * ### `FCALL function numkeys [key [key ...]] [arg [arg ...]]`
     * Invokes a function.
     *
     * [Doc](https://redis.io/commands/fcall/)
     * @since 7.0.0
     * @param readOnly Whether to allow commands that modify data
     * @return Value returned by executed function
     */
    public suspend fun fcall(
        function: String,
        keys: Array<String>,
        args: Array<String>,
        readOnly: Boolean = false
    ): Any?

    /**
     * ### `FUNCTION DELETE library-name`
     * Delete a library and all its functions.
     *
     * [Doc](https://redis.io/commands/function-delete/)
     * @since 7.0.0
     */
    public suspend fun functionDelete(libraryName: String)

    /**
     * ### `FUNCTION FLUSH [ASYNC | SYNC]`
     * Deletes all the libraries.
     *
     * [Doc](https://redis.io/commands/function-flush/)
     * @since 7.0.0
     */
    public suspend fun functionFlush(sync: SyncOption)

    /**
     * ### `FUNCTION KILL`
     * Kill a function that is currently executing.
     *
     * [Doc](https://redis.io/commands/function-kill/)
     * @since 7.0.0
     */
    public suspend fun functionKill()

    /**
     * ### `FUNCTION LOAD \[REPLACE] function-code`
     * Loads a library to Redis.
     *
     * [Doc](https://redis.io/commands/function-load/)
     * @since 7.0.0
     * @return The library name that was loaded
     */
    public suspend fun functionLoad(replace: Boolean, functionCode: String): String
}

internal interface FunctionCommandExecutor : CommandExecutor, FunctionCommands, BaseFunctionCommands {
    override suspend fun fcall(function: String, keys: Array<String>, args: Array<String>, readOnly: Boolean) =
        execute(_fcall(function, keys, args, readOnly))

    override suspend fun functionDelete(libraryName: String) {
        execute(_functionDelete(libraryName))
    }

    override suspend fun functionFlush(sync: SyncOption) {
        execute(_functionFlush(sync))
    }

    override suspend fun functionKill() {
        execute(_functionKill())
    }

    override suspend fun functionLoad(replace: Boolean, functionCode: String): String =
        execute(_functionLoad(replace, functionCode)).responseTo()
}
