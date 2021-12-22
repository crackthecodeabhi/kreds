package io.github.crackthecodeabhi.kreds.pipeline

import io.github.crackthecodeabhi.kreds.KredsException
import io.netty.handler.codec.redis.ErrorRedisMessage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import io.github.crackthecodeabhi.kreds.commands.*
import io.github.crackthecodeabhi.kreds.connection.DefaultKredsClient
import io.github.crackthecodeabhi.kreds.ExclusiveObject
import io.github.crackthecodeabhi.kreds.lockByCoroutineJob
import io.github.crackthecodeabhi.kreds.protocol.KredsRedisDataException
import kotlin.jvm.Throws


public class Response<T> internal constructor(private val responseFlow: Flow<List<Any?>>, private val index: Int){

    @Suppress("UNCHECKED_CAST")
    @Throws(KredsException::class,KredsRedisDataException::class)
    public suspend fun get(): T  = when(val value = responseFlow.first().ifEmpty { throw KredsException("Operation was cancelled.")  }[index]){
        is KredsException -> throw value
        else -> value as T
    }
}

internal interface QueuedCommand {
    suspend fun <T> add(commandExecution: CommandExecution): Response<T>
}

public interface Pipeline : PipelineStringCommands, PipelineKeyCommands, PipelineHashCommands, PipelineSetCommands,
    PipelineListCommands, PipelineHyperLogLogCommands {
    public suspend fun execute()
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
