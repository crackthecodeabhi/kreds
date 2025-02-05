/*
 *  Copyright (C) 2024 Abhijith Shivaswamy
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

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldHaveKey
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe

class ConfigCommandsTest : FunSpec({
    lateinit var c: ConfigCommands
    val clientSetup = ClientSetup().then { c = it.client }
    beforeSpec(clientSetup)
    afterSpec(ClientTearDown(clientSetup))

    test("Config commands > Set").config(enabledOrReasonIf = clientSetup.enableIf(REDIS_6_0_0)) {
        c.configSet("notify-keyspace-events", "AKE") shouldBe "OK"
        c.configGet("notify-keyspace-events") shouldBe mapOf("notify-keyspace-events" to "AKE")
        c.configSet("notify-keyspace-events", "") shouldBe "OK"
    }

    test("Config commands > Get").config(enabledOrReasonIf = clientSetup.enableIf(REDIS_6_0_0)) {
        c.configGet("notify-keyspace-events") shouldBe mapOf("notify-keyspace-events" to "")
    }

    test("Config commands > Get Multi-Map").config(enabledOrReasonIf = clientSetup.enableIf(REDIS_6_0_0)) {
        c.configGet("notify-keyspace-events", "slowlog-max-len") shouldHaveSize 2
        c.configGet("notify-keyspace-events", "slowlog-max-len") shouldHaveKey "notify-keyspace-events"
        c.configGet("notify-keyspace-events", "slowlog-max-len") shouldHaveKey "slowlog-max-len"
        c.configGet("slowlog-max-*") shouldHaveKey "slowlog-max-len"
    }

})