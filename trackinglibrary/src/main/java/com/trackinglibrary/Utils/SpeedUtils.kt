package com.trackinglibrary.Utils

internal object SpeedUtils {
    fun calcAverageSpeed(time: Long, distance: Double): Double = distance / time.toDouble()
}