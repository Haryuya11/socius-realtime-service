package com.uit.rabbitmq

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import com.uit.config.RabbitMQConfig
import com.uit.enums.EventTypes
import com.uit.enums.RoutingType
import com.uit.model.NotificationMessage
import com.uit.utils.logger
import com.uit.websocket.ConnectionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/**
 * RabbitMQ consumer that listens for notification messages and forwards them to users via WebSocket.
 * @param connectionManager The WebSocket connection manager to send notifications.
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
                                        RoutingType.NOTIFICATION -> {
                                            handleNotification(messageBody)
                                        }

                                        RoutingType.CHAT -> {
                                            handleChatMessage(messageBody)
                                        }

                                        else -> {
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
     * Handles incoming notification messages.
     * @param messageBody The message body as a JSON string.
     */
    private suspend fun handleNotification(messageBody: String) {
        val message = json.decodeFromString<NotificationMessage>(messageBody)
        if (connectionManager.isConnected(message.receiverId)) {
            connectionManager.sendToUser(
                message.receiverId,
                EventTypes.NOTIFICATION,
                message,
            )
        }
    }

    /**
     * Handles incoming chat messages.
     * @param messageBody The message body as a JSON string.
     */
    private fun handleChatMessage(messageBody: String) {
        // We will implement chat message handling later
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
