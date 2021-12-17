package org.kreds

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import java.math.BigDecimal
import java.nio.charset.Charset

fun String.toByteBuf(): ByteBuf = Unpooled.copiedBuffer(this, Charset.defaultCharset())

fun ByteBuf.toDefaultCharset(): String = this.toString(Charset.defaultCharset())

fun String.toArgument() = StringArgument(this)

fun ULong.toArgument() = StringArgument(this.toString())

fun Long.toArgument() = StringArgument(this.toString(10))

fun Int.toArgument() = StringArgument(this.toString(10))

fun BigDecimal.toArgument() = StringArgument(this.toEngineeringString())

fun Array<out String>.toArguments(): Array<out StringArgument> = this.map(String::toArgument).toTypedArray()

data class FieldValue<out A, out B>(val field: A,val value: B)