package io.github.crackthecodeabhi.kreds.connection

import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.handler.codec.redis.RedisMessage
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import io.github.crackthecodeabhi.kreds.args.*
import io.github.crackthecodeabhi.kreds.commands.*
import io.github.crackthecodeabhi.kreds.protocol.*
import io.github.crackthecodeabhi.kreds.pipeline.*
import io.github.crackthecodeabhi.kreds.lockByCoroutineJob


//TODO: INCRBY can be negative also! check that in api, should accept long, not ulong

public object KredsClientGroup {
    private val eventLoopGroup = NioEventLoopGroup()
    public fun newClient(endpoint: Endpoint, config: KredsClientConfig = defaultClientConfig): KredsClient =
        DefaultKredsClient(endpoint, eventLoopGroup,config)

    public fun newSubscriberClient(endpoint: Endpoint, handler: KredsSubscriber, config: KredsClientConfig = defaultSubscriberClientConfig): KredsSubscriberClient =
        DefaultKredsSubscriberClient(endpoint, eventLoopGroup, handler, config)

    public suspend fun shutdown() {
        eventLoopGroup.shutdownGracefully().suspendableAwait()
    }
}

public interface KredsClient : AutoCloseable, KeyCommands, StringCommands, ConnectionCommands, PublisherCommands, HashCommands, SetCommands,
    ListCommands, HyperLogLogCommands {
    public fun pipelined(): Pipeline
}

internal abstract class AbstractKredsClient(endpoint: Endpoint, eventLoopGroup: EventLoopGroup, config: KredsClientConfig) :
    KonnectionImpl(endpoint, eventLoopGroup,config), CommandExecutor{

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

internal class DefaultKredsClient(endpoint: Endpoint, eventLoopGroup: EventLoopGroup, config: KredsClientConfig) :
    AbstractKredsClient(endpoint, eventLoopGroup,config), KredsClient, KeyCommandExecutor, StringCommandsExecutor,
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

