/*
 *  Copyright (C) 2023 Abhijith Shivaswamy
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
import io.netty.util.ResourceLeakDetector

class MemoryLeak: FunSpec({
    lateinit var client: KredsClient
    val clientSetup = ClientSetup().then { client = it.client }
    beforeSpec(clientSetup)
    afterSpec(ClientTearDown(clientSetup))
    beforeTest(ClearDB(clientSetup))
    ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID)

    // This test should pass, but observe the logs for memory leak errors by netty
    test("Memory leak test") {
        val key = "key"
        val value = "value"
        for (i in 0..100) {
            client.set(key, value)
            client.get(key)
            System.gc()
        }
    }
})