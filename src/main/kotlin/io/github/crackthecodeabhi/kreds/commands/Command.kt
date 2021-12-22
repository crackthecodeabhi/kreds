package io.github.crackthecodeabhi.kreds.commands

import io.github.crackthecodeabhi.kreds.args.Argument
import io.github.crackthecodeabhi.kreds.protocol.ICommandProcessor

interface Command{
    /**
     * Command string
     */
    val string: String

    /**
     * A sub command
     */
    val subCommand: Command?
}

class CommandExecution(val command: Command, val processor: ICommandProcessor, vararg val args: Argument)

