package com.uit.model

import com.uit.enums.EventTypes
import com.uit.enums.RealtimeDomain
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Generic wrapper for realtime events received from RabbitMQ.
 * This wrapper is used for both messages and notifications from Spring Core.
 */
@Serializable
data class RealtimeEvent(
    /** Unique event identifier */
    val eventId: String? = null,
    /** The domain this event belongs to (MESSAGE, NOTIFICATION, SYSTEM) */
    val domain: RealtimeDomain? = null,
    /** The type of event (NEW_MESSAGE, MESSAGE_UPDATED, etc.) */
    val eventType: String? = null,
    /**
     * Target user IDs - determines who should receive this event.
     * MESSAGE -> participants of conversation
     * NOTIFICATION -> size = 1 (receiver)
     * SYSTEM -> multiple users or entire system
     */
    val targetUserIds: List<String>? = null,
    /** The event payload - can be MessageEventPayload, NotificationEventPayload, etc. */
    val payload: JsonElement? = null,
    /** Timestamp when the event was created */
    val timestamp: String? = null,
) {
    /**
     * Gets the EventTypes enum from the eventType string.
     * @return The matching EventTypes or null if not found.
     */
    fun getEventType(): EventTypes? = eventType?.let { EventTypes.fromCode(it) }
}
