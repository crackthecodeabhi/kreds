package org.kreds.commands

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.kreds.connection.DefaultKredsClient
import org.kreds.connection.ExclusiveObject
import org.kreds.protocol.CommandExecution
import java.util.concurrent.atomic.AtomicReference


class Response<T>(private val responseFlow: Flow<List<Any?>>, private val index: Int){
    private val response = AtomicReference<T>(null)

    suspend fun get(): T {
        if(response.get() == null){
            @Suppress("UNCHECKED_CAST")
            response.compareAndSet(null, responseFlow.first()[index] as T)
        }
        return response.get()
    }
}

interface PipelineExecutor{
    suspend fun executePipeline(commands: List<CommandExecution>): List<Any?>
}

interface QueuedCommand {
    suspend fun <T> add(commandExecution: CommandExecution): Response<T>
}

interface Pipeline: PipelineStringCommands, PipelineKeyCommands, PipelineHashCommands, PipelineSetCommands, PipelineListCommands {
    suspend fun execute()
}


class PipelineImpl(private val client: DefaultKredsClient): ExclusiveObject(), Pipeline, PipelineStringCommandsExecutor, PipelineKeyCommandExecutor,PipelineHashCommandExecutor, PipelineSetCommandExecutor, PipelineListCommandExecutor{

    override val mutex: Mutex = Mutex()

    private var done = false

    private val responseFlow  = MutableSharedFlow<List<Any?>>(1)

    private val commands = mutableListOf<CommandExecution>()
    private val commandResponse = mutableListOf<Any?>()

    override suspend fun <T> add(commandExecution: CommandExecution): Response<T> = mutex.withLock {
        commands.add(commandExecution)
        return Response(responseFlow.asSharedFlow(), commands.lastIndex)
    }

    override suspend fun execute(): Unit = mutex.withLock {
        if(!done) {
            commandResponse.addAll(client.executePipeline(commands))
            done = true
            responseFlow.tryEmit(commandResponse)
        }
    }
}
