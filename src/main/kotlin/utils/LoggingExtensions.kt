package com.uit.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Extension function to provide a logger for any class.
 */
inline fun <reified T> T.logger(): Logger = LoggerFactory.getLogger(T::class.java)
