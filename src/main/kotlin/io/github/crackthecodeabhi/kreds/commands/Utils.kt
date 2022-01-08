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

import io.github.crackthecodeabhi.kreds.protocol.KredsRedisDataException

/**
 * Throws if response from redis is null and [throwEx] is true with given [opName] else returns [this] casting to [R]
 * @throws KredsRedisDataException if null and [throwEx] is true
 */
internal inline fun <T, reified R> T?.responseTo(opName: String? = null, throwEx: Boolean = true): R {
    return this?.let { this as R } ?: run {
        if (throwEx) throw KredsRedisDataException("received null from redis ${opName ?: ""}")
        else this as R
    }
}

internal inline fun <T, reified R> T.asReturnType(): R {
    return this as R
}