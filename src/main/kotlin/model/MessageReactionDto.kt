package com.uit.model

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for message reactions.
 * Matches the MessageReactionDto structure from Spring Core.
 */
@Serializable
data class MessageReactionDto(
    /** The message ID that the reaction is associated with */
    val messageId: String? = null,
    /** The employee ID who added the reaction */
    val employeeId: String? = null,
    /** The reaction type (emoji code) */
    val reaction: String? = null,
    /** Timestamp when the reaction was created */
    val createdAt: String? = null,
)
