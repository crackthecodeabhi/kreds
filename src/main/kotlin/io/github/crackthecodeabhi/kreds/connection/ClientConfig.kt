package io.github.crackthecodeabhi.kreds.connection

class KredsClientConfig private constructor(builder: Builder) {

    companion object{
        const val NO_READ_TIMEOUT: Int = -1
    }

    val connectTimeOutMillis: Int

    val soKeepAlive: Boolean

    /**
     * In Subscriber client, readTimeout can be set to -1, to never timeout from reading from subscription connection.
     */
    val readTimeoutSeconds: Int

    init {
        // init with configured values or default
        connectTimeOutMillis = builder.connectTimeOutMillis ?: 5000
        readTimeoutSeconds = builder.readTimeoutSeconds ?: 30
        soKeepAlive = builder.soKeepAlive ?: true
    }

    data class Builder(
        var connectTimeOutMillis: Int? = null,
        var readTimeoutSeconds: Int? = null,
        var soKeepAlive: Boolean? = null
    ) {
        /**
         * @throws IllegalArgumentException if any argument is invalid or conflicts with other configuration
         */
        fun build(): KredsClientConfig = KredsClientConfig(this)
    }
}

val defaultClientConfig = KredsClientConfig.Builder().build()

val defaultSubscriberClientConfig = KredsClientConfig.Builder(readTimeoutSeconds = KredsClientConfig.NO_READ_TIMEOUT).build()