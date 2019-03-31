package com.trackinglibrary.services

import android.content.Intent
import android.location.Location
import android.os.Handler
import android.os.IBinder
import android.text.TextUtils
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.gson.Gson
import com.trackinglibrary.model.GeofenceItem
import com.trackinglibrary.settings.TrackerSettings
import java.util.*

class RegisterGeofenceService : BaseTrackerService() {

    val handler = Handler()
    var geofenceController: GeofenceController? = null

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
                    if (location.accuracy < 40) {
                        handler.post {
                            if (TextUtils.isEmpty(TrackerSettings(this@RegisterGeofenceService).getGeofenceStr())) {
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
            settings.setGeofenceStr(geofenceStr)

            geofenceController =
                GeofenceController(
                    this@RegisterGeofenceService,
                    location.latitude,
                    location.longitude,
                    settings.getGeofenceRadius().toFloat(),
                    "MyTestGeofence"
                )
            geofenceController!!.call()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (geofenceController != null) {
            geofenceController!!.unregister()
        }
    }
}