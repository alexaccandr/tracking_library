package com.trackinglibrary

import android.app.Application
import android.location.Location
import android.os.HandlerThread
import android.util.Log
import com.kite.model.settings.TrackerSettings
import com.trackinglibrary.database.TrackRecord
import com.trackinglibrary.database.TrackRecordDao
import com.trackinglibrary.model.*
import com.trackinglibrary.services.TrackingService
import com.trackinglibrary.utils.ContextUtils
import com.trackinglibrary.utils.DatabaseUtils
import com.trackinglibrary.utils.LocationUtils
import com.trackinglibrary.utils.RxBus
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.realm.Realm
import java.util.*
import java.util.concurrent.TimeUnit


object TrackRecorder {

    private val tag = TrackRecorder::class.java.simpleName
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
        initialized = true

        this.context = context

        DatabaseUtils.initDatabase(context)
        startQueue()
        if (shouldContinueRecording()) {
            continueRecording()
        }

        // test suites
        applyTestSuits()
    }

    @Synchronized
    @JvmStatic
    fun start() {
        checkInitialized()

        if (started) {
            return
        }

        val startTime = Calendar.getInstance().timeInMillis
        executeStartTrack(startTime)
        startGpsService()
        started = true
    }

    @Synchronized
    @JvmStatic
    fun stop() {
        checkInitialized()

        if (!started) {
            return
        }

        stopGpsService()
        executeStopTrack(Calendar.getInstance().timeInMillis)
        started = false
    }

    @JvmStatic
    fun hasStarted(): Boolean {
        checkInitialized()

        return hasStarted0()
    }

    private fun hasStarted0(): Boolean {
        Realm.getDefaultInstance().use {
            return TrackRecordDao(it).hasStartedTrack()
        }
    }

    @JvmStatic
    fun setFrequency(frequency: Long) {
        checkInitialized()

        if (frequency in TimeUnit.MINUTES.toMillis(1)..TimeUnit.MINUTES.toMillis(60)) {
            executeUpdateFrequency(frequency)
        } else {
            Log.e(tag, "frequency($frequency) ignored, frequency must be in range [1, 60]")
        }
    }

    @JvmStatic
    fun getFrequency(): Long {
        return TrackerSettings(context).getFrequency()
    }

    @JvmStatic
    fun getTracks(): Array<Track> {
        val realm = Realm.getDefaultInstance()
        realm.use {
            val dao = TrackRecordDao(realm)
            return ModelAdapter.adaptTracks(dao.selectTracks())
        }
    }

    @JvmStatic
    fun getTrack(id: String): Track {
        val realm = Realm.getDefaultInstance()
        realm.use {
            val dao = TrackRecordDao(realm)
            val track: TrackRecord = dao.selectTrack(id)
            return ModelAdapter.adaptTrack(track)
        }
    }

    @JvmStatic
    fun registerAverageSpeedChangeListener(
        scheduler: Scheduler, listener: (TrackAverageSpeed) -> Unit
    ): Disposable {
        return RxBus.listen(TrackAverageSpeed::class.java).observeOn(scheduler).subscribe {
            listener(it)
        }
    }

    @JvmStatic
    fun registerTrackStatusChangeListener(
        scheduler: Scheduler, listener: (TrackStatus) -> Unit
    ): Disposable {
        return RxBus.listen(TrackStatus::class.java).observeOn(scheduler).subscribe {
            Log.d(tag, "Listener called TrackStatus(${it.started})")
            listener(it)
        }
    }

    @JvmStatic
    fun registerTrackLocationChangeListener(
        scheduler: Scheduler,
        listener: (TrackPoint) -> Unit
    ): Disposable {
        return RxBus.listen(TrackPoint::class.java).observeOn(scheduler).subscribe {
            listener(it)
        }
    }

    private fun startQueue() {
        val handlerThread = HandlerThread("trackRecorderQueue")
        handlerThread.start()
        val looper = handlerThread.looper
        val settings = TrackerSettings(context)
        handlerQueue = TrackHandlerQueue(settings, looper)
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

    private fun shouldContinueRecording(): Boolean = hasStarted0()

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

private fun TrackRecorder.applyTestSuits() {

    val trackCount = Realm.getDefaultInstance().use {
        TrackRecordDao(it).hasTracks()
    }

    // already has records, skip suites
    if (trackCount > 0) {
        return
    }

    fun runTest(startTime: Long, latitude: Double, longitude: Double, stepPerMinute: Double) {
        fun createLocation(lat: Double, lon: Double, time: Long): Location {
            val location = Location("gps")
            location.latitude = lat
            location.longitude = lon
            location.time = time
            return location
        }


        val realm: Realm = Realm.getDefaultInstance()
        val dao = TrackRecordDao(realm)
        realm.executeTransaction {
            val track = dao.createTrack(startTime)

            var loc1: Location? = null
            var loc2: Location? = null
            for (i in 0..100) {
                loc1 = if (loc1 == null) {
                    createLocation(latitude, longitude, startTime)
                } else {
                    loc2
                }
                loc2 = createLocation(
                    loc1!!.latitude + stepPerMinute,
                    loc1.longitude,
                    loc1.time + TimeUnit.MINUTES.toMillis(1)
                )

                TrackRecordDao(it).saveLocation(
                    track.id,
                    loc2.latitude,
                    loc2.longitude,
                    loc2.time
                )
                track.totalTime = track.totalTime + (loc2.time - loc1.time)
                track.totalDistance = track.totalDistance + LocationUtils.calcDistance(loc1, loc2)
            }
            dao.stopTrack(track.id, loc2!!.time)
        }
    }

    val startTime = Calendar.getInstance().timeInMillis - TimeUnit.DAYS.toMillis(3)
    runTest(startTime, 45.045447, 38.978492, 0.01)

    val startTime2 = Calendar.getInstance().timeInMillis - TimeUnit.DAYS.toMillis(2)
    runTest(startTime2, 55.759203, 37.619121, 0.015)

    val startTime3 = Calendar.getInstance().timeInMillis - TimeUnit.DAYS.toMillis(1)
    runTest(startTime3, 59.937245, 30.331714, 0.012)
}