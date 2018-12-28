package com.trackinglibrary

import android.app.Application
import android.location.Location
import android.os.HandlerThread
import android.os.Process
import android.util.Log
import com.kite.model.settings.TrackerSettings
import com.trackinglibrary.Utils.ContextUtils
import com.trackinglibrary.Utils.RxBus
import com.trackinglibrary.database.TrackRecordDao
import com.trackinglibrary.model.ModelAdapter
import com.trackinglibrary.model.Track
import com.trackinglibrary.model.TrackAverageSpeed
import com.trackinglibrary.model.TrackStatus
import com.trackinglibrary.services.TrackingService
import io.reactivex.disposables.Disposable
import io.realm.Realm
import org.joda.time.DateTime


object TrackRecorder {

    private var initialized = false
    private var started = false
    private lateinit var handlerQueue: TrackHandlerQueue
    private lateinit var context: Application

    @Synchronized
    fun initialize(context: Application) {
        if (initialized) {
            Log.i("info", "Tracker already initialized")
            // already initialized
            return
        }

        this.context = context

        startQueue()
        if (shouldContinueRecording()) {
            continueRecording()
        }
        initialized = true
    }

    private fun startQueue() {
        val handlerThread = HandlerThread("trackRecorderQueue", Process.THREAD_PRIORITY_BACKGROUND)
        handlerThread.start()
        val looper = handlerThread.looper
        val settings = TrackerSettings(context)
        handlerQueue = TrackHandlerQueue(settings, looper)
    }

    @Synchronized
    fun start() {
        checkInitialized()

        if (started) {
            return
        }

        started = true
        val startTime = DateTime.now().millis
        executeStartTrack(startTime)
        startGpsService()
    }

    @Synchronized
    fun stop() {
        checkInitialized()

        if (!started) {
            return
        }

        started = false
        stopGpsService()
        executeStopTrack(DateTime.now().millis)
    }

    fun hasStarted(): Boolean {
        checkInitialized()

        val realm = Realm.getDefaultInstance()
        realm.use {
            val dao = TrackRecordDao(realm)
            return dao.hasStartedTrack()
        }
    }

    fun setFrequency(frequency: Long) {
        checkInitialized()

        executeUpdateFrequency(frequency)
    }

    fun getTracks(): Array<Track> {
        val realm = Realm.getDefaultInstance()
        realm.use {
            val dao = TrackRecordDao(realm)
            return ModelAdapter.adaptTracks(dao.selectTracks())
        }
    }

    fun registerAverageSpeedChangeListener(listener: (TrackAverageSpeed) -> Unit): Disposable? {
        return RxBus.listen(TrackAverageSpeed::class.java).subscribe {
            listener(it)
        }
    }

    fun registerTrackStatusChangeListener(listener: (TrackStatus) -> Unit): Disposable? {
        return RxBus.listen(TrackStatus::class.java).subscribe {
            listener(it)
        }
    }

    private fun checkInitialized() {
        if (!initialized) {
            throw IllegalStateException("Tracker not initialized")
        }
    }

    private fun continueRecording() {
        started = true
        startGpsService()
    }

    private fun shouldContinueRecording(): Boolean = hasStarted()

    private fun startGpsService() {
        ContextUtils.startService(context, TrackingService::class.java)
    }

    private fun stopGpsService() {
        ContextUtils.stopService(context, TrackingService::class.java)
    }

    internal fun executeStartTrack(startTime: Long) {
        handlerQueue.putQueueStartTrack(startTime)
    }

    internal fun executeStopTrack(stopTime: Long) {
        handlerQueue.putQueueStopTrack(stopTime)
    }

    internal fun saveLocation(location: Location) {
        handlerQueue.putQueueSaveLocation(location)
    }

    internal fun executeUpdateFrequency(frequency: Long) {
        handlerQueue.putQueueUpdateFrequency(frequency)
    }

    internal fun executeUpdateAverageSpeed(data: Pair<Long, Double>) {
        handlerQueue.putQueueUpdateAverageSpeed(data)
    }
}