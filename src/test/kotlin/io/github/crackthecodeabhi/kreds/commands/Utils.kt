/*
 *  Copyright (C) 2021 Abhijith Shivaswamy
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

package io.github.crackthecodeabhi.kreds.commands

import io.github.crackthecodeabhi.kreds.args.SyncOption
import io.github.crackthecodeabhi.kreds.connection.*
import io.kotest.core.spec.AfterSpec
import io.kotest.core.spec.BeforeSpec
import io.kotest.core.spec.Spec
import io.kotest.matchers.shouldBe
import net.swiftzer.semver.SemVer

val REDIS_6_2_0 = SemVer.parse("6.2.0")
val REDIS_7_0_0 = SemVer.parse("7.0.0")

fun getTestClient(endpoint: Endpoint? = null, config: KredsClientConfig? = null): KredsClient {
    return config?.let {
        KredsClientGroup.newClient(endpoint ?: Endpoint.from("127.0.0.1:6379"), it)
    } ?: KredsClientGroup.newClient(endpoint ?: Endpoint.from("127.0.0.1:6379"))
}

typealias AndThen<T> = suspend (spec: T) -> Unit

interface Then<T> {
    fun then(andThen: AndThen<T>): T
}

class ClientSetup : BeforeSpec, Then<ClientSetup> {
    lateinit var client: KredsClient
    lateinit var serverVersion: SemVer
    var andThen: AndThen<ClientSetup>? = null
    override suspend fun invoke(p1: Spec) {
        client = getTestClient(config = KredsClientConfig.Builder(readTimeoutSeconds = 1).build(defaultClientConfig))
        client.flushAll(SyncOption.SYNC)
        serverVersion = SemVer.parse(client.serverVersion())
        andThen?.invoke(this)
    }

    override fun then(andThen: AndThen<ClientSetup>) = apply {
        this.andThen = andThen
    }
}

class ClientTearDown(private val setup: ClientSetup) : AfterSpec, Then<ClientTearDown> {
    var andThen: AndThen<ClientTearDown>? = null
    override suspend fun invoke(p1: Spec) {
        setup.client.close()
        andThen?.invoke(this)
    }

    override fun then(andThen: AndThen<ClientTearDown>): ClientTearDown = apply {
        this.andThen = andThen
    }
}

fun String.shouldBeOk() = this shouldBe "OK"