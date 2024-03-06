/*
 *  Copyright (C) 2024 Abhijith Shivaswamy
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

import io.github.crackthecodeabhi.kreds.args.JsonSetOption
import io.github.crackthecodeabhi.kreds.args.createArguments
import io.github.crackthecodeabhi.kreds.args.toArgument
import io.github.crackthecodeabhi.kreds.pipeline.QueuedCommand
import io.github.crackthecodeabhi.kreds.pipeline.Response
import io.github.crackthecodeabhi.kreds.protocol.*

public interface PipelineJsonCommands {

    /**
     * ### ` JSON.ARRAPPEND key [path] value [value ...] `
     *
     * Append the json values into the array at path after the last element in it.
     *
     * [Doc](https://redis.io/commands/json.arrappend/)
     * @since JSON 1.0.0
     * @return [] if the matching JSON value is not an array.
     */
    public suspend fun jsonArrAppend(
        key: String,
        path: String,
        elements: Array<String>
    ): Response<List<Int?>>

    /**
     * ### ` JSON.ARRAPPEND key [path] value [value ...] `
     *
     * Append the json values into the array at path after the last element in it.
     *
     * [Doc](https://redis.io/commands/json.arrappend/)
     * @since JSON 1.0.0
     * @return [] if the matching JSON value is not an array.
     */
    public suspend fun jsonArrAppend(
        key: String,
        path: String,
        element: String,
        vararg elements: String
    ): Response<List<Int?>>

    /**
     * ### ` JSON.ARRINDEX key path value [ start [stop]] `
     *
     * Searches for the first occurrence of a scalar JSON value in an array.
     * The optional inclusive start (default 0) and exclusive stop (default 0, meaning that the last element is included)
     * specify a slice of the array to search. Negative values are interpreted as starting from the end.
     * Note: out-of-range indexes round to the array's start and end. An inverse index range
     * (such as the range from 1 to 0) will return unfound.
     *
     * [Doc](https://redis.io/commands/json.arrindex/)
     * @since JSON 1.0.0
     * @return [] if the matching JSON value is not an array.
     */
    public suspend fun jsonArrIndex(
        key: String,
        path: String,
        value: String,
        start: Int = 0,
        stop: Int = 0
    ): Response<List<Long?>>

    /**
     * ### ` JSON.ARRINSERT key path index value [value ...] `
     *
     * Inserts the json values into the array at path before the index (shifts to the right).
     * The index must be in the array's range. Inserting at index 0 prepends to the array.
     * Negative index values start from the end of the array.
     *
     * [Doc](https://redis.io/commands/json.arrinsert/)
     * @since JSON 1.0.0
     * @return [] if the matching JSON value is not an array.
     */
    public suspend fun jsonArrInsert(
        key: String,
        path: String,
        index: Int,
        elements: Array<String>
    ): Response<List<Int?>>

    /**
     * ### ` JSON.ARRINSERT key path index value [value ...] `
     *
     * Inserts the json values into the array at path before the index (shifts to the right).
     * The index must be in the array's range. Inserting at index 0 prepends to the array.
     * Negative index values start from the end of the array.
     *
     * [Doc](https://redis.io/commands/json.arrinsert/)
     * @since JSON 1.0.0
     * @return [] if the matching JSON value is not an array.
     */
    public suspend fun jsonArrInsert(
        key: String,
        path: String,
        index: Int,
        element: String,
        vararg elements: String
    ): Response<List<Int?>>

    /**
     * ### ` JSON.ARRLEN key [path] `
     *
     * Reports the length of the JSON Array at path in key.
     * path defaults to root if not provided. Returns null if the key or path do not exist.
     *
     * [Doc](https://redis.io/commands/json.arrlen/)
     * @since JSON 1.0.0
     * @return [] if the matching JSON value is not an array.
     */
    public suspend fun jsonArrLen(key: String, path: String): Response<List<Int?>>

    /**
     * ### ` JSON.ARRPOP key [ path [index]] `
     *
     * Removes and returns an element from the index in the array.
     * path defaults to root if not provided. index is the position in the array to start
     * popping from (defaults to -1, meaning the last element). Out-of-range indexes round
     * to their respective array ends. Popping an empty array returns null.
     *
     * [Doc](https://redis.io/commands/json.arrpop/)
     * @since JSON 1.0.0
     * @return [] if the matching JSON value is not an array.
     */
    public suspend fun jsonArrPop(key: String, path: String, index: Int = -1): Response<List<String?>>

    /**
     * ### ` JSON.ARRTRIM key path start stop `
     *
     * Trims an array so that it contains only the specified inclusive range of elements.
     * This command is extremely forgiving and using it with out-of-range indexes will not produce an error.
     * There are a few differences between how RedisJSON v2.0 and legacy versions handle out-of-range indexes.
     * Behavior as of RedisJSON v2.0:
     * - If start is larger than the array's size or start > stop, returns 0 and an empty array.
     * - If start is < 0, then start from the end of the array.
     * - If stop is larger than the end of the array, it will be treated like the last element.
     *
     * [Doc](https://redis.io/commands/json.arrtrim/)
     * @since JSON 1.0.0
     * @return [] if the matching JSON value is not an array.
     */
    public suspend fun jsonArrTrim(key: String, path: String, start: Int, stop: Int): Response<List<Int?>>

    /**
     * ### ` JSON.CLEAR key [path] `
     *
     * Clears container values (Arrays/Objects), and sets numeric values to 0.
     * Already cleared values are ignored: empty containers, and zero numbers.
     * path defaults to root if not provided. Non-existing paths are ignored.
     *
     * [Doc](https://redis.io/commands/json.clear/)
     * @since JSON 2.0.0
     * @return Integer reply: specifically the number of values cleared.
     */
    public suspend fun jsonClear(key: String, path: String): Response<Long>

    /**
     * ### ` https://redis.io/commands/json.del/ `
     *
     * Deletes a value.
     * path defaults to root if not provided. Ignores nonexistent keys and paths. Deleting an object's root is equivalent to deleting the key from Redis.
     *
     * [Doc](https://redis.io/commands/json.del/)
     * @since JSON 1.0.0
     * @return Integer reply - the number of paths deleted (0 or more).
     */
    public suspend fun jsonDel(key: String, path: String): Response<Long>

    /**
     * ### ` JSON.GET key [INDENT indent] [NEWLINE newline] [SPACE space] [paths [paths ...]] `
     *
     * Returns the value at path in JSON serialized form.
     * This command accepts multiple path arguments. If no path is given, it defaults to the value's root.
     * The following subcommands change the reply's format (all are empty string by default):
     * - INDENT sets the indentation string for nested levels
     * - NEWLINE sets the string that's printed at the end of each line
     * - SPACE sets the string that's put between a key and a value
     *
     * [Doc](https://redis.io/commands/json.get/)
     * @since JSON 1.0.0
     * @return [] - each string is the JSON serialization of each JSON value that matches a path.
     */
    public suspend fun jsonGet(
        key: String,
        paths: Array<String>,
        indent: String? = null,
        newline: String? = null,
        space: String? = null
    ): Response<String?>

    /**
     * ### ` JSON.GET key [INDENT indent] [NEWLINE newline] [SPACE space] [paths [paths ...]] `
     *
     * Returns the value at path in JSON serialized form.
     * This command accepts multiple path arguments. If no path is given, it defaults to the value's root.
     * The following subcommands change the reply's format (all are empty string by default):
     * - INDENT sets the indentation string for nested levels
     * - NEWLINE sets the string that's printed at the end of each line
     * - SPACE sets the string that's put between a key and a value
     *
     * [Doc](https://redis.io/commands/json.get/)
     * @since JSON 1.0.0
     * @return [] - each string is the JSON serialization of each JSON value that matches a path.
     */
    public suspend fun jsonGet(
        key: String,
        path: String,
        vararg paths: String,
        indent: String? = null,
        newline: String? = null,
        space: String? = null
    ): Response<String?>

    /**
     * ### ` JSON.MGET key [key ...] path `
     *
     * Returns the values at path from multiple key arguments. Returns null for nonexistent keys and nonexistent paths.
     *
     * [Doc](https://redis.io/commands/json.mget/)
     * @since JSON 1.0.0
     * @return [] - the JSON serialization of the value at each key's path.
     */
    public suspend fun jsonMGet(keys: Array<String>, path: String): Response<List<String>>

    /**
     * ### ` JSON.MGET key [key ...] path `
     *
     * Returns the values at path from multiple key arguments. Returns null for nonexistent keys and nonexistent paths.
     *
     * [Doc](https://redis.io/commands/json.mget/)
     * @since JSON 1.0.0
     * @return [] - the JSON serialization of the value at each key's path.
     */
    public suspend fun jsonMGet(key: String, vararg keys: String, path: String): Response<List<String>>

    /**
     * ### ` JSON.NUMINCRBY key path value `
     *
     * Increments the number value stored at path by number.
     *
     * [Doc](https://redis.io/commands/json.numincrby/)
     * @since JSON 1.0.0
     * @return [] if the matching JSON value is not a number.
     */
    public suspend fun jsonNumIncrBy(key: String, path: String, by: Long): Response<String>

    /**
     * ### ` JSON.NUMINCRBY key path value `
     *
     * Increments the number value stored at path by number.
     *
     * [Doc](https://redis.io/commands/json.numincrby/)
     * @since JSON 1.0.0
     * @return [] if the matching JSON value is not a number.
     */
    public suspend fun jsonNumIncrBy(key: String, path: String, by: Double): Response<String>

    /**
     * ### ` JSON.OBJKEYS key [path] `
     *
     * Returns the keys in the object that's referenced by path.
     * path defaults to root if not provided. Returns null if the object is empty or either key or path do not exist.
     *
     * [Doc](https://redis.io/commands/json.objkeys/)
     * @since JSON 1.0.0
     * @return [] if the matching JSON value is not an object.
     */
    public suspend fun jsonObjKeys(key: String, path: String): Response<List<List<String>?>>

    /**
     * ### ` JSON.OBJLEN key [path] `
     *
     * Reports the number of keys in the JSON Object at path in key.
     * path defaults to root if not provided. Returns null if the key or path do not exist.
     *
     * [Doc](https://redis.io/commands/json.objlen/)
     * @since JSON 1.0.0
     * @return [] if the matching JSON value is not an object.
     */
    public suspend fun jsonObjLen(key: String, path: String): Response<List<Int?>>

    /**
     * ### ` JSON.SET key path value [ NX | XX] `
     *
     * Sets the JSON value at path in key.
     *
     * For new Redis keys the path must be the root. For existing keys, when the entire path exists,
     * the value that it contains is replaced with the json value. For existing keys, when the path
     * exists, except for the last element, a new child is added with the json value.
     * Adds a key (with its respective value) to a JSON Object (in a RedisJSON data type key)
     * only if it is the last child in the path, or it is the parent of a new child being added
     * in the path. The optional subcommands modify this behavior for both new RedisJSON data type
     * keys as well as the JSON Object keys in them:
     * - NX - only set the key if it does not already exist
     * - XX - only set the key if it already exists
     *
     * [Doc](https://redis.io/commands/json.set/)
     * @since JSON 1.0.0
     * @return false if the specified NX or XX conditions were not met.
     */
    public suspend fun jsonSet(key: String, path: String, value: String, option: JsonSetOption? = null): Response<Boolean>

    /**
     * ### ` JSON.STRAPPEND key [path] value `
     *
     * Appends the json-string values to the string at path.
     *
     * [Doc](https://redis.io/commands/json.strappend/)
     * @since JSON 1.0.0
     * @return [] if the matching JSON value is not an array.
     */
    public suspend fun jsonStrAppend(key: String, path: String, value: String): Response<List<Int?>>

    /**
     * ### ` JSON.STRLEN key [path] `
     *
     * Reports the length of the JSON String at path in key.
     * Returns null if the key or path do not exist.
     *
     * [Doc](https://redis.io/commands/json.strlen/)
     * @since JSON 1.0.0
     * @return [] if the matching JSON value is not a string.
     */
    public suspend fun jsonStrLen(key: String, path: String): Response<List<Int?>>

    /**
     * ### ` JSON.TOGGLE key [path] `
     *
     * Toggle a boolean value stored at path.
     *
     * [Doc](https://redis.io/commands/json.toggle/)
     * @since JSON 2.0.0
     * @return [] element for JSON values matching the path which are not boolean.
     */
    public suspend fun jsonToggle(key: String, path: String): Response<List<Int?>>

    /**
     * ### ` JSON.TYPE key [path] `
     *
     * Reports the type of JSON value at path.
     * Returns null if the key or path do not exist.
     *
     * [Doc](https://redis.io/commands/json.type/)
     * @since JSON 1.0.0
     * @return [] - for each path, the value's type.
     */
    public suspend fun jsonType(key: String, path: String): List<String>
}

internal interface PipelineJsonCommandExecutor : QueuedCommand, PipelineJsonCommands, BaseJsonCommands {

    override suspend fun jsonArrAppend(key: String, path: String, elements: Array<String>): Response<List<Int?>> =
        add(_jsonArrAppend(key,path,elements)).responseTo()

    override suspend fun jsonArrAppend(
        key: String,
        path: String,
        element: String,
        vararg elements: String
    ): Response<List<Int?>> =
        add(_jsonArrAppend(key, path, arrayOf(element, *elements))).responseTo()

    override suspend fun jsonArrIndex(key: String, path: String, value: String, start: Int, stop: Int): Response<List<Long?>> =
        add(_jsonArrIndex(key, path, value, start, stop)).responseTo()

    override suspend fun jsonArrInsert(key: String, path: String, index: Int, elements: Array<String>): Response<List<Int?>> =
        add(_jsonArrInsert(key, path, index, elements)).responseTo()

    override suspend fun jsonArrInsert(
        key: String,
        path: String,
        index: Int,
        element: String,
        vararg elements: String
    ): Response<List<Int?>> = add(_jsonArrInsert(key, path, index, elements = arrayOf(element, *elements))).responseTo()

    override suspend fun jsonArrLen(key: String, path: String): Response<List<Int?>> =
        add(_jsonArrLen(key, path)).responseTo()

    override suspend fun jsonArrPop(key: String, path: String, index: Int): Response<List<String?>> =
        add(_jsonArrPop(key, path, index)).responseTo()

    override suspend fun jsonArrTrim(key: String, path: String, start: Int, stop: Int): Response<List<Int?>> =
        add(_jsonArrTrim(key, path, start, stop)).responseTo()

    override suspend fun jsonClear(key: String, path: String): Response<Long> =
        add(_jsonClear(key, path)).responseTo()

    override suspend fun jsonDel(key: String, path: String): Response<Long> =
        add(_jsonDel(key, path)).responseTo()

    override suspend fun jsonGet(
        key: String,
        paths: Array<String>,
        indent: String?,
        newline: String?,
        space: String?
    ): Response<String?> = add(_jsonGet(key, paths = paths, indent, newline, space))

    override suspend fun jsonGet(
        key: String,
        path: String,
        vararg paths: String,
        indent: String?,
        newline: String?,
        space: String?
    ): Response<String?> = add(_jsonGet(key, paths = arrayOf(path, *paths), indent, newline, space))

    override suspend fun jsonMGet(keys: Array<String>, path: String): Response<List<String>> =
        add(_jsonMGet(keys, path)).responseTo()

    override suspend fun jsonMGet(key: String, vararg keys: String, path: String): Response<List<String>> =
        add(_jsonMGet(keys = arrayOf(key, *keys), path)).responseTo()

    override suspend fun jsonNumIncrBy(key: String, path: String, by: Long): Response<String> =
        add(_jsonNumIncrBy(key, path, by)).responseTo()

    override suspend fun jsonNumIncrBy(key: String, path: String, by: Double): Response<String> =
        add(_jsonNumIncrBy(key, path, by)).responseTo()

    override suspend fun jsonObjKeys(key: String, path: String): Response<List<List<String>?>> =
        add(_jsonObjKeys(key, path)).responseTo()

    override suspend fun jsonObjLen(key: String, path: String): Response<List<Int?>> =
        add(_jsonObjLen(key, path)).responseTo()

    override suspend fun jsonSet(key: String, path: String, value: String, option: JsonSetOption?): Response<Boolean> =
        add(_jsonSet(key, path, value, option)).responseTo()

    override suspend fun jsonStrAppend(key: String, path: String, value: String): Response<List<Int?>> =
        add(_jsonStrAppend(key, path, value)).responseTo()

    override suspend fun jsonStrLen(key: String, path: String): Response<List<Int?>> =
        add(_jsonStrLen(key, path)).responseTo()

    override suspend fun jsonToggle(key: String, path: String): Response<List<Int?>> =
        add(_jsonToggle(key, path)).responseTo()

    override suspend fun jsonType(key: String, path: String): List<String> =
        add(_jsonType(key, path)).responseTo()
}
