package com.uit.enums

/**
 * Enum class representing different routing types for RabbitMQ messages.
 */
enum class RoutingType {
    /** Notification-related messages */
    NOTIFICATION,

    /** Chat/Message-related messages */
    CHAT,

    /** Combined message routing (for RealtimeEvent) */
    MESSAGE,
}
