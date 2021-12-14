package org.kreds.commands

import org.kreds.ExpireOption
import org.kreds.PExpireOption

interface PipelineKeyCommands{
    /**
     * @see [KeyCommands.del]
     */
    fun del(vararg keys: String): Response<Long>

    /**
     * @see [KeyCommands.copy]
     */
    fun copy(source: String, destination: String, destinationDb: String? = null, replace: Boolean? = null): Response<Long>

    /**
     * @see [KeyCommands.dump]
     */
    fun dump(key: String): Response<String?>

    /**
     * @see [KeyCommands.exists]
     */
    fun exists(vararg keys: String): Response<Long>

    /**
     * @see [KeyCommands.expire]
     */
    fun expire(key: String, seconds: ULong, expireOption: ExpireOption? = null): Response<Long>

    /**
     * @see [KeyCommands.expireAt]
     */
    fun expireAt(key: String, timestamp: ULong, expireOption: ExpireOption? = null): Response<Long>

    /**
     * @see [KeyCommands.expireTime]
     */
    fun expireTime(key: String): Response<Long>

    /**
     * @see [KeyCommands.keys]
     */
    fun keys(pattern: String): Response<List<String>>

    /**
     * @see [KeyCommands.move]
     */
    fun move(key: String, db: String): Response<Long>

    /**
     * @see [KeyCommands.persist]
     */
    fun persist(key: String): Response<Long>

    /**
     * @see [KeyCommands.pexpire]
     */
    fun pexpire(key: String, milliseconds: ULong,expireOption: PExpireOption? = null): Response<Long>

    /**
     * @see [KeyCommands.pexpireat]
     */
    fun pexpireat(key:String, millisecondsTimestamp: ULong, expireOption: PExpireOption? = null /* = org.kreds.ExpireOption? */): Response<Long>

    /**
     * @see [KeyCommands.pexpiretime]
     */
    fun pexpiretime(key: String): Response<Long>

    /**
     * @see [KeyCommands.pttl]
     */
    fun pttl(key: String): Response<Long>

    /**
     * @see [KeyCommands.randomKey]
     */
    fun randomKey(): Response<String?>

    /**
     * @see [KeyCommands.rename]
     */
    fun rename(key: String, newKey: String): Response<String>

    /**
     * @see [KeyCommands.renamenx]
     */
    fun renamenx(key: String, newKey: String): Response<Long>

    /**
     * @see [KeyCommands.touch]
     */
    fun touch(vararg keys: String): Response<Long>

    /**
     * @see [KeyCommands.ttl]
     */
    fun ttl(key: String): Response<Long>

    /**
     * @see [KeyCommands.type]
     */
    fun type(key: String): Response<String>

    /**
     * @see [KeyCommands.unlink]
     */
    fun unlink(vararg keys: String): Response<Long>
}

interface PipelineKeyCommandExecutor: QueuedCommand, PipelineKeyCommands, BaseKeyCommands {

    override fun del(vararg keys: String): Response<Long>  = add(_del(*keys))

    override fun copy(source: String, destination: String, destinationDb: String?, replace: Boolean?): Response<Long> =
        add(_copy(source, destination, destinationDb, replace))

    override fun dump(key: String): Response<String?> = add(_dump(key))

    override fun exists(vararg keys: String): Response<Long> = add(_exists(*keys))

    override fun expire(key: String, seconds: ULong, expireOption: ExpireOption?): Response<Long> =
        add(_expire(key, seconds, expireOption))

    override fun expireAt(key: String, timestamp: ULong, expireOption: ExpireOption?): Response<Long> =
        add(_expireAt(key, timestamp, expireOption))

    override fun expireTime(key: String): Response<Long> =
        add(_expireTime(key))

    override fun keys(pattern: String): Response<List<String>> =
        add(_keys(pattern))

    override fun move(key: String, db: String): Response<Long> =
        add(_move(key, db))

    override fun persist(key: String): Response<Long> =
        add(_persist(key))

    override fun pexpire(key: String, milliseconds: ULong, expireOption: PExpireOption?): Response<Long> =
        add(_pexpire(key, milliseconds, expireOption))

    override fun pexpireat(key: String, millisecondsTimestamp: ULong, expireOption: PExpireOption?): Response<Long> =
        add(_pexpireat(key, millisecondsTimestamp, expireOption))

    override fun pexpiretime(key: String): Response<Long> =
        add(_pexpiretime(key))

    override fun pttl(key: String): Response<Long> =
        add(_pttl(key))

    override fun randomKey(): Response<String?> =
        add(_randomKey())

    override fun rename(key: String, newKey: String): Response<String> =
        add(_rename(key, newKey))

    override fun renamenx(key: String, newKey: String): Response<Long> =
        add(_renamenx(key, newKey))

    override fun touch(vararg keys: String): Response<Long> =
        add(_touch(*keys))

    override fun ttl(key: String): Response<Long> =
        add(_ttl(key))

    override fun type(key: String): Response<String> =
        add(_type(key))

    override fun unlink(vararg keys: String): Response<Long> =
        add(_unlink(*keys))
}