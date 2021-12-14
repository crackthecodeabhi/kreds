package org.kreds.commands

import org.kreds.connection.KredsClient
import org.kreds.protocol.CommandExecution


class Response<T>(private val responseList: List<Response<*>>, private val index: Int){
    @Suppress("UNCHECKED_CAST")
    fun get(): T {
        return responseList[index] as T
    }
}

interface PipelineExecutor{
    suspend fun execute(commands: List<CommandExecution>): List<Response<*>>
}

interface Pipeline: PipelineStringCommands {
    fun <T> add(commandExecution: CommandExecution): Response<T>
    suspend fun execute()
}

class PipelineImpl(private val client: KredsClient): PipelineStringCommandsExecutor{

    private val commands = mutableListOf<CommandExecution>()
    private val commandResponse = mutableListOf<Response<*>>()

    override fun <T> add(commandExecution: CommandExecution): Response<T>{
        commands.add(commandExecution)
        return Response(commandResponse,commands.lastIndex)
    }

    override suspend fun execute(){
        commandResponse.addAll(client.execute(commands))
    }
}