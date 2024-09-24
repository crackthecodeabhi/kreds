import io.github.crackthecodeabhi.kreds.args.createArguments
import io.github.crackthecodeabhi.kreds.commands.Command
import io.github.crackthecodeabhi.kreds.commands.CommandExecution
import io.github.crackthecodeabhi.kreds.commands.MapElementProcessor
import io.github.crackthecodeabhi.kreds.commands.asReturnType
import io.github.crackthecodeabhi.kreds.protocol.CommandExecutor
import io.github.crackthecodeabhi.kreds.protocol.SimpleStringCommandProcessor

/*
 *  Copyright (C) 2024 Abhijith Shivaswamy
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
internal enum class ConfigCommand(
    override val subCommand: Command? = null,
    override val string: String = "CONFIG"
) : Command {
    CONFIG_SET(ConfigSubCommand.SET),
    CONFIG_GET(ConfigSubCommand.GET),
}

internal enum class ConfigSubCommand(override val subCommand: Command? = null) : Command {
    SET, GET;

    override val string = name
}

public interface ConfigCommands {

    /**
     * ###  Reconfigure the server at run time without the need to restart Redis.
     *
     * [Doc](https://redis.io/commands/config-set)
     * @since 1.0.0
     * @return `OK` if the operation was successful.
     */
    public suspend fun configSet(key: String, value: String, vararg params: Pair<String, String>): String?

    /**
     * ###  Read the configuration parameters of a running Redis server.
     *
     * [Doc](https://redis.io/commands/config-get)
     * @since 1.0.0
     * @return [Map]
     */
    public suspend fun configGet(key: String, vararg keys: String): Map<String, String>

}

internal interface BaseConfigCommands {

    fun _configSet(key: String, value: String, vararg params: Pair<String, String>) = CommandExecution(
        ConfigCommand.CONFIG_SET, SimpleStringCommandProcessor, *createArguments(
            key, value, *params
        )
    )

    fun _configGet(key: String, vararg params: String) = CommandExecution(
        ConfigCommand.CONFIG_GET, MapElementProcessor, *createArguments(
            key, *params
        )
    )

}

internal interface ConfigCommandExecutor : CommandExecutor, ConfigCommands, BaseConfigCommands {
    override suspend fun configSet(key: String, value: String, vararg params: Pair<String, String>): String? = execute(
        _configSet(key, value, *params)
    )

    override suspend fun configGet(key: String, vararg keys: String): Map<String, String> = execute(
        _configGet(key, *keys)
    ).asReturnType()
}