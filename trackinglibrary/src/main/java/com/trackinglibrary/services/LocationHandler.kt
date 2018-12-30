package com.trackinglibrary.services

import android.location.Location
import android.util.Log
import com.trackinglibrary.TrackRecorder
import com.trackinglibrary.utils.LocationUtils
import java.util.concurrent.TimeUnit

internal class LocationHandler(
    var saveFreqTime: Long/*in millis*/,
    var lastSavedLocationTime: Long = 0L,
    val distanceListener: (Pair<Long, Double>) -> Unit
) {

    // for calc average speed
    var lastLocation: Location? = null
    // passed time for avSpeed
    var timePassed: Long = 0L
    var distancePassed: Double = 0.0

    fun newLocation(location: Location) {

        // first location or frequency case
        if (lastSavedLocationTime == 0L || (location.time - lastSavedLocationTime) >= saveFreqTime) {
            lastSavedLocationTime = location.time
            saveLocation(location)
        }

        calcTimeAndDistance(lastLocation, location)

        lastLocation = location
    }

    private fun calcTimeAndDistance(location: Location?, newLocation: Location) {
        if (location != null) {
            val newDistance = LocationUtils.calcDistance(location, newLocation)
            val newTime = newLocation.time - location.time
            // in meters
            val totalDistance = distancePassed + newDistance
            // in seconds
            val totalTime = TimeUnit.MILLISECONDS.toSeconds(timePassed + newTime)

            if (totalDistance >= 0 && totalTime >= 0) {
                // save time & distance
                distancePassed = totalDistance
                timePassed = totalTime
                // notify changed
                distanceListener(Pair(totalTime, totalDistance))
            } else {
                Log.e("Wrong data", "(totalDistance=$totalDistance, totalTime=$totalTime)")
            }
        }
    }

    private fun saveLocation(location: Location) {
        TrackRecorder.saveLocation(location)
    }
}