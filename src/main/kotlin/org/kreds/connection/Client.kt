package org.kreds.connection

import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.handler.codec.redis.RedisMessage
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import org.kreds.Argument
import org.kreds.commands.*
import org.kreds.protocol.*

//TODO: INCRBY can be negative also! check that in api, should accept long, not ulong

object KredsClientGroup {
    private val eventLoopGroup = NioEventLoopGroup()
    fun newClient(endpoint: Endpoint): KredsClient =
        DefaultKredsClient(endpoint, eventLoopGroup)

    fun newSubscriberClient(endpoint: Endpoint, handler: KredsSubscriber): KredsSubscriberClient =
        DefaultKredsSubscriberClient(endpoint, eventLoopGroup, handler)

    suspend fun shutdown() {
        eventLoopGroup.shutdownGracefully().suspendableAwait()
    }
}

interface KredsClient : AutoCloseable, KeyCommands, StringCommands, ConnectionCommands, PublisherCommands, HashCommands, SetCommands,
    ListCommands, HyperLogLogCommands {
    fun pipelined(): Pipeline
}

internal abstract class AbstractKredsClient(endpoint: Endpoint, eventLoopGroup: EventLoopGroup) :
    KonnectionImpl(endpoint, eventLoopGroup), CommandExecutor{

    override suspend fun <T> execute(command: Command, processor: ICommandProcessor, vararg args: Argument): T =
        lockByCoroutineJob {
            connectWriteAndFlush(processor.encode(command, *args))
            processor.decode(read())
        }

    override suspend fun <T> execute(commandExecution: CommandExecution): T = lockByCoroutineJob {
        with(commandExecution) {
            connectWriteAndFlush(processor.encode(command, *args))
            processor.decode(read())
        }
    }

    override suspend fun executeCommands(commands: List<CommandExecution>): List<RedisMessage> = lockByCoroutineJob {
        connect()
        commands.forEach {
            with(it){
                write(processor.encode(command, *args))
            }
        }
        flush()
        // collect the response messages.
        val responseList = mutableListOf<RedisMessage>()
        repeat(commands.size) {
            responseList.add(it,read())
        }
        responseList
    }
}

internal class DefaultKredsClient(endpoint: Endpoint, eventLoopGroup: EventLoopGroup) :
    AbstractKredsClient(endpoint, eventLoopGroup), KredsClient, KeyCommandExecutor, StringCommandsExecutor,
    ConnectionCommandsExecutor, PublishCommandExecutor, HashCommandsExecutor, SetCommandExecutor, ListCommandExecutor,
    HyperLogLogCommandExecutor {

    override val mutex: Mutex = Mutex()

    override fun pipelined(): Pipeline = PipelineImpl(this)

    override fun close() {
        runBlocking {
            lockByCoroutineJob {
                disconnect()
            }
        }
    }
}

