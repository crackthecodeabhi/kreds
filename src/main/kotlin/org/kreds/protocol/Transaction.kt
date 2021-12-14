package org.kreds.protocol

import org.kreds.commands.*
import org.kreds.connection.KredsClient
import org.kreds.protocol.TransactionCommand.*

enum class TransactionCommand: Command {
    MULTI, EXEC;

    private val command = name.replace('_', ' ')
    override val string: String = command
}

interface TransactionExecutor{
    suspend fun executeTransaction(commands: List<CommandExecution>): List<Any?>
}

interface Transaction: PipelineKeyCommands,PipelineStringCommands{
    suspend fun exec(): List<Any?>
}

class TransactionImpl(private val client: KredsClient): Transaction,PipelineStringCommandsExecutor,PipelineKeyCommandExecutor{

    private val commands = mutableListOf(CommandExecution(MULTI, SimpleStringCommandProcessor))
    private val commandResponse = mutableListOf<Any?>()

    override fun <T> add(commandExecution: CommandExecution): Response<T> {
        commands.add(commandExecution)
        //MULTI command response won't be available in response list. EXEC response will be list of response of command executed.
        // lastIndex will never be -1 because MULTI and above commands.add(), this tiny adjustment is for this reason.
        return Response(commandResponse,commands.lastIndex - 1)
    }

    override suspend fun exec(): List<Any?> {
        commands.add(CommandExecution(EXEC, ArrayCommandProcessor))
        commandResponse.addAll(client.executeTransaction(commands))
        return commandResponse
    }
}