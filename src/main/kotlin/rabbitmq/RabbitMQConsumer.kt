package com.uit.rabbitmq

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import com.uit.config.RabbitMQConfig
import com.uit.enums.EventTypes
import com.uit.enums.RealtimeDomain
import com.uit.enums.RoutingType
import com.uit.model.MessageEventPayload
import com.uit.model.NotificationEventPayload
import com.uit.model.ReactionEventPayload
import com.uit.model.RealtimeEvent
import com.uit.model.SystemEventPayload
import com.uit.model.TypingIndicatorPayload
import com.uit.utils.logger
import com.uit.websocket.ConnectionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

/**
 * RabbitMQ consumer that listens for realtime events and forwards them to users via WebSocket.
 * Supports both MESSAGE and NOTIFICATION domains from Spring Core.
 * @param connectionManager The WebSocket connection manager to send events.
 * @param config The RabbitMQ configuration.
 */
class RabbitMQConsumer(
    private val connectionManager: ConnectionManager,
    private val config: RabbitMQConfig,
) {
    private val logger = logger()
    private val json = Json { ignoreUnknownKeys = true }
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var connection: Connection? = null
    private var channel: Channel? = null

    /**
     * Starts the RabbitMQ consumer.
     */
    fun start() {
        scope.launch {
            try {
                val factory = createConnectionFactory()
                connection = factory.newConnection()
                channel = connection?.createChannel()
                val exchangeName = config.exchangeName
                channel?.exchangeDeclare(exchangeName, "topic", true)

                /** Create a temporary queue for consuming messages */
                val queueName = channel?.queueDeclare()?.queue

                // Bind queue to exchange with routing key
                config.routingHandlers.keys.forEach { routingKey ->
                    channel?.queueBind(queueName, exchangeName, routingKey)
                }

                logger.info("RabbitMQ consumer started. Queue: $queueName")

                val consumer =
                    object : DefaultConsumer(channel) {
                        override fun handleDelivery(
                            consumerTag: String,
                            envelope: Envelope,
                            properties: AMQP.BasicProperties,
                            body: ByteArray,
                        ) {
                            scope.launch {
                                try {
                                    val routingType = config.routingHandlers[envelope.routingKey]
                                    val messageBody = body.decodeToString()
                                    when (routingType) {
                                        // All routing types use the same RealtimeEvent structure from Spring Core.
                                        // NOTIFICATION: for notification-related events
                                        // MESSAGE: for message-related events (NEW_MESSAGE, MESSAGE_UPDATED, etc.)
                                        // CHAT: legacy routing type, kept for backward compatibility
                                        RoutingType.NOTIFICATION,
                                        RoutingType.MESSAGE,
                                        RoutingType.CHAT,
                                        -> {
                                            handleRealtimeEvent(messageBody)
                                        }

                                        null -> {
                                            logger.warn(
                                                "Unknown routing type for key: ${envelope.routingKey}",
                                            )
                                        }
                                    }
                                } catch (ex: Exception) {
                                    logger.error("Error processing message", ex)
                                } finally {
                                    channel?.basicAck(envelope.deliveryTag, false)
                                }
                            }
                        }
                    }

                channel?.basicConsume(queueName, false, consumer)
            } catch (e: Exception) {
                logger.error("Failed to start RabbitMQ consumer", e)
            }
        }
    }

    /**
     * Handles incoming realtime events from Spring Core.
     * @param messageBody The message body as a JSON string.
     */
    private suspend fun handleRealtimeEvent(messageBody: String) {
        try {
            val event = json.decodeFromString<RealtimeEvent>(messageBody)
            val targetUserIds = event.targetUserIds ?: return

            if (!connectionManager.isAnyConnected(targetUserIds)) {
                logger.debug("No target users connected, skipping event: ${event.eventType}")
                return
            }

            when (event.domain) {
                RealtimeDomain.MESSAGE -> handleMessageDomainEvent(event, targetUserIds)
                RealtimeDomain.NOTIFICATION -> handleNotificationDomainEvent(event, targetUserIds)
                RealtimeDomain.SYSTEM -> handleSystemDomainEvent(event, targetUserIds)
                null -> logger.warn("Received event with null domain: ${event.eventId}")
            }
        } catch (e: Exception) {
            logger.error("Error parsing realtime event: ${e.message}", e)
        }
    }

    /**
     * Handles MESSAGE domain events (NEW_MESSAGE, MESSAGE_UPDATED, MESSAGE_DELETED, TYPING_INDICATOR).
     */
    private suspend fun handleMessageDomainEvent(
        event: RealtimeEvent,
        targetUserIds: List<String>,
    ) {
        val eventType =
            event.eventTypeEnum ?: run {
                logger.warn("Message unknown event type: ${event.eventType}")
                return
            }

        val domain =
            event.domain ?: run {
                logger.warn("Message event missing domain: ${event.domain}")
                RealtimeDomain.NOTIFICATION
            }

        when (eventType) {
            EventTypes.NEW_MESSAGE,
            EventTypes.MESSAGE_UPDATED,
            EventTypes.MESSAGE_DELETED,
            -> {
                event.payload?.let { payload ->
                    try {
                        val messagePayload = json.decodeFromJsonElement<MessageEventPayload>(payload)
                        connectionManager.sendToUsers(targetUserIds, domain, eventType, messagePayload)
                        logger.info("Sent ${event.eventType} to ${targetUserIds.size} users")
                    } catch (e: Exception) {
                        logger.error("Error parsing MessageEventPayload: ${e.message}", e)
                    }
                }
            }

            EventTypes.TYPING_INDICATOR -> {
                event.payload?.let { payload ->
                    try {
                        val typingPayload = json.decodeFromJsonElement<TypingIndicatorPayload>(payload)
                        connectionManager.sendToUsers(targetUserIds, domain, eventType, typingPayload)
                        logger.debug("Sent typing indicator to ${targetUserIds.size} users")
                    } catch (e: Exception) {
                        logger.error("Error parsing TypingIndicatorPayload: ${e.message}", e)
                    }
                }
            }

            EventTypes.REACTION_ADDED,
            EventTypes.REACTION_REMOVED,
            -> {
                event.payload?.let { payload ->
                    try {
                        val reactionPayload = json.decodeFromJsonElement<ReactionEventPayload>(payload)
                        connectionManager.sendToUsers(targetUserIds, domain, eventType, reactionPayload)
                        logger.info("Sent ${event.eventType} to ${targetUserIds.size} users")
                    } catch (e: Exception) {
                        logger.error("Error parsing ReactionEventPayload: ${e.message}", e)
                    }
                }
            }

            else -> {
                logger.warn("Unhandled MESSAGE domain event type: ${event.eventType}")
            }
        }
    }

    /**
     * Handles NOTIFICATION domain events (NEW_NOTIFICATION).
     */
    private suspend fun handleNotificationDomainEvent(
        event: RealtimeEvent,
        targetUserIds: List<String>,
    ) {
        val eventType =
            event.eventTypeEnum ?: run {
                logger.warn("Unknown notification event type: ${event.eventType}, defaulting to NEW_NOTIFICATION")
                return
            }

        val domain =
            event.domain ?: run {
                logger.warn("Notification event missing domain: ${event.domain}")
                RealtimeDomain.NOTIFICATION
                return
            }

        event.payload?.let { payload ->
            try {
                val notificationPayload = json.decodeFromJsonElement<NotificationEventPayload>(payload)
                connectionManager.sendToUsers(targetUserIds, domain, eventType, notificationPayload)
                logger.info("Sent notification to ${targetUserIds.size} users")
            } catch (e: Exception) {
                logger.error("Error parsing NotificationEventPayload: ${e.message}", e)
            }
        }
    }

    /**
     * Handles SYSTEM domain events.
     * System events can have various payload structures, so they are wrapped in SystemEventPayload.
     */
    private suspend fun handleSystemDomainEvent(
        event: RealtimeEvent,
        targetUserIds: List<String>,
    ) {
        val eventType =
            event.eventTypeEnum ?: run {
                logger.warn("Unknown system event type: ${event.eventType}")
                return
            }

        val domain =
            event.domain ?: run {
                logger.warn("System event missing domain: ${event.domain}")
                RealtimeDomain.SYSTEM
                return
            }

        event.payload?.let { payload ->
            // Wrap raw payload in SystemEventPayload for consistent handling on client side
            val systemPayload = SystemEventPayload(data = payload)
            connectionManager.sendToUsers(targetUserIds, domain, eventType, systemPayload)
            logger.info("Sent system event to ${targetUserIds.size} users")
        }
    }

    /**
     * Creates and configures the RabbitMQ connection factory.
     * @return The configured ConnectionFactory.
     */
    private fun createConnectionFactory(): ConnectionFactory =
        ConnectionFactory().apply {
            host = config.host
            port = config.port
            username = config.username
            password = config.password
            virtualHost = config.vhost

            if (config.ssl) {
                useSslProtocol()
            }
        }

    /**
     * Stops the RabbitMQ consumer and releases resources.
     */
    fun stop() {
        scope.cancel()
        channel?.close()
        connection?.close()
        logger.info("RabbitMQ consumer stopped")
    }
}
