package org.kreds.commands

import org.kreds.commands.HyperLogLogCommand.*
import org.kreds.args.*
import org.kreds.protocol.*
import org.kreds.pipeline.Response
import org.kreds.pipeline.QueuedCommand

enum class HyperLogLogCommand(override val subCommand: Command? = null): Command{
    PFADD,PFCOUNT,PFMERGE;

    override val string = name
}

interface BaseHyperLogLogCommands{

    fun _pfadd(key: String, vararg elements: String)=
        CommandExecution(PFADD, IntegerCommandProcessor,*createArguments(key,elements))

    fun _pfcount(key: String, vararg keys: String) =
        CommandExecution(PFCOUNT, IntegerCommandProcessor, *createArguments(key,keys))

    fun _pfmerge(destKey: String,sourceKey: String, vararg sourceKeys: String)=
        CommandExecution(PFMERGE, SimpleStringCommandProcessor,*createArguments(
            destKey,sourceKey,sourceKeys
        ))
}

interface HyperLogLogCommands {

    /**
     * ### ` PFADD key [element [element ...]] `
     *
     * Adds all the element arguments to the HyperLogLog data structure stored at the variable name specified as first argument
     *
     * [Doc](https://redis.io/commands/pfadd)
     * @since 2.8.9
     * @return 1 if at least 1 HyperLogLog internal register was altered. 0 otherwise.
     */
    suspend fun pfadd(key: String, vararg elements: String): Long

    /**
     * ### ` PFCOUNT key [key ...]`
     *
     * When called with a single key, returns the approximated cardinality computed by the HyperLogLog data structure stored at the specified variable, which is 0 if the variable does not exist.
     * When called with multiple keys, returns the approximated cardinality of the union of the HyperLogLogs passed, by internally merging the HyperLogLogs stored at the provided keys into a temporary HyperLogLog.
     *
     * [Doc](https://redis.io/commands/pfcount)
     * @since 2.8.9
     * @return The approximated number of unique elements observed via PFADD.
     */
    suspend fun pfcount(key: String, vararg keys: String): Long

    /**
     * ### ` PFMERGE destkey sourcekey [sourcekey ...] `
     *
     * Merge multiple HyperLogLog values into an unique value that will approximate the cardinality of the union of the observed Sets of the source HyperLogLog structures.
     *
     * [Doc](https://redis.io/commands/pfmerge)
     * @since 2.8.9
     * @return The command just returns OK.
     */
    suspend fun pfmerge(destKey: String,sourceKey: String, vararg sourceKeys: String): String
}

interface HyperLogLogCommandExecutor: HyperLogLogCommands, CommandExecutor, BaseHyperLogLogCommands{

    override suspend fun pfadd(key: String, vararg elements: String): Long =
        execute(_pfadd(key, *elements))

    override suspend fun pfcount(key: String, vararg keys: String): Long =
        execute(_pfcount(key, *keys))

    override suspend fun pfmerge(destKey: String, sourceKey: String, vararg sourceKeys: String): String =
        execute(_pfmerge(destKey, sourceKey, *sourceKeys))
}


interface PipelineHyperLogLogCommands {
    /**
     * @see [HyperLogLogCommands.pfadd]
     */
    suspend fun pfadd(key: String, vararg elements: String): Response<Long>

    /**
     * @see [HyperLogLogCommands.pfcount]
     */
    suspend fun pfcount(key: String, vararg keys: String): Response<Long>

    /**
     * @see [HyperLogLogCommands.pfmerge]
     */
    suspend fun pfmerge(destKey: String,sourceKey: String, vararg sourceKeys: String): Response<String>
}


interface PipelineHyperLogLogCommandExecutor: PipelineHyperLogLogCommands, QueuedCommand, BaseHyperLogLogCommands {
    override suspend fun pfadd(key: String, vararg elements: String): Response<Long> =
        add(_pfadd(key, *elements))

    override suspend fun pfcount(key: String, vararg keys: String): Response<Long> =
        add(_pfcount(key, *keys))


    override suspend fun pfmerge(destKey: String, sourceKey: String, vararg sourceKeys: String): Response<String> =
        add(_pfmerge(destKey, sourceKey, *sourceKeys))
}

