package org.kreds.pipeline

import org.kreds.KredsException
import io.netty.handler.codec.redis.ErrorRedisMessage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import org.kreds.commands.*
import org.kreds.connection.DefaultKredsClient
import org.kreds.ExclusiveObject
import org.kreds.lockByCoroutineJob
import org.kreds.protocol.KredsRedisDataException
import kotlin.jvm.Throws


class Response<T>(private val responseFlow: Flow<List<Any?>>, private val index: Int){

    @Suppress("UNCHECKED_CAST")
    @Throws(KredsException::class,KredsRedisDataException::class)
    suspend fun get(): T  = when(val value = responseFlow.first().ifEmpty { throw KredsException("Operation was cancelled.")  }[index]){
        is KredsException -> throw value
        else -> value as T
    }
}

interface QueuedCommand {
    suspend fun <T> add(commandExecution: CommandExecution): Response<T>
}

interface Pipeline : PipelineStringCommands, PipelineKeyCommands, PipelineHashCommands, PipelineSetCommands,
    PipelineListCommands, PipelineHyperLogLogCommands {
    suspend fun execute()
}


internal class PipelineImpl(private val client: DefaultKredsClient) : ExclusiveObject, Pipeline,
    PipelineStringCommandsExecutor, PipelineKeyCommandExecutor, PipelineHashCommandExecutor, PipelineSetCommandExecutor,
    PipelineListCommandExecutor, PipelineHyperLogLogCommandExecutor {

    override val mutex: Mutex = Mutex()

    private var done = false

    private val responseFlow = MutableSharedFlow<List<Any?>>(1)

    private val sharedResponseFlow: Flow<List<Any?>> = responseFlow.asSharedFlow()

    private val commands = mutableListOf<CommandExecution>()
    private val commandResponse = mutableListOf<Any?>()

    override suspend fun <T> add(commandExecution: CommandExecution): Response<T> = lockByCoroutineJob {
        commands.add(commandExecution)
        return Response(sharedResponseFlow, commands.lastIndex)
    }

    private suspend fun executePipeline(commands: List<CommandExecution>):List<Any?> = lockByCoroutineJob {
        val responseList = mutableListOf<Any?>()
        val responseMessages = client.executeCommands(commands)
        responseList.addAll(
            responseMessages.mapIndexed { idx, it ->
                when(it){
                    //TODO: check this
                    is ErrorRedisMessage -> KredsRedisDataException(it.content(),null,enableSuppression = true, writableStackTrace = false)
                    else -> commands[idx].processor.decode(it)
                }
            })
        return responseList
    }

    override suspend fun execute(): Unit = lockByCoroutineJob {
        if (!done) {
            commandResponse.addAll(executePipeline(commands))
            done = true
            responseFlow.tryEmit(commandResponse.toMutableList())
        }
    }
}
