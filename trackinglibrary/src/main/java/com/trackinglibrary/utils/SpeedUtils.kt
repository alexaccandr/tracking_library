package com.trackinglibrary.utils

import java.util.concurrent.TimeUnit

internal object SpeedUtils {
//    fun calcAverageSpeed(time: Long, distance: Double): Double = distance / time.toDouble()

    fun calcAverageSpeed(timeMillis: Long, distance: Double): Double {
        return if (timeMillis == 0L || distance == 0.0) 0.0 else {
            val timeInSeconds = (TimeUnit.MILLISECONDS.toSeconds(timeMillis).toDouble())
            if (timeInSeconds > 0) distance / timeInSeconds
            else 0.0
        }
//        return if (timeMillis == 0L || distance == 0.0) 0.0 else distance / timeMillis.toDouble()
    }
}