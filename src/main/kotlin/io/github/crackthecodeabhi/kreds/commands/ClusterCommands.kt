package io.github.crackthecodeabhi.kreds.commands

import io.github.crackthecodeabhi.kreds.args.createArguments
import io.github.crackthecodeabhi.kreds.protocol.CommandExecutor
import io.github.crackthecodeabhi.kreds.commands.ClusterCommand.*
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
}

internal interface ClusterCommandExecutor: BaseClusterCommands, ClusterCommands, CommandExecutor {

    override suspend fun asking(): String = execute(_asking())

    override suspend fun clusterAddSlots(slot: String, vararg slots: String): String =
        execute(_clusterAddSlots(slot,*slots))

    override suspend fun clusterAddSlotsRange(slotSpec: Pair<Int, Int>, vararg slotSpecs: Pair<Int, Int>): String =
        execute(_clusterAddSlotsRange(slotSpec, *slotSpecs))
}