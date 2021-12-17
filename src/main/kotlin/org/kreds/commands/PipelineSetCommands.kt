package org.kreds.commands

interface PipelineSetCommands {
    suspend fun sadd(key: String, member: String, vararg members: String): Response<Long>

    suspend fun scard(key: String): Response<Long>

    suspend fun sdiff(key: String, vararg keys: String): Response<List<String>>

    suspend fun sdiffstore(destination: String, key: String, vararg keys: String): Response<Long>

    suspend fun sinter(key: String, vararg keys: String): Response<List<String>>

    suspend fun sintercard(numkeys: Int,key: String, vararg keys: String, limit: Long?): Response<Long>

    suspend fun sinterstore(destination: String,key: String, vararg keys: String): Response<Long>

    suspend fun sismember(key: String, member: String): Response<Long>

    suspend fun smembers(key: String): Response<List<String>>

    suspend fun smismember(key: String, member: String, vararg  members: String): Response<List<Long>>

    suspend fun smove(source: String, destination: String, member: String): Response<Long>

    suspend fun spop(key: String): Response<String?>

    suspend fun spop(key: String, count: Int): Response<List<String>>

    suspend fun srandmember(key: String): Response<String?>

    suspend fun srandmember(key: String, count: Int): Response<List<String>>

    suspend fun srem(key: String, member: String,vararg members: String): Response<Long>

    suspend fun sunion(key: String, vararg keys: String): Response<List<String>>

    suspend fun sunionstore(destination: String, key: String, vararg keys: String): Response<Long>

    suspend fun sscan(key: String, cursor: Long, matchPattern: String? = null, count: Long? = null): Response<SScanResult>
}

interface PipelineSetCommandExecutor: BaseSetCommands,PipelineSetCommands, QueuedCommand{
    override suspend fun sadd(key: String, member: String, vararg members: String): Response<Long> =
        add(_sadd(key, member, *members))

    override suspend fun scard(key: String): Response<Long> =
        add(_scard(key))

    override suspend fun sdiff(key: String, vararg keys: String): Response<List<String>> =
        add(_sdiff(key, *keys))

    override suspend fun sdiffstore(destination: String, key: String, vararg keys: String): Response<Long> =
        add(_sdiffstore(destination, key, *keys))

    override suspend fun sinter(key: String, vararg keys: String): Response<List<String>> =
        add(_sinter(key, *keys))

    override suspend fun sintercard(numkeys: Int, key: String, vararg keys: String, limit: Long?): Response<Long> =
        add(_sintercard(numkeys, key, *keys, limit = limit))

    override suspend fun sinterstore(destination: String, key: String, vararg keys: String): Response<Long> =
        add(_sinterstore(destination, key, *keys))

    override suspend fun sismember(key: String, member: String): Response<Long> =
        add(_sismember(key, member))

    override suspend fun smembers(key: String): Response<List<String>> =
        add(_smembers(key))

    override suspend fun smismember(key: String, member: String, vararg members: String): Response<List<Long>> =
        add(_smismember(key, member, *members))

    override suspend fun smove(source: String, destination: String, member: String): Response<Long> =
        add(_smove(source, destination, member))

    override suspend fun spop(key: String): Response<String?> =
        add((_spop(key)))

    override suspend fun spop(key: String, count: Int): Response<List<String>> =
        add(_spop(key, count))

    override suspend fun srandmember(key: String): Response<String?> =
        add(_srandmember(key))

    override suspend fun srandmember(key: String, count: Int): Response<List<String>> =
        add(_srandmember(key, count))

    override suspend fun srem(key: String, member: String, vararg members: String): Response<Long> =
        add(_srem(key, member, *members))

    override suspend fun sunion(key: String, vararg keys: String): Response<List<String>> =
        add(_sunion(key, *keys))

    override suspend fun sunionstore(destination: String, key: String, vararg keys: String): Response<Long> =
        add(_sunionstore(destination, key, *keys))

    override suspend fun sscan(key: String, cursor: Long, matchPattern: String?, count: Long?): Response<SScanResult> =
        add(_sscan(key, cursor, matchPattern, count))
}