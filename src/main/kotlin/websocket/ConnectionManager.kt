package com.uit.websocket

import com.uit.enums.EventTypes
import com.uit.enums.RealtimeDomain
import com.uit.model.WebSocketMessage
import com.uit.utils.logger
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

/** Manages WebSocket connections and sending notifications to connected clients. */
class ConnectionManager {
    /** Logger instance for logging connection events. */
    private val logger = logger()

    /** Thread-safe map of client IDs to their WebSocket sessions. */
    private val connections = ConcurrentHashMap<String, DefaultWebSocketSession>()

    /**
     * Adds a new WebSocket connection for a client.
     * @param clientId The unique identifier for the client.
     * @param session The WebSocket session associated with the client.
     */
    fun addConnection(
        clientId: String,
        session: DefaultWebSocketSession,
    ) {
        connections[clientId] = session
        logger.info("Client connected: $clientId. Total connections: ${connections.size}")
    }

    /**
     * Removes a WebSocket connection for a client.
     * @param clientId The unique identifier for the client.
     */
    fun removeConnection(clientId: String) {
        connections.remove(clientId)
        logger.info("Client disconnected: $clientId. Total connections: ${connections.size}")
    }

    /**
     * Sends a typed message to a specific user via WebSocket.
     * @param clientId The unique identifier for the client.
     * @param domain The domain of the event being sent.
     * @param type The type of event being sent.
     * @param data The data payload of the message.
     */
    suspend inline fun <reified T> sendToUser(
        clientId: String,
        domain: RealtimeDomain,
        type: EventTypes,
        data: T,
    ) {
        try {
            val message = WebSocketMessage(domain, type, data)
            val json = Json.encodeToString(message)
            sendRawMessage(clientId, json)
        } catch (e: Exception) {
            logSerializationError(clientId, e)
        }
    }

    /**
     * Sends a typed message to multiple users via WebSocket.
     * @param clientIds The list of unique identifiers for the clients.
     * @param domain The domain of the event being sent.
     * @param type The type of event being sent.
     * @param data The data payload of the message.
     */
    suspend inline fun <reified T> sendToUsers(
        clientIds: List<String>,
        domain: RealtimeDomain,
        type: EventTypes,
        data: T,
    ) {
        try {
            val message = WebSocketMessage(domain, type, data)
            val json = Json.encodeToString(message)
            clientIds.forEach { clientId ->
                if (isConnected(clientId)) {
                    sendRawMessage(clientId, json)
                }
            }
        } catch (e: Exception) {
            logMultiUserSerializationError(e)
        }
    }

    /** Logs serialization errors for multi-user sends.
     * @param e The exception that occurred during serialization.
     */
    @PublishedApi
    internal fun logMultiUserSerializationError(e: Exception) {
        logger.error("Error serializing message for multiple users", e)
    }

    /** Internal check for connection status (for inline function access).
     * @param clientId The unique identifier for the client.
     * @return True if the client is connected, false otherwise.
     */
    @PublishedApi
    internal fun isConnected(clientId: String): Boolean = connections.containsKey(clientId)

    /**
     * Sends a raw JSON message to a specific user via WebSocket.
     * @param clientId The unique identifier for the client.
     * @param jsonContent The JSON string to send.
     */
    @PublishedApi
    internal suspend fun sendRawMessage(
        clientId: String,
        jsonContent: String,
    ) {
        val session = connections[clientId]
        if (session != null) {
            try {
                session.send(Frame.Text(jsonContent))
                logger.debug("Message sent to $clientId")
            } catch (_: ClosedSendChannelException) {
                logger.warn("Failed to send to $clientId: Connection closed")
                removeConnection(clientId)
            } catch (e: Exception) {
                logger.error("Error sending message to $clientId", e)
            }
        } else {
            logger.debug("Client $clientId not connected, skipping message")
        }
    }

    /** Logs serialization errors.
     * @param clientId The unique identifier for the client.
     * @param e The exception that occurred during serialization.
     */
    @PublishedApi
    internal fun logSerializationError(
        clientId: String,
        e: Exception,
    ) {
        logger.error("Error serializing message for $clientId", e)
    }

    /**
     * Checks if any of the given clients are currently connected.
     * @param clientIds The list of unique identifiers for the clients.
     * @return True if at least one client is connected, false otherwise.
     */
    fun isAnyConnected(clientIds: List<String>): Boolean = clientIds.any { isConnected(it) }

    /** Gets the number of active WebSocket connections.
     * @return The count of active connections.
     */
    fun getActiveConnections(): Int = connections.size
}
