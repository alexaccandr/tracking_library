package com.trackinglibrary.services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.tasks.Task
import com.trackinglibrary.settings.TrackerSettings
import com.trackinglibrary.utils.NotificationUtils

internal class TrackAutoRecorderService : Service() {

    private val tag = TrackAutoRecorderService::class.java.simpleName
    private var settings: TrackerSettings? = null
    private var task1: Task<Void>? = null
    private var task2: Task<Void>? = null
    private val pIntent1 = lazy { getActivityDetectionPendingIntent() }
    private val pIntent2 = lazy { getActivityDetectionPendingIntent2() }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "onCreate")
        settings = TrackerSettings(this)

        startForeground(
            NotificationUtils.FOREGROUND_RECOGNITION_SERVICE_ID,
            NotificationUtils.createOrUpdateTrackerNotification(
                this,
                NotificationUtils.FOREGROUND_RECOGNITION_SERVICE_ID,
                "channelRecognition",
                "channelNameRecognition",
                "Recording activity recognition"
            )
        )

        registerRecognition()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "onDestroy")
        if (task1 != null) {
            ActivityRecognition.getClient(this).removeActivityTransitionUpdates(pIntent1.value)
        }
        if (task2 != null) {
            ActivityRecognition.getClient(this).removeActivityUpdates(pIntent2.value)
        }
        settings?.unregisterListeners()
    }

    private fun registerRecognition() {

        val transitions = registerTransitions()
        task1 = createTransitionsRequest(transitions)

        task2 = createUpdatesRequest()
    }

    private fun createUpdatesRequest(): Task<Void> {

        val task2 = ActivityRecognition.getClient(this)
            .requestActivityUpdates(0, pIntent2.value)

        task2.addOnSuccessListener {
            // Handle success
            Log.e("Client", "Success")
        }

        task2.addOnFailureListener {
            // Handle error
            it.printStackTrace()
            Log.e("Client", "Failure")
        }

        return task2
    }

    private fun createTransitionsRequest(transitions: List<ActivityTransition>): Task<Void> {
        val request = ActivityTransitionRequest(transitions)
        val task = ActivityRecognition.getClient(this)
            .requestActivityTransitionUpdates(request, pIntent1.value)

        task.addOnSuccessListener {
            // Handle success
            Log.e("Client", "Success")
        }

        task.addOnFailureListener {
            // Handle error
            it.printStackTrace()
            Log.e("Client", "Failure")
        }

        return task
    }

    private fun getActivityDetectionPendingIntent(): PendingIntent {
        val intent = Intent(this, ActivityTransitionService::class.java)
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

    }

    private fun getActivityDetectionPendingIntent2(): PendingIntent {
        val intent = Intent(this, ActivityConfidenceStatisticsService::class.java)
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun registerTransitions(): List<ActivityTransition> {
        return mutableListOf<ActivityTransition>().apply {
            add(
                ActivityTransition.Builder().setActivityType(DetectedActivity.IN_VEHICLE)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER).build()
            )
            add(
                ActivityTransition.Builder().setActivityType(DetectedActivity.ON_BICYCLE)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER).build()
            )
            add(
                ActivityTransition.Builder().setActivityType(DetectedActivity.ON_FOOT)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER).build()
            )
            add(
                ActivityTransition.Builder().setActivityType(DetectedActivity.STILL)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER).build()
            )
            add(
                ActivityTransition.Builder().setActivityType(DetectedActivity.WALKING)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER).build()
            )
            add(
                ActivityTransition.Builder().setActivityType(DetectedActivity.RUNNING)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER).build()
            )
        }
    }
}