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

import io.github.crackthecodeabhi.kreds.args.ExpireOption
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.github.crackthecodeabhi.kreds.pipeline.Response
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.delay
import kotlin.reflect.KClass
import kotlin.reflect.cast

class KeyCommandsTest : FunSpec({
    lateinit var c: KeyCommands
    lateinit var client: KredsClient
    val clientSetup = ClientSetup().then { c = it.client; client = it.client }
    beforeSpec(clientSetup)
    afterSpec(ClientTearDown(clientSetup))

    test("Key Commands - API 7").config(enabledOrReasonIf = clientSetup.enableIf(REDIS_7_0_0)) {
        client.set("redis7", "redis7")
        c.expire("redis7", 1u, ExpireOption.NX) shouldBe 1
        delay(1500)
        c.exists("redis7") shouldBe 0
    }

    test("Key commands - API 6").config(enabledOrReasonIf = clientSetup.enableIf(REDIS_6_0_0)) {
        c.randomKey() shouldBe null
        c.keys("*") shouldHaveSize 0
        c.exists("key1", "key2") shouldBe 0
        client.set("key1", "value1")
        client.set("key2", "value2")
        c.keys("key*") shouldHaveSize 2
        c.exists("key1") shouldBe 1
        c.randomKey() shouldNotBe null
        c.dump("key1") shouldNotBe null
        c.del("key1") shouldBe 1
        c.expire("key2", 1u) shouldBe 1
        delay(1500)
        c.exists("key2") shouldBe 0
    }

    /* Executes the above test case in a pipeline. */
    test("Pipeline Key commands - API 6").config(enabledOrReasonIf = clientSetup.enableIf(REDIS_6_0_0)) {
        val pipeline = client.pipelined()
        val responseList = mutableListOf<Pair<Response<*>, KClass<*>?>>()
        responseList += Pair(pipeline.randomKey(), null)
        responseList += Pair(pipeline.keys("*"), List::class)
        responseList += Pair(pipeline.exists("key1", "key2"), Long::class)
        responseList += Pair(pipeline.set("key1", "value1"), String::class)
        responseList += Pair(pipeline.set("key2", "value2"), String::class)
        responseList += Pair(pipeline.keys("key*"), List::class)
        responseList += Pair(pipeline.exists("key1"), Long::class)
        responseList += Pair(pipeline.randomKey(), String::class)
        responseList += Pair(pipeline.dump("key1"), String::class)
        responseList += Pair(pipeline.del("key1"), Long::class)
        responseList += Pair(pipeline.expire("key2", 1u), Long::class)
        pipeline.execute()
        val resultList =
            responseList.map {
                if (it.second == null) it.first.get() else it.second!!.cast(it.first.get())
            }
        resultList[0] shouldBe null
        resultList[1] as List<*> shouldHaveSize 0
        resultList[2] shouldBe 0
        (resultList[3] as String).shouldBeOk()
        (resultList[4] as String).shouldBeOk()
        resultList[5] as List<*> shouldHaveSize 2
        resultList[6] shouldBe 1
        resultList[7] shouldNotBe null
        resultList[8] shouldNotBe null
        resultList[9] shouldBe 1
        resultList[10] shouldBe 1
        delay(1500)
        c.exists("key2") shouldBe 0
    }
})