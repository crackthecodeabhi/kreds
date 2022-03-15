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

package io.github.crackthecodeabhi.kreds

import io.github.crackthecodeabhi.kreds.connection.*
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/*fun main(){
   runBlocking {
       val job  = launch(Dispatchers.Default) {
                newClient(Endpoint.from("127.0.0.1:6379")).use { client ->
                    client.set("foo", "100")
                    println("incremented value of foo ${client.incr("foo")}") // prints 101
                    client.expire("foo", 3u) // set expiration to 3 seconds
                    delay(3000)
                    assert(client.get("foo") == null)
                    println("done")
                }
            }
       job.join()
       shutdown()
   }
}*/

/*
fun main(){
   runBlocking {
       newClient(Endpoint.from("127.0.0.1:6379")).use { client  ->
           val pipe = client.pipelined() // Create a pipeline object on this client connection.

           pipe.set("bar","500")
           val incrementedValueFn = pipe.incr("bar") // incrementedValueFn can be invoked post pipeline execution to get command result.
           pipe.sadd("myset","A","B","C","D","E","F","G","H","I")
           val setCountFn = pipe.scard("myset")

           pipe.execute() // execute the pipeline of commands.

           assert(incrementedValueFn() == 501L)
           assert(setCountFn() == 9L)
       }
       shutdown() // shutdown the Kreds event loop on application shutdown.
   }
}*/

fun main(){
    runBlocking {
        coroutineScope {
            launch {
                delay(5.seconds)
                publish()
            }
            val kredSubscriptionHandler = object : AbstractKredsSubscriber() {

                override fun onMessage(channel: String, message: String) {
                    println("Received message: $message from channel $channel")
                }

                override fun onSubscribe(channel: String, subscribedChannels: Long) {
                    println("Subscribed to channel: $channel")
                }

                override fun onUnsubscribe(channel: String, subscribedChannels: Long) {
                    println("Unsubscribed from channel: $channel")
                }

                override fun onException(ex: Throwable) {
                    println("Exception while handling subscription to redis: ${ex.stackTrace}")
                }

            }
            newSubscriberClient(Endpoint.from("127.0.0.1:6379"),kredSubscriptionHandler).use { client ->
                client.subscribe("notifications")
                delay(30.seconds)
                client.unsubscribe("notifications")
            }
        }
        shutdown()
    }
}

suspend fun publish(){
    newClient(Endpoint.from("127.0.0.1:6379")).use { publisher ->
        for(i in 1 until 10){
           publisher.publish("notifications","message-$i")
        }
    }
}