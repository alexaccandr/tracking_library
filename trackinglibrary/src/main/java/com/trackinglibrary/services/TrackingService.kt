package com.trackinglibrary.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.HandlerThread
import android.os.IBinder
import android.os.Process
import android.util.Log
import com.kite.model.settings.TrackerSettings
import com.trackinglibrary.TrackRecorder
import com.trackinglibrary.database.TrackRecordDao
import com.trackinglibrary.utils.NotificationUtils
import io.realm.Realm
import java.util.concurrent.TimeUnit

internal class TrackingService : Service(), LocationListener {

    private val tag = TrackingService::class.java.simpleName
    private var mLocationManager: LocationManager? = null
    private var locationHandler: LocationHandler? = null
    private var settings: TrackerSettings? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        mLocationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (mLocationManager == null) {
            return
        }
        settings = TrackerSettings(this)

        startForeground(
            NotificationUtils.FOREGROUND_SERVICE_ID,
            NotificationUtils.createOrUpdateTrackerNotification(this)
        )

        initLocationHandler()
        registerLocationListener()
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

        mLocationManager?.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            TimeUnit.SECONDS.toMillis(10),
            0F,
            this,
            looper
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        mLocationManager?.removeUpdates(this)
        settings?.unregisterListeners()
    }

    override fun onLocationChanged(location: Location?) {
        if (location != null) {
            newLocation(location)
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        // ignored
    }

    override fun onProviderEnabled(provider: String?) {
        // ignored
    }

    override fun onProviderDisabled(provider: String?) {
        // ignored
    }

    private fun newLocation(location: Location) {
        locationHandler?.newLocation(location)
    }
}