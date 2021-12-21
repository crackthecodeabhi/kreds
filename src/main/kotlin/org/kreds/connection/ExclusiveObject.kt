package org.kreds.connection

import kotlinx.coroutines.job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.coroutineContext

/**
 * An exclusive object is to be accessed only while holding this mutex of that object.
 * Implementations should be @CoroutineSafe
 */
internal interface ExclusiveObject {
    val mutex: Mutex
}

internal suspend inline fun <R> ExclusiveObject.lockByCoroutineJob(block: () -> R): R {
    return if(mutex.holdsLock(coroutineContext.job)) block()
    else mutex.withLock(coroutineContext.job) {
        block()
    }
}