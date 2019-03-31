package com.trackinglibrary.services

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task

class GeofenceController(val context: Context, val lat: Double, val lon: Double,val radius: Float, val geofenceRequestId: String) {

    private val geofencingClient = LocationServices.getGeofencingClient(context)
    var task: Task<Void>? = null
    fun buildGeofence(): Geofence {

        return Geofence.Builder()
            .setRequestId(geofenceRequestId)
            .setCircularRegion(
                lat,
                lon,
                radius
            )
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

    }

    fun buildGeofencingRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(listOf(geofence))
            .build()
    }

    val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceIntentService::class.java)
        PendingIntent.getService(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    @SuppressLint("MissingPermission")
    fun call() {
        val geofence = buildGeofencingRequest(buildGeofence())
        task = geofencingClient.addGeofences(geofence, geofencePendingIntent)
            .addOnSuccessListener {
                Log.d("Geofence", "added successfully")
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }

    fun unregister() {
        geofencingClient.removeGeofences(geofencePendingIntent)
    }
}