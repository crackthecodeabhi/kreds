package io.github.crackthecodeabhi.kreds.connection

import kotlinx.coroutines.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import io.github.crackthecodeabhi.kreds.Kreds

class TestClient {

    companion object{
        @JvmStatic
        @AfterAll
        fun shutdown(){
            runBlocking {
                KredsClientGroup.shutdown()
            }
        }
    }
    @Test
    fun testClient(): Unit = runBlocking {
        launch(Dispatchers.Default){
            KredsClientGroup.newClient(Endpoint.from("127.0.0.1:6379")).use { client ->
                println("Client id = ${client.clientId()}")
                println("Client setname = ${client.clientSetname("YAAAS")}")
                println("Client getname = ${client.clientGetName()}")

                client.pubsubHelp().forEach(::println)
                if(client.del("abhi") == 1L) println("Deleted key: abhi")
                client.keys("*").forEach {
                    println("key = $it")
                }
                client.set("abhi","100")
                println("increment abhi ${client.incr("abhi")}")

                if(client.expire("abhi",3u) == 1L){
                    println("abhi set to expire in 3 seconds")
                } else println("failed to expire key")
                delay(3500)
                println("Expecting to get null for abhi now ${client.get("abhi")}")
            }
        }
    }

   /* @Test
    fun pubsubTest() : Unit = runBlocking {
        val eventsScope = CoroutineScope(coroutineContext + CoroutineName("Client Event Handler Scope") + Dispatchers.Default.limitedParallelism(1))
        val handler = object : AbstractKredsSubscriber(eventsScope){
            override fun onMessage(channel: String, message: String) {
                println(message)
            }

            override fun onSubscribe(channel: String, subscribedChannels: Long) {
                println("subscribed with $subscribedChannels")
            }

            override fun onUnsubscribe(channel: String, subscribedChannels: Long) {
                println("unsubscribed with $subscribedChannels")
            }

            override fun onException(ex: Throwable) {
                println(ex)
            }
        }
        val subscriber = KredsClientGroup.newSubscriberClient(Endpoint.from("127.0.0.1:6379"),handler)
        withContext(Dispatchers.Default){
            launch {
                subscriber.subscribe("hello")
                println("subscribed")
            }
            launch{
                delay(30000)
                subscriber.unsubscribe("hello")
                println("unsubscribed")
            }
            launch {
                val publisher = KredsClientGroup.newClient(Endpoint.from("127.0.0.1:6379"))
                repeat(100000){
                    if(publisher.publish("hello","$it: Publisher!") == 0L)
                        println("No one subscribed at $it")
                }
                publisher.close()
            }

        }
    }*/

    /*@Test
    fun failInterleavedRequests() : Unit = runBlocking {
        val client = KredsClientGroup.newClient(Endpoint.from("127.0.0.1:6379"))
        launch(Kreds) {
            delay(1000)
            println(client.set("num","159"))
        }
        launch(Kreds) {
            delay(1000)
            println(client.incr("num"))
        }
    }*/
}