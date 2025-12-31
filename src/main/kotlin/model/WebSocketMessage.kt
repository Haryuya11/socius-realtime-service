package com.uit.model

import com.uit.enums.EventTypes
import kotlinx.serialization.Serializable

/**
 * A generic WebSocket message wrapper.
 */
@Serializable
data class WebSocketMessage<T>(
    val type: EventTypes,
    val data: T,
)
