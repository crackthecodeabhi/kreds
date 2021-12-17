package org.kreds.protocol

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.kreds.commands.*
import org.kreds.connection.DefaultKredsClient
import org.kreds.connection.ExclusiveObject
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

interface Transaction: PipelineKeyCommands,PipelineStringCommands, PipelineHashCommands, PipelineListCommands{
    suspend fun exec(): List<Any?>
}

class TransactionImpl(private val client: DefaultKredsClient): ExclusiveObject(), Transaction,PipelineStringCommandsExecutor,PipelineKeyCommandExecutor,PipelineHashCommandExecutor, PipelineListCommandExecutor{

    override val mutex: Mutex = Mutex()

    private var done: Boolean = false
    private val responseFlow = MutableSharedFlow<List<Any?>>(1)

    private val commands = mutableListOf(CommandExecution(MULTI, SimpleStringCommandProcessor))
    private val commandResponse = mutableListOf<Any?>()

    override suspend fun <T> add(commandExecution: CommandExecution): Response<T> = mutex.withLock {
        commands.add(commandExecution)
        //MULTI command response won't be available in response list. EXEC response will be list of response of command executed.
        // lastIndex will never be -1 because MULTI and above commands.add(), this tiny adjustment is for this reason.
        Response(responseFlow,commands.lastIndex - 1)
    }

    override suspend fun exec(): List<Any?> = mutex.withLock {
        return if(done) commandResponse
        else {
            commands.add(CommandExecution(EXEC, ArrayCommandProcessor))
            commandResponse.addAll(client.executeTransaction(commands))
            done = true
            responseFlow.tryEmit(commandResponse)
            commandResponse
        }
    }
}