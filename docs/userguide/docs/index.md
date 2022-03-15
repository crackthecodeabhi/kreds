# Getting Started 

## What is Kreds?

Kreds is an asynchronous, non-blocking redis client library written in Kotlin using co-routines.

It makes use if Java NIO, Netty, Kotlin Coroutines. It provides an idiomatic approach to use redis in your server applications.

The following features of redis are implemented in Kreds.

* Commands operating on Strings, Hash, Lists, Keys, Sets, Sorted Sets. ✔️
* Blocking commands. ✔️
* Pipelining. ✔️
* Publish/Subscribe. ✔️
* Connection handling commands. ✔️

## Basic usage 
Here's a basic usage of the library.

```kotlin title="Basic Usage"
fun main(){
    runBlocking {
        val job  = launch(Dispatchers.Default) {
            newClient(Endpoint.from("127.0.0.1:6379")).use { client ->
                client.set("foo", "100")
                // prints 101
                println("incremented value of foo ${client.incr("foo")}") 
                client.expire("foo", 3u) // set expiration to 3 seconds
                delay(3000)
                assert(client.get("foo") == null)
                println("done")
            } // <--- the client/connection to redis is closed.
        }
        job.join() // wait for the co-routine to complete
        shutdown() // shutdown the Kreds Event loop.
    }
}
```

## Pipelining

Kreds supports pipelining of Redis commands

Pipelining enables you to execute redis commands in a batch, instead of making a network call for each command.

###Pipelining usage

```kotlin title="Pipeline Execution"
fun main(){
    runBlocking {
        newClient(Endpoint.from("127.0.0.1:6379")).use { client  ->
            val pipe = client.pipelined() // Create a pipeline object on this client connection.

            pipe.set("bar","500")
            // incrementedValueFn can be invoked post pipeline execution to get command result.
            val incrementedValueFn = pipe.incr("bar")  
            pipe.sadd("myset","A","B","C","D","E","F","G","H","I")
            val setCountFn = pipe.scard("myset")

            pipe.execute() // execute the pipeline of commands.

            assert(incrementedValueFn() == 501L)
            assert(setCountFn() == 9L)
        }
        shutdown() // shutdown the Kreds event loop on application shutdown.
    }
}
```

## Pub/Sub

Use Kreds to publish message and subscribe to channels.
Kreds does not use single thread per subscription, so you are not limited by the number of threads you have.

This enabled Kreds applications to subscribe to many channels and scale up easily.

### Subscriber Usage

```kotlin title="Redis Subscriber"
fun main(){
    runBlocking {
        // This call returns after all the coroutines finish.
        coroutineScope {
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
        shutdown() // Shutdown the Kreds event loop on application shutdown.
    }
}
```

###Publisher Usage

```kotlin title="Redis Publisher"
suspend fun publish(){
    newClient(Endpoint.from("127.0.0.1:6379")).use { publisher ->
        for(i in 1 until 10){
            publisher.publish("notifications","message-$i")
        }
    }
}
```