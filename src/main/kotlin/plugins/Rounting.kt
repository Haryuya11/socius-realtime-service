package com.uit.plugins

import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

/**
 * Configures routing for the Ktor application.
 *
 * This function sets up a simple health check endpoint at "/health"
 * that responds with "OK" to indicate the server is running.
 */
fun Application.configureRouting() {
    routing {
        get("/health") {
            call.respondText("OK")
        }
    }
}
