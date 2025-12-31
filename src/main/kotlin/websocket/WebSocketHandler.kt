package com.uit.websocket

import com.uit.utils.logger
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.consumeEach

fun Route.appWebSocket(connectionManager: ConnectionManager) {
    /** Logger instance for logging WebSocket events. */
    val logger = logger()

    authenticate("azure-jwt") {
        webSocket("/ws/hub") {
            val principal = call.principal<JWTPrincipal>()
            // Extract client ID from JWT claim "oid"
            val clientId = principal?.payload?.getClaim("oid")?.asString()

            if (clientId == null) {
                logger.warn("WebSocket connection rejected: Missing client ID")
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid token"))
                return@webSocket
            }

            // Register the new connection
            connectionManager.addConnection(clientId, this)
            logger.info("WebSocket established for client: $clientId")

            try {
                incoming.consumeEach { frame ->
                    when (frame) {
                        is Frame.Text -> {
                            val text = frame.readText()
                            if (text == "ping") {
                                send(Frame.Text("pong"))
                            }
                        }

                        else -> {
                            // Ignore other frame types
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error("WebSocket error for $clientId", e)
            } finally {
                // Clean up connection on close
                connectionManager.removeConnection(clientId)
                logger.info("WebSocket closed for client: $clientId")
            }
        }
    }
}
