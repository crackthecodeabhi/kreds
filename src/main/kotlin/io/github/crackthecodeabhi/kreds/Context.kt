package io.github.crackthecodeabhi.kreds

import kotlinx.coroutines.*
import java.util.concurrent.Executors
import kotlin.coroutines.*


public object KredsContext{
    @JvmStatic
    internal val context = Executors.newSingleThreadExecutor { r -> Thread(r, "KredsRunner") }.asCoroutineDispatcher()
}

private class KredsContinuation<T>(val cont: Continuation<T>): Continuation<T>{
    override val context: CoroutineContext = KredsContext.context

    override fun resumeWith(result: Result<T>) {
        KredsContext.context.dispatch(context){
            cont.resumeWith(result)
        }
    }
}

public object Kreds: AbstractCoroutineContextElement(ContinuationInterceptor), ContinuationInterceptor{
    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> = KredsContinuation(continuation)
}