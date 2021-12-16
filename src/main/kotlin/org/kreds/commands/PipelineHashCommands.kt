package org.kreds.commands

import org.kreds.FieldValuePair

interface PipelineHashCommands{
    /**
     * @see [HashCommands.hdel]
     */
    fun hdel(key: String, field: String, vararg moreFields: String): Response<Long>

    /**
     * @see [HashCommands.hexists]
     */
    fun hexists(key: String, field: String): Response<Long>

    /**
     * @see [HashCommands.hget]
     */
    fun hget(key: String, field: String): Response<String?>

    /**
     * @see [HashCommands.hgetAll]
     */
    fun hgetAll(key: String): Response<List<String>>

    /**
     * @see [HashCommands.hincrBy]
     */
    fun hincrBy(key: String, field: String, increment: Long): Response<Long>

    /**
     * @see [HashCommands.hincrByFloat]
     */
     fun hincrByFloat(key: String,field: String, increment: Long): Response<String>

    /**
     * @see [HashCommands.hkeys]
     */
     fun hkeys(key: String): Response<List<String>>

    /**
     * @see [HashCommands.hlen]
     */
     fun hlen(key: String): Response<Long>

    /**
     * @see [HashCommands.hmget]
     */
     fun hmget(key: String, field: String, vararg fields: String): Response<List<String?>>

    /**
     * @see [HashCommands.hrandfield]
     */
     fun hrandfield(key: String): Response<String?>


    /**
     * @see [HashCommands.hrandfield]
     */
     fun hrandfield(key: String, count: Int, withValues: Boolean? = null): Response<List<String>>


    /**
     * @see [HashCommands.hset]
     */
     fun hset(key: String, fieldValuePair: FieldValuePair, vararg fieldValuePairs: FieldValuePair): Response<Long>

    /**
     * @see [HashCommands.hsetnx]
     */
     fun hsetnx(key: String,field: String, value: String): Response<Long>

    /**
     * @see [HashCommands.hstrlen]
     */
     fun hstrlen(key: String, field: String): Response<Long>

     /**
     * @see [HashCommands.hvals]
     */
     fun hvals(key: String): Response<List<String>>
}

interface PipelineHashCommandExecutor: QueuedCommand, PipelineHashCommands, BaseHashCommands {
    override fun hdel(key: String, field: String, vararg moreFields: String): Response<Long> =
        add(_hdel(key,field, *moreFields))

    override fun hexists(key: String, field: String): Response<Long> =
        add(_hexists(key, field))

    override fun hget(key: String, field: String): Response<String?> =
        add(_hget(key, field))

    override fun hgetAll(key: String): Response<List<String>> =
        add(_hgetAll(key))

    override fun hincrBy(key: String, field: String, increment: Long): Response<Long> =
        add(_hincrBy(key, field, increment))

    override fun hincrByFloat(key: String, field: String, increment: Long): Response<String> =
        add(_hincrByFloat(key, field, increment))

    override fun hkeys(key: String): Response<List<String>> =
        add(_hkeys(key))

    override fun hlen(key: String): Response<Long> =
        add(_hlen(key))

    override fun hmget(key: String, field: String, vararg fields: String): Response<List<String?>> =
        add(_hmget(key, field, *fields))

    override fun hrandfield(key: String): Response<String?> =
        add(_hrandfield(key))

    override fun hrandfield(key: String, count: Int, withValues: Boolean?): Response<List<String>> =
        add(_hrandfield(key, count, withValues))

    override fun hset(
        key: String,
        fieldValuePair: FieldValuePair,
        vararg fieldValuePairs: FieldValuePair
    ): Response<Long> =
        add(_hset(key, fieldValuePair, *fieldValuePairs))

    override fun hsetnx(key: String, field: String, value: String): Response<Long> =
        add(_hsetnx(key, field, value))

    override fun hstrlen(key: String, field: String): Response<Long> =
        add(_hstrlen(key, field))

    override fun hvals(key: String): Response<List<String>> =
        add(_hvals(key))
}