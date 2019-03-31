package com.trackinglibrary.services

import android.content.Intent
import android.location.Location
import android.os.Handler
import android.os.IBinder
import android.os.Message
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.gson.Gson
import com.trackinglibrary.TrackRecorder
import com.trackinglibrary.database.LogItem
import com.trackinglibrary.prefs.MyLocaation
import com.trackinglibrary.prefs.RealTimeSettings
import io.realm.Realm
import java.util.*
import java.util.concurrent.TimeUnit

internal class TrackingService : BaseTrackerService() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun getLocationCallback(): LocationCallback? {
        return locationCallback
    }

    lateinit var stopTrackingHandler: Handler
    val what = 100
    override fun onCreate() {
        super.onCreate()
        stopTrackingHandler = StopTrackHandler()

        resetStopMessage()
    }

    private fun resetStopMessage() {
        stopTrackingHandler.removeMessages(what)
        stopTrackingHandler.sendEmptyMessageDelayed(what, TimeUnit.MINUTES.toMillis(5))
    }

    private val locationCallback = object : LocationCallback() {

        override fun onLocationResult(r: LocationResult?) {
            super.onLocationResult(r)
            if (r != null) {
                newLocation(r.lastLocation)
                val location = r.lastLocation

                fun saveToSettings(l: Location) {
                    val settings = RealTimeSettings(this@TrackingService)
                    settings.executeTransaction {
                        val myLoc = MyLocaation(l.bearing, l.speed, l.accuracy)
                        val locationSerialized = Gson().toJson(myLoc)
                        settings.setLocationV2(locationSerialized)
                    }
                }

                fun saveToDatabase(l: Location) {

                    fun mps_to_kmph(mps: Float): Float {
                        return (3.6f * mps);
                    }

                    if (l.hasSpeed()) {
                        Realm.getDefaultInstance().use {
                            it.executeTransaction {
                                val logItem = LogItem(
                                    Calendar.getInstance().timeInMillis,
                                    LogItem.Companion.Type.TYPE_SPEED.typeId,
                                    String.format(Locale.US, "%.3f", mps_to_kmph(l.speed))
                                )
                                it.insert(logItem)
                            }
                        }
                    }
                }

                resetStopMessage()

                if (location.hasAccuracy()) {
                    if (location.accuracy < 60) {
                        saveToSettings(location)
                        saveToDatabase(location)
                    }
                } else {
                    saveToSettings(location)
                    saveToDatabase(location)
                }
            }
        }
    }
}

class StopTrackHandler : Handler() {
    override fun handleMessage(msg: Message?) {
        super.handleMessage(msg)

        if (msg != null && msg.what == 100) {
            TrackRecorder.stop()
        }
    }
}