package com.trackinglibrary.utils

internal object TimeUtils {
    private val SECOND = 1000
    private val MINUTE = 60 * SECOND
    private val HOUR = 60 * MINUTE
    private val DAY = 24 * HOUR

    fun millisToHours(millis: Long): Float = millis.toFloat() / HOUR.toFloat()
}