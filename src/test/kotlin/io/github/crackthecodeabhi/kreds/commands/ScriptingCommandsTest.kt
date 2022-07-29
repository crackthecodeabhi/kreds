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

import io.github.crackthecodeabhi.kreds.args.SyncOption
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.github.crackthecodeabhi.kreds.protocol.KredsRedisDataException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldHaveLength

class ScriptingCommandsTest : FunSpec({
    lateinit var client: KredsClient
    val clientSetup = ClientSetup().then { client = it.client }
    beforeSpec(clientSetup)
    afterSpec(ClientTearDown(clientSetup))
    beforeTest(ClearDB(clientSetup))

    val testKey = "test"
    val testArgument = "Hello World!"

    test("Scripting commands").config {
        client.set(testKey, testArgument)!!.shouldBeOk()
        client.eval(
            "return redis.call('GET', KEYS[1])",
            arrayOf(testKey),
            emptyArray()
        ) shouldBe testArgument

        val hash = client.scriptLoad("return ARGV[1]") shouldHaveLength 40 // SHA-1 digest length
        client.scriptExists(hash) shouldBe listOf(true)
        client.evalSha(hash, emptyArray(), arrayOf(testArgument)) shouldBe testArgument
        client.scriptFlush(SyncOption.SYNC)
        client.scriptExists(hash) shouldBe listOf(false)
        shouldThrow<KredsRedisDataException> { client.evalSha(hash, emptyArray(), arrayOf(testArgument)) }

        shouldThrow<KredsRedisDataException> { client.scriptKill() }
    }

    test("Scripting commands >= API 7").config(enabledOrReasonIf = clientSetup.enableIf(REDIS_7_0_0)) {
        shouldThrow<KredsRedisDataException> {
            client.eval("return redis.call('DEL', KEYS[1])", arrayOf(testKey), emptyArray(), readOnly = true)
        }
    }
})
