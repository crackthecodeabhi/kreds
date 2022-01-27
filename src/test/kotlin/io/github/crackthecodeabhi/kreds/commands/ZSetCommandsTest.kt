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

class ZSetCommandsTest : FunSpec({
    lateinit var client: KredsClient
    val clientSetup = ClientSetup().then { client = it.client }
    beforeSpec(clientSetup)
    afterSpec(ClientTearDown(clientSetup))
    beforeTest(ClearDB(clientSetup))

    test("ZSet Commands >= API 6").config(enabledOrReasonIf = clientSetup.enableIf(REDIS_6_0_0)) {
        client.zadd("zset", scoreMember = 1 to "kreds", scoreMembers = arrayOf(2 to "redis")) shouldBe 2
        client.zcard("zset") shouldBe 2

        client.zadd("zset1", scoreMember = 10 to "A", scoreMembers = arrayOf(20 to "B"))
        client.zcount("zset1", 1, 10) shouldBe 1

        client.zincrBy("zset", 1.5, "kreds") shouldBe "2.5"

    }

    test("ZSet Commands >= API 6.2").config(enabledOrReasonIf = clientSetup.enableIf(REDIS_6_2_0)) {
        client.zadd("zset", scoreMember = 1 to "kreds", scoreMembers = arrayOf(2 to "redis")) shouldBe 2
        client.zcard("zset") shouldBe 2

        client.zadd("zset1", scoreMember = 10 to "A", scoreMembers = arrayOf(20 to "B"))
        client.zcount("zset1", 1, 10) shouldBe 1
    }
})