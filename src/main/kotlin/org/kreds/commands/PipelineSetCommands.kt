package org.kreds.commands

interface PipelineSetCommands {
    fun sadd(key: String, member: String, vararg members: String): Response<Long>

    fun scard(key: String): Response<Long>

    fun sdiff(key: String, vararg keys: String): Response<List<String>>

    fun sdiffstore(destination: String, key: String, vararg keys: String): Response<Long>

    fun sinter(key: String, vararg keys: String): Response<List<String>>

    fun sintercard(numkeys: Int,key: String, vararg keys: String, limit: Long?): Response<Long>

    fun sinterstore(destination: String,key: String, vararg keys: String): Response<Long>

    fun sismember(key: String, member: String): Response<Long>

    fun smembers(key: String): Response<List<String>>

    fun smismember(key: String, member: String, vararg  members: String): Response<List<Long>>

    fun smove(source: String, destination: String, member: String): Response<Long>

    fun spop(key: String): Response<String?>

    fun spop(key: String, count: Int): Response<List<String>>

    fun srandmember(key: String): Response<String?>

    fun srandmember(key: String, count: Int): Response<List<String>>

    fun srem(key: String, member: String,vararg members: String): Response<Long>

    fun sunion(key: String, vararg keys: String): Response<List<String>>

    fun sunionstore(destination: String, key: String, vararg keys: String): Response<Long>

    fun sscan(key: String, cursor: Long, matchPattern: String? = null, count: Long? = null): Response<SScanResult>
}

interface PipelineSetCommandExecutor: BaseSetCommands,PipelineSetCommands, QueuedCommand{
    override fun sadd(key: String, member: String, vararg members: String): Response<Long> =
        add(_sadd(key, member, *members))

    override fun scard(key: String): Response<Long> =
        add(_scard(key))

    override fun sdiff(key: String, vararg keys: String): Response<List<String>> =
        add(_sdiff(key, *keys))

    override fun sdiffstore(destination: String, key: String, vararg keys: String): Response<Long> =
        add(_sdiffstore(destination, key, *keys))

    override fun sinter(key: String, vararg keys: String): Response<List<String>> =
        add(_sinter(key, *keys))

    override fun sintercard(numkeys: Int, key: String, vararg keys: String, limit: Long?): Response<Long> =
        add(_sintercard(numkeys, key, *keys, limit = limit))

    override fun sinterstore(destination: String, key: String, vararg keys: String): Response<Long> =
        add(_sinterstore(destination, key, *keys))

    override fun sismember(key: String, member: String): Response<Long> =
        add(_sismember(key, member))

    override fun smembers(key: String): Response<List<String>> =
        add(_smembers(key))

    override fun smismember(key: String, member: String, vararg members: String): Response<List<Long>> =
        add(_smismember(key, member, *members))

    override fun smove(source: String, destination: String, member: String): Response<Long> =
        add(_smove(source, destination, member))

    override fun spop(key: String): Response<String?> =
        add((_spop(key)))

    override fun spop(key: String, count: Int): Response<List<String>> =
        add(_spop(key, count))

    override fun srandmember(key: String): Response<String?> =
        add(_srandmember(key))

    override fun srandmember(key: String, count: Int): Response<List<String>> =
        add(_srandmember(key, count))

    override fun srem(key: String, member: String, vararg members: String): Response<Long> =
        add(_srem(key, member, *members))

    override fun sunion(key: String, vararg keys: String): Response<List<String>> =
        add(_sunion(key, *keys))

    override fun sunionstore(destination: String, key: String, vararg keys: String): Response<Long> =
        add(_sunionstore(destination, key, *keys))

    override fun sscan(key: String, cursor: Long, matchPattern: String?, count: Long?): Response<SScanResult> =
        add(_sscan(key, cursor, matchPattern, count))
}