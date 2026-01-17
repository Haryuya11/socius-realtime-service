package com.uit.enums

/**
 * Enum class representing different types of realtime events.
 * These correspond to the RealtimeEventType in Spring Core.
 */
enum class EventTypes(
    val code: String,
) {
    /** New message event */
    NEW_MESSAGE("NEW_MESSAGE"),

    /** Message updated event */
    MESSAGE_UPDATED("MESSAGE_UPDATED"),

    /** Message deleted event */
    MESSAGE_DELETED("MESSAGE_DELETED"),

    /** Typing indicator event */
    TYPING_INDICATOR("TYPING_INDICATOR"),

    /** New notification event */
    NEW_NOTIFICATION("NEW_NOTIFICATION"),

    /** Legacy notification type for backwards compatibility */
    NOTIFICATION("NOTIFICATION"),

    /** Legacy chat type for backwards compatibility */
    CHAT("CHAT"),
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
