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

import io.github.crackthecodeabhi.kreds.args.SyncOption
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

private lateinit var client: KredsClient
private lateinit var c: HyperLogLogCommands

class HyperLogLogCommandsTest : BehaviorSpec({

    beforeSpec {
        client = getTestClient()
        client.flushAll(SyncOption.SYNC)
        c = client
    }
    afterSpec {
        client.close()
    }

    Given("pfadd") {
        When("new key") {
            Then("returns 1") {
                c.pfadd("newkey", "element1", "element2") shouldBe 1
            }
        }
    }
})