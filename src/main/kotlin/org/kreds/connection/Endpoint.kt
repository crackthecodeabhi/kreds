package org.kreds.connection

import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress
import kotlin.jvm.Throws

data class Endpoint(val host: String, val port: Int){
    companion object{
        /**
         * Creates Endpoint from string.
         * @param string String to parse. Must be in <b>"host:port"</b> format.
         * @return parsed Endpoint
         * @throws IllegalArgumentException
         */
        @Throws(IllegalArgumentException::class)
        fun from(string: String): Endpoint{
            val lastColon = string.lastIndexOf(":")
            if(lastColon == -1) throw IllegalArgumentException("Endpoint string should be of format <host>:<port>. Given: $string")
            val host = string.substring(0, lastColon)
            val port = string.substring(lastColon + 1).toInt()
            return Endpoint(host, port)
        }
    }
}

fun Endpoint.toSocketAddress(): SocketAddress = InetSocketAddress(InetAddress.getByName(this.host),this.port)

