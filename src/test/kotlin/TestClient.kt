import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.kreds.SetOption
import org.kreds.connection.Endpoint
import org.kreds.connection.KredsClient
import org.kreds.to

class TestClient {

    @Test
    fun testClient()= runBlocking {
        val client = KredsClient.newClient(Endpoint.from("127.0.0.1:6379"))
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
    }
}