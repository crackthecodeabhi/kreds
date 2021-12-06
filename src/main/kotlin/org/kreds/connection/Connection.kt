package org.kreds.connection

import java.io.Closeable


class Connection(private val socketChannel: AsyncSocketChan): Closeable{

    suspend fun connect(){
        if(!isConnected()){
            socketChannel = socketFactory.createSocket()
        }
    }

    suspend fun sendCommand(command: String){
        connect()
        socketChannel?.writeAsync(command.encodeToByteArray())
    }

    fun isConnected() = socketChannel?.isConnected() ?: false

    fun disconnect() {
        socketChannel?.apply { close() }
    }

    override fun close() = disconnect()
}