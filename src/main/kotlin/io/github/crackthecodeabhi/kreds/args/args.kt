package io.github.crackthecodeabhi.kreds.args

import io.github.crackthecodeabhi.kreds.KredsException
import java.math.BigDecimal
import kotlin.IllegalArgumentException

internal sealed interface Argument

internal data class KeyValueArgument(val key: String, val value: String) : Argument {
    override fun toString() = "${key.uppercase()} $value"
}

internal data class KeyOnlyArgument(val key: String) : Argument {
    override fun toString() = key.uppercase()
}

internal data class StringArgument(val value: String) : Argument {
    override fun toString() = value
}

internal fun <T> T.toArgument(): Argument =
    when (this) {
        is String -> StringArgument(this)
        is Int -> StringArgument(this.toString(10))
        is Long -> StringArgument(this.toString(10))
        is ULong -> StringArgument(this.toString(10))
        is BigDecimal -> StringArgument(this.toEngineeringString())
        else -> throw KredsException("Fatal error, cannot convert to Argument, unknown type.")
    }

internal fun Array<out String>.toArguments(): Array<out StringArgument> =
    this.map { it.toArgument() as StringArgument }.toTypedArray()

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