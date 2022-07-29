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

import io.github.crackthecodeabhi.kreds.args.SyncOption
import io.github.crackthecodeabhi.kreds.args.createArguments
import io.github.crackthecodeabhi.kreds.args.toArgument
import io.github.crackthecodeabhi.kreds.protocol.*

private const val SCRIPT_COMMAND = "SCRIPT"

internal enum class ScriptingCommand(override val subCommand: Command? = null, commandString: String? = null) :
    Command {
    EVAL, EVAL_RO, EVALSHA, EVALSHA_RO,
    SCRIPT_EXISTS(ScriptCommand.EXISTS, SCRIPT_COMMAND),
    SCRIPT_FLUSH(ScriptCommand.FLUSH, SCRIPT_COMMAND),
    SCRIPT_KILL(ScriptCommand.KILL, SCRIPT_COMMAND),
    SCRIPT_LOAD(ScriptCommand.LOAD, SCRIPT_COMMAND);

    override val string = commandString ?: name
}

internal enum class ScriptCommand(override val subCommand: Command? = null) : Command {
    EXISTS, FLUSH, KILL, LOAD;

    override val string = name
}

internal interface BaseScriptingCommands {
    fun _eval(script: String, keys: Array<String>, args: Array<String>, readOnly: Boolean) =
        CommandExecution(
            if (readOnly) ScriptingCommand.EVAL_RO else ScriptingCommand.EVAL,
            AllCommandProcessor,
            script.toArgument(),
            keys.size.toArgument(),
            *createArguments(*keys),
            *createArguments(*args)
        )

    fun _evalSha(hash: String, keys: Array<String>, args: Array<String>, readOnly: Boolean) =
        CommandExecution(
            if (readOnly) ScriptingCommand.EVALSHA_RO else ScriptingCommand.EVALSHA,
            AllCommandProcessor,
            hash.toArgument(),
            keys.size.toArgument(),
            *createArguments(*keys),
            *createArguments(*args)
        )

    fun _scriptExists(hashes: Array<String>) =
        CommandExecution(
            ScriptingCommand.SCRIPT_EXISTS,
            ArrayCommandProcessor,
            *createArguments(*hashes)
        )

    fun _scriptFlush(syncOption: SyncOption) =
        CommandExecution(
            ScriptingCommand.SCRIPT_FLUSH,
            SimpleStringCommandProcessor,
            syncOption
        )

    fun _scriptKill() =
        CommandExecution(
            ScriptingCommand.SCRIPT_KILL,
            SimpleStringCommandProcessor
        )

    fun _scriptLoad(script: String) =
        CommandExecution(
            ScriptingCommand.SCRIPT_LOAD,
            BulkStringCommandProcessor,
            script.toArgument()
        )
}

public interface ScriptingCommands {
    /**
     * ### ` EVAL/EVAL_RO script numkeys [key [key ...]] [arg [arg ...]] `
     *
     * Invoke the execution of a server-side Lua script.
     * The first argument is the script's source code.
     * Scripts are written in Lua and executed by the embedded Lua 5.1 interpreter in Redis.
     *
     * The second argument is the number of input key name arguments, followed by all the
     * keys accessed by the script. These names of input keys are available to the script
     * as the KEYS global runtime variable Any additional input arguments should not represent
     * names of keys.
     *
     * Important: to ensure the correct execution of scripts, both in standalone and clustered
     * deployments, all names of keys that a script accesses must be explicitly provided as
     * input key arguments. The script should only access keys whose names are given as input
     * arguments. Scripts should never access keys with programmatically-generated names or
     * based on the contents of data structures stored in the database.
     *
     * Please refer to the Redis Programmability and Introduction to Eval Scripts for more information about Lua scripts.
     *
     * [Doc](https://redis.io/commands/eval/)
     * @since 2.6.0
     * @param readOnly Whether to allow commands that modify data
     * @return Value returned by the executed script
     */
    public suspend fun eval(script: String, keys: Array<String>, args: Array<String>, readOnly: Boolean = false): Any?

    /**
     * ### ` EVALSHA/EVALSHA_RO sha1 numkeys [key [key ...]] [arg [arg ...]] `
     *
     * Evaluate a script from the server's cache by its SHA1 digest.
     * The server caches scripts by using the SCRIPT LOAD command. The command is otherwise identical to EVAL.
     *
     * Please refer to the Redis Programmability and Introduction to Eval Scripts for more information about Lua scripts.
     *
     * [Doc](https://redis.io/commands/evalsha/)
     * @since 2.6.0
     * @return Value returned by the executed script
     */
    public suspend fun evalSha(hash: String, keys: Array<String>, args: Array<String>, readOnly: Boolean = false): Any?

    /**
     * ### ` SCRIPT EXISTS sha1 [sha1 ...] `
     *
     * Returns information about the existence of the scripts in the script cache.
     * This command accepts one or more SHA1 digests and returns a list of ones or
     * zeros to signal if the scripts are already defined or not inside the script
     * cache. This can be useful before a pipelining operation to ensure that scripts
     * are loaded (and if not, to load them using SCRIPT LOAD) so that the pipelining
     * operation can be performed solely using EVALSHA instead of EVAL to save bandwidth.
     *
     * For more information about EVAL scripts please refer to Introduction to Eval Scripts.
     *
     * [Doc](https://redis.io/commands/script-exists/)
     * @since 2.6.0
     * @return An array of booleans that correspond to the specified SHA1 digest arguments.
     * For every corresponding SHA1 digest of a script that actually exists in the script cache, true is returned, otherwise false is returned.
     */
    public suspend fun scriptExists(hashes: Array<String>): List<Boolean>

    /**
     * ### ` SCRIPT EXISTS sha1 [sha1 ...] `
     *
     * Returns information about the existence of the scripts in the script cache.
     * This command accepts one or more SHA1 digests and returns a list of ones or
     * zeros to signal if the scripts are already defined or not inside the script
     * cache. This can be useful before a pipelining operation to ensure that scripts
     * are loaded (and if not, to load them using SCRIPT LOAD) so that the pipelining
     * operation can be performed solely using EVALSHA instead of EVAL to save bandwidth.
     *
     * For more information about EVAL scripts please refer to Introduction to Eval Scripts.
     *
     * [Doc](https://redis.io/commands/script-exists/)
     * @since 2.6.0
     * @return An array of booleans that correspond to the specified SHA1 digest arguments.
     * For every corresponding SHA1 digest of a script that actually exists in the script cache, true is returned, otherwise false is returned.
     */
    public suspend fun scriptExists(hash: String, vararg hashes: String): List<Boolean>

    /**
     * ### ` SCRIPT FLUSH [ASYNC | SYNC] `
     *
     * Flush the Lua scripts cache.
     * By default, SCRIPT FLUSH will synchronously flush the cache. Starting with Redis 6.2,
     * setting the lazyfree-lazy-user-flush configuration directive to "yes" changes the default flush mode to asynchronous.
     *
     * It is possible to use one of the following modifiers to dictate the flushing mode explicitly:
     * - ASYNC: flushes the cache asynchronously
     * - SYNC: flushes the cache synchronously
     *
     * For more information about EVAL scripts please refer to Introduction to Eval Scripts.
     *
     * [Doc](https://redis.io/commands/script-flush/)
     * @since 2.6.0
     */
    public suspend fun scriptFlush(syncOption: SyncOption)

    /**
     * ### ` SCRIPT KILL `
     *
     * Kills the currently executing EVAL script, assuming no write operation was yet performed by the script.
     * This command is mainly useful to kill a script that is running for too much time(for instance, because it entered
     * an infinite loop because of a bug). The script will be killed, and the client currently blocked into
     * EVAL will see the command returning with an error.
     *
     * If the script has already performed write operations, it can not be killed in this way because it would violate Lua's
     * script atomicity contract. In such a case, only SHUTDOWN NOSAVE can kill the script, killing the Redis process
     * in a hard way and preventing it from persisting with half-written information.
     *
     * For more information about EVAL scripts please refer to Introduction to Eval Scripts.
     *
     * [Doc](https://redis.io/commands/script-kill/)
     * @since 2.6.0
     */
    public suspend fun scriptKill()

    /**
     * ### ` SCRIPT LOAD script `
     *
     * Load a script into the scripts cache, without executing it. After the specified command is loaded into
     * the script cache it will be callable using EVALSHA with the correct SHA1 digest of the script, exactly
     * like after the first successful invocation of EVAL.
     *
     * The script is guaranteed to stay in the script cache forever (unless SCRIPT FLUSH is called).
     * The command works in the same way even if the script was already present in the script cache.
     *
     * For more information about EVAL scripts please refer to Introduction to Eval Scripts.
     *
     * [Doc](https://redis.io/commands/script-load/)
     * @since 2.6.0
     * @return SHA1 digest of the script added into the script cache.
     */
    public suspend fun scriptLoad(script: String): String
}

internal interface ScriptingCommandExecutor : BaseScriptingCommands, ScriptingCommands, CommandExecutor {
    override suspend fun eval(script: String, keys: Array<String>, args: Array<String>, readOnly: Boolean): Any? =
        execute(_eval(script, keys, args, readOnly))

    override suspend fun evalSha(hash: String, keys: Array<String>, args: Array<String>, readOnly: Boolean): Any? =
        execute(_evalSha(hash, keys, args, readOnly))

    override suspend fun scriptExists(hashes: Array<String>): List<Boolean> =
        execute(_scriptExists(hashes))!!.map { it == 1L }

    override suspend fun scriptExists(hash: String, vararg hashes: String): List<Boolean> =
        scriptExists(arrayOf(hash, *hashes))

    override suspend fun scriptFlush(syncOption: SyncOption) {
        execute(_scriptFlush(syncOption))
    }

    override suspend fun scriptKill() {
        execute(_scriptKill())
    }

    override suspend fun scriptLoad(script: String): String =
        execute(_scriptLoad(script)).responseTo()
}
