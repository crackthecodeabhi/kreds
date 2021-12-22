package io.github.crackthecodeabhi.kreds.args

import io.github.crackthecodeabhi.kreds.KredsException
import java.math.BigDecimal
import kotlin.IllegalArgumentException
import kotlin.jvm.Throws

data class KeyValuePair(val key: String, val value: String)
infix fun String.toKV(other: String) = KeyValuePair(this,other)

data class FieldValuePair(val field: String,val value: String)
infix fun String.toFV(other: String) = FieldValuePair(this,other)

sealed interface Argument

data class KeyValueArgument(val key: String, val value: String): Argument{
    override fun toString() = "${key.uppercase()} $value"
}

data class KeyOnlyArgument(val key: String): Argument{
    override fun toString() = key.uppercase()
}

data class StringArgument(val value: String): Argument{
    override fun toString() = value
}

fun String.toArgument() = StringArgument(this)

fun ULong.toArgument() = StringArgument(this.toString())

fun Long.toArgument() = StringArgument(this.toString(10))

fun Int.toArgument() = StringArgument(this.toString(10))

fun BigDecimal.toArgument() = StringArgument(this.toEngineeringString())

fun Array<out String>.toArguments(): Array<out StringArgument> = this.map(String::toArgument).toTypedArray()


/**
 * The EXPIRE,EXPIREAT commands supports a set of options since Redis 7.0:
 *
 * NX -- Set expiry only when the key has no expiry
 *
 * XX -- Set expiry only when the key has an existing expiry
 *
 * GT -- Set expiry only when the new expiry is greater than current one
 *
 * LT -- Set expiry only when the new expiry is less than current one
 */
enum class ExpireOption: Argument{
    NX,XX,GT,LT;
    override fun toString(): String = name
}

typealias PExpireOption = ExpireOption

fun createArguments(vararg args: Any?): Array<out Argument>{
    val argList = mutableListOf<Argument>()
    for(arg in args){
        if(arg == null) continue
        when(arg){
            is String -> argList.add(arg.toArgument())
            is ULong -> argList.add(arg.toArgument())
            is Long -> argList.add(arg.toArgument())
            is Int -> argList.add(arg.toArgument())
            is BigDecimal -> argList.add(arg.toArgument())
            is KeyValuePair -> {
                argList.add(arg.key.toArgument())
                argList.add(arg.value.toArgument())
            }
            is FieldValuePair -> {
                argList.add(arg.field.toArgument())
                argList.add(arg.value.toArgument())
            }
            is Argument -> argList.add(arg)
            else -> throw KredsException("Fatal error. Unknown argument type.")
        }
    }
    return argList.toTypedArray()
}

class SetOption private constructor(
    val exSeconds:ULong? = null,
    val pxMilliseconds: ULong? = null,
    val exatTimestamp: ULong? = null,
    val pxatMillisecondTimestamp: ULong? = null,
    val keepTTL: Boolean? = null,
    val nx: Boolean? = null,
    val xx: Boolean? = null,
    val get: Boolean? = null){

    data class Builder(
        var exSeconds:ULong? = null,
        var pxMilliseconds: ULong? = null,
        var exatTimestamp: ULong? = null,
        var pxatMillisecondTimestamp: ULong? = null,
        var keepTTL: Boolean? = null,
        var nx: Boolean? = null,
        var xx: Boolean? = null,
        var get: Boolean? = null
    ){
        fun exSeconds(exSeconds: ULong) = apply { this.exSeconds = exSeconds }
        fun pxMilliseconds(pxMilliseconds: ULong) = apply { this.pxMilliseconds = pxMilliseconds }
        fun exatTimestamp(exatTimestamp: ULong) = apply { this.exatTimestamp = exatTimestamp }
        fun pxatMillisecondTimestamp(pxatMillisecondTimestamp: ULong) = apply { this.pxatMillisecondTimestamp = pxatMillisecondTimestamp }
        fun keepTTL(keepTTL: Boolean)= apply { this.keepTTL = keepTTL }
        fun nx(nx: Boolean) = apply { this.nx = nx }
        fun xx(xx: Boolean) = apply { this.xx = xx }
        fun get(get: Boolean) = apply { this.get = get }

        /**
         * @throws IllegalArgumentException in case the argument provided conflict
         */
        @Throws(IllegalArgumentException::class)
        fun build(): SetOption{
            if(listOfNotNull(exSeconds, pxMilliseconds, exatTimestamp, pxatMillisecondTimestamp, keepTTL).size>1)
                throw IllegalArgumentException("Only one of the options (EX,PX,EXAT,PXAT,KEEPTTL) allowed or none.")
            if(listOfNotNull(nx,xx).size > 1)
                throw IllegalArgumentException("Either NX or XX are allowed or none.")
            return SetOption(exSeconds,pxMilliseconds,exatTimestamp,pxatMillisecondTimestamp,keepTTL,nx,xx,get)
        }
    }
}

class GetExOption private constructor(
    val exSeconds: ULong? = null,
    val pxMilliseconds: ULong? = null,
    val exatTimestamp: ULong? = null,
    val pxatMillisecondTimestamp: ULong? = null,
    val persist: Boolean? = null){

    data class Builder(
        var exSeconds: ULong? = null,
        var pxMilliseconds: ULong? = null,
        var exatTimestamp: ULong? = null,
        var pxatMillisecondTimestamp: ULong? = null,
        var persist: Boolean? = null
    ){
        fun exSeconds(exSeconds: ULong) = apply { this.exSeconds = exSeconds }
        fun pxMilliseconds(pxMilliseconds: ULong) = apply { this.pxMilliseconds = pxMilliseconds }
        fun exatTimestamp(exatTimestamp: ULong)= apply { this.exatTimestamp = exatTimestamp }
        fun pxatMillisecondTimestamp(pxatMillisecondTimestamp: ULong)= apply { this.pxatMillisecondTimestamp = pxatMillisecondTimestamp }
        fun persist(persist: Boolean) = apply { this.persist = persist }

        @Throws(IllegalArgumentException::class)
        fun build(): GetExOption{
            if(listOfNotNull(exSeconds,pxMilliseconds,exatTimestamp,pxatMillisecondTimestamp,persist).size>1)
                throw IllegalArgumentException("Only one option is valid.")
            return GetExOption(exSeconds,pxMilliseconds,exatTimestamp,pxatMillisecondTimestamp,persist)
        }
    }
}


enum class ClientListType: Argument {
    normal, master, replica, pubsub;

    override fun toString(): String = "TYPE $name"
}

enum class ClientPauseOption: Argument{
    WRITE,ALL;

    override fun toString(): String = name
}

enum class ClientReplyOption: Argument{
    ON,OFF,SKIP;
    override fun toString() = name
}

enum class BeforeAfterOption: Argument{
    BEFORE,AFTER;
    override fun toString() = name
}

enum class LeftRightOption: Argument{
    LEFT,RIGHT;
    override fun toString(): String = name
}