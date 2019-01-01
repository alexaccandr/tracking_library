package com.trackinglibrary.services

import android.location.Location
import android.util.Log
import com.trackinglibrary.utils.LocationUtils

internal class LocationHandler(
    var saveFreqTime: Long/*in millis*/,
    var lastSavedLocationTime: Long = 0L,
    val timeAndDistanceListener: (Pair<Long, Double>) -> Unit,
    val locationListener: (location: Location) -> Unit
) {

    private val tag = LocationHandler::class.java.simpleName
    // for calc average speed
    var lastLocation: Location? = null

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
            locationListener(location)
        }

        // update average speed
        saveTimeAndDistance(lastLocation, location)

        lastLocation = location
    }

    private fun saveTimeAndDistance(location: Location?, newLocation: Location) {
        if (location != null) {
            val newDistance = LocationUtils.calcDistance(location, newLocation)
            val newTime = newLocation.time - location.time

            if (newTime > 0) {
                // save time & distance
                // notify changed
                timeAndDistanceListener(Pair(newTime, newDistance))
            } else {
                Log.e("Wrong data", "(totalDistance=$newDistance, totalTime=$newTime)")
            }
        }
    }
}