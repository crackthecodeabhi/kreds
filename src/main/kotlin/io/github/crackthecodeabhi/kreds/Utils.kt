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

package io.github.crackthecodeabhi.kreds

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import java.nio.charset.Charset

/**
 * Any class marked with this annotation is **SAFE** to be called concurrently from coroutines.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
internal annotation class CoroutineSafe

/**
 * Any class marked with this annotation is **UNSAFE** to be called concurrently from coroutines.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
internal annotation class CoroutineUnsafe


internal fun String.toByteBuf(): ByteBuf = Unpooled.copiedBuffer(this, Charset.defaultCharset())

internal fun ByteBuf.toDefaultCharset(): String = this.toString(Charset.defaultCharset())

public data class FieldValue<out A, out B>(val field: A, val value: B)
public typealias StringFieldValue = FieldValue<String, String>

internal fun <T> List<T>.second(): T {
    if (isEmpty() || size < 2)
        throw NoSuchElementException("List has no second element.")
    return this[1]
}

internal fun <T> List<T>.third(): T {
    if (isEmpty() || size < 3)
        throw NoSuchElementException("List has no third element.")
    return this[2]
}

/**
 * Returns the element casting it to return type.
 * @throws NoSuchElementException
 * @throws ClassCastException
 */
internal inline fun <T, reified R> List<T>.getAs(index: Int): R {
    if (isEmpty() || size < (index + 1)) throw NoSuchElementException("List has no element at index: $index")
    return this[index] as R
}