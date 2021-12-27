package io.github.crackthecodeabhi.kreds.commands

import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.github.crackthecodeabhi.kreds.connection.KredsClientGroup

fun getTestClient(endpoint: Endpoint? = null): KredsClient {
    return KredsClientGroup.newClient(endpoint ?: Endpoint.from("127.0.0.1:6379"))
}