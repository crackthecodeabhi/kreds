# Welcome to Kreds user guide


## How to use

```kotlin
launch {
    KredsClientGroup.newClient(Endpoint.from("127.0.0.1:6379")).use { client ->
        client.set("foo","100") 
        println("incremented value of foo ${client.incr("foo")}") // prints 101
        client.expire("foo",3u) // set expiration to 3 seconds
        delay(3000)
        assert(client.get("foo") == null)
    }
    KredsClientGroup.shutdown() // shutdown the client group.
}
```
