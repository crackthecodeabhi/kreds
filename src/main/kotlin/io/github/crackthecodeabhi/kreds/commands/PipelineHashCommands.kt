package io.github.crackthecodeabhi.kreds.commands

import io.github.crackthecodeabhi.kreds.args.FieldValuePair
import io.github.crackthecodeabhi.kreds.pipeline.QueuedCommand
import io.github.crackthecodeabhi.kreds.pipeline.Response

interface PipelineHashCommands{
    /**
     * @see [HashCommands.hdel]
     */
    suspend fun hdel(key: String, field: String, vararg moreFields: String): Response<Long>

    /**
     * @see [HashCommands.hexists]
     */
    suspend fun hexists(key: String, field: String): Response<Long>

    /**
     * @see [HashCommands.hget]
     */
    suspend fun hget(key: String, field: String): Response<String?>

    /**
     * @see [HashCommands.hgetAll]
     */
    suspend fun hgetAll(key: String): Response<List<String>>

    /**
     * @see [HashCommands.hincrBy]
     */
    suspend fun hincrBy(key: String, field: String, increment: Long): Response<Long>

    /**
     * @see [HashCommands.hincrByFloat]
     */
    suspend fun hincrByFloat(key: String,field: String, increment: Long): Response<String>

    /**
     * @see [HashCommands.hkeys]
     */
    suspend fun hkeys(key: String): Response<List<String>>

    /**
     * @see [HashCommands.hlen]
     */
    suspend fun hlen(key: String): Response<Long>

    /**
     * @see [HashCommands.hmget]
     */
    suspend fun hmget(key: String, field: String, vararg fields: String): Response<List<String?>>

    /**
     * @see [HashCommands.hrandfield]
     */
    suspend fun hrandfield(key: String): Response<String?>


    /**
     * @see [HashCommands.hrandfield]
     */
    suspend fun hrandfield(key: String, count: Int, withValues: Boolean? = null): Response<List<String>>


    /**
     * @see [HashCommands.hset]
     */
    suspend fun hset(key: String, fieldValuePair: FieldValuePair, vararg fieldValuePairs: FieldValuePair): Response<Long>

    /**
     * @see [HashCommands.hsetnx]
     */
    suspend fun hsetnx(key: String,field: String, value: String): Response<Long>

    /**
     * @see [HashCommands.hstrlen]
     */
    suspend fun hstrlen(key: String, field: String): Response<Long>

     /**
     * @see [HashCommands.hvals]
     */
     suspend fun hvals(key: String): Response<List<String>>

    /**
     * @see  [HashCommands.hscan]
     */
    suspend fun hscan(key: String, cursor: Long, matchPattern: String? = null, count: Long? = null): Response<HScanResult>
}

interface PipelineHashCommandExecutor: QueuedCommand, PipelineHashCommands, BaseHashCommands {
    override suspend fun hdel(key: String, field: String, vararg moreFields: String): Response<Long> =
        add(_hdel(key,field, *moreFields))

    override suspend fun hexists(key: String, field: String): Response<Long> =
        add(_hexists(key, field))

    override suspend fun hget(key: String, field: String): Response<String?> =
        add(_hget(key, field))

    override suspend fun hgetAll(key: String): Response<List<String>> =
        add(_hgetAll(key))

    override suspend fun hincrBy(key: String, field: String, increment: Long): Response<Long> =
        add(_hincrBy(key, field, increment))

    override suspend fun hincrByFloat(key: String, field: String, increment: Long): Response<String> =
        add(_hincrByFloat(key, field, increment))

    override suspend fun hkeys(key: String): Response<List<String>> =
        add(_hkeys(key))

    override suspend fun hlen(key: String): Response<Long> =
        add(_hlen(key))

    override suspend fun hmget(key: String, field: String, vararg fields: String): Response<List<String?>> =
        add(_hmget(key, field, *fields))

    override suspend fun hrandfield(key: String): Response<String?> =
        add(_hrandfield(key))

    override suspend fun hrandfield(key: String, count: Int, withValues: Boolean?): Response<List<String>> =
        add(_hrandfield(key, count, withValues))

    override suspend fun hset(
        key: String,
        fieldValuePair: FieldValuePair,
        vararg fieldValuePairs: FieldValuePair
    ): Response<Long> =
        add(_hset(key, fieldValuePair, *fieldValuePairs))

    override suspend fun hsetnx(key: String, field: String, value: String): Response<Long> =
        add(_hsetnx(key, field, value))

    override suspend fun hstrlen(key: String, field: String): Response<Long> =
        add(_hstrlen(key, field))

    override suspend fun hvals(key: String): Response<List<String>> =
        add(_hvals(key))

    override suspend fun hscan(key: String, cursor: Long, matchPattern: String?, count: Long?): Response<HScanResult> =
        add(_hscan(key, cursor, matchPattern, count))
}