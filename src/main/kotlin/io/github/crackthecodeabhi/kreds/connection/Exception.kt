package io.github.crackthecodeabhi.kreds.connection

import io.github.crackthecodeabhi.kreds.KredsException

public open class KredsConnectionException: KredsException {
    internal companion object {
        @JvmStatic val serialVersionUID = -942312682189773098L
    }
    internal constructor(message: String): super(message)
    internal constructor(throwable: Throwable): super( throwable)
    internal constructor(message: String, throwable: Throwable): super(message, throwable)
}

/**
 * Exception thrown when an I/O timeout occurs, like read, write, connection timeouts.
 */
public class KredsTimeoutException internal constructor(message: String, cause: Throwable): KredsConnectionException(message,cause)

/**
 * Exception thrown when any I/O operation is invoked on a [Konnection] which is not yet connected.
 */
public class KredsNotYetConnectedException internal constructor():KredsConnectionException("Not yet Connected.")