package io.github.crackthecodeabhi.kreds.connection

import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import io.netty.util.concurrent.Future
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal suspend fun <V> Future<V>.suspendableAwait(): V {
   return suspendCoroutine { cont ->
       addListener { f ->
           if(f.isDone && f.isSuccess) cont.resume(now)
           else cont.resumeWithException(f.cause())
       }
   }
}

internal suspend fun ChannelFuture.suspendableAwait(): Channel {
    return suspendCoroutine { cont ->
        addListener(object : ChannelFutureListener{
            override fun operationComplete(future: ChannelFuture) {
                if(future.isDone && future.isSuccess) cont.resume(future.channel())
                else cont.resumeWithException(future.cause())
            }
        })
    }
}