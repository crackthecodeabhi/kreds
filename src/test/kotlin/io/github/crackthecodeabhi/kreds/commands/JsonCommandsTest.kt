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

import io.github.crackthecodeabhi.kreds.commands.JsonCommands.Companion.ROOT_PATH
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class JsonCommandsTest : FunSpec({
    lateinit var client: KredsClient
    val clientSetup = ClientSetup().then { client = it.client }
    beforeSpec(clientSetup)
    afterSpec(ClientTearDown(clientSetup))
    beforeTest(ClearDB(clientSetup))

    test("RedisJSON commands").config(enabledOrReasonIf = clientSetup.enableIfModulePresent("ReJSON")) {
        val key = "doc"

        // JSON.ARRAPPEND
        client.jsonSet(
            key,
            ROOT_PATH,
            "{\"a\":[1], \"nested\": {\"a\": [1,2]}, \"nested2\": {\"a\": 42}}"
        ) shouldBe true
        client.jsonArrAppend(key, "$..a", "3", "4") shouldBe listOf(3, 4, null)
        client.jsonGet(
            key,
            ROOT_PATH
        ) shouldBe "[{\"a\":[1,3,4],\"nested\":{\"a\":[1,2,3,4]},\"nested2\":{\"a\":42}}]"

        // JSON.ARRINDEX
        client.jsonSet(key, ROOT_PATH, "{\"a\":[1,2,3,2], \"nested\": {\"a\": [3,4]}}") shouldBe true
        client.jsonArrIndex(key, "$..a", "2") shouldBe listOf(1, -1)
        client.jsonSet(key, ROOT_PATH, "{\"a\":[1,2,3,2], \"nested\": {\"a\": false}}") shouldBe true
        client.jsonArrIndex(key, "$..a", "2") shouldBe listOf(1, null)

        // JSON.ARRINSERT
        client.jsonSet(key, ROOT_PATH, "{\"a\":[3], \"nested\": {\"a\": [3,4]}}") shouldBe true
        client.jsonArrInsert(key, "$..a", 0, "1", "2") shouldBe listOf(3, 4)
        client.jsonGet(key, ROOT_PATH) shouldBe "[{\"a\":[1,2,3],\"nested\":{\"a\":[1,2,3,4]}}]"
        client.jsonSet(key, ROOT_PATH, "{\"a\":[1,2,3,2], \"nested\": {\"a\": false}}") shouldBe true
        client.jsonArrInsert(key, "$..a", 0, "1", "2") shouldBe listOf(6, null)

        // JSON.ARRLEN
        client.jsonSet(key, ROOT_PATH, "{\"a\":[3], \"nested\": {\"a\": [3,4]}}") shouldBe true
        client.jsonArrLen(key, "$..a") shouldBe listOf(1, 2)
        client.jsonSet(key, ROOT_PATH, "{\"a\":[1,2,3,2], \"nested\": {\"a\": false}}") shouldBe true
        client.jsonArrLen(key, "$..a") shouldBe listOf(4, null)

        // JSON.ARRPOP
        client.jsonSet(key, ROOT_PATH, "{\"a\":[3], \"nested\": {\"a\": [3,4]}}") shouldBe true
        client.jsonArrPop(key, "$..a") shouldBe listOf("3", "4")
        client.jsonGet(key, ROOT_PATH) shouldBe "[{\"a\":[],\"nested\":{\"a\":[3]}}]"
        client.jsonSet(
            key,
            ROOT_PATH,
            "{\"a\":[\"foo\", \"bar\"], \"nested\": {\"a\": false}, \"nested2\": {\"a\":[]}}"
        ) shouldBe true
        client.jsonArrPop(key, "$..a") shouldBe listOf("\"bar\"", null, null)

        // JSON.ARRTRIM
        client.jsonSet(key, ROOT_PATH, "{\"a\":[], \"nested\": {\"a\": [1,4]}}") shouldBe true
        client.jsonArrTrim(key, "$..a", 1, 1) shouldBe listOf(0, 1)
        client.jsonGet(key, ROOT_PATH) shouldBe "[{\"a\":[],\"nested\":{\"a\":[4]}}]"

        client.jsonSet(key, ROOT_PATH, "{\"a\":[1,2,3,2], \"nested\": {\"a\": false}}") shouldBe true
        client.jsonArrTrim(key, "$..a", 1, 1) shouldBe listOf(1, null)
        client.jsonGet(key, ROOT_PATH) shouldBe "[{\"a\":[2],\"nested\":{\"a\":false}}]"

        // JSON.CLEAR
        client.jsonSet(key, ROOT_PATH, "{\"obj\":{\"a\":1, \"b\":2}, \"arr\":[1,2,3], \"str\": \"foo\", \"bool\": true, \"int\": 42, \"float\": 3.14}") shouldBe true
        client.jsonClear(key, "$.*") shouldBe 6
        client.jsonGet(key, ROOT_PATH) shouldBe "[{\"obj\":{},\"arr\":[],\"str\":\"\",\"bool\":false,\"int\":0,\"float\":0}]"
    }
})
