package com.uit.model

import kotlinx.serialization.Serializable

/**
 * Data class representing a notification message.
 */
@Serializable
data class NotificationMessage(
    val id: Long,
    val receiverId: String,
    val deliveryType: Short,
    val payload: NotificationPayload,
    val isRead: Short,
    val createdAt: String,
)
