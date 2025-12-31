package com.uit.plugins

import com.auth0.jwk.JwkProviderBuilder // 1. Cần import cái này
import com.uit.config.JwtConfig
import com.uit.utils.logger
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond
import java.net.URI
import java.util.concurrent.TimeUnit // 2. Import time unit

fun Application.configureSecurity(jwtConfig: JwtConfig) {
    val logger = logger()

    logger.info("Configuring JWT authentication")
    logger.info("JWKS URL: ${jwtConfig.jwksUrl}")

    // 3. TẠO JWK PROVIDER (Quan trọng)
    // Thay vì truyền URL trực tiếp, ta tạo Provider để nó tự động cache key
    val jwkProvider =
        JwkProviderBuilder(URI(jwtConfig.jwksUrl).toURL())
            .cached(10, 24, TimeUnit.HOURS) // Cache key 24h (Azure ít khi đổi key)
            .rateLimited(10, 1, TimeUnit.MINUTES) // Chống spam request lên Azure
            .build()

    authentication {
        jwt("azure-jwt") {
            realm = "notification-service"

            // 4. SỬ DỤNG PROVIDER VỪA TẠO
            verifier(jwkProvider, jwtConfig.issuer) {
                acceptLeeway(3)
                withAudience(jwtConfig.audience) // Ktor sẽ tự check audience ở bước verify này luôn
            }

            validate { credential ->
                // Lúc này Token đã được Verify chữ ký (Signature) OK rồi
                // Ta chỉ check thêm các claim nghiệp vụ
                val oid = credential.payload.getClaim("oid")?.asString()
                val sub = credential.payload.subject

                if (oid.isNullOrEmpty()) {
                    logger.warn("Missing oid claim in token")
                    return@validate null
                }

                // Lưu ý: Đôi khi Azure dùng 'oid' làm định danh chính thay vì 'sub'
                // Nhưng check cả 2 cho chắc cũng được
                if (sub.isNullOrEmpty()) {
                    logger.warn("Missing subject claim in token")
                    return@validate null
                }

                logger.info("✅ Token validated for oid: $oid")
                JWTPrincipal(credential.payload)
            }

            challenge { _, _ ->
                logger.warn("⚠️ JWT authentication challenge triggered")
                call.respond(HttpStatusCode.Unauthorized, "Invalid or expired token")
            }
        }
    }
}
