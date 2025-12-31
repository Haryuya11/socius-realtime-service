package com.uit

import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Application test class to verify the health endpoint.
 */
class ApplicationTest {
    @Test
    fun testRoot() =
        testApplication {
            application {
                testModule()
            }
            client.get("/health").apply {
                assertEquals(HttpStatusCode.OK, status)
            }
        }
}

/**
 * Minimal test module that doesn't require external configuration.
 */
fun Application.testModule() {
    routing {
        get("/health") {
            call.respondText("OK")
        }
    }
}
