package com.uit.enums

/**
 * Enum class representing different types of realtime events.
 * These correspond to the RealtimeEventType in Spring Core.
 */
enum class EventTypes(
    val code: String,
    val description: String,
) {
    /** New message event */
    NEW_MESSAGE("NEW_MESSAGE", "New message event"),

    /** Message updated event */
    MESSAGE_UPDATED("MESSAGE_UPDATED", "Message updated event"),

    /** Message deleted event */
    MESSAGE_DELETED("MESSAGE_DELETED", "Message deleted event"),

    /** Typing indicator event */
    TYPING_INDICATOR("TYPING_INDICATOR", "Typing indicator event"),

    /** New notification event */
    NEW_NOTIFICATION("NEW_NOTIFICATION", "New notification event"),

    /** Legacy notification type for backwards compatibility */
    NOTIFICATION("NOTIFICATION", "Legacy notification event"),

    /** Legacy chat type for backwards compatibility */
    CHAT("CHAT", "Legacy chat event"),
    ;

    companion object {
        /**
         * Finds an EventType by its code string.
         * @param code The code string to look up.
         * @return The matching EventTypes or null if not found.
         */
        fun fromCode(code: String): EventTypes? = entries.find { it.code == code }
    }
}
