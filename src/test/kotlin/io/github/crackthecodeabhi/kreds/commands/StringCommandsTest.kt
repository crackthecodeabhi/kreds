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

import io.github.crackthecodeabhi.kreds.args.SetOption
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.github.crackthecodeabhi.kreds.protocol.KredsRedisDataException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal

class StringCommandTest : FunSpec({
    lateinit var client: KredsClient
    val clientSetup = ClientSetup().then { client = it.client }
    beforeSpec(clientSetup)
    afterSpec(ClientTearDown(clientSetup))
    beforeTest(ClearDB(clientSetup))

    test("String Commands >= API 6.2").config(enabledOrReasonIf = clientSetup.enableIf(REDIS_6_2_0)) {
        client.set("key1", "value1")!!.shouldBeOk()
        client.getDel("key1") shouldBe "value1"
        client.getDel("non-existing-key") shouldBe null
    }

    test("String Commands >= API 6").config(enabledOrReasonIf = clientSetup.enableIf(REDIS_6_0_0)) {
        client.set("newKey", "100")!!.shouldBeOk()
        client.set("newKey", "101", SetOption.Builder(get = true).build()) shouldBe "100"
        client.set("noKey", "100", SetOption.Builder(get = true).build()) shouldBe null

        client.get("non-existing") shouldBe null
        client.hset("hashKey", "foo" to "bar") shouldBe 1
        shouldThrow<KredsRedisDataException> { client.get("hashKey") }

        client.set("appendKey", "abc")
        client.append("appendKey", "xyz") shouldBe 6

        client.getRange("appendKey", 0, 1) shouldBe "ab"

        client.getSet("getsetkey", "value1") shouldBe null
        client.getSet("getsetkey", "value2") shouldBe "value1"

        client.mget("getsetkey", "non-exisiting-key") shouldBe listOf("value2", null)

        client.mset("msetkey1" to "msetval1", "msetkey2" to "msetval2").shouldBeOk()

        client.set("number", "100")
        client.incr("number") shouldBe 101
        client.decr("number") shouldBe 100
        client.incrBy("number", 10) shouldBe 110
        client.decrBy("number", 20) shouldBe 90
        client.incrByFloat("number", BigDecimal.valueOf(0.00005)) shouldBe "90.00005"

        client.set("lengthtest", "kreds")!!.shouldBeOk()
        client.strlen("lengthtest") shouldBe 5
    }

    test("Pipeline String Commands >= 6.2").config(enabledOrReasonIf = clientSetup.enableIf(REDIS_6_2_0)) {
        val pipe = client.pipelined()
        val responseList = mutableListOf<ResponseType<Any>>()

        responseList += pipe.set("key1", "value1").toResponseType()
        responseList += pipe.getDel("key1").toResponseType()
        responseList += pipe.getDel("non-existing-key").toResponseType()

        pipe.execute()

        run {
            var i = 0
            (responseList[i++]() as String).shouldBeOk()
            responseList[i++]() shouldBe "value1"
            responseList[i++]() shouldBe null
        }
    }

    test("Pipeline String Commands >= 6").config(enabledOrReasonIf = clientSetup.enableIf(REDIS_6_0_0)) {
        val pipe = client.pipelined()
        val responseList = mutableListOf<ResponseType<Any>>()

        responseList += pipe.set("newKey", "100").toResponseType()

        responseList += pipe.set("newKey", "101", SetOption.Builder(get = true).build()).toResponseType()
        responseList += pipe.set("noKey", "100", SetOption.Builder(get = true).build()).toResponseType()

        responseList += pipe.get("non-existing").toResponseType()
        responseList += pipe.hset("hashKey", "foo" to "bar").toResponseType()
        responseList += pipe.get("hashKey").toResponseType()

        responseList += pipe.set("appendKey", "abc").toResponseType()
        responseList += pipe.append("appendKey", "xyz").toResponseType()

        responseList += pipe.getRange("appendKey", 0, 1).toResponseType()

        responseList += pipe.getSet("getsetkey", "value1").toResponseType()
        responseList += pipe.getSet("getsetkey", "value2").toResponseType()

        responseList += pipe.mget("getsetkey", "non-exisiting-key").toResponseType()

        responseList += pipe.mset("msetkey1" to "msetval1", "msetkey2" to "msetval2").toResponseType()

        responseList += pipe.set("number", "100").toResponseType()
        responseList += pipe.incr("number").toResponseType()
        responseList += pipe.decr("number").toResponseType()
        responseList += pipe.incrBy("number", 10).toResponseType()
        responseList += pipe.decrBy("number", 20).toResponseType()
        responseList += pipe.incrByFloat("number", BigDecimal.valueOf(0.00005)).toResponseType()

        responseList += pipe.set("lengthtest", "kreds").toResponseType()
        responseList += pipe.strlen("lengthtest").toResponseType()

        pipe.execute()


        run {
            var i = 0
            (responseList[i++]() as String).shouldBeOk()
            responseList[i++]() shouldBe "100"
            responseList[i++]() shouldBe null
            responseList[i++]() shouldBe null
            responseList[i++]() shouldBe 1
            shouldThrow<KredsRedisDataException> { responseList[i++]() }
            (responseList[i++]() as String).shouldBeOk()
            responseList[i++]() shouldBe 6
            responseList[i++]() shouldBe "ab"
            responseList[i++]() shouldBe null
            responseList[i++]() shouldBe "value1"
            responseList[i++]() shouldBe listOf("value2", null)
            (responseList[i++]() as String).shouldBeOk()
            (responseList[i++]() as String).shouldBeOk()
            responseList[i++]() shouldBe 101
            responseList[i++]() shouldBe 100
            responseList[i++]() shouldBe 110
            responseList[i++]() shouldBe 90
            responseList[i++]() shouldBe "90.00005"
            (responseList[i++]() as String).shouldBeOk()
            responseList[i++]() shouldBe 5
        }
    }
})