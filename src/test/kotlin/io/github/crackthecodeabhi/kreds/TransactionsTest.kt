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

package io.github.crackthecodeabhi.kreds

import io.github.crackthecodeabhi.kreds.commands.ClearDB
import io.github.crackthecodeabhi.kreds.commands.ClientSetup
import io.github.crackthecodeabhi.kreds.commands.ClientTearDown
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class HashCommandsTest : FunSpec({
    lateinit var client: KredsClient
    val clientSetup = ClientSetup().then { client = it.client }
    beforeSpec(clientSetup)
    afterSpec(ClientTearDown(clientSetup))
    beforeTest(ClearDB(clientSetup))

    test("Simple Transaction").config(enabled = false) {
        val txn = client.transaction()
        txn.multi()
        txn.set("txn", "test")
        txn.set("txn_id", "1")
        val resp1 = txn.get("txn")
        val resp2 = txn.get("txn_id")
        txn.exec()

        resp1() shouldBe "txn"
        resp2() shouldBe "1"
    }
})