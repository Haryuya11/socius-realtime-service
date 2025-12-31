package com.uit.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.path
import org.slf4j.event.Level

/**
 * Configures monitoring for the Ktor application.
 *
 * This function installs the CallLogging plugin to log incoming HTTP requests.
 * It logs requests at the INFO level for paths starting with "/".
 */
fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
}
