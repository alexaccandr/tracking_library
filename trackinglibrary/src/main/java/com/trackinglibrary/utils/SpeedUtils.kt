package com.trackinglibrary.utils

import java.util.concurrent.TimeUnit

internal object SpeedUtils {
//    fun calcAverageSpeed(time: Long, distance: Double): Double = distance / time.toDouble()

    fun calcAverageSpeed(timeMillis: Long, distance: Double): Double {
        return if (timeMillis == 0L) 0.0 else distance / (TimeUnit.MILLISECONDS.toSeconds(timeMillis).toDouble())
    }
}