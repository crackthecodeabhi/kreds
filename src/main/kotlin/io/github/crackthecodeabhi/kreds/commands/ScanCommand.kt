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

import io.github.crackthecodeabhi.kreds.FieldValue
import io.github.crackthecodeabhi.kreds.StringFieldValue
import io.github.crackthecodeabhi.kreds.protocol.ArrayCommandProcessor
import io.github.crackthecodeabhi.kreds.protocol.ICommandProcessor
import io.github.crackthecodeabhi.kreds.protocol.KredsRedisDataException
import io.github.crackthecodeabhi.kreds.second
import io.netty.handler.codec.redis.RedisMessage

public interface IScanResult<R> {
    public val cursor: Long
    public val elements: List<R>
}

internal typealias IScanResultProvider<R> = (cursor: Long, elements: List<R>) -> IScanResult<R>

public data class ScanResult(override val cursor: Long, override val elements: List<String>) : IScanResult<String>

public typealias SScanResult = ScanResult

public data class HScanResult(override val cursor: Long, override val elements: List<FieldValue<String, String>>) :
    IScanResult<FieldValue<String, String>>

public data class ZScanResult(override val cursor: Long, override val elements: List<Pair<String, Long>>) :
    IScanResult<Pair<String, Long>>

internal abstract class AbstractScanResultProcessor<T : IScanResultProvider<R>, R>(private val scanResultProvider: T) :
    ICommandProcessor<IScanResult<R>> {
    override fun decode(message: RedisMessage): IScanResult<R> {
        val scanResult =
            ArrayCommandProcessor.decode(message) ?: throw KredsRedisDataException("received null from server.")
        //Scan result format:
        //1. Cursor, as string
        //2. a list of R
        try {
            if (scanResult.size != 2) throw KredsRedisDataException("Failed to decode *SCAN result. Received Invalid response from server.")
            val cursor: Long = (scanResult.first() as String).toLong()

            @Suppress("UNCHECKED_CAST")
            val list: List<R> = scanResult.second() as List<R>
            return scanResultProvider(cursor, list)
        } catch (ex: Throwable) {
            when (ex) {
                is NumberFormatException, is ClassCastException ->
                    throw KredsRedisDataException("Failed to decode *SCAN result.", ex)
                else -> throw ex
            }
        }
    }
}

internal object ScanResultProcessor : AbstractScanResultProcessor<IScanResultProvider<String>, String>(::ScanResult)

internal typealias SScanResultProcessor = ScanResultProcessor

internal object HScanResultProcessor :
    AbstractScanResultProcessor<IScanResultProvider<StringFieldValue>, StringFieldValue>(::HScanResult)

internal object ZScanResultProcessor :
    AbstractScanResultProcessor<IScanResultProvider<Pair<String, Long>>, Pair<String, Long>>(::ZScanResult)