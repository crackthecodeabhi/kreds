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
import io.github.crackthecodeabhi.kreds.connection.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldHaveMinLength

private lateinit var client: KredsClient
private lateinit var c: ConnectionCommands
private lateinit var serverVersion: String

class ConnectionCommandsTest : FunSpec({
    beforeSpec {
        client = getTestClient(config = KredsClientConfig.Builder(readTimeoutSeconds = 1).build(defaultClientConfig))
        client.flushAll(SyncOption.SYNC)
        serverVersion = client.serverVersion()
        c = client
    }
    afterSpec {
        client.close()
    }

    test("7.0.0+ Commands") {
        if (serverVersion == "7.0.0") {
            c.clientNoEvict(true) shouldBe "OK"
        }
    }

    test("Connection commands") {
        c.clientId() shouldBeGreaterThan 0
        c.clientInfo() shouldHaveMinLength 1
        c.clientSetname("CONN_TEST_NAME") shouldBe "OK"
        c.clientGetName() shouldBe "CONN_TEST_NAME"
        c.echo("ECHO_TEST") shouldBe "ECHO_TEST"
        c.ping("PING_TEST") shouldBe "PING_TEST"
    }
})