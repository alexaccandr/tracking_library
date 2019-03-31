package com.trackinglibrary.services

import android.content.Context
import com.pathsense.android.sdk.location.PathsenseLocationProviderApi


class PathsenseController(
    val context: Context,
    val lat: Double,
    val lon: Double,
    val radius: Int,
    val geofenceRequestId: String
) {

    fun call() {
        val api = PathsenseLocationProviderApi.getInstance(context)
        api.addGeofence(
            geofenceRequestId,
            lat,
            lon,
            if (radius < 50) 50 else radius,
            PathSenseGeofenceReceiver::class.java
        )
    }
}