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

import io.github.crackthecodeabhi.kreds.args.BeforeAfterOption
import io.github.crackthecodeabhi.kreds.args.LeftRightOption
import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.github.crackthecodeabhi.kreds.connection.newBlockingClient
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

class ListCommandsTest : FunSpec({
    lateinit var client: KredsClient
    val clientSetup = ClientSetup().then { client = it.client }
    beforeSpec(clientSetup)
    afterSpec(ClientTearDown(clientSetup))
    beforeTest(ClearDB(clientSetup))

    test("Blocking List commands >= API 6").config(enabledOrReasonIf = clientSetup.enableIf(REDIS_6_0_0)) {
        newBlockingClient(Endpoint.from("127.0.0.1:6379")).use { blockingClient ->
            val result = async { blockingClient.blpop("waitlist", "waitlist1", timeout = 2.seconds) }
            delay(1000)
            client.lpush("waitlist1", "x") shouldBe 1
            val (list, element) = result.await()!!
            list shouldBe "waitlist1"
            element shouldBe "x"
        }
    }

    test("List commands >= API 6").config(enabledOrReasonIf = clientSetup.enableIf(REDIS_6_0_0)) {
        client.lpushx("list", "n1", "n2") shouldBe 0
        client.rpushx("list", "n1", "n2") shouldBe 0
        client.llen("list") shouldBe 0
        client.lpush("list", "e1", "e2") shouldBe 2
        client.llen("list") shouldBe 2
        client.rpush("list", "e3", "e4") shouldBe 4
        client.lpushx("list", "e0", "e00") shouldBe 6
        client.rpushx("list", "e5", "e6") shouldBe 8

        client.lrange("list", 0, 7) shouldContainInOrder listOf("e00", "e0", "e2", "e1", "e3", "e4", "e5", "e6")

        client.lindex("list", 0) shouldBe "e00"
        client.lindex("list", 100) shouldBe null

        client.linsert("list", BeforeAfterOption.BEFORE, "e3", "e22") shouldBe 9

        client.lrange("list", 0, 8) shouldContainInOrder listOf("e00", "e0", "e2", "e1", "e22", "e3", "e4", "e5", "e6")

        client.lset("list", 0, "e000") shouldNotBe null
        client.lindex("list", 0) shouldBe "e000"

        client.lpush("another", "x", "y", "z") shouldBe 3
        client.lmove("list", "another", LeftRightOption.RIGHT, LeftRightOption.LEFT) shouldBe "e6"
        client.lrange("another", 0, 3) shouldContainInOrder listOf("e6", "z", "y", "x")

        client.ltrim("another", 0, 2) shouldNotBe null
        client.lrange("another", 0, 2) shouldContainInOrder listOf("e6", "z", "y")
        client.lindex("another", 3) shouldBe null

        client.lrange("list", 0, 8) shouldContainInOrder listOf("e000", "e0", "e2", "e1", "e22", "e3", "e4", "e5")

        client.lpop("list") shouldBe "e000"
        client.lpop("nolist") shouldBe null
        client.lpop("list", 3)!! shouldContainInOrder listOf("e0", "e2", "e1")
        // client.lpop("nolist", 100) shouldBe null TODO: fix this after verification

        client.lrange("list", 0, 3) shouldContainInOrder listOf("e22", "e3", "e4", "e5")

        client.lrem("list", 0, "e22") shouldBe 1

        client.rpop("list") shouldBe "e5"
        client.rpop("list", 2)!! shouldContainInOrder listOf("e4", "e3")
        client.llen("list") shouldBe 0
    }
})