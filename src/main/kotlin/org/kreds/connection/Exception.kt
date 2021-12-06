package org.kreds.connection

import KredsException

class KredsConnectionException: KredsException{
    companion object {
        @JvmStatic val serialVersionUID = -942312682189773098L
    }
    constructor(message: String): super(message)
    constructor(throwable: Throwable): super( throwable)
    constructor(message: String, throwable: Throwable): super(message, throwable)
}