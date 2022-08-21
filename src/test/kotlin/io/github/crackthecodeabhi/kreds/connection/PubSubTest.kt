/*
 *  Copyright (C) 2022 Abhijith Shivaswamy
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

package io.github.crackthecodeabhi.kreds.connection

import io.github.crackthecodeabhi.kreds.commands.ClientSetup
import io.github.crackthecodeabhi.kreds.commands.ClientTearDown
import io.github.crackthecodeabhi.kreds.commands.shouldBeOk
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

//TODO: if Subscriber.quit is called while in subscription

class PubSubTest : FunSpec({
    val setup = ClientSetup()
    beforeSpec(setup)
    afterSpec(ClientTearDown(setup))

    test("Pub sub") {
        val testChannel = "TEST_CHANNEL"
        val publisher: PublisherCommands = setup.client
        val counter = AtomicInteger(0)
        launch {
            val subscriber =
                newSubscriberClient(setup.client.endpoint, object : AbstractKredsSubscriber() {
                    override fun onException(ex: Throwable) {
                        println(ex)
                    }

                    override fun onMessage(channel: String, message: String) {
                        repeat(message.toInt()) {
                            counter.incrementAndGet()
                        }
                    }

                    override fun onSubscribe(channel: String, subscribedChannels: Long) {
                        println("Subscribed to $channel")
                    }

                    override fun onUnsubscribe(channel: String, subscribedChannels: Long) {
                        println("Unsubscribed from $channel")
                    }
                })

            subscriber.subscribe(testChannel)
            subscriber.ping("OK")!!.shouldBeOk()
            repeat(1000) {
                publisher.publish(testChannel, "1")
            }
            delay(100)
            counter.get() shouldBe 1000
            println("Counter = ${counter.get()}")
            subscriber.unsubscribe(testChannel)
            subscriber.close()
        }
    }
})