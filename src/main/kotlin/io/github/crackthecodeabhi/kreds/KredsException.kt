package io.github.crackthecodeabhi.kreds

/**
 * Base exception for all exceptions throws by Kreds
 */
public open class KredsException: RuntimeException{
    internal companion object {
        @JvmStatic val serialVersionUID = -342312132189773098L
    }
    internal constructor(message: String): super(message)
    internal constructor(throwable: Throwable): super( throwable)
    internal constructor(message: String, throwable: Throwable): super(message, throwable)
    internal constructor(message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean) : super(
        message,
        cause,
        enableSuppression,
        writableStackTrace
    )
}