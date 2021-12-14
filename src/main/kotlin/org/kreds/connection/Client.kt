package org.kreds.connection

import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import org.kreds.Argument
import org.kreds.commands.*
import org.kreds.protocol.*


object KredsClientGroup{
    private val eventLoopGroup = NioEventLoopGroup()
    fun newClient(endpoint: Endpoint): KredsClient = DefaultKredsClient(endpoint, eventLoopGroup)
    suspend fun shutdown(){
        eventLoopGroup.shutdownGracefully().suspendableAwait()
    }
}

interface KredsClient: KeyCommands,StringCommands,ConnectionCommands, CommandExecutor, PipelineExecutor{
    fun pipelined(): Pipeline
}

class DefaultKredsClient(endpoint: Endpoint,eventLoopGroup: EventLoopGroup): DefaultKConnection(endpoint,eventLoopGroup), KredsClient, KeyCommandExecutor, StringCommandsExecutor, ConnectionCommandsExecutor{

    override fun pipelined(): Pipeline = PipelineImpl(this)

    override suspend fun <T> execute(command: Command, processor: ICommandProcessor, vararg args: Argument): T {
        writeAndFlush(processor.encode(command,*args))
        return processor.decode(readChannel.receive())
    }

    override suspend fun <T> execute(commandExecution: CommandExecution): T {
        with(commandExecution){
            writeAndFlush(processor.encode(command,*args))
            return processor.decode(readChannel.receive())
        }
    }

    override suspend fun execute(commands: List<CommandExecution>): List<Response<*>> {
        val head = commands.dropLast(1)
        head.forEach{
            with(it){
                write(processor.encode(command,*args))
            }
        }
        with(commands.last()){
            writeAndFlush(processor.encode(command,*args))
        }
        // collect the response.
        val responseList = mutableListOf<Response<*>>()
        repeat(commands.size){
            val cmd = commands[it]
            responseList.add(it,cmd.processor.decode(readChannel.receive()))
        }
        return responseList
    }
}

