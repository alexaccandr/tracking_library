package com.trackinglibrary.utils

import android.location.Location
import android.location.LocationManager
import com.google.android.gms.location.LocationRequest
import java.util.concurrent.TimeUnit

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


    fun createFusedRequest(
        priority: Int,
        intervalSec: Long,
        fastestIntervalSec: Long,
        smallestDisplacement: Float
    ): LocationRequest {
        val request = LocationRequest.create()
        request.priority = priority
        request.expirationTime
        request.interval = TimeUnit.SECONDS.toMillis(intervalSec)
        request.fastestInterval = TimeUnit.SECONDS.toMillis(fastestIntervalSec)
        request.smallestDisplacement = smallestDisplacement
        return request
    }
}