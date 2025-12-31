package com.uit.model

import kotlinx.serialization.Serializable

/**
 * Data class representing the payload of a notification.
 */
@Serializable
data class NotificationPayload(
    val title: String,
    val content: String,
    val linkUrl: String?,
)
