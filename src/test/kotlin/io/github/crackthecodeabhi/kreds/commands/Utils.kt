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

package io.github.crackthecodeabhi.kreds.commands

import io.github.crackthecodeabhi.kreds.args.ServerInfoSection
import io.github.crackthecodeabhi.kreds.args.SyncOption
import io.github.crackthecodeabhi.kreds.connection.*
import io.github.crackthecodeabhi.kreds.pipeline.Response
import io.kotest.core.spec.AfterSpec
import io.kotest.core.spec.BeforeSpec
import io.kotest.core.spec.BeforeTest
import io.kotest.core.spec.Spec
import io.kotest.core.test.Enabled
import io.kotest.core.test.TestCase
import io.kotest.matchers.shouldBe
import net.swiftzer.semver.SemVer
import kotlin.reflect.KClass
import kotlin.reflect.cast

val REDIS_6_0_0 = SemVer.parse("6.0.0")
val REDIS_6_2_0 = SemVer.parse("6.2.0")
val REDIS_7_0_0 = SemVer.parse("7.0.0")

internal fun getTestClient(endpoint: Endpoint? = null, config: KredsClientConfig? = null): InternalKredsClient {
    val redisPort = System.getProperty("REDIS_PORT") ?: throw RuntimeException("Required system property: REDIS_PORT")
    val e = endpoint ?: Endpoint.from("127.0.0.1:${redisPort}")
    val client = config?.let { newClient(e, it) } ?: newClient(e)
    return client as InternalKredsClient
}

typealias AndThen<T> = suspend (spec: T) -> Unit

interface Then<T> {
    fun then(andThen: AndThen<T>): T
}

internal class ClientSetup : BeforeSpec, Then<ClientSetup> {
    lateinit var client: InternalKredsClient
        private set
    lateinit var serverVersion: SemVer
        private set
    lateinit var serverModules: Set<String>
        private set
    var andThen: AndThen<ClientSetup>? = null

    override suspend fun invoke(p1: Spec) {
        client = getTestClient(config = KredsClientConfig.Builder(readTimeoutSeconds = 1).build(defaultClientConfig))
        client.flushAll(SyncOption.SYNC)
        serverVersion = SemVer.parse(client.serverVersion())

        serverModules = client
            .info(ServerInfoSection.modules)!!
            .lines()
            .mapNotNull { line ->
                val start = "module:name="
                if (!line.startsWith(start)) {
                    null
                } else {
                    line.substring(start.length, line.indexOf(','))
                }
            }
            .toSet()

        andThen?.invoke(this)
    }

    override fun then(andThen: AndThen<ClientSetup>) = apply {
        this.andThen = andThen
    }

    fun enableIf(version: SemVer): (TestCase) -> Enabled = {
        if (serverVersion < version) Enabled.disabled("Target $serverVersion < required $version")
        else Enabled.enabled
    }

    fun enableIfModulePresent(module: String): (TestCase) -> Enabled = {
        if (!serverModules.contains(module))
            Enabled.disabled("Module $module is not installed")
        else
            Enabled.enabled
    }
}

internal class ClientTearDown(private val setup: ClientSetup) : AfterSpec, Then<ClientTearDown> {
    var andThen: AndThen<ClientTearDown>? = null
    override suspend fun invoke(p1: Spec) {
        setup.client.close()
        andThen?.invoke(this)
    }

    override fun then(andThen: AndThen<ClientTearDown>): ClientTearDown = apply {
        this.andThen = andThen
    }
}

internal class ClearDB(private val setup: ClientSetup) : BeforeTest {
    override suspend fun invoke(p1: TestCase) {
        setup.client.flushDb(SyncOption.SYNC)
    }
}

fun String.shouldBeOk() = this shouldBe "OK"

inline fun <reified R> List<*>.getAs(index: Int): R = this[index] as R

typealias ResponseType<R> = Pair<Response<R?>, KClass<R>?>

inline fun <reified R : Any> Response<R?>.toResponseType(): ResponseType<R> {
    return Pair(this, R::class)
}

suspend inline fun <T : Any> ResponseType<T>.get(): T? {
    return if (second == null) null
    else if (first() == null) null
    else second!!.cast(first())
}

suspend inline operator fun <T : Any> ResponseType<T>.invoke(): T? = get()
