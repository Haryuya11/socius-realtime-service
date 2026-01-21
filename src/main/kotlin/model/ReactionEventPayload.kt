package com.uit.model

import kotlinx.serialization.Serializable

/**
 * Payload for reaction-related realtime events.
 * Matches the ReactionEventPayload structure from Spring Core.
 */
@Serializable
data class ReactionEventPayload(
    /** The conversation ID */
    val conversationId: String? = null,
    /** The message ID that the reaction is associated with */
    val messageId: String? = null,
    /** The reaction data (for REACTION_ADDED) */
    val reaction: MessageReactionDto? = null,
    /** The employee ID who added/removed the reaction */
    val employeeId: String? = null,
    /** The reaction type (emoji code) that was removed (for REACTION_REMOVED) */
    val reactionType: String? = null,
)
