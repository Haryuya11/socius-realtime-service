package com.uit.plugins

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.interfaces.Payload
import com.uit.config.JwtConfig
import com.uit.utils.logger
import io.ktor.http.HttpStatusCode
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTAuthenticationProvider
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond
import org.slf4j.Logger
import java.net.URI
import java.util.concurrent.TimeUnit

private const val REALM = "notification-service"
private const val CACHE_SIZE = 10L
private const val CACHE_EXPIRES_HOURS = 24L
private const val RATE_LIMIT_BUCKET_SIZE = 10L
private const val RATE_LIMIT_REFILL_MINUTES = 1L
private const val LEEWAY_SECONDS = 3L
private const val TOKEN_PARAM = "token"

/**
 * Configures security for the Ktor application using JWT authentication.
 *
 * This function sets up JWT authentication with two configurations:
 * one for standard HTTP requests and another for WebSocket connections
 * that accept tokens via query parameters.
 *
 * @param jwtConfig The JWT configuration containing issuer, audience, and JWKS URL.
 */
fun Application.configureSecurity(jwtConfig: JwtConfig) {
    val logger = logger()

    logger.info("Configuring JWT authentication")
    logger.info("JWKS URL: ${jwtConfig.jwksUrl}")

    val jwkProvider = buildJwkProvider(jwtConfig.jwksUrl)

    authentication {
        jwt("azure-jwt-ws") {
            configureJwt(jwkProvider, jwtConfig, logger)
            authHeader { call ->
                call.request.queryParameters[TOKEN_PARAM]?.let {
                    HttpAuthHeader.Single("Bearer", it)
                }
            }
        }

        jwt("azure-jwt") {
            configureJwt(jwkProvider, jwtConfig, logger)
        }
    }
}

/**
 * Builds a JWK provider with caching and rate limiting.
 *
 * @param jwksUrl The URL of the JWKS endpoint.
 * @return A configured JwkProvider instance.
 */
private fun buildJwkProvider(jwksUrl: String): JwkProvider =
    JwkProviderBuilder(URI(jwksUrl).toURL())
        .cached(CACHE_SIZE, CACHE_EXPIRES_HOURS, TimeUnit.HOURS)
        .rateLimited(RATE_LIMIT_BUCKET_SIZE, RATE_LIMIT_REFILL_MINUTES, TimeUnit.MINUTES)
        .build()

/**
 * Configures JWT authentication for the given provider and configuration.
 *
 * @param jwkProvider The JWK provider for token verification.
 * @param jwtConfig The JWT configuration containing issuer and audience.
 * @param logger The logger for logging authentication events.
 */
private fun JWTAuthenticationProvider.Config.configureJwt(
    jwkProvider: JwkProvider,
    jwtConfig: JwtConfig,
    logger: Logger,
) {
    realm = REALM

    verifier(jwkProvider, jwtConfig.issuer) {
        acceptLeeway(LEEWAY_SECONDS)
        withAudience(jwtConfig.audience)
    }

    validate { credential ->
        validateToken(credential.payload, logger)
    }

    challenge { _, _ ->
        logger.warn("JWT authentication challenge triggered")
        call.respond(HttpStatusCode.Unauthorized, "Invalid or expired token")
    }
}

/**
 * Validates the JWT token payload.
 *
 * @param payload The JWT token payload.
 * @param logger The logger for logging validation events.
 * @return A JWTPrincipal if validation is successful; null otherwise.
 */
private fun validateToken(
    payload: Payload,
    logger: Logger,
): JWTPrincipal? {
    val oid = payload.getClaim("oid")?.asString()
    val sub = payload.subject

    if (oid.isNullOrEmpty()) {
        logger.warn("Missing oid claim in token")
        return null
    }

    if (sub.isNullOrEmpty()) {
        logger.warn("Missing subject claim in token")
        return null
    }

    logger.info("Token validated for oid: $oid")
    return JWTPrincipal(payload)
}
