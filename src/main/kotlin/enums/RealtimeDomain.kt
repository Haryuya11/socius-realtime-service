package com.uit.enums

import kotlinx.serialization.Serializable

/**
 * Enum class representing different domains for realtime events.
 * Matches the RealtimeDomain in Spring Core.
 */
@Serializable
enum class RealtimeDomain(
    val code: String,
    val description: String,
) {
    /** Message-related events */
    MESSAGE("MESSAGE", "Message domain"),

    /** Notification-related events */
    NOTIFICATION("NOTIFICATION", "Notification domain"),

    /** System-related events */
    SYSTEM("SYSTEM", "System domain"),
    ;

    companion object {
        /**
         * Finds a RealtimeDomain by its code string.
         * @param code The code string to look up.
         * @return The matching RealtimeDomain or null if not found.
         */
        fun fromCode(code: String): RealtimeDomain? = entries.find { it.code == code }
    }
}
