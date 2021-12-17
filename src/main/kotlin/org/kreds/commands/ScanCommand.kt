package org.kreds.commands

import io.netty.handler.codec.redis.RedisMessage
import org.kreds.FieldValue
import org.kreds.protocol.*
import org.kreds.second
import java.lang.ClassCastException

interface IScanResult<R>{
    val cursor: Long
    val elements: List<R>
}

data class ScanResult(override val cursor: Long, override val elements: List<String>): IScanResult<String>

typealias SScanResult = ScanResult

data class HScanResult(override val cursor: Long, override val elements: List<FieldValue<String,String>>): IScanResult<FieldValue<String, String>>


abstract class AbstractScanResultProcessor<R>: CommandProcessor(ArrayHandler),ICommandProcessor{
    override fun <T> decode(message: RedisMessage): T {
        val scanResult = super.decode<List<Any>>(message)
        //Scan result format:
        //1. Cursor, as string
        //2. a list of R
        try{
            if(scanResult.size != 2) throw KredsRedisDataException("Failed to decode *SCAN result. Received Invalid response from server.")
            val cursor: Long = (scanResult.first() as String).toLong()
            @Suppress("UNCHECKED_CAST")
            val list: List<R> = scanResult.second() as List<R>
            @Suppress("UNCHECKED_CAST")
            return Pair(cursor,list) as T
        } catch (ex: Throwable){
            when(ex){
                is NumberFormatException, is ClassCastException ->
                    throw KredsRedisDataException("Failed to decode *SCAN result.",ex)
                else -> throw ex
            }
        }
    }
}

object ScanResultProcessor: AbstractScanResultProcessor<String>(){
    override fun <T> decode(message: RedisMessage): T {
        val (cursor, list) = super.decode<Pair<Long,List<String>>>(message)
        @Suppress("UNCHECKED_CAST")
        return ScanResult(cursor,list) as T
    }
}

typealias SScanResultProcessor = ScanResultProcessor

object HScanResultProcessor: AbstractScanResultProcessor<FieldValue<String,String>>(){
    override fun <T> decode(message: RedisMessage): T {
        val (cursor, list) = super.decode<Pair<Long,List<FieldValue<String,String>>>>(message)
        @Suppress("UNCHECKED_CAST")
        return HScanResult(cursor, list) as T
    }
}