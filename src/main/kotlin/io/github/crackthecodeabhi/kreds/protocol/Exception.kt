package io.github.crackthecodeabhi.kreds.protocol

import io.github.crackthecodeabhi.kreds.KredsException

class KredsRedisDataException: KredsException {
    companion object {
        @JvmStatic val serialVersionUID = -942312682189778098L
    }
    constructor(message: String): super(message)
    constructor(throwable: Throwable): super( throwable)
    constructor(message: String, throwable: Throwable): super(message, throwable)
    constructor(message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean) : super(
        message,
        cause,
        enableSuppression,
        writableStackTrace
    )
}