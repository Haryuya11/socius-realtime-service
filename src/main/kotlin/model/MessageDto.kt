package com.uit.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Data Transfer Object for Message entity.
 * Matches the MessageDto structure from Spring Core.
 */
@Serializable
data class MessageDto(
    val messageId: String? = null,
    val conversationId: String? = null,
    val senderId: String? = null,
    val content: String? = null,
    val messageType: String? = null,
    val parentMessageId: String? = null,
    val metadata: JsonElement? = null,
    val isEdited: Boolean? = null,
    val editedAt: String? = null,
    val createdAt: String? = null,
)
