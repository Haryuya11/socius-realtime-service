package com.uit.plugins

import com.uit.websocket.ConnectionManager
import com.uit.websocket.appWebSocket
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import kotlin.time.Duration.Companion.seconds

/**
 * Configures WebSocket support for the Ktor application.
 *
 * This function installs the WebSockets feature with specified parameters
 * and sets up routing for notification WebSocket connections using the provided
 * [ConnectionManager].
 *
 * @param connectionManager manages active WebSocket connections for notifications
 */
fun Application.configureSockets(connectionManager: ConnectionManager) {
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        appWebSocket(connectionManager)
    }
}
