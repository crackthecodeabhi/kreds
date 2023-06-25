/*
 *  Copyright (C) 2023 Abhijith Shivaswamy
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

class FunctionCommandsTest : FunSpec({
    lateinit var client: KredsClient
    val clientSetup = ClientSetup().then { client = it.client }
    beforeSpec(clientSetup)
    afterSpec(ClientTearDown(clientSetup))
    beforeTest(ClearDB(clientSetup))
    beforeTest { client.functionFlush(SyncOption.SYNC) }

    test("Function commands").config(enabledOrReasonIf = clientSetup.enableIf(REDIS_7_0_0)) {
        val libraryName = "functionstest"
        val functionName = "testfunction"
        val code = """
            #!lua name=$libraryName
            redis.register_function("$functionName", function(keys, args)
                return redis.call("SET", keys[1], args[1])
            end)
        """.trimIndent()

        suspend fun checkNotFound() {
            shouldThrow<KredsRedisDataException> {
                // ERR Function not found
                client.fcall(functionName, arrayOf("key"), arrayOf("value"))
            }
        }

        checkNotFound()

        client.functionLoad(replace = false, code) shouldBe libraryName

        shouldThrow<KredsRedisDataException> {
            // ERR Library 'functionstest' already exists
            client.functionLoad(replace = false, code)
        }

        client.functionLoad(replace = true, code) shouldBe libraryName

        val key = "test_key"
        val value = "Hello World"
        (client.fcall(functionName, arrayOf(key), arrayOf(value)) as String).shouldBeOk()
        client.get(key) shouldBe value
        client.del(key)

        shouldThrow<KredsRedisDataException> {
            // ERR Can not execute a script with write flag using *_ro command.
            client.fcall(functionName, arrayOf("key"), arrayOf("value"), readOnly = true)
        }

        client.functionDelete(libraryName)
        checkNotFound()

        client.functionLoad(replace = false, code) shouldBe libraryName

        client.functionFlush(SyncOption.SYNC)
        checkNotFound()

        shouldThrow<KredsRedisDataException> {
            // NOTBUSY No scripts in execution right now.
            client.functionKill()
        }
    }
})