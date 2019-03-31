package com.trackinglibrary.services

import android.content.Intent
import android.location.Location
import android.os.Handler
import android.os.IBinder
import android.text.TextUtils
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.gson.Gson
import com.pathsense.android.sdk.location.PathsenseLocationProviderApi
import com.trackinglibrary.model.GeofenceItem
import com.trackinglibrary.settings.TrackerSettings
import java.util.*

class RegisterPathsenseService : BaseTrackerService() {

    val handler = Handler()
    var pathsenseController: PathsenseController? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun getLocationCallback(): LocationCallback? {
        return locationCallback
    }

    override fun getSmallestInterval(): Float {
        return 0f
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(r: LocationResult?) {
            val location = r?.lastLocation
            if (location != null) {
                if (location.hasAccuracy()) {
                    if (location.accuracy < 30) {
                        handler.post {
                            if (TextUtils.isEmpty(TrackerSettings(this@RegisterPathsenseService).getGeofencePathsenseStr())) {
                                unregisterLocationListener()
                                registerGeofence(location)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun unregisterLocationListener() {
        unregisterLocationUpdates()
    }

    private fun registerGeofence(location: Location) {
        val geofenceItem = GeofenceItem(Calendar.getInstance().timeInMillis, null, location.latitude, location.longitude)
        val settings = TrackerSettings(this)
        settings.executeTransaction {
            val geofenceStr = Gson().toJson(geofenceItem)
            settings.setGeofencePathsenseStr(geofenceStr)
            
            pathsenseController = PathsenseController(
                    this@RegisterPathsenseService,
                    location.latitude,
                    location.longitude,
                    settings.getGeofenceRadius(),
                    "MyTestGeofence"
                )
            pathsenseController!!.call()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (pathsenseController != null) {
            val api = PathsenseLocationProviderApi.getInstance(this)
            api.removeGeofences()
            api.destroy()
        }
    }
}