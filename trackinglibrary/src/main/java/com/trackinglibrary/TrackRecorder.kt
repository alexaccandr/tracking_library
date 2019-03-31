package com.trackinglibrary

import android.app.Application
import android.location.Location
import android.os.HandlerThread
import android.util.Log
import com.trackinglibrary.database.TrackRecord
import com.trackinglibrary.database.TrackRecordDao
import com.trackinglibrary.model.*
import com.trackinglibrary.services.TrackAutoRecorderService
import com.trackinglibrary.services.TrackingService
import com.trackinglibrary.settings.TrackerSettings
import com.trackinglibrary.utils.ContextUtils
import com.trackinglibrary.utils.DatabaseUtils
import com.trackinglibrary.utils.RxBus
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.realm.Realm
import org.jetbrains.annotations.NotNull
import java.util.*
import java.util.concurrent.TimeUnit


object TrackRecorder {

    private val tag = TrackRecorder::class.java.simpleName
    private var initialized = false
    private var startedTracker = false
    private var startedActivityRecognition = false
    private lateinit var handlerQueue: TrackHandlerQueue
    private lateinit var context: Application

    @Synchronized
    @JvmStatic
    fun initialize(@NotNull context: Application) {
        if (initialized) {
            Log.i("info", "Tracker already initialized")
            // already initialized
            return
        }
        initialized = true

        this.context = context

        clearGeofenceAndStill()
        DatabaseUtils.initDatabase(context)
        startQueue()
        if (shouldContinueRecording()) {
            continueRecordingTrack()
        }
        if (shouldContinueRecordingRecognition()) {
            continueRecordingRecognition()
        }
    }

    private fun clearGeofenceAndStill() {
        val settings = TrackerSettings(context)
        settings.executeTransaction {
            settings.setGeofenceStr("")
            settings.setGeofencePathsenseStr("")
            settings.setStillRegistered(false)
        }
    }

    @Synchronized
    @JvmStatic
    fun start() {
        checkInitialized()

        if (startedTracker) {
            return
        }

        val startTime = Calendar.getInstance().timeInMillis
        executeStartTrack(startTime)
        startGpsService()
        startedTracker = true
    }

    @Synchronized
    @JvmStatic
    fun stop() {
        checkInitialized()

        if (!startedTracker) {
            return
        }

        stopGpsService()
        executeStopTrack(Calendar.getInstance().timeInMillis)
        startedTracker = false
    }

    @Synchronized
    @JvmStatic
    fun startRecognition() {
        checkInitialized()

        if (startedActivityRecognition) {
            return
        }

        executeStartRecognition()
        startRecognitionService()
        startedActivityRecognition = true
    }

    @Synchronized
    @JvmStatic
    fun stopRecognition() {
        checkInitialized()

        if (!startedActivityRecognition) {
            return
        }

        stopRecognitionService()
        executeStopRecognition()
        startedActivityRecognition = false
    }

    @JvmStatic
    fun hasStarted(): Boolean {
        checkInitialized()

        return hasStarted0()
    }

    @JvmStatic
    fun hasStartedRecognition(): Boolean {
        checkInitialized()

        return hasStartedRecognition0()
    }

    private fun hasStarted0(): Boolean {
        Realm.getDefaultInstance().use {
            return TrackRecordDao(it).hasStartedTrack()
        }
    }

    private fun hasStartedRecognition0(): Boolean = TrackerSettings(context).isRecognitionStarted()

    @JvmStatic
    fun setFrequency(@NotNull frequency: Long) {
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
    fun getTrack(@NotNull id: String): Track {
        val realm = Realm.getDefaultInstance()
        realm.use {
            val dao = TrackRecordDao(realm)
            val track: TrackRecord = dao.selectTrack(id)
            return ModelAdapter.adaptTrack(track)
        }
    }

    @JvmStatic
    fun registerAverageSpeedChangeListener(
        @NotNull scheduler: Scheduler,
        @NotNull listener: (TrackAverageSpeed) -> Unit
    ): Disposable {
        return RxBus.listen(TrackAverageSpeed::class.java).observeOn(scheduler).subscribe {
            listener(it)
        }
    }

    @JvmStatic
    fun registerTrackStatusChangeListener(
        @NotNull scheduler: Scheduler,
        @NotNull listener: (TrackStatus) -> Unit
    ): Disposable {
        return RxBus.listen(TrackStatus::class.java).observeOn(scheduler).subscribe {
            Log.d(tag, "Listener called TrackStatus(${it.started})")
            listener(it)
        }
    }

    @JvmStatic
    fun registerTrackLocationChangeListener(
        @NotNull scheduler: Scheduler,
        @NotNull listener: (TrackPoint) -> Unit
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
        handlerQueue = TrackHandlerQueue(context, settings, looper)
    }

    private fun checkInitialized() {
        if (!initialized) {
            throw IllegalStateException("Tracker not initialized")
        }
    }

    private fun continueRecordingTrack() {
        startedTracker = true
        startGpsService()
    }

    private fun continueRecordingRecognition() {
        startedActivityRecognition = true
        startRecognitionService()
    }

    private fun shouldContinueRecording(): Boolean = hasStarted0()

    private fun shouldContinueRecordingRecognition(): Boolean = hasStartedRecognition0()

    private fun startGpsService() {
        ContextUtils.startService(context, TrackingService::class.java)
    }

    private fun stopGpsService() {
        ContextUtils.stopService(context, TrackingService::class.java)
    }

    private fun startRecognitionService() {
        ContextUtils.startService(context, TrackAutoRecorderService::class.java)
    }

    private fun stopRecognitionService() {
        ContextUtils.stopService(context, TrackAutoRecorderService::class.java)
    }

    internal fun executeStartTrack(startTime: Long) {
        handlerQueue.putQueueStartTrack(startTime)
    }

    internal fun executeStopTrack(stopTime: Long) {
        handlerQueue.putQueueStopTrack(stopTime)
    }

    internal fun executeStartRecognition() {
        handlerQueue.putQueueStartRecognition()
    }

    internal fun executeStopRecognition() {
        handlerQueue.putQueueStopRecognition()
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