package com.trackinglibrary.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.HandlerThread
import android.os.IBinder
import android.os.PowerManager
import android.os.Process
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.trackinglibrary.TrackRecorder
import com.trackinglibrary.database.TrackRecordDao
import com.trackinglibrary.settings.TrackerSettings
import com.trackinglibrary.utils.NotificationUtils
import com.trackinglibrary.utils.WakeLockUtils
import io.realm.Realm
import java.util.concurrent.TimeUnit

open class BaseTrackerService : Service() {
    private val tag = TrackingService::class.java.simpleName
    private var mLocationManager: LocationManager? = null
    private var locationHandler: LocationHandler? = null
    private var settings: TrackerSettings? = null
    private var locationApi: FusedLocationProviderClient? = null
    private var myWakeLock: PowerManager.WakeLock? = null

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

        WakeLockUtils.acquireWakeLock(this, "myWakeLock:myLock")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "onDestroy")
        unregisterLocationUpdates()
        settings?.unregisterListeners()
        if (myWakeLock != null) {
            WakeLockUtils.releaseWakeLock(myWakeLock!!)
        }
    }

    fun unregisterLocationUpdates() {
        locationApi?.removeLocationUpdates(getLocationCallback())
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
        request.smallestDisplacement = getSmallestInterval()

        locationApi!!.requestLocationUpdates(request, getLocationCallback(), looper)
    }

    open fun getSmallestInterval() = 0f

    open fun getLocationCallback(): LocationCallback? {
        return null
    }

    fun newLocation(location: Location) {
        locationHandler?.newLocation(location)
    }
}