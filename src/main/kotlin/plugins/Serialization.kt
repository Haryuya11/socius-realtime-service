package com.uit.plugins

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

/**
 * Configures JSON serialization for the Ktor application.
 *
 * This function installs the ContentNegotiation plugin with JSON support
 * using kotlinx.serialization. It also sets up a sample route that responds
 * with a JSON object.
 */
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
    routing {
        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}
