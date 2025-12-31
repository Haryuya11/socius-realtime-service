package com.uit.config

import com.uit.enums.RoutingType

/**
 * Data class representing RabbitMQ configuration parameters.
 *
 * @property host The RabbitMQ server host.
 * @property port The RabbitMQ server port.
 * @property username The username for RabbitMQ authentication.
 * @property password The password for RabbitMQ authentication.
 * @property vhost The virtual host to connect to in RabbitMQ.
 * @property ssl Indicates whether to use SSL for the connection.
 */
data class RabbitMQConfig(
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val vhost: String,
    val ssl: Boolean,
    val exchangeName: String,
    val routingHandlers: Map<String, RoutingType>,
) {
    companion object {
        /**
         * Creates a [RabbitMQConfig] instance by reading values from the [ConfigManager].
         *
         * @return A [RabbitMQConfig] populated with RabbitMQ connection parameters.
         */
        fun fromConfigManager(): RabbitMQConfig =
            RabbitMQConfig(
                host = ConfigManager.getString("rabbitmq.host"),
                port = ConfigManager.getInt("rabbitmq.port"),
                username = ConfigManager.getString("rabbitmq.username"),
                password = ConfigManager.getString("rabbitmq.password"),
                vhost = ConfigManager.getString("rabbitmq.vhost"),
                ssl = ConfigManager.getBoolean("rabbitmq.ssl"),
                exchangeName = ConfigManager.getString("rabbitmq.exchange.name"),
                routingHandlers =
                    mapOf(ConfigManager.getString("notification.routing.key") to RoutingType.NOTIFICATION),
            )
    }
}
