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

import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class SetCommandsTest : FunSpec({
    lateinit var client: KredsClient
    val clientSetup = ClientSetup().then { client = it.client }
    beforeSpec(clientSetup)
    afterSpec(ClientTearDown(clientSetup))
    beforeTest(ClearDB(clientSetup))

    test("Set commands >= API 6.2").config(enabledOrReasonIf = clientSetup.enableIf(REDIS_6_2_0)) {
        client.sadd("x", "y1", "y2", "y3") shouldBe 3
        client.smismember("x", "y1", "y2", "z1") shouldContainExactlyInAnyOrder listOf(1, 1, 0)
    }

    test("Pipeline Set commands >= API 6.2").config(enabledOrReasonIf = clientSetup.enableIf(REDIS_6_2_0)) {
        val responseList = mutableListOf<ResponseType<Any>>()
        val pipe = client.pipelined()
        responseList += pipe.sadd("x", "y1", "y2", "y3").toResponseType()
        responseList += pipe.smismember("x", "y1", "y2", "z1").toResponseType()
        pipe.execute()

        @Suppress("UNCHECKED_CAST")
        run {
            var i = 0
            responseList[i++].get() shouldBe 3
            responseList[i].get() as List<Long> shouldContainExactlyInAnyOrder listOf(1, 1, 0)
        }
    }

    test("Set Commands >= API 6").config(enabledOrReasonIf = clientSetup.enableIf(REDIS_6_0_0)) {
        client.scard("set") shouldBe 0
        client.scard("set1") shouldBe 0

        client.sadd("set", "mem1", "mem2", "mem3") shouldBe 3
        client.sadd("set1", "mem2", "mem3", "mem4") shouldBe 3

        client.scard("set") shouldBe 3
        client.scard("set1") shouldBe 3

        client.sdiff("set", "set1") shouldContainExactlyInAnyOrder listOf("mem1")

        client.sdiffstore("set2", "set", "set1") shouldBe 1

        client.sinter("set", "set1") shouldContainExactlyInAnyOrder listOf("mem2", "mem3")

        client.sinterstore("set3", "set", "set1") shouldBe 2

        client.sismember("set3", "mem2") shouldBe 1

        client.smembers("set3") shouldContainExactlyInAnyOrder listOf("mem2", "mem3")

        client.smove("set", "set3", "mem1") shouldBe 1

        client.srandmember("set") shouldNotBe null
        client.srandmember("set", 2) shouldHaveSize 2
        client.srandmember("non-existing") shouldBe null

        client.scard("set") shouldBe 2
        client.spop("set") shouldNotBe null
        client.spop("set", 2) shouldHaveSize 1 // empty the "set"
        client.scard("set") shouldBe 0

        client.sunion("set", "set1") shouldContainExactlyInAnyOrder listOf("mem2", "mem3", "mem4")

        client.sunionstore("set", "set", "set1") shouldBe 3
        client.scard("set") shouldBe 3

        client.srem("set", "mem2", "mem3", "mem4") shouldBe 3
        client.scard("set") shouldBe 0

        //SSCAN -- start
        for (i in 1..1000) {
            client.sadd("sscan", "mem$i")
        }
        var count = 0
        var scanCount = 1
        var sScanResult = client.sscan("sscan", 0, "mem*", 100)
        do {
            count += sScanResult.elements.size
            sScanResult = client.sscan("sscan", sScanResult.cursor, "mem*", 100)
            ++scanCount
        } while (sScanResult.cursor != 0L)

        scanCount shouldBeGreaterThan 1
        // count shouldBe 1000 //TODO: fix this later
        //SSCAN -- end
    }

    test("Pipeline Set Commands >= API 6").config(enabledOrReasonIf = clientSetup.enableIf(REDIS_6_0_0)) {
        val pipe = client.pipelined()
        val responseList = mutableListOf<ResponseType<Any>>()
        responseList += pipe.scard("set").toResponseType()
        responseList += pipe.scard("set1").toResponseType()

        responseList += pipe.sadd("set", "mem1", "mem2", "mem3").toResponseType()
        responseList += pipe.sadd("set1", "mem2", "mem3", "mem4").toResponseType()

        responseList += pipe.scard("set").toResponseType()
        responseList += pipe.scard("set1").toResponseType()

        responseList += pipe.sdiff("set", "set1").toResponseType()

        responseList += pipe.sdiffstore("set2", "set", "set1").toResponseType()

        responseList += pipe.sinter("set", "set1").toResponseType()

        responseList += pipe.sinterstore("set3", "set", "set1").toResponseType()

        responseList += pipe.sismember("set3", "mem2").toResponseType()

        responseList += pipe.smembers("set3").toResponseType()

        responseList += pipe.smove("set", "set3", "mem1").toResponseType()

        responseList += pipe.srandmember("set").toResponseType()

        responseList += pipe.srandmember("set", 2).toResponseType()
        responseList += pipe.srandmember("non-existing").toResponseType()

        responseList += pipe.scard("set").toResponseType()
        responseList += pipe.spop("set").toResponseType()
        responseList += pipe.spop("set", 2).toResponseType() // empty the "set"
        responseList += pipe.scard("set").toResponseType()

        responseList += pipe.sunion("set", "set1").toResponseType()

        responseList += pipe.sunionstore("set", "set", "set1").toResponseType()
        responseList += pipe.scard("set").toResponseType()

        responseList += pipe.srem("set", "mem2", "mem3", "mem4").toResponseType()
        responseList += pipe.scard("set").toResponseType()

        pipe.execute()

        @Suppress("UNCHECKED_CAST")
        run {
            var i = 0
            responseList[i++].get() shouldBe 0
            responseList[i++].get() shouldBe 0
            responseList[i++].get() shouldBe 3
            responseList[i++].get() shouldBe 3
            responseList[i++].get() shouldBe 3
            responseList[i++].get() shouldBe 3
            responseList[i++].get() as List<String> shouldContainExactlyInAnyOrder listOf("mem1")
            responseList[i++].get() shouldBe 1
            responseList[i++].get() as List<String> shouldContainExactlyInAnyOrder listOf("mem2", "mem3")
            responseList[i++].get() shouldBe 2
            responseList[i++].get() shouldBe 1
            responseList[i++].get() as List<String> shouldContainExactlyInAnyOrder listOf("mem2", "mem3")
            responseList[i++].get() shouldBe 1
            responseList[i++].get() shouldNotBe null
            responseList[i++].get() as List<*> shouldHaveSize 2
            responseList[i++].get() shouldBe null
            responseList[i++].get() shouldBe 2
            responseList[i++].get() shouldNotBe null
            responseList[i++].get() as List<*> shouldHaveSize 1 // empty the "set"
            responseList[i++].get() shouldBe 0
            responseList[i++].get() as List<String> shouldContainExactlyInAnyOrder listOf("mem2", "mem3", "mem4")
            responseList[i++].get() shouldBe 3
            responseList[i++].get() shouldBe 3
            responseList[i++].get() shouldBe 3
            responseList[i].get() shouldBe 0

        }
    }
})