package com.trackinglibrary.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.HandlerThread
import android.os.IBinder
import android.os.Process
import android.util.Log
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.kite.model.settings.TrackerSettings
import com.trackinglibrary.TrackRecorder
import com.trackinglibrary.database.LogItem
import com.trackinglibrary.database.TrackRecordDao
import com.trackinglibrary.prefs.MyLocaation
import com.trackinglibrary.prefs.RealTimeSettings
import com.trackinglibrary.utils.NotificationUtils
import io.realm.Realm
import java.util.*
import java.util.concurrent.TimeUnit

internal class TrackingService : Service() {

    private val tag = TrackingService::class.java.simpleName
    private var mLocationManager: LocationManager? = null
    private var locationHandler: LocationHandler? = null
    private var settings: TrackerSettings? = null
    private var locationApi: FusedLocationProviderClient? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "onCreate")
        mLocationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (mLocationManager == null) {
            return
        }
        settings = TrackerSettings(this)

        startForeground(
            NotificationUtils.FOREGROUND_TRACKER_SERVICE_ID,
            NotificationUtils.createOrUpdateTrackerNotification(
                this,
                NotificationUtils.FOREGROUND_TRACKER_SERVICE_ID,
                "channelTracker",
                "channelNameTracker",
                "Recording Tracker"
            )
        )

        initLocationHandler()
        registerLocationListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "onDestroy")

        locationApi?.removeLocationUpdates(locationCallback)
        settings?.unregisterListeners()
    }

    private fun initLocationHandler() {
        val s = settings!!
        val saveLocationFrequency = settings!!.getFrequency()
        val lastLocTime = Realm.getDefaultInstance().use {
            TrackRecordDao(it).lastLocationTime()
        }
        locationHandler = LocationHandler(saveLocationFrequency, lastLocTime,
            {
                // save new interval time/distance
                TrackRecorder.executeUpdateAverageSpeed(it)
            }, {
                // save interval location
                TrackRecorder.saveLocation(it)
            }
        )
        s.registerFrequencyChangedListener {
            Log.d(tag, "Save location frequency changes=" + TimeUnit.MILLISECONDS.toMinutes(it))
            locationHandler!!.saveFreqTime = it
        }
    }

    @SuppressLint("MissingPermission")
    private fun registerLocationListener() {

        val handler = HandlerThread("TrackerServiceHandler", Process.THREAD_PRIORITY_BACKGROUND)
        handler.start()
        val looper = handler.looper

        locationApi = LocationServices.getFusedLocationProviderClient(this)
        val request = LocationRequest.create()
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        request.interval = TimeUnit.SECONDS.toMillis(1)
        request.fastestInterval = TimeUnit.SECONDS.toMillis(1)
        request.smallestDisplacement = 0f

        locationApi!!.requestLocationUpdates(request, locationCallback, looper)
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
                            it.executeTransaction{
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

    private fun newLocation(location: Location) {
        locationHandler?.newLocation(location)
    }
}