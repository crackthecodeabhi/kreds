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
            "$",
            "{\"a\":[1], \"nested\": {\"a\": [1,2]}, \"nested2\": {\"a\": 42}}"
        ) shouldBe true
        client.jsonArrAppend(key, "$..a", "3", "4") shouldBe listOf(3, 4, null)
        client.jsonGet(
            key,
            "$"
        ) shouldBe "[{\"a\":[1,3,4],\"nested\":{\"a\":[1,2,3,4]},\"nested2\":{\"a\":42}}]"

        // JSON.ARRINDEX
        client.jsonSet(key, "$", "{\"a\":[1,2,3,2], \"nested\": {\"a\": [3,4]}}") shouldBe true
        client.jsonArrIndex(key, "$..a", "2") shouldBe listOf(1, -1)
        client.jsonSet(key, "$", "{\"a\":[1,2,3,2], \"nested\": {\"a\": false}}") shouldBe true
        client.jsonArrIndex(key, "$..a", "2") shouldBe listOf(1, null)

        // JSON.ARRINSERT
        client.jsonSet(key, "$", "{\"a\":[3], \"nested\": {\"a\": [3,4]}}") shouldBe true
        client.jsonArrInsert(key, "$..a", 0, "1", "2") shouldBe listOf(3, 4)
        client.jsonGet(key, "$") shouldBe "[{\"a\":[1,2,3],\"nested\":{\"a\":[1,2,3,4]}}]"
        client.jsonSet(key, "$", "{\"a\":[1,2,3,2], \"nested\": {\"a\": false}}") shouldBe true
        client.jsonArrInsert(key, "$..a", 0, "1", "2") shouldBe listOf(6, null)

        // JSON.ARRLEN
        client.jsonSet(key, "$", "{\"a\":[3], \"nested\": {\"a\": [3,4]}}") shouldBe true
        client.jsonArrLen(key, "$..a") shouldBe listOf(1, 2)
        client.jsonSet(key, "$", "{\"a\":[1,2,3,2], \"nested\": {\"a\": false}}") shouldBe true
        client.jsonArrLen(key, "$..a") shouldBe listOf(4, null)

        // JSON.ARRPOP
        client.jsonSet(key, "$", "{\"a\":[3], \"nested\": {\"a\": [3,4]}}") shouldBe true
        client.jsonArrPop(key, "$..a") shouldBe listOf("3", "4")
        client.jsonGet(key, "$") shouldBe "[{\"a\":[],\"nested\":{\"a\":[3]}}]"
        client.jsonSet(
            key,
            "$",
            "{\"a\":[\"foo\", \"bar\"], \"nested\": {\"a\": false}, \"nested2\": {\"a\":[]}}"
        ) shouldBe true
        client.jsonArrPop(key, "$..a") shouldBe listOf("\"bar\"", null, null)

        // JSON.ARRTRIM
        client.jsonSet(key, "$", "{\"a\":[], \"nested\": {\"a\": [1,4]}}") shouldBe true
        client.jsonArrTrim(key, "$..a", 1, 1) shouldBe listOf(0, 1)
        client.jsonGet(key, "$") shouldBe "[{\"a\":[],\"nested\":{\"a\":[4]}}]"

        client.jsonSet(key, "$", "{\"a\":[1,2,3,2], \"nested\": {\"a\": false}}") shouldBe true
        client.jsonArrTrim(key, "$..a", 1, 1) shouldBe listOf(1, null)
        client.jsonGet(key, "$") shouldBe "[{\"a\":[2],\"nested\":{\"a\":false}}]"

        // JSON.CLEAR
        client.jsonSet(key, "$", "{\"obj\":{\"a\":1, \"b\":2}, \"arr\":[1,2,3], \"str\": \"foo\", \"bool\": true, \"int\": 42, \"float\": 3.14}") shouldBe true
        client.jsonClear(key, "$.*") shouldBe 4
        client.jsonGet(key, "$") shouldBe "[{\"obj\":{},\"arr\":[],\"str\":\"foo\",\"bool\":true,\"int\":0,\"float\":0}]"

        // JSON.DEL
        client.jsonSet(key, "$", "{\"a\": 1, \"nested\": {\"a\": 2, \"b\": 3}}") shouldBe true
        client.jsonDel(key, "$..a") shouldBe 2
        client.jsonGet(key, "$") shouldBe "[{\"nested\":{\"b\":3}}]"

        // JSON.GET
        client.jsonSet(key, "$", "{\"a\":2, \"b\": 3, \"nested\": {\"a\": 4, \"b\": null}}") shouldBe true
        client.jsonGet(key, "$..b") shouldBe "[3,null]"

        // JSON.MGET
        client.jsonSet("doc1", "$", "{\"a\":1, \"b\": 2, \"nested\": {\"a\": 3}, \"c\": null}") shouldBe true
        client.jsonSet("doc2", "$", "{\"a\":4, \"b\": 5, \"nested\": {\"a\": 6}, \"c\": null}") shouldBe true
        client.jsonMGet("doc1", "doc2", path = "$..a") shouldBe listOf("[1,3]", "[4,6]")

        // JSON.NUMINCRBY
        client.jsonSet(key, "$", "{\"a\":\"b\",\"b\":[{\"a\":2}, {\"a\":5}, {\"a\":\"c\"}]}") shouldBe true
        client.jsonNumIncrBy(key, "$.a", 2) shouldBe "[null]"
        client.jsonNumIncrBy(key, "$..a", 2) shouldBe "[null,4,7,null]"

        // JSON.OBJKEYS
        client.jsonSet(key, "$", "{\"a\":[3], \"nested\": {\"a\": {\"b\":2, \"c\": 1}}}") shouldBe true
        client.jsonObjKeys(key, "$..a") shouldBe listOf(null, listOf("b", "c"))

        // JSON.OBJLEN
        client.jsonSet(key, "$", "{\"a\":[3], \"nested\": {\"a\": {\"b\":2, \"c\": 1}}}") shouldBe true
        client.jsonObjLen(key, "$..a") shouldBe listOf(null, 2)

        // JSON.SET
        client.jsonSet(key, "$", "{\"a\":2}") shouldBe true
        client.jsonSet(key, "$.a", "3") shouldBe true
        client.jsonGet(key, "$") shouldBe "[{\"a\":3}]"

        client.jsonSet(key, "$", "{\"a\":2}") shouldBe true
        client.jsonSet(key, "$.b", "8") shouldBe true
        client.jsonGet(key, "$") shouldBe "[{\"a\":2,\"b\":8}]"

        client.jsonSet(key, "$", "{\"f1\": {\"a\":1}, \"f2\":{\"a\":2}}") shouldBe true
        client.jsonSet(key, "$..a", "3") shouldBe true
        client.jsonGet(key, "$") shouldBe "[{\"f1\":{\"a\":3},\"f2\":{\"a\":3}}]"

        // JSON.STRAPPEND
        client.jsonSet(key, "$", "{\"a\":\"foo\", \"nested\": {\"a\": \"hello\"}, \"nested2\": {\"a\": 31}}") shouldBe true
        client.jsonStrAppend(key, "$..a", "\"baz\"") shouldBe listOf(6, 8, null)
        client.jsonGet(key, "$") shouldBe "[{\"a\":\"foobaz\",\"nested\":{\"a\":\"hellobaz\"},\"nested2\":{\"a\":31}}]"

        // JSON.STRLEN
        client.jsonSet(key, "$", "{\"a\":\"foo\", \"nested\": {\"a\": \"hello\"}, \"nested2\": {\"a\": 31}}") shouldBe true
        client.jsonStrLen(key, "$..a") shouldBe listOf(3, 5, null)

        // JSON.TOGGLE
        client.jsonSet(key, "$", "{\"bool\": true}") shouldBe true
        client.jsonToggle(key, "$.bool") shouldBe listOf(0)
        client.jsonGet(key, "$") shouldBe "[{\"bool\":false}]"
        client.jsonToggle(key, "$.bool") shouldBe listOf(1)
        client.jsonGet(key, "$") shouldBe "[{\"bool\":true}]"

        // JSON.TYPE
        client.jsonSet(key, "$", "{\"a\":2, \"nested\": {\"a\": true}, \"foo\": \"bar\"}") shouldBe true
        client.jsonType(key, "$..foo") shouldBe listOf("string")
        client.jsonType(key, "$..a") shouldBe listOf("integer", "boolean")
        client.jsonType(key, "$..dummy") shouldBe listOf()
    }
})
