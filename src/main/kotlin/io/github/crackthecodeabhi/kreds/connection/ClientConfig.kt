package io.github.crackthecodeabhi.kreds.connection

public class KredsClientConfig private constructor(builder: Builder) {

    public companion object{
        public const val NO_READ_TIMEOUT: Int = -1
    }

    public val connectTimeOutMillis: Int

    public val soKeepAlive: Boolean

    /**
     * In Subscriber client, readTimeout can be set to -1, to never timeout from reading from subscription connection.
     */
    public val readTimeoutSeconds: Int

    init {
        // init with configured values or default
        connectTimeOutMillis = builder.connectTimeOutMillis ?: 5000
        readTimeoutSeconds = builder.readTimeoutSeconds ?: 30
        soKeepAlive = builder.soKeepAlive ?: true
    }

    public data class Builder(
        var connectTimeOutMillis: Int? = null,
        var readTimeoutSeconds: Int? = null,
        var soKeepAlive: Boolean? = null
    ) {
        /**
         * @throws IllegalArgumentException if any argument is invalid or conflicts with other configuration
         */
        public fun build(): KredsClientConfig = KredsClientConfig(this)
    }
}

internal val defaultClientConfig : KredsClientConfig = KredsClientConfig.Builder().build()

internal val defaultSubscriberClientConfig : KredsClientConfig = KredsClientConfig.Builder(readTimeoutSeconds = KredsClientConfig.NO_READ_TIMEOUT).build()