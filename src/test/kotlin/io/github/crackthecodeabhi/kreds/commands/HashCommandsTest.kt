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
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.math.BigDecimal

class HashCommandsTest : FunSpec({
    lateinit var client: KredsClient
    val clientSetup = ClientSetup().then { client = it.client }
    beforeSpec(clientSetup)
    afterSpec(ClientTearDown(clientSetup))

    test("Hash commands >= API 6.2").config(enabledOrReasonIf = clientSetup.enableIf(REDIS_6_2_0)) {
        client.hrandfield("non-existing-random") shouldBe null
        client.hrandfield("non-existing-random", 5, true) shouldHaveSize 0

        client.hset("random", "rand1" to "value1", "rand2" to "value2") shouldBe 2

        client.hrandfield("random") shouldNotBe null

        client.hrandfield("random", 1) shouldHaveSize 1

        client.hrandfield("random", 2, true) shouldHaveSize 4
    }

    test("Hash commands >= API 6").config(enabledOrReasonIf = clientSetup.enableIf(REDIS_6_0_0)) {
        client.hexists("hkey", "key") shouldBe 0
        client.hset("hkey", "key" to "value", "key1" to "value1")
        client.hexists("hkey", "key") shouldBe 1
        client.hlen("hkey") shouldBe 2
        client.hget("hkey", "key") shouldBe "value"
        client.hget("hkey", "non-existing") shouldBe null
        client.hstrlen("hkey", "key") shouldBe "value".length
        client.hgetAll("hkey") shouldContainAll listOf("key", "value", "key1", "value1")
        client.hkeys("hkey") shouldContainAll listOf("key", "key1")
        client.hkeys("non-existing") shouldHaveSize 0
        client.hvals("hkey") shouldContainAll listOf("value", "value1")
        client.hvals("non-existing") shouldHaveSize 0
        client.hmget("hkey", "key", "key1", "non-existing") shouldContainInOrder listOf("value", "value1", null)

        client.hsetnx("hkey", "number-key", "100") shouldBe 1
        client.hsetnx("hkey", "number-key", "150") shouldBe 0

        client.hincrBy("hkey", "number-key", 10) shouldBe 110
        client.hincrBy("hkey", "number-key", -60) shouldBe 50

        client.hincrByFloat("hkey", "number-key", BigDecimal.valueOf(0.005)) shouldBe "50.005"
        client.hincrByFloat("hkey", "number-key", BigDecimal.valueOf(-0.005)) shouldBe "50"

        client.hdel("hkey", "key", "key1") shouldBe 2
        client.exists("key", "key1") shouldBe 0

        // HSCAN - start
        for (i in 1..1000) {
            client.hset("hscan", "hscan-field$i" to "value$i")
        }
        client.hset("hscan", "field1" to "value1") // to test match pattern
        var count = 0
        var scanCount = 1
        var hSR = client.hscan("hscan", 0, "hscan-field*", 200)
        do {
            count += hSR.elements.size
            hSR = client.hscan("hscan", hSR.cursor, "hscan-field*", 200)
            ++scanCount
        } while (hSR.cursor != 0L)

        count shouldBeInRange 1990..2010 // TODO: why this inconsistency?
        println("hscan iteration count = $count")
        scanCount shouldBeGreaterThan 1 // assert multiple scan iterations

        //HSCAN - end
    }
})