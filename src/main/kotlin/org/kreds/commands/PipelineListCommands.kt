package org.kreds.commands

import org.kreds.BeforeAfterOption
import org.kreds.LeftRightOption

interface PipelineListCommands {

    suspend fun lindex(key: String, index: Long): Response<String?>

    suspend fun linsert(key: String, beforeAfterOption: BeforeAfterOption, pivot: String, element: String): Response<Long>

    suspend fun llen(key: String): Response<Long>

    suspend fun lmove(
        source: String,
        destination: String,
        leftRightOption1: LeftRightOption,
        leftRightOption2: LeftRightOption
    ): Response<String>

    suspend fun lmpop(numkeys: Long, key: String, vararg keys: String, leftRight: LeftRightOption, count: Long?): Response<LMPOPResult?>

    suspend fun lpop(key: String): Response<String?>

    suspend fun lpop(key: String, count: Long): Response<List<String>?>
}

interface PipelineListCommandExecutor: PipelineListCommands, QueuedCommand, BaseListCommands{
    override suspend fun lindex(key: String, index: Long): Response<String?> = add(_lindex(key, index))

    override suspend fun linsert(
        key: String,
        beforeAfterOption: BeforeAfterOption,
        pivot: String,
        element: String
    ): Response<Long> = add(_linsert(key, beforeAfterOption, pivot, element))

    override suspend fun llen(key: String): Response<Long> = add(_llen(key))

    override suspend fun lmove(
        source: String,
        destination: String,
        leftRightOption1: LeftRightOption,
        leftRightOption2: LeftRightOption
    ): Response<String> = add(_lmove(source, destination, leftRightOption1, leftRightOption2))

    override suspend fun lmpop(
        numkeys: Long,
        key: String,
        vararg keys: String,
        leftRight: LeftRightOption,
        count: Long?
    ): Response<LMPOPResult?> = add(_lmpop(numkeys, key, *keys, leftRight = leftRight, count = count))

    override suspend fun lpop(key: String): Response<String?> = add(_lpop(key))

    override suspend fun lpop(key: String, count: Long): Response<List<String>?> = add(_lpop(key, count))
}