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

import io.github.crackthecodeabhi.kreds.commands.*
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.github.crackthecodeabhi.kreds.pipeline.KredsTransactionException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TransactionsTest : FunSpec({
    lateinit var client: KredsClient
    val clientSetup = ClientSetup().then { client = it.client }
    beforeSpec(clientSetup)
    afterSpec(ClientTearDown(clientSetup))
    beforeTest(ClearDB(clientSetup))

    test("Simple Transaction") {
        val txn = client.transaction()
        txn.multi()
        txn.set("txn", "test")
        txn.set("txn_id", "1")
        val resp1 = txn.get("txn")
        val resp2 = txn.get("txn_id")
        txn.exec()

        resp1() shouldBe "test"
        resp2() shouldBe "1"
    }

    test("Transaction with Watch") {
        client.set("kred", "1")!!.shouldBeOk()
        client.set("kred2", "2")!!.shouldBeOk()
        val txn = client.transaction()
        txn.watch("kred", "kred2")
        txn.multi()
        val setResponse = txn.set("kred", "3")
        val incrResponse = txn.incr("kred2")
        txn.exec()

        setResponse()!!.shouldBeOk()
        incrResponse() shouldBe 3
    }

    // How do we simulate a WATCH failure?
    test("Transaction with Watch aborted").config(enabled = false) {
        client.set("kred", "1")!!.shouldBeOk()
        client.set("kred2", "2")!!.shouldBeOk()

        val txn = client.transaction()
        txn.watch("kred", "kred2") //WATCH for any changes.
        txn.multi()

        val client2 = getTestClient()
        client2.set("kred", "SOME OTHER VALUE") // change kred key from different connection
        client2.close()

        txn.set("kred", "3")
        txn.incr("kred2")
        shouldThrow<KredsTransactionException> { txn.exec() } // Transaction should fail because exec returned null.
    }

    //TODO: Test case for 1. MULTI/WATCH is called in transaction, 2. Other commands are called before starting a transaction except WATCH
})