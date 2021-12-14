package org.kreds.commands

import org.kreds.*
import java.math.BigDecimal

interface PipelineStringCommands{
    /**
     * @see [StringCommands.get]
     */
    fun get(key: String): Response<String?>

    /**
     * @see [StringCommands.set]
     */
    fun set(key: String, value: String, setOption: SetOption? = null): Response<String?>

    /**
     * @see [StringCommands.append]
     */
    fun append(key: String, value: String): Response<Long>

    /**
     * @see [StringCommands.decr]
     */
    fun decr(key: String): Response<Long>

    /**
     * @see [StringCommands.decrBy]
     */
    fun decrBy(key: String, decrement: Long): Response<Long>

    /**
     * @see [StringCommands.getDel]
     */
    fun getDel(key: String): Response<String?>

    /**
     * @see [StringCommands.getRange]
     */
    fun getRange(key: String, start: Int, end: Int): Response<String?>

    /**
     * @see [StringCommands.getSet]
     */
    fun getSet(key: String,value: String): Response<String?>

    /**
     * @see [StringCommands.incr]
     */
    fun incr(key: String): Response<Long>

    /**
     * @see [StringCommands.incrBy]
     */
    fun incrBy(key: String, increment: Long): Response<Long>

    /**
     * @see [StringCommands.incrByFloat]
     */
    fun incrByFloat(key: String,increment: BigDecimal): Response<String?>

    /**
     * @see [StringCommands.mget]
     */
    fun mget(vararg keys: String): Response<List<String?>>

    /**
     * @see [StringCommands.mset]
     */
    fun mset(vararg keyValues: KeyValuePair): Response<String>

    /**
     * @see [StringCommands.getEx]
     */
    fun getEx(key: String, getExOption: GetExOption? = null): Response<String?>
}


interface PipelineStringCommandsExecutor: PipelineStringCommands,BaseStringCommands, Pipeline {
    override fun get(key: String): Response<String?> = add(_get(key))

    override fun set(key: String, value: String, setOption: SetOption?): Response<String?> = add(_set(key,value,setOption))

    override fun append(key: String, value: String): Response<Long> = add(_append(key, value))

    override fun decr(key: String): Response<Long> = add(_decr(key))

    override fun decrBy(key: String, decrement: Long): Response<Long> = add(_decrBy(key,decrement))

    override fun getDel(key: String): Response<String?> = add(_getDel(key))

    override fun getRange(key: String, start: Int, end: Int): Response<String?> = add(_getRange(key, start, end))

    override fun getSet(key: String, value: String): Response<String?> = add(_getSet(key, value))

    override fun incr(key: String): Response<Long> = add(_incr(key))

    override fun incrBy(key: String, increment: Long): Response<Long> = add(_incrBy(key, increment))

    override fun incrByFloat(key: String, increment: BigDecimal): Response<String?> = add(_incrByFloat(key, increment))

    override fun mget(vararg keys: String): Response<List<String?>> = add(_mget(*keys))

    override fun mset(vararg keyValues: KeyValuePair): Response<String> = add(_mset(*keyValues))

    override fun getEx(key: String, getExOption: GetExOption?): Response<String?> = add(_getEx(key, getExOption))
}
