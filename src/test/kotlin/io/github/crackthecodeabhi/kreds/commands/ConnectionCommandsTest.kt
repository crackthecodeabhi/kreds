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

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldHaveMinLength

class ConnectionCommandsTest : FunSpec({
    lateinit var c: ConnectionCommands
    val clientSetup = ClientSetup().then { c = it.client }
    beforeSpec(clientSetup)
    afterSpec(ClientTearDown(clientSetup))

    test("Connection commands > API 7").config(enabledOrReasonIf = clientSetup.enableIf(REDIS_7_0_0)) {
        c.clientNoEvict(true) shouldBe "OK"
    }

    test("Connection commands > API 6.2").config(enabledOrReasonIf = clientSetup.enableIf(REDIS_6_2_0)) {
        c.clientInfo() shouldHaveMinLength 1
    }

    test("Connection commands > API 6").config(enabledOrReasonIf = clientSetup.enableIf(REDIS_6_0_0)) {
        c.clientId() shouldBeGreaterThan 0
        c.clientSetname("CONN_TEST_NAME") shouldBe "OK"
        c.clientGetName() shouldBe "CONN_TEST_NAME"
        c.echo("ECHO_TEST") shouldBe "ECHO_TEST"
        c.ping("PING_TEST") shouldBe "PING_TEST"
    }
})