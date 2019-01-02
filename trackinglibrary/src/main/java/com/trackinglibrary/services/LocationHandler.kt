package com.trackinglibrary.services

import android.location.Location
import android.util.Log
import com.trackinglibrary.utils.LocationUtils
import java.util.concurrent.TimeUnit

internal class LocationHandler(
    var saveFreqTime: Long/*in millis*/,
    var lastSavedLocationTime: Long = 0L,
    val timeAndDistanceListener: (Pair<Long, Double>) -> Unit,
    val locationListener: (location: Location) -> Unit
) {

    init {
        if (saveFreqTime > TimeUnit.MINUTES.toMillis(60)) {
            throw IllegalArgumentException("saveFreqTime should not be > 60 minutes")
        }

        if (saveFreqTime < TimeUnit.MINUTES.toMillis(1)) {
            throw IllegalArgumentException("saveFreqTime should not be < 1 minute")
        }
        if (lastSavedLocationTime < 0L) {
            throw IllegalArgumentException("lastSavedLocationTime($lastSavedLocationTime) should be positive")
        }
    }

    private val tag = LocationHandler::class.java.simpleName
    // for calc average speed
    internal var lastLocation: Location? = null

    fun newLocation(location: Location) {

        // validate location
        if (location.hasAccuracy() && location.accuracy > 20f) {
            Log.d(tag, "bad location accuracy(${location.accuracy})")
            return
        }

        // first location or frequency case
        if (lastSavedLocationTime == 0L || (location.time - lastSavedLocationTime) >= saveFreqTime) {
            lastSavedLocationTime = location.time
            // notify new location
            notifyNewLocation(location)
        }

        // update average speed
        saveTimeAndDistance(lastLocation, location)

        lastLocation = location
    }

    internal fun notifyNewLocation(location: Location?) {
        if (location != null) {
            locationListener(location)
        }
    }

    internal fun saveTimeAndDistance(location: Location?, newLocation: Location?) {
        if (location != null && newLocation != null) {
            val newDistance = LocationUtils.calcDistance(location, newLocation)
            val newTime = newLocation.time - location.time

            if (newTime > 0) {
                // save time & distance
                // notify changed
                notifyTimeDistanceChanged(newTime, newDistance)
            } else {
                Log.e("Wrong data", "(totalDistance=$newDistance, totalTime=$newTime)")
            }
        }
    }

    internal fun notifyTimeDistanceChanged(newTime: Long, newDistance: Double) {
        timeAndDistanceListener(Pair(newTime, newDistance))
    }
}