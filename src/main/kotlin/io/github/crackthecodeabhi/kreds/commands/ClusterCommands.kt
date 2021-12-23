package io.github.crackthecodeabhi.kreds.commands

import io.github.crackthecodeabhi.kreds.args.FailOverOption
import io.github.crackthecodeabhi.kreds.args.createArguments
import io.github.crackthecodeabhi.kreds.args.toArgument
import io.github.crackthecodeabhi.kreds.commands.ClusterCommand.*
import io.github.crackthecodeabhi.kreds.protocol.*
import io.github.crackthecodeabhi.kreds.protocol.ArrayCommandProcessor
import io.github.crackthecodeabhi.kreds.protocol.BulkStringCommandProcessor
import io.github.crackthecodeabhi.kreds.protocol.CommandExecutor
import io.github.crackthecodeabhi.kreds.protocol.IntegerCommandProcessor
import io.github.crackthecodeabhi.kreds.protocol.SimpleStringCommandProcessor

internal enum class ClusterCommand(override val subCommand: Command? = null, commandString: String? = null): Command{
    ASKING,READONLY,READWRITE,
    ADDSLOTS,ADDSLOTSRANGE,BUMPEPOCH,COUNT_FAILURE_REPORTS(commandString = "COUNT-FAILURE-REPORTS"),
    COUNTKEYSINSLOT,DELSLOTS,DELSLOTSRANGE,FAILOVER,FLUSHSLOTS,FORGET,
    GETKEYSINSLOT,INFO,KEYSLOT,LINKS,MEET,MYID,NODES,REPLICAS,REPLICATE,
    RESET,SAVECONFIG,SET_CONFIG_EPOCH(commandString = "SET-CONFIG-EPOCH"),
    SETSLOT,SLAVES,SLOTS,

    CLUSTER_ADD_SLOTS(ADDSLOTS),
    CLUSTER_ADD_SLOTS_RANGE(ADDSLOTSRANGE),
    CLUSTER_BUMPEPOCH(BUMPEPOCH),
    CLUSTER_COUNT_FAILURE_REPORTS(COUNT_FAILURE_REPORTS),
    CLUSTER_COUNT_KEYS_IN_SLOT(COUNTKEYSINSLOT),
    CLUSTER_DEL_SLOTS(DELSLOTS),
    CLUSTER_DEL_SLOTS_RANGE(DELSLOTSRANGE),
    CLUSTER_FAILOVER(FAILOVER),
    CLUSTER_FLUSHSLOTS(FLUSHSLOTS),
    CLUSTER_FORGET(FORGET),
    CLUSTER_GET_KEYS_IN_SLOT(GETKEYSINSLOT),
    CLUSTER_INFO(INFO),
    CLUSTER_KEY_SLOT(KEYSLOT),
    CLUSTER_LINKS(LINKS),
    CLUSTER_MEET(MEET),
    CLUSTER_MY_ID(MYID),
    CLUSTER_NODES(NODES),
    CLUSER_REPLICAS(REPLICAS),
    CLUSTER_REPLICATE(REPLICATE),
    CLUSTER_RESET(RESET),
    CLUSTER_SAVE_CONFIG(SAVECONFIG),
    CLUSTER_SET_CONFIG_EPOCH(SET_CONFIG_EPOCH),
    CLUSTER_SET_SLOT(SETSLOT),
    CLUSTER_SLAVES(SLAVES),
    CLUSTER_SLOTS(SLOTS);

    override val string = commandString ?: name
}


internal interface BaseClusterCommands {
    fun _asking() = CommandExecution(ASKING, SimpleStringCommandProcessor)
    fun _clusterAddSlots(slot: String, vararg slots: String) =
        CommandExecution(CLUSTER_ADD_SLOTS, SimpleStringCommandProcessor,
            *createArguments(slot,*slots))
    fun _clusterAddSlotsRange(slotSpec: Pair<Int,Int>, vararg slotSpecs: Pair<Int,Int>) =
        CommandExecution(CLUSTER_ADD_SLOTS_RANGE, SimpleStringCommandProcessor,*createArguments(slotSpec,*slotSpecs))

    fun _clusterBumpEpoch()=
        CommandExecution(CLUSTER_BUMPEPOCH, SimpleStringCommandProcessor)

    fun _clusterCountFailureReports(nodeId: String) =
        CommandExecution(CLUSTER_COUNT_FAILURE_REPORTS, IntegerCommandProcessor,nodeId.toArgument())

    fun _clusterCountKeysInSlot(slot: Int) =
        CommandExecution(CLUSTER_COUNT_KEYS_IN_SLOT, IntegerCommandProcessor,slot.toArgument())

    fun _clusterDelSlots(slot: Int, vararg slots: Int) =
        CommandExecution(CLUSTER_DEL_SLOTS, SimpleStringCommandProcessor,*createArguments(slot,slots))

    fun _clusterDelSlotsRange(slotSpec: Pair<Int,Int>, vararg slotSpecs: Pair<Int,Int>) =
        CommandExecution(CLUSTER_DEL_SLOTS_RANGE, SimpleStringCommandProcessor,*createArguments(slotSpec,slotSpecs))

    fun _clusterFailOver(option: FailOverOption? = null) =
        CommandExecution(CLUSTER_FAILOVER, SimpleStringCommandProcessor,*createArguments(option))

    fun _clusterFlushSlots() =
        CommandExecution(CLUSTER_FLUSHSLOTS, SimpleStringCommandProcessor)

    fun _clusterForget(nodeId: String) =
        CommandExecution(CLUSTER_FORGET, SimpleStringCommandProcessor,nodeId.toArgument())

    fun _clusterGetKeysInSlot(slot: Int, count: Int) =
        CommandExecution(CLUSTER_GET_KEYS_IN_SLOT, ArrayCommandProcessor,slot.toArgument(),count.toArgument())

    fun _clusterInfo() =
        CommandExecution(CLUSTER_INFO, BulkStringCommandProcessor)
}

public interface ClusterCommands {
    /**
     * ### ASKING
     *
     * When a cluster client receives an -ASK redirect, the ASKING command is sent to the target node followed by the command which was redirected. This is normally done automatically by cluster clients.
     *
     * [Doc](https://redis.io/commands/asking)
     * @since 3.0.0
     * @return OK
     */
    public suspend fun asking(): String

    /**
     * ### ` CLUSTER ADDSLOTS slot [slot ...] `
     *
     * [Doc](https://redis.io/commands/cluster-addslots)
     * @since 3.0.0
     * @return OK
     */
    public suspend fun clusterAddSlots(slot: String, vararg slots: String): String

    /**
     * ### ` CLUSTER ADDSLOTSRANGE start-slot end-slot [start-slot end-slot ...] `
     *
     * The CLUSTER ADDSLOTSRANGE is similar to the CLUSTER ADDSLOTS command in that they both assign hash slots to nodes.
     *
     * [Doc](https://redis.io/commands/cluster-addslotsrange)
     * @since 7.0.0
     * @return OK
     */
    public suspend fun clusterAddSlotsRange(slotSpec: Pair<Int,Int>, vararg slotSpecs: Pair<Int,Int>): String

    /**
     * ### CLUSTER BUMPEPOCH
     *
     * Advances the cluster config epoch.
     *
     * [Doc](https://redis.io/commands/cluster-bumpepoch)
     * @since 3.0.0
     * @return BUMPED if the epoch was incremented, or STILL if the node already has the greatest config epoch in the cluster.
     */
    public suspend fun clusterBumpEpoch(): String

    /**
     * ###  CLUSTER COUNT-FAILURE-REPORTS node-id
     *
     * [Doc](https://redis.io/commands/cluster-count-failure-reports)
     * @since 3.0.0
     * @return the number of active failure reports for the node.
     */
    public suspend fun clusterCountFailureReports(nodeId: String): Long

    /**
     * ###  CLUSTER COUNTKEYSINSLOT slot
     *
     * Returns the number of keys in the specified Redis Cluster hash slot.
     *
     * [Doc](https://redis.io/commands/cluster-countkeysinslot)
     * @since 3.0.0
     * @return The number of keys in the specified hash slot, or an error if the hash slot is invalid.
     */
    public suspend fun clusterCountKeysInSlot(slot: Int): Long

    /**
     * ### ` CLUSTER DELSLOTS slot [slot ...] `
     *
     * [Doc](https://redis.io/commands/cluster-delslots)
     * @since 3.0.0
     * @return OK
     */
    public suspend fun clusterDelSlots(slot: Int, vararg slots: Int): String

    /**
     * ### ` CLUSTER DELSLOTSRANGE start-slot end-slot [start-slot end-slot ...] `
     *
     * [Doc](https://redis.io/commands/cluster-delslotsrange)
     * @since 7.0.0
     * @return OK
     */
    public suspend fun clusterDelSlotsRange(slotSpec: Pair<Int,Int>, vararg slotSpecs: Pair<Int,Int>): String

    /**
     * ### ` CLUSTER FAILOVER [FORCE|TAKEOVER] `
     *
     * [Doc](https://redis.io/commands/cluster-failover)
     * @since 3.0.0
     * @return OK if the command was accepted and a manual failover is going to be attempted. An error if the operation cannot be executed, for example if we are talking with a node which is already a master.
     */
    public suspend fun clusterFailOver(option: FailOverOption? = null): String

    /**
     * ### CLUSTER FLUSHSLOTS
     *
     * Deletes all slots from a node.
     *
     * [Doc](https://redis.io/commands/cluster-flushslots)
     * @since 3.0.0
     * @return OK
     */
    public suspend fun clusterFlushSlots(): String

    /**
     * ###  CLUSTER FORGET node-id
     *
     * [Doc](https://redis.io/commands/cluster-forget)
     * @since 3.0.0
     * @return OK if the command was executed successfully, otherwise an error is returned.
     */
    public suspend fun clusterForget(nodeId: String): String

    /**
     * ###  CLUSTER GETKEYSINSLOT slot count
     *
     * [Doc](https://redis.io/commands/cluster-getkeysinslot)
     * @since 3.0.0
     * @return From 0 to count key names in a list.
     */
    public suspend fun clusterGetKeysInSlot(slot: Int, count: Int): List<String>

    /**
     * ### CLUSTER INFO
     *
     * [Doc](https://redis.io/commands/cluster-info)
     * @since 3.0.0
     * @return A map between named fields and values in the form of <field>:<value> lines separated by newlines composed by the two bytes CRLF.
     */
    public suspend fun clusterInfo(): String

    //TODO: continue from CLUSTER KEYSLOT
}

internal interface ClusterCommandExecutor: BaseClusterCommands, ClusterCommands, CommandExecutor {

    override suspend fun asking(): String = execute(_asking())

    override suspend fun clusterAddSlots(slot: String, vararg slots: String): String =
        execute(_clusterAddSlots(slot,*slots))

    override suspend fun clusterAddSlotsRange(slotSpec: Pair<Int, Int>, vararg slotSpecs: Pair<Int, Int>): String =
        execute(_clusterAddSlotsRange(slotSpec, *slotSpecs))

    override suspend fun clusterBumpEpoch(): String =
        execute(_clusterBumpEpoch())

    override suspend fun clusterCountFailureReports(nodeId: String): Long =
        execute(_clusterCountFailureReports(nodeId))

    override suspend fun clusterCountKeysInSlot(slot: Int): Long =
        execute(_clusterCountKeysInSlot(slot))

    override suspend fun clusterDelSlots(slot: Int, vararg slots: Int): String =
        execute(_clusterDelSlots(slot,*slots))

    override suspend fun clusterDelSlotsRange(slotSpec: Pair<Int, Int>, vararg slotSpecs: Pair<Int, Int>): String =
        execute(_clusterDelSlotsRange(slotSpec, *slotSpecs))

    override suspend fun clusterFailOver(option: FailOverOption?): String =
        execute(_clusterFailOver(option))

    override suspend fun clusterFlushSlots(): String =
        execute(_clusterFlushSlots())

    override suspend fun clusterForget(nodeId: String): String =
        execute(_clusterForget(nodeId))

    override suspend fun clusterGetKeysInSlot(slot: Int, count: Int): List<String> =
        execute(_clusterGetKeysInSlot(slot, count))

    override suspend fun clusterInfo(): String =
        execute(_clusterInfo())
}