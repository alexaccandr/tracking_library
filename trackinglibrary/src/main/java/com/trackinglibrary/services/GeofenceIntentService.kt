package com.trackinglibrary.services

import android.app.IntentService
import android.content.Intent
import com.trackinglibrary.TrackRecorder
import com.trackinglibrary.settings.TrackerSettings
import com.trackinglibrary.utils.ContextUtils

class GeofenceIntentService : IntentService("geofence intent service") {

    override fun onHandleIntent(intent: Intent?) {

        // show notification
        NewMessageNotification.createNotification(this, "Geofence Google", "On exit geofence")

        // clear geofence settings
        val settings = TrackerSettings(this)
        settings.executeTransaction {
            settings.setGeofenceStr("")
        }

        // stop geofense service
        ContextUtils.stopService(this, RegisterGeofenceService::class.java)

        // start track if not started
        TrackRecorder.start()
    }
}