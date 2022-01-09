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

import io.github.crackthecodeabhi.kreds.args.Argument
import io.github.crackthecodeabhi.kreds.protocol.ICommandProcessor

internal interface Command {
    /**
     * Command string
     */
    val string: String

    /**
     * A sub command
     */
    val subCommand: Command?
}

internal class CommandExecution<T>(val command: Command, val processor: ICommandProcessor<T>, vararg val args: Argument)


/**
 * Operations block the connection until the operation completes or operation timeouts.
 * */
public interface BlockingOperation