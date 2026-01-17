package com.uit.model

import kotlinx.serialization.Serializable

/**
 * Payload for notification-related realtime events.
 * Note: targetUserIds (receivers) are now in the RealtimeEvent wrapper, not in this payload.
 */
@Serializable
data class NotificationEventPayload(
    /** The notification ID */
    val notificationId: String? = null,
    /** The notification title */
    val title: String? = null,
    /** The notification content */
    val content: String? = null,
    /** The redirect URL for the notification */
    val redirectUrl: String? = null,
)
