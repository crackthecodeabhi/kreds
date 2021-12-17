package org.kreds.commands

import org.kreds.BeforeAfterOption
import org.kreds.LeftRightOption

interface PipelineListCommands {

    fun lindex(key: String, index: Long): Response<String?>

    fun linsert(key: String, beforeAfterOption: BeforeAfterOption, pivot: String, element: String): Response<Long>

    fun llen(key: String): Response<Long>

    fun lmove(
        source: String,
        destination: String,
        leftRightOption1: LeftRightOption,
        leftRightOption2: LeftRightOption
    ): Response<String>

    fun lmpop(numkeys: Long, key: String, vararg keys: String, leftRight: LeftRightOption, count: Long?): Response<LMPOPResult?>

    fun lpop(key: String): Response<String?>

    fun lpop(key: String, count: Long): Response<List<String>?>
}

interface PipelineListCommandExecutor: PipelineListCommands, QueuedCommand, BaseListCommands{
    override fun lindex(key: String, index: Long): Response<String?> = add(_lindex(key, index))

    override fun linsert(
        key: String,
        beforeAfterOption: BeforeAfterOption,
        pivot: String,
        element: String
    ): Response<Long> = add(_linsert(key, beforeAfterOption, pivot, element))

    override fun llen(key: String): Response<Long> = add(_llen(key))

    override fun lmove(
        source: String,
        destination: String,
        leftRightOption1: LeftRightOption,
        leftRightOption2: LeftRightOption
    ): Response<String> = add(_lmove(source, destination, leftRightOption1, leftRightOption2))

    override fun lmpop(
        numkeys: Long,
        key: String,
        vararg keys: String,
        leftRight: LeftRightOption,
        count: Long?
    ): Response<LMPOPResult?> = add(_lmpop(numkeys, key, *keys, leftRight = leftRight, count = count))

    override fun lpop(key: String): Response<String?> = add(_lpop(key))

    override fun lpop(key: String, count: Long): Response<List<String>?> = add(_lpop(key, count))
}