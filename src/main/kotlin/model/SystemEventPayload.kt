package com.uit.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Payload for system-related realtime events.
 * System events can have various payload structures, so this uses a generic JsonElement.
 */
@Serializable
data class SystemEventPayload(
    /** The raw payload data as JSON */
    val data: JsonElement? = null,
)
