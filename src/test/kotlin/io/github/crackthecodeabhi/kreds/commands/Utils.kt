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

import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.github.crackthecodeabhi.kreds.connection.KredsClientConfig
import io.github.crackthecodeabhi.kreds.connection.KredsClientGroup

fun getTestClient(endpoint: Endpoint? = null, config: KredsClientConfig? = null): KredsClient {
    return config?.let {
        KredsClientGroup.newClient(endpoint ?: Endpoint.from("127.0.0.1:6379"), it)
    } ?: KredsClientGroup.newClient(endpoint ?: Endpoint.from("127.0.0.1:6379"))
}