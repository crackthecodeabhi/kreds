import kotlinx.coroutines.runBlocking
import org.kreds.connection.Connection
import org.kreds.connection.DefaultKredSocketFactory
import org.kreds.connection.Endpoint

fun main(){
    val connection = Connection(DefaultKredSocketFactory(Endpoint.from("127.0.0.1:6379")))
    runBlocking {
        connection.connect()
        connection.sendCommand("SET x 103\r\n")
    }
}