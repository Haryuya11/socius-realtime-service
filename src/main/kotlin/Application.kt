package com.uit

import com.uit.config.JwtConfig
import com.uit.config.RabbitMQConfig
import com.uit.plugins.configureRouting
import com.uit.plugins.configureSecurity
import com.uit.plugins.configureSockets
import com.uit.rabbitmq.RabbitMQConsumer
import com.uit.websocket.ConnectionManager
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.slf4j.LoggerFactory

/**
 * Entry point for the Notification Service application.
 *
 * This function initializes and starts the Ktor server with the necessary
 * configurations, including security, WebSocket support, and RabbitMQ
 * message consumption.
 */
fun main() {
    val logger = LoggerFactory.getLogger("Application")
    val port = 8081
    val host = "0.0.0.0"
    logger.info("Starting Notification Service on port $port")

    embeddedServer(Netty, port = port, host = host, module = Application::module)
        .start(wait = true)
}

/**
 * Configures the Ktor application module.
 *
 * This function sets up security, WebSocket connections, routing,
 * and initializes the RabbitMQ consumer to process incoming messages.
 */
fun Application.module() {
    val connectionManager = ConnectionManager()
    val rabbitConfig = RabbitMQConfig.fromConfigManager()
    val jwtConfig = JwtConfig.fromConfigManager()

    configureSecurity(jwtConfig)
    configureSockets(connectionManager)
    configureRouting()

    val rabbitConsumer = RabbitMQConsumer(connectionManager, rabbitConfig)
    rabbitConsumer.start()

    monitor.subscribe(ApplicationStopped) {
        rabbitConsumer.stop()
    }
}
