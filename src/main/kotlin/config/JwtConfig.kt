package com.uit.config

/**
 * Data class representing JWT configuration parameters.
 *
 * @property issuer The issuer URL for the JWT tokens.
 * @property audience The audience (client ID) for the JWT tokens.
 * @property jwksUrl The URL to fetch the JSON Web Key Set (JWKS).
 */
data class JwtConfig(
    val issuer: String,
    val audience: String,
    val jwksUrl: String,
) {
    companion object {
        /**
         * Creates a [JwtConfig] instance by reading values from the [ConfigManager].
         *
         * @return A [JwtConfig] populated with issuer, audience, and JWKS URL.
         */
        fun fromConfigManager(): JwtConfig {
            val tenantId = ConfigManager.getString("azure.tenant.id")
            val clientId = ConfigManager.getString("azure.client.id")

            return JwtConfig(
                issuer = "https://$tenantId.ciamlogin.com/$tenantId/v2.0",
                audience = clientId,
                jwksUrl = "https://$tenantId.ciamlogin.com/$tenantId/discovery/v2.0/keys",
            )
        }
    }
}
