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
import io.github.crackthecodeabhi.kreds.args.SyncOption
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.github.crackthecodeabhi.kreds.protocol.KredsRedisDataException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal

private lateinit var client: KredsClient
private lateinit var c: StringCommands

class StringCommandsTest : BehaviorSpec({
    beforeSpec {
        client = getTestClient()
        client.flushAll(SyncOption.SYNC)
        c = client
    }
    afterSpec {
        client.close()
    }

    Given("set") {
        When("new key") {
            Then("return OK") {
                c.set("newKey", "100") shouldBe "OK"
            }
        }
        When("existing key with GET option") {
            Then("return old value") {
                c.set("newKey", "101", SetOption.Builder(get = true).build()) shouldBe "100"
            }
        }
        When("key does not exist with GET option") {
            Then("return null") {
                c.set("noKey", "100", SetOption.Builder(get = true).build()) shouldBe null
            }
        }
    }

    Given("get") {
        When("non-existing key") {
            Then("return null") {
                c.get("non-existing") shouldBe null
            }
        }
        When("non-string type") {
            Then("throws exception") {
                client.hset("hashKey", "foo" to "bar")
                shouldThrow<KredsRedisDataException> {
                    c.get("hashKey")
                }
            }
        }
    }

    Given("append") {
        When("append to existing key") {
            Then("length of string after append") {
                c.set("appendKey", "abc")
                c.append("appendKey", "xyz") shouldBe 6
            }
        }
    }
})

class StringCommandTestFunc : FunSpec({
    beforeSpec {
        client = getTestClient()
        client.flushAll(SyncOption.SYNC)
        c = client
    }
    afterSpec {
        client.close()
    }

    test("increment/decrement") {
        c.set("number", "100")
        c.incr("number") shouldBe 101
        c.decr("number") shouldBe 100
        c.incrBy("number", 10) shouldBe 110
        c.decrBy("number", 20) shouldBe 90
        c.incrByFloat("number", BigDecimal.valueOf(0.00005)) shouldBe "90.00005"
    }
})