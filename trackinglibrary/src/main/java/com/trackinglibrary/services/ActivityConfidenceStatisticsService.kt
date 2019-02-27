package com.trackinglibrary.services

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import com.google.gson.Gson
import com.trackinglibrary.database.LogItem
import com.trackinglibrary.prefs.RealTimeSettings
import com.trackinglibrary.prefs.RecognitionObject
import io.realm.Realm
import java.util.*


class ActivityConfidenceStatisticsService : IntentService("zazaz") {

    override fun onHandleIntent(intent: Intent?) {
//        if (ActivityTransitionResult.hasResult(intent)) {
//            val result = ActivityTransitionResult.extractResult(intent)
        //Check whether the Intent contains activity recognition data//
        if (ActivityRecognitionResult.hasResult(intent)) {

//If data is available, then extract the ActivityRecognitionResult from the Intent//
            val result = ActivityRecognitionResult.extractResult(intent);

//Get an array of DetectedActivity objects//
            val detectedActivities: ArrayList<DetectedActivity> =
                result.probableActivities as ArrayList<DetectedActivity>

            detectedActivities.forEach {
                Log.d("", "" + it.type + ", ${it.confidence}")
            }

            val item = RecognitionObject(Calendar.getInstance().timeInMillis, detectedActivities)
            val itemStr = Gson().toJson(item)
            val settings = RealTimeSettings(this)
            settings.executeTransaction {
                settings.setData(itemStr)
                settings.setLastStillConf(
                    detectedActivities.find { it.type == DetectedActivity.STILL }?.confidence ?: 0
                )
                settings.setLastInVehicleConf(
                    detectedActivities.find { it.type == DetectedActivity.IN_VEHICLE }?.confidence ?: 0
                )
            }

            saveLogItem(detectedActivities)
        }
    }

    private fun saveLogItem(confidenceList: ArrayList<DetectedActivity>) {

        fun findConfidence(type: Int): String {
            return confidenceList.find { it.type == type }?.confidence?.toString() ?: ""
        }

        val time = Calendar.getInstance().timeInMillis
        val logDataStr = "$time," +
                "${findConfidence(DetectedActivity.ON_BICYCLE)}," +
                "${findConfidence(DetectedActivity.ON_FOOT)}," +
                "${findConfidence(DetectedActivity.RUNNING)}," +
                "${findConfidence(DetectedActivity.STILL)}," +
                "${findConfidence(DetectedActivity.TILTING)}," +
                "${findConfidence(DetectedActivity.WALKING)}," +
                findConfidence(DetectedActivity.IN_VEHICLE)

        Realm.getDefaultInstance().use {
            it.executeTransaction {
                val logItem = LogItem(time, LogItem.Companion.Type.TYPE_ACCELEROMETER.typeId, logDataStr)
                it.insert(logItem)
            }
        }
    }
}
