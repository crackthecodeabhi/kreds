package org.kreds.connection

import kotlinx.coroutines.sync.Mutex

/**
 * An exclusive object is to be accessed only while holding this mutex of that object.
 */
abstract class ExclusiveObject {
    protected abstract val mutex: Mutex
}