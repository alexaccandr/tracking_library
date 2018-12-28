package com.trackinglibrary.Utils

import android.location.Location
import android.location.LocationManager

internal object LocationUtils {

    fun calcDistance(location: Location, nextLocation: Location): Double {
        return calcDistance(
            location.latitude, location.longitude, nextLocation.latitude,
            nextLocation.longitude
        )
    }

    fun calcDistance(x: Double, y: Double, x2: Double, y2: Double): Double {
        if (x == 0.0 || y == 0.0 || x2 == 0.0 || y2 == 0.0) {
            return 0.0
        }
        val originLocation = Location(LocationManager.GPS_PROVIDER)
        val destinationLocation = Location(LocationManager.GPS_PROVIDER)
        originLocation.latitude = x
        originLocation.longitude = y
        destinationLocation.latitude = x2
        destinationLocation.longitude = y2
        return originLocation.distanceTo(destinationLocation).toDouble()
    }
}