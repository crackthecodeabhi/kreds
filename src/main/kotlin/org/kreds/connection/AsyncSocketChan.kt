package org.kreds.connection

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.Closeable
import java.lang.RuntimeException
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousCloseException
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import kotlin.coroutines.*
import kotlin.jvm.Throws

class AsynchronousCloseExceptionWithBuffer(val buffer: ByteBuffer): RuntimeException()

class AsyncSocketChan(private val asyncSocketChannel: AsynchronousSocketChannel): Closeable{

    fun isConnected(): Boolean = asyncSocketChannel.remoteAddress != null && asyncSocketChannel.isOpen

    @Throws(KredsConnectionException::class)
    suspend fun connectAsync(endpoint: Endpoint){
        suspendCoroutine<Unit> {
            asyncSocketChannel.connect(endpoint.toSocketAddress(),it,object : CompletionHandler<Void, Continuation<Unit>>{
                override fun completed(result: Void?, attachment: Continuation<Unit>) = attachment.resume(Unit)
                override fun failed(exc: Throwable, attachment: Continuation<Unit>) = attachment.resumeWithException(exc)
            })
        }
        readCoroutineScope.launch {
            readFromSocketChannel()
        }
    }

    suspend fun writeAsync(bytes: ByteArray){
        return suspendCoroutine {
            val writeBuffer = ByteBuffer.wrap(bytes)
            asyncSocketChannel.write(writeBuffer,it, object: CompletionHandler<Int, Continuation<Unit>> {
                override fun completed(result: Int, attachment: Continuation<Unit>) {
                    if(result != 0) asyncSocketChannel.write(writeBuffer,attachment,this)
                    else attachment.resume(Unit)
                }
                override fun failed(exc: Throwable, attachment: Continuation<Unit>) = attachment.resumeWithException(exc)
            })
        }
    }

    private val readCoroutineScope = CoroutineScope(Dispatchers.IO)
    val readChannel: Channel<Byte> = Channel()
    val READ_BUFFER_SIZE_BYTES = 256

    private suspend fun readFromSocketChannel(){
        while(coroutineContext.isActive){
            var buff: ByteBuffer
            try{
                buff = suspendCoroutine {
                    val buffer = ByteBuffer.allocate(4)
                    asyncSocketChannel.read(buffer,it,object : CompletionHandler<Int,Continuation<ByteBuffer>>{
                        override fun completed(result: Int, attachment: Continuation<ByteBuffer>) {
                            if(buffer.remaining() > 0){
                                asyncSocketChannel.read(buffer,attachment,this)
                            }
                            else attachment.resume(buffer)
                        }
                        override fun failed(exc: Throwable, attachment: Continuation<ByteBuffer>){
                            when(exc){
                                is AsynchronousCloseException -> attachment.resumeWithException(AsynchronousCloseExceptionWithBuffer(buffer))
                                else -> attachment.resumeWithException(exc)
                            }
                        }
                    })
                }
            }catch (ex: AsynchronousCloseExceptionWithBuffer){
                sendBytes(ex.buffer)
                break
            }
            sendBytes(buff)
        }
        readChannel.close()
    }

    private suspend fun sendBytes(buffer: ByteBuffer){
        buffer.flip()
        while(buffer.hasRemaining()) readChannel.send(buffer.get())
    }

    override fun close() {
        asyncSocketChannel.close()
    }
}