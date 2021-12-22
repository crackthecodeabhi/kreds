package io.github.crackthecodeabhi.kreds.connection

import io.github.crackthecodeabhi.kreds.KredsException

open class KredsConnectionException: KredsException {
    companion object {
        @JvmStatic val serialVersionUID = -942312682189773098L
    }
    constructor(message: String): super(message)
    constructor(throwable: Throwable): super( throwable)
    constructor(message: String, throwable: Throwable): super(message, throwable)
}

/**
 * Exception thrown when an I/O timeout occurs, like read, write, connection timeouts.
 */
class KredsTimeoutException(message: String, cause: Throwable): KredsConnectionException(message,cause)

/**
 * Exception thrown when any I/O operation is invoked on a [Konnection] which is not yet connected.
 */
class KredsNotYetConnectedException:KredsConnectionException("Not yet Connected.")