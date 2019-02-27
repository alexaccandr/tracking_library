package com.trackinglibrary.services

import android.app.IntentService
import android.content.Intent
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity
import com.trackinglibrary.database.MyTrackTransition
import com.trackinglibrary.prefs.RealTimeSettings
import io.realm.Realm
import java.util.*


class ActivityTransitionService : IntentService(TAG) {

    override fun onCreate() {
        super.onCreate()
    }

    override fun onHandleIntent(intent: Intent?) {
        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)
            val transition = result!!.transitionEvents.maxBy { it.elapsedRealTimeNanos }

            val settings = RealTimeSettings(this)
            settings.executeTransaction {
                if(transition!= null) {
                    settings.setLastTransition(transition.activityType)
                }
            }

            if (transition != null) {
                NewMessageNotification.createNotification(
                    this,
                    getActivityString(transition.activityType),
                    getActivityString(transition.activityType)
                )
            }
        }
    }

    companion object {
        protected val TAG = "Activity"

        internal fun getActivityString(detectedActivityType: Int): String {
            when (detectedActivityType) {
                DetectedActivity.ON_BICYCLE -> return "ON_BICYCLE"
                DetectedActivity.ON_FOOT -> return "ON_FOOT"
                DetectedActivity.RUNNING -> return "RUNNING"
                DetectedActivity.STILL -> return "STILL"
                DetectedActivity.TILTING -> return "TITLING"
                DetectedActivity.WALKING -> return "WALKING"
                DetectedActivity.IN_VEHICLE -> return "IN_VEHICLE"
                else -> return "SOMETHING_ELSE: $detectedActivityType"
            }
        }
    }
}
