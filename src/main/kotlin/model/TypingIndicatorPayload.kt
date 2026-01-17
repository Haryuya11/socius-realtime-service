package com.uit.model

import kotlinx.serialization.Serializable

/**
 * Payload for typing indicator realtime events.
 */
@Serializable
data class TypingIndicatorPayload(
    /** The conversation ID */
    val conversationId: String? = null,
    /** The employee ID of the user who is typing */
    val employeeId: String? = null,
    /** Whether the user is currently typing */
    val isTyping: Boolean? = null,
)
