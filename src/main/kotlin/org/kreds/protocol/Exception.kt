package org.kreds.protocol

import KredsException

class KredsRedisDataException: KredsException{
    companion object {
        @JvmStatic val serialVersionUID = -942312682189778098L
    }
    constructor(message: String): super(message)
    constructor(throwable: Throwable): super( throwable)
    constructor(message: String, throwable: Throwable): super(message, throwable)
}