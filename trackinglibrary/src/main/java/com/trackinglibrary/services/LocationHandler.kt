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

    // for calc average speed
    var lastLocation: Location? = null

    fun newLocation(location: Location) {

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