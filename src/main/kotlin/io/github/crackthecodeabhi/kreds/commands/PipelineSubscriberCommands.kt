/*
 *  Copyright (C) 2023 Abhijith Shivaswamy
 *   See the notice.md file distributed with this work for additional
 *   information regarding copyright ownership.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package io.github.crackthecodeabhi.kreds.commands

import io.github.crackthecodeabhi.kreds.args.createArguments
import io.github.crackthecodeabhi.kreds.args.toArgument
import io.github.crackthecodeabhi.kreds.connection.PubSubCommand
import io.github.crackthecodeabhi.kreds.pipeline.QueuedCommand
import io.github.crackthecodeabhi.kreds.pipeline.Response
import io.github.crackthecodeabhi.kreds.protocol.ArrayCommandProcessor
import io.github.crackthecodeabhi.kreds.protocol.IntegerCommandProcessor

public interface PipelinePublishCommands {
    public suspend fun publish(channel: String, message: String): Response<Long>
    public suspend fun pubsubChannels(pattern: String?): Response<List<String>>
    public suspend fun pubsubNumpat(): Response<Long>
    public suspend fun pubsubNumsub(vararg channels: String): Response<List<Any>>
    public suspend fun pubsubHelp(): Response<List<String>>
}

internal interface PipelinePublishCommandExecutor : QueuedCommand, PipelinePublishCommands {
    override suspend fun publish(channel: String, message: String): Response<Long> =
        add(CommandExecution(PubSubCommand.PUBLISH, IntegerCommandProcessor, channel.toArgument(), message.toArgument()))

    override suspend fun pubsubChannels(pattern: String?): Response<List<String>> =
        add(CommandExecution(PubSubCommand.PUBSUB_CHANNELS, ArrayCommandProcessor, *createArguments(pattern)).responseTo("pubsub channels"))

    override suspend fun pubsubNumpat(): Response<Long> =
        add(CommandExecution(PubSubCommand.PUBSUB_NUMPAT, IntegerCommandProcessor))

    override suspend fun pubsubNumsub(vararg channels: String): Response<List<Any>> =
        add(CommandExecution(PubSubCommand.PUBSUB_NUMSUB, ArrayCommandProcessor, *createArguments(*channels)).responseTo("pubsub numsub"))

    override suspend fun pubsubHelp(): Response<List<String>> =
        add(CommandExecution(PubSubCommand.PUBSUB_HELP, ArrayCommandProcessor).responseTo("pubsub help"))
}