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

package io.github.crackthecodeabhi.kreds.args

import io.github.crackthecodeabhi.kreds.KredsException
import java.math.BigDecimal

internal sealed interface Argument

internal object EmptyArgument : Argument

internal data class KeyValueArgument(val key: String, val value: String) : Argument {
    override fun toString() = "${key.uppercase()} $value"
}

internal data class KeyOnlyArgument(val key: String) : Argument {
    override fun toString() = key.uppercase()
}

@JvmInline
internal value class StringArgument(val value: String) : Argument {
    override fun toString(): String = value
}

internal fun <T> T.toArgument(): Argument =
    when (this) {
        is String -> StringArgument(this)
        is Int -> StringArgument(this.toString(10))
        is Long -> StringArgument(this.toString(10))
        is Double -> StringArgument(this.toBigDecimal().toPlainString())
        is ULong -> StringArgument(this.toString(10))
        is BigDecimal -> StringArgument(this.toEngineeringString())
        else -> throw KredsException("Fatal error, cannot convert to Argument, unknown type.")
    }

internal fun Array<out String>.toArguments(): Array<out StringArgument> =
    this.map { it.toArgument() as StringArgument }.toTypedArray()

/**
 * null arguments are skipped.
 */
internal fun createArguments(vararg args: Any?): Array<out Argument> {
    val argList = mutableListOf<Argument>()
    for (arg in args) {
        if (arg == null) continue
        when (arg) {
            is String, is ULong, is Long, is Int, is BigDecimal -> argList.add(arg.toArgument())
            is Pair<*, *> -> {
                argList.add(arg.first.toArgument())
                argList.add(arg.second.toArgument())
            }
            is Argument -> argList.add(arg)
            else -> throw KredsException("Fatal error. Unknown argument type.")
        }
    }
    return argList.toTypedArray()
}