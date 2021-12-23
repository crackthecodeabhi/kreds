package io.github.crackthecodeabhi.kreds.commands

import io.github.crackthecodeabhi.kreds.args.*
import io.github.crackthecodeabhi.kreds.commands.ConnectionCommand.*
import io.github.crackthecodeabhi.kreds.commands.ClientConnectionCommand.*
import io.github.crackthecodeabhi.kreds.protocol.*

internal enum class ClientConnectionCommand(override val subCommand: Command? = null, override val string: String = "CLIENT"): Command{
    CLIENT_CACHING(CACHING),
    CLIENT_GETNAME(GETNAME),
    CLIENT_GETREDIR(GETREDIR),
    CLIENT_LIST(LIST),
    CLIENT_NO_EVICT(NO_EVICT),
    CLIENT_ID(ID),
    CLIENT_INFO(INFO),
    CLIENT_PAUSE(PAUSE),
    CLIENT_REPLY(REPLY),
    CLIENT_SETNAME(SETNAME),
    CLIENT_UNPAUSE(UNPAUSE),
}
internal enum class ConnectionCommand(command: String? = null, override val subCommand: Command? = null): Command{
    AUTH,CACHING,GETNAME,GETREDIR,LIST,NO_EVICT("NO-EVICT"),
    ID,INFO,PAUSE,REPLY,SETNAME,UNPAUSE,ECHO,PING,QUIT,RESET,SELECT;

    override val string: String = command ?: name
}

internal interface BaseConnectionCommands{
    fun _auth(password: String) = CommandExecution(AUTH, SimpleStringCommandProcessor,password.toArgument())
    fun _auth(username: String, password: String) = CommandExecution(AUTH, SimpleStringCommandProcessor,username.toArgument(),password.toArgument())
    fun _clientList(clientListType: ClientListType? = null, vararg clientIds: String): CommandExecution {
        val idArg = if(clientIds.isEmpty()) null else {
            "ID ${clientIds.joinToString(" ")}"
        }
        return CommandExecution(
            CLIENT_LIST, BulkStringCommandProcessor, *createArguments(
                clientListType,
                idArg
            ))
    }
    fun _clientNoEvict(on: Boolean) =
        CommandExecution(
            CLIENT_NO_EVICT,
            SimpleStringCommandProcessor,
            if(on) "ON".toArgument() else "OFF".toArgument()
        )
    fun _clientCaching(yes: Boolean) =
        CommandExecution(CLIENT_CACHING, SimpleStringCommandProcessor,(if(yes) "YES" else "NO").toArgument())
    fun _clientGetName() = CommandExecution(CLIENT_GETNAME, BulkStringCommandProcessor)
    fun _clientGetRedir() = CommandExecution(CLIENT_GETREDIR, IntegerCommandProcessor)
    fun _clientId() = CommandExecution(CLIENT_ID, IntegerCommandProcessor)
    fun _clientInfo()=CommandExecution(CLIENT_INFO, BulkStringCommandProcessor)
    fun _clientPause(timeout: ULong, clientPauseOption: ClientPauseOption? = null) =
        CommandExecution(CLIENT_PAUSE, SimpleStringCommandProcessor,*createArguments(
            timeout,clientPauseOption
        ))
    fun _clientReply(clientReplyOption: ClientReplyOption)=
        CommandExecution(CLIENT_REPLY, SimpleStringCommandProcessor,clientReplyOption)

    fun _clientSetname(connectionName: String)= CommandExecution(CLIENT_SETNAME, SimpleStringCommandProcessor,connectionName.toArgument())
    fun _clientUnpause() = CommandExecution(CLIENT_UNPAUSE, SimpleStringCommandProcessor)
    fun _echo(message: String) = CommandExecution(ECHO, BulkStringCommandProcessor, message.toArgument())
    fun _ping(message: String?)= CommandExecution(PING, SimpleStringCommandProcessor,*createArguments(message))
    fun _quit() = CommandExecution(QUIT, SimpleStringCommandProcessor)
    fun _reset() = CommandExecution(RESET, SimpleStringCommandProcessor)
    fun _select(index: ULong)= CommandExecution(SELECT, SimpleStringCommandProcessor,index.toArgument())
}

public interface ConnectionCommands{
    /**
     * ###  AUTH password
     *
     * [Doc](https://redis.io/commands/auth)
     * @since 1.0.0
     * @return string reply an error if the password, or username/password pair, is invalid.
     */
    public suspend fun auth(password: String): String

    /**
     * ###  AUTH username password
     *
     * [Doc](https://redis.io/commands/auth)
     * @since 1.0.0
     * @return string reply an error if the password, or username/password pair, is invalid.
     */
    public suspend fun auth(username: String, password: String): String

    /**
     * ### CLIENT CACHING YES|NO
     *
     * [Doc](https://redis.io/commands/client-caching)
     *
     * @since 6.0.0
     * @return OK
     */
    public suspend fun clientCaching(yes: Boolean): String

    /**
     * ### CLIENT GETNAME
     *
     * [Doc](https://redis.io/commands/client-getname)
     *
     * @since 2.6.9
     * @return The connection name, or a null if no name is set.
     */
    public suspend fun clientGetName(): String?

    /**
     * ### CLIENT GETREDIR
     *
     * [Doc](https://redis.io/commands/client-getredir)
     *
     * @since 6.0.0
     * @return the ID of the client we are redirecting the notifications to. The command returns -1 if client tracking is not enabled, or 0 if client tracking is enabled but we are not redirecting the notifications to any client.
     */
    public suspend fun clientGetRedir(): Long

    /**
     * ### CLIENT ID
     *
     * [Doc](https://redis.io/commands/client-id)
     *
     * @since 5.0.0
     * @return The id of the client.
     */
    public suspend fun clientId(): Long

    /**
     * ### CLIENT INFO
     *
     * [Doc](https://redis.io/commands/client-info)
     *
     * @since 6.2.0
     * @return a unique string
     */
    public suspend fun clientInfo(): String

    /**
     * ### ` CLIENT LIST [TYPE normal|master|replica|pubsub] [ID client-id [client-id ...]]`
     *
     * [Doc](https://redis.io/commands/client-list)
     * @since 2.4.0
     * @return a unique string [refer](https://redis.io/commands/client-list)
     */
    public suspend fun clientList(clientListType: ClientListType? = null, vararg clientIds: String): String?

    /**
     * ###  CLIENT NO-EVICT ON|OFF
     *
     * [Doc](https://redis.io/commands/client-no-evict)
     * @since 7.0.0
     * @return OK
     */
    public suspend fun clientNoEvict(on: Boolean): String

    /**
     * ###  CLIENT PAUSE timeout [WRITE|ALL]
     *
     * [Doc](https://redis.io/commands/client-pause)
     * @since 2.9.50
     * @return OK or exception if the timeout is invalid.
     */
    public suspend fun clientPause(timeout: ULong, clientPauseOption: ClientPauseOption? = null): String

    /**
     * ###  CLIENT REPLY ON|OFF|SKIP
     *
     * [Doc](https://redis.io/commands/client-reply)
     * @since 3.2.0
     * @return OK
     */
    public suspend fun clientReply(clientReplyOption: ClientReplyOption): String

    /**
     * ###  CLIENT SETNAME connection-name
     *
     * [Doc](https://redis.io/commands/client-setname)
     * @since 2.6.9
     * @return OK
     */
    public suspend fun clientSetname(connectionName: String): String

    /**
     * ### CLIENT UNPAUSE
     *
     * [Doc](https://redis.io/commands/client-unpause)
     * @since 6.2.0
     * @return OK
     */
    public suspend fun clientUnpause(): String

    /**
     * ###  ECHO message
     *
     * [Doc](https://redis.io/commands/echo)
     * @since 1.0.0
     * @return message
     */
    public suspend fun echo(message: String): String

    /**
     * ###  `PING [message]`
     *
     * [Doc](https://redis.io/commands/ping)
     * @since 1.0.0
     * @return string reply
     */
    public suspend fun ping(message: String? = null): String

    /**
     * ### QUIT
     *
     * [Doc](https://redis.io/commands/quit)
     * @since 1.0.0
     * @return OK
     */
    public suspend fun quit(): String

    /**
     * ### RESET
     *
     * [Doc](https://redis.io/commands/reset)
     * @since 6.2
     * @return RESET
     */
    public suspend fun reset():String

    /**
     * ### SELECT index
     *
     * [Doc](https://redis.io/commands/select)
     * @since 1.0.0
     * @return String
     */
    public suspend fun select(index: ULong): String

}

internal interface ConnectionCommandsExecutor: CommandExecutor, ConnectionCommands, BaseConnectionCommands {
    override suspend fun auth(password: String): String = execute(_auth(password))
    override suspend fun auth(username: String, password: String): String =
        execute(_auth(username,password))

    override suspend fun clientCaching(yes: Boolean): String =
        execute(_clientCaching(yes))

    override suspend fun clientGetName(): String? =
        execute(_clientGetName())

    override suspend fun clientGetRedir(): Long =
        execute(_clientGetRedir())

    override suspend fun clientId(): Long =
        execute(_clientId())

    override suspend fun clientInfo(): String =
        execute(_clientInfo())

    override suspend fun clientList(clientListType: ClientListType?, vararg clientIds: String): String? =
        execute(_clientList(clientListType,*clientIds))

    override suspend fun clientNoEvict(on: Boolean): String =
        execute(_clientNoEvict(on))

    override suspend fun clientPause(timeout: ULong, clientPauseOption: ClientPauseOption?): String =
        execute(_clientPause(timeout, clientPauseOption))

    override suspend fun clientReply(clientReplyOption: ClientReplyOption): String =
        execute(_clientReply(clientReplyOption))

    override suspend fun clientSetname(connectionName: String): String =
        execute(_clientSetname(connectionName))

    override suspend fun clientUnpause(): String =
        execute(_clientUnpause())

    override suspend fun echo(message: String): String =
        execute(_echo(message))

    override suspend fun ping(message: String?): String =
        execute(_ping(message))

    override suspend fun quit(): String =
        execute(_quit())

    override suspend fun reset(): String =
        execute(_reset())

    override suspend fun select(index: ULong): String =
        execute(_select(index))
}