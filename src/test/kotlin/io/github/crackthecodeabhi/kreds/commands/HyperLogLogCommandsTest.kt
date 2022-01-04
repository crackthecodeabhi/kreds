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
import io.kotest.matchers.shouldBe

class HyperLogLogCommandsTest : FunSpec({
    lateinit var c: HyperLogLogCommands
    lateinit var client: KredsClient
    val clientSetup = ClientSetup().then { c = it.client; client = it.client }
    beforeSpec(clientSetup)
    afterSpec(ClientTearDown(clientSetup))
    beforeTest(ClearDB(clientSetup))

    test("HyperLogLog Commands >= API 6").config(enabledOrReasonIf = clientSetup.enableIf(REDIS_6_0_0)) {
        c.pfadd("newkey", "element1", "element2") shouldBe 1
        c.pfadd("newkey1", "element1", "element2") shouldBe 1
        c.pfcount("newkey", "newkey1") shouldBe 2
        c.pfmerge("newkey", "newkey1").shouldBeOk()
    }

    test("Pipelined Hyperloglog commands >= API 6") {
        val pipe = client.pipelined()
        val responseList = mutableListOf<ResponseType<Any>>()
        responseList += pipe.pfadd("pipekey", "element1", "element2").toResponseType()
        responseList += pipe.pfadd("pipekey1", "element1", "element2").toResponseType()
        responseList += pipe.pfcount("pipekey", "pipekey1").toResponseType()
        responseList += pipe.pfmerge("pipekey", "pipekey1").toResponseType()
        pipe.execute()
        var i = 0
        responseList[i++].get() shouldBe 1
        responseList[i++].get() shouldBe 1
        responseList[i++].get() shouldBe 2
        (responseList[i].get() as String).shouldBeOk()
    }
})