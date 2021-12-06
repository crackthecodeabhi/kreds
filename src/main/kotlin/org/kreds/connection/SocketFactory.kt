package org.kreds.connection

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Creates a socket with given configuration.
 */
interface KRedSocketFactory {
    @Throws(KredsConnectionException::class)
    suspend fun createSocket(): AsyncSocketChan
}


class DefaultKredSocketFactory(private val endpoint: Endpoint) : KRedSocketFactory {

    @Throws(KredsConnectionException::class)
    override suspend fun createSocket(): AsyncSocketChan {
        try {
            val socketChannel = AsyncSocketChan(withContext(Dispatchers.IO) { AsynchronousSocketChannel.open() })
            socketChannel.connectAsync(endpoint)
            return socketChannel
        } catch (ex: Exception){
            throw KredsConnectionException("Failed to connect to remove $endpoint",ex)
        }
    }
}