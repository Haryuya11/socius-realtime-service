package com.uit.model

import com.uit.enums.EventTypes
import com.uit.enums.RealtimeDomain
import kotlinx.serialization.Serializable

/**
 * A generic WebSocket message wrapper.
 */
@Serializable
data class WebSocketMessage<T>(
    val domain: RealtimeDomain,
    val type: EventTypes,
    val data: T,
)
