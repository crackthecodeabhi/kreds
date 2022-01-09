/*
 *  Copyright (C) 2022 Abhijith Shivaswamy
 *   See the notice.md file distributed with this work for additional
 *   information regarding copyright ownership.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package io.github.crackthecodeabhi.kreds.connection

public class KredsClientConfig {

    public companion object {
        public const val NO_READ_TIMEOUT: Int = -1
    }

    public val connectTimeOutMillis: Int

    public val soKeepAlive: Boolean

    /**
     * In Subscriber client, readTimeout can be set to -1, to never timeout from reading from subscription connection.
     */
    public val readTimeoutSeconds: Int

    private constructor(builder: Builder) {
        // init with configured values or default
        connectTimeOutMillis = builder.connectTimeOutMillis ?: 5000
        readTimeoutSeconds = builder.readTimeoutSeconds ?: 30
        soKeepAlive = builder.soKeepAlive ?: true
    }

    private constructor(builder: Builder, other: KredsClientConfig) {
        // init with configured values or copy from other
        connectTimeOutMillis = builder.connectTimeOutMillis ?: other.connectTimeOutMillis
        readTimeoutSeconds = builder.readTimeoutSeconds ?: other.readTimeoutSeconds
        soKeepAlive = builder.soKeepAlive ?: other.soKeepAlive
    }

    public data class Builder(
        var connectTimeOutMillis: Int? = null,
        var readTimeoutSeconds: Int? = null,
        var soKeepAlive: Boolean? = null
    ) {
        /**
         * @param defaultSource if any property is not set, values will be initialized from [defaultSource]
         * @throws IllegalArgumentException if any argument is invalid or conflicts with other configuration
         */
        public fun build(defaultSource: KredsClientConfig? = null): KredsClientConfig =
            defaultSource?.let { KredsClientConfig(this, defaultSource) } ?: KredsClientConfig(this)
    }
}

internal val defaultClientConfig: KredsClientConfig = KredsClientConfig.Builder().build()

internal val defaultSubscriberClientConfig: KredsClientConfig =
    KredsClientConfig.Builder(readTimeoutSeconds = KredsClientConfig.NO_READ_TIMEOUT).build()

internal val defaultBlockingKredsClientConfig: KredsClientConfig =
    KredsClientConfig.Builder(readTimeoutSeconds = KredsClientConfig.NO_READ_TIMEOUT).build()