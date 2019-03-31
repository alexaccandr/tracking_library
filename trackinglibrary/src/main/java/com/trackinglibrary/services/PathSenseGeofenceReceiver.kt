package com.trackinglibrary.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pathsense.android.sdk.location.PathsenseGeofenceEvent
import com.trackinglibrary.TrackRecorder
import com.trackinglibrary.settings.TrackerSettings
import com.trackinglibrary.utils.ContextUtils


class PathSenseGeofenceReceiver: BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent?) {

        val geofenceEvent = PathsenseGeofenceEvent.fromIntent(intent)
        if (geofenceEvent != null) {
            if (geofenceEvent.isIngress) {
                // ingress
                // do something
            } else if (geofenceEvent.isEgress) {
                // show notification
                NewMessageNotification.createNotification(context, "Geofence Pathsense", "On exit geofence")

                // clear geofence settings
                val settings = TrackerSettings(context)
                settings.executeTransaction {
                    settings.setGeofencePathsenseStr("")
                }

                // stop geofense service
                ContextUtils.stopService(context, RegisterGeofenceService::class.java)

                // start track if not started
                TrackRecorder.start()
            }
        }
    }

}