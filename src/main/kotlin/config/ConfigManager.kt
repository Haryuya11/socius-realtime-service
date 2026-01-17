package com.uit.config

import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.security.keyvault.secrets.SecretClientBuilder
import com.uit.utils.logger

/**
 * ConfigManager is responsible for loading configuration settings from
 * environment variables and Azure Key Vault.
 */
object ConfigManager {
    private val logger = logger()
    private val config = mutableMapOf<String, String>()

    init {
        loadConfiguration()
    }

    /**
     * Loads configuration from environment variables and Azure Key Vault if available.
     */
    private fun loadConfiguration() {
        loadEnvironmentVariables()

        val keyVaultEndpoint = System.getenv("KEY_VAULT_ENDPOINT")
        if (!keyVaultEndpoint.isNullOrEmpty()) {
            loadFromKeyVault(keyVaultEndpoint)
        } else {
            logger.warn("KEY_VAULT_ENDPOINT not set, using environment variables only")
        }
    }

    /**
     * Loads configuration from environment variables.
     * Only use when code locally or when Key Vault is not available.
     */
    private fun loadEnvironmentVariables() {
        val envVars =
            mapOf(
                "rabbitmq.host" to System.getenv("RABBIT_HOST"),
                "rabbitmq.port" to System.getenv("RABBIT_PORT"),
                "rabbitmq.username" to System.getenv("RABBIT_USERNAME"),
                "rabbitmq.password" to System.getenv("RABBIT_PASSWORD"),
                "rabbitmq.vhost" to System.getenv("RABBIT_VHOST"),
                "rabbitmq.ssl" to System.getenv("RABBIT_SSL"),
                "azure.tenant.id" to System.getenv("AZURE_TENANT_ID"),
                "azure.client.id" to System.getenv("AZURE_CLIENT_ID"),
                "rabbitmq.exchange.name" to System.getenv("RABBITMQ_EXCHANGE_NAME"),
                "notification.routing.key" to System.getenv("NOTIFICATION_ROUTING_KEY"),
                "message.routing.key" to System.getenv("MESSAGE_ROUTING_KEY"),
            )

        config.putAll(envVars)
        logger.info("Loaded configuration from environment variables")
    }

    /**
     * Loads configuration from Azure Key Vault.
     * @param endpoint The Key Vault endpoint URL.
     */
    private fun loadFromKeyVault(endpoint: String) {
        try {
            val credential = DefaultAzureCredentialBuilder().build()
            val secretClient =
                SecretClientBuilder()
                    .vaultUrl(endpoint)
                    .credential(credential)
                    .buildClient()

            val secretKeys =
                listOf(
                    "RABBITMQ-HOST",
                    "RABBITMQ-PORT",
                    "RABBITMQ-USERNAME",
                    "RABBITMQ-PASSWORD",
                    "RABBITMQ-VHOST",
                    "RABBITMQ-SSL",
                    "AZURE-TENANT-ID",
                    "AZURE-CLIENT-ID",
                    "RABBITMQ-EXCHANGE-NAME",
                    "NOTIFICATION-ROUTING-KEY",
                    "MESSAGE-ROUTING-KEY",
                )

            secretKeys.forEach { secretName ->
                try {
                    val secretValue = secretClient.getSecret(secretName).value
                    val configKey = secretName.lowercase().replace("-", ".")
                    config[configKey] = secretValue
                    logger.debug("Loaded secret: $secretName")
                } catch (e: Exception) {
                    logger.warn("Failed to load secret $secretName: ${e.message}")
                }
            }

            logger.info("Successfully loaded secrets from Azure Key Vault")
        } catch (e: Exception) {
            logger.error("Failed to connect to Azure Key Vault", e)
        }
    }

    /**
     * Retrieves a configuration value as a String.
     * @param key The configuration key.
     * @return The configuration value.
     * @throws IllegalStateException if the key is not found.
     */
    fun getString(key: String): String = config[key] ?: throw IllegalStateException("Configuration key not found: $key")

    /**
     * Retrieves a configuration value as a String, or null if not found or empty.
     * @param key The configuration key.
     * @return The configuration value, or null if not found or empty.
     */
    fun getStringOrNull(key: String): String? = config[key]?.takeIf { it.isNotBlank() }

    /**
     * Retrieves a configuration value as an Int.
     * @param key The configuration key.
     * @return The configuration value as an Int.
     * @throws IllegalStateException if the key is not found or not a valid integer.
     */
    fun getInt(key: String): Int =
        getString(key).toIntOrNull()
            ?: throw IllegalStateException("Configuration key $key is not a valid integer")

    /**
     * Retrieves a configuration value as a Boolean.
     * @param key The configuration key.
     * @return The configuration value as a Boolean.
     * @throws IllegalStateException if the key is not found.
     */
    fun getBoolean(key: String): Boolean = getString(key).toBoolean()
}
