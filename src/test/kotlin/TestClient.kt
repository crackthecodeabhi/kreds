import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.kreds.connection.Endpoint
import org.kreds.connection.KredsClientGroup

class TestClient {

    @Test
    fun testClient()= runBlocking {
        val client = KredsClientGroup.newClient(Endpoint.from("127.0.0.1:6379"))
        try{
            println("Client id = ${client.echo("Hello redis")}")
            val pipeline = client.pipelined()
            val setResp = pipeline.set("abhi","590")
            val getResp = pipeline.get("abhi")
            val incrResp = pipeline.incr("abhi")
            pipeline.execute()
            println("setResp = ${setResp.get()}")
            println("getResp = ${getResp.get()}")
            println("incrResp = ${incrResp.get()}")
        }catch (ex: KredsException){
            println("caught exception $ex")
        }
        finally {
            KredsClientGroup.shutdown()
        }
    }

    @Test
    fun transactionTest() = runBlocking {
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