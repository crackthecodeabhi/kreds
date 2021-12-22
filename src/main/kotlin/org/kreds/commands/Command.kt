package org.kreds.commands

import org.kreds.args.Argument
import org.kreds.protocol.ICommandProcessor

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

