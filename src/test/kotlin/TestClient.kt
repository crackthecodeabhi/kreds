import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.kreds.Kreds
import org.kreds.connection.AbstractKredsSubscriber
import org.kreds.connection.Endpoint
import org.kreds.connection.KredsClientGroup

class TestClient {

    @Test
    fun testClient(): Unit = runBlocking {
        launch(Kreds) {
            val client = KredsClientGroup.newClient(Endpoint.from("127.0.0.1:6379"))
            try {
                println(client.clientInfo())
                println("Client id = ${client.clientGetName()}")
                val pipeline = client.pipelined()
                val setResp = pipeline.set("abhi", "590")
                val getResp = pipeline.get("abhi")
                val incrResp = pipeline.incr("abhi")
                pipeline.execute()
                println("setResp = ${setResp.get()}")
                println("getResp = ${getResp.get()}")
                println("incrResp = ${incrResp.get()}")
            }
            finally {
                KredsClientGroup.shutdown()
            }
        }
    }

    @Test
    fun transactionTest(): Unit = runBlocking {
        launch(Kreds) {
            val client = KredsClientGroup.newClient(Endpoint.from("127.0.0.1:6379"))
            try{
                val transaction = client.multi()
                transaction.set("abhi","150")
                val incrResp = transaction.incr("abhi")
                transaction.del("abhi")
                transaction.exec()
                println("incr resp = ${incrResp.get()}")
            }
            finally {
                KredsClientGroup.shutdown()
            }
        }
    }

    @Test
    fun pubsubTest(){
        val handler = object : AbstractKredsSubscriber(){
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
        val publisher = KredsClientGroup.newClient(Endpoint.from("127.0.0.1:6379"))
        runBlocking {
            launch(Kreds) {
                subscriber.subscribe("hello")
                launch(Kreds) {
                    delay(10000)
                    subscriber.unsubscribe("hello")
                }
                repeat(10) {
                    publisher.publish("hello","from IDE")
                }
            }
        }
    }

    @Test
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
    }
}