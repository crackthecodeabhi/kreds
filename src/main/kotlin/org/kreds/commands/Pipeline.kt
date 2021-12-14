package org.kreds.commands

import org.kreds.connection.KredsClient
import org.kreds.protocol.CommandExecution


class Response<T>(private val responseList: List<Any?>, private val index: Int){
    @Suppress("UNCHECKED_CAST")
    fun get(): T {
        return responseList[index] as T
    }
}

interface PipelineExecutor{
    suspend fun executePipeline(commands: List<CommandExecution>): List<Any?>
}

interface QueuedCommand {
    fun <T> add(commandExecution: CommandExecution): Response<T>
}

interface Pipeline: PipelineStringCommands, PipelineKeyCommands {
    suspend fun execute()
}

class PipelineImpl(private val client: KredsClient): Pipeline, PipelineStringCommandsExecutor, PipelineKeyCommandExecutor{

    private val commands = mutableListOf<CommandExecution>()
    private val commandResponse = mutableListOf<Any?>()

    override fun <T> add(commandExecution: CommandExecution): Response<T>{
        commands.add(commandExecution)
        return Response(commandResponse,commands.lastIndex)
    }

    override suspend fun execute(){
        commandResponse.addAll(client.executePipeline(commands))
    }
}