package io.github.crackthecodeabhi.kreds

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * An exclusive object is to be accessed only while holding this mutex of that object.
 * Implementations should be @CoroutineSafe
 */
internal interface ExclusiveObject {
    val mutex: Mutex
    val key: ReentrantMutexContextKey
}

internal data class ReentrantMutexContextKey(val mutex: Mutex): CoroutineContext.Key<ReentrantMutexContextElement>
internal class ReentrantMutexContextElement(override val key: ReentrantMutexContextKey): CoroutineContext.Element

internal suspend inline fun <R> ExclusiveObject.withReentrantLock(crossinline block: suspend () -> R): R {
    if(coroutineContext[key] != null) return block()

    return withContext(ReentrantMutexContextElement(key)){
        this@withReentrantLock.mutex.withLock {
            block()
        }
    }
}