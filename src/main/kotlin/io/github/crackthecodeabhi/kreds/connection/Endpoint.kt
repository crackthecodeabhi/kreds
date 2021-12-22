package io.github.crackthecodeabhi.kreds.connection

import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress

public data class Endpoint(val host: String, val port: Int){
    public companion object{
        /**
         * Creates Endpoint from string.
         * @param string String to parse. Must be in <b>"host:port"</b> format.
         * @return parsed Endpoint
         * @throws IllegalArgumentException
         */
        public fun from(string: String): Endpoint{
            val lastColon = string.lastIndexOf(":")
            if(lastColon == -1) throw IllegalArgumentException("Endpoint string should be of format <host>:<port>. Given: $string")
            val host = string.substring(0, lastColon)
            val port = string.substring(lastColon + 1).toInt()
            return Endpoint(host, port)
        }
    }
}

internal fun Endpoint.toSocketAddress(): SocketAddress = InetSocketAddress(InetAddress.getByName(this.host),this.port)

