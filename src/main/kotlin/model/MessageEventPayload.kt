package com.uit.model

import kotlinx.serialization.Serializable

/**
 * Payload for message-related realtime events.
 */
@Serializable
data class MessageEventPayload(
    /** The conversation ID */
    val conversationId: String? = null,
    /** The message ID (for MESSAGE_DELETED or references) */
    val messageId: String? = null,
    /** The full message data (for NEW_MESSAGE, MESSAGE_UPDATED) */
    val message: MessageDto? = null,
)
