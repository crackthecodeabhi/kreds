package io.github.crackthecodeabhi.kreds.protocol

import io.github.crackthecodeabhi.kreds.KredsException

public class KredsRedisDataException: KredsException {
    internal companion object {
        @JvmStatic val serialVersionUID = -942312682189778098L
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