package com.trackinglibrary

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.google.android.gms.location.DetectedActivity
import com.trackinglibrary.database.TrackRecord
import com.trackinglibrary.database.TrackRecordDao
import com.trackinglibrary.model.ModelAdapter
import com.trackinglibrary.model.TrackAverageSpeed
import com.trackinglibrary.model.TrackPoint
import com.trackinglibrary.model.TrackStatus
import com.trackinglibrary.prefs.BaseSettings
import com.trackinglibrary.prefs.BaseSettings.Companion.SETTING_LAST_STILL_CONF
import com.trackinglibrary.prefs.RealTimeSettings
import com.trackinglibrary.prefs.SettingsController
import com.trackinglibrary.prefs.SettingsControllerListener
import com.trackinglibrary.services.NewMessageNotification
import com.trackinglibrary.settings.TrackerSettings
import com.trackinglibrary.utils.BluetoothUtils
import com.trackinglibrary.utils.DatabaseUtils
import com.trackinglibrary.utils.RxBus
import com.trackinglibrary.utils.SpeedUtils
import io.realm.Realm
import java.util.concurrent.TimeUnit


internal class TrackHandlerQueue constructor(val context: Context, val settings: TrackerSettings, looper: Looper) :
    Handler(looper) {

    val tag = TrackHandlerQueue::class.java.simpleName

    private companion object {
        const val MSG_START_TRACK = 0
        const val MSG_STOP_TRACK = 1
        const val MSG_START_RECOGNITION = 5
        const val MSG_STOP_RECOGNITION = 6
        const val MSG_SAVE_LOCATION = 2
        const val MSG_SAVE_FREQUENCY = 3
        const val MSG_SAVE_AVERAGE_SPEED_DATA = 4
        const val MSG_PENDING_STOP = 7
        const val MSG_PENDING_STOP_FAST = 8
        const val MSG_PENDING_START = 9
        const val MSG_NEW_RECOGNITION_EVENT = 10
    }

    private var realm: Realm? = null
    private var track: TrackRecord? = null

    private var rrtSettingsController: SettingsController? = null
    private var isRecordingTrack = false
    private var isPendingToStart = false
    private var isPendingToStop = false
    private var isPendingToStopFast = false

    init {
        post {
            realm = Realm.getDefaultInstance()
            track = TrackRecordDao(realm!!).lastTrack()
            if (track == null) {
                DatabaseUtils.close(realm)
            }
            if (TrackRecorder.hasStartedRecognition()) {
                registerRttController()
            }
        }
    }

    override fun handleMessage(msg: Message?) {
        super.handleMessage(msg)

        if (msg == null) {
            return
        }

        when (msg.what) {
            MSG_START_TRACK -> {
                Log.d(tag, "msg: MSG_START_TRACK")
                if (track == null) {
                    realm = DatabaseUtils.openDB()
                    val startTime = msg.obj as Long

                    // open track
                    createTrack(startTime)

                    notifyTrackStarted(track!!, true)
                }
            }
            MSG_STOP_TRACK -> {
                Log.d(tag, "msg: MSG_STOP_TRACK")
                if (track != null) {
                    val stopTime = msg.obj as Long

                    // close track
                    stopTrack(realm!!, stopTime)

                    notifyTrackStarted(track!!, false)

                    // close database
                    DatabaseUtils.close(realm)

                    track = null
                }
            }
            MSG_START_RECOGNITION -> {
                registerRttController()
                settings.executeTransaction {
                    settings.setRecognitionStarted(true)
                }
            }
            MSG_STOP_RECOGNITION -> {
                val settings = TrackerSettings(context)
                settings.executeTransaction {
                    settings.setRecognitionStarted(false)
                }
                unregisterSettingsController()

                // clear all flags
                isRecordingTrack = false
                updateRecognitionFlags()
            }
            MSG_SAVE_LOCATION -> {
                Log.d(tag, "msg: MSG_SAVE_LOCATION")
                if (track != null) {
                    val location = msg.obj as Location

                    // add location
                    saveLocation(realm!!, track!!.id, location)

                    notifyLocationChanged(track!!.id, location)
                }
            }
            MSG_SAVE_FREQUENCY -> {
                Log.d(tag, "msg: MSG_SAVE_FREQUENCY")
                val frequency = msg.obj as Long
                Log.d(tag, "save value=$frequency")

                // update frequency
                saveFrequency(frequency)
            }
            MSG_SAVE_AVERAGE_SPEED_DATA -> {
                Log.d(tag, "msg: MSG_SAVE_AVERAGE_SPEED_DATA")
                if (track != null) {
                    val data = msg.obj as Pair<Long, Double>

                    // update average speed data
                    updateAverageSpeed(realm!!, track!!.id, data)

                    val averageSpeed =
                        SpeedUtils.calcAverageSpeed(track!!.totalTime, track!!.totalDistance)
                    notifyAverageSpeedChanged(averageSpeed)
                }
            }
            MSG_PENDING_START -> {
                isRecordingTrack = true
                NewMessageNotification.createNotification(context, "START DRIVING!!!!!!", "START DRIVING!!!!!!")
            }
            MSG_NEW_RECOGNITION_EVENT -> {
                val realTimeSettings = RealTimeSettings(context)
                if (settings.isRecognitionStarted()) {
                    val lastStillConf = realTimeSettings.getLastStillConf()
                    val lastInVehicleConf = realTimeSettings.getLastInVehicleConf()

                    val drivingCase = realTimeSettings.getLastTransition() == DetectedActivity.IN_VEHICLE
                            && lastInVehicleConf > 90 && lastStillConf < 3

                    if (isRecordingTrack) {
                        // TODO
//                    if (!inVehicleCase) {
//
//                        // pending to stop
//                        when {
//                            !isPendingToStop -> {
//                                putQueuePending(putStop = true)
//                                updateRecognitionFlags(pStop = true)
//                            }
//                            !isPendingToStopFast -> {
//
//                            }
//                        }
//                    }
                    } else {

                        if (drivingCase) {
                            if (isPendingToStart) {
                                // ignore
                            } else {
                                putQueuePending(putStart = true)
                                updateRecognitionFlags(pendingStart = true)
                            }
                        } else {
                            putQueuePending()
                            updateRecognitionFlags()
                        }
                    }
                }
            }
        }
    }

    private fun registerRttController() {
        val rttSettings = RealTimeSettings(context)
        if (rrtSettingsController == null) {
            rrtSettingsController = SettingsController(
                context, rrtListener, rttSettings.preferences,
                BaseSettings.SETTING_LAST_TRANSITION,
                BaseSettings.SETTING_LAST_IN_VEHICLE_CONF,
                BaseSettings.SETTING_LAST_STILL_CONF
            )
        }
    }

    private fun unregisterSettingsController() {
        if (rrtSettingsController != null) {
            rrtSettingsController!!.unregisterListeners()
            rrtSettingsController = null
        }
    }


    val WHAT_STILL = 12
    val stillHandler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)

            if (msg != null) {
                if (msg.what == 12) {
                    removeMessages(WHAT_STILL)
                    val s = TrackerSettings(context)
                    if (s.isStillRegistered()) {
                        val isTrackStarted = TrackRecorder.hasStarted()
                        val threshold = TrackerSettings(context).getStill()
                        // show notification
                        NewMessageNotification.createNotification(
                            context,
                            "Still threshold($threshold)",
                            if (isTrackStarted) "Track already started" else "Start new track"
                        )

                        // start track if not started
                        TrackRecorder.start()
                    }
                }
            }
        }
    }

    private val rrtListener = object : SettingsControllerListener {
        override fun onChange(context: Context, sharedPreferences: SharedPreferences, key: String) {
            putQueueNewRecognitionEvent()

            if (key == SETTING_LAST_STILL_CONF) {
                val s = TrackerSettings(context)
                if (s.isStillRegistered()) {

                    if (TrackRecorder.hasStarted()) {
                        stillHandler.removeMessages(WHAT_STILL)
                    } else {
                        val rttSettings = RealTimeSettings(context)
                        val threshold = s.getStill()
                        val lastValue = rttSettings.getLastStillConf()
                        if (lastValue in 1..threshold) {
                            stillHandler.sendEmptyMessageDelayed(WHAT_STILL, TimeUnit.SECONDS.toMillis(10))
                        } else {
                            stillHandler.removeMessages(WHAT_STILL)
                        }
                    }
                } else {
                    stillHandler.removeMessages(WHAT_STILL)
                }
            }
        }
    }

    private fun updateRecognitionFlags(
        pendingStop: Boolean = false,
        pendingStopFast: Boolean = false,
        pendingStart: Boolean = false
    ) {
        isPendingToStop = pendingStop
        isPendingToStopFast = pendingStopFast
        isRecordingTrack = pendingStart
    }

    private fun createTrack(startTime: Long) {
        Log.d(tag, "createTrack: startTime=$startTime")
        DatabaseUtils.executeTransaction(realm!!, Realm.Transaction {
            track = TrackRecordDao(realm!!).createTrack(startTime)
        })
        Log.d(tag, "createTrack: new trackId=${track!!.id}")
    }

    private fun stopTrack(realm: Realm, stopTime: Long) {
        Log.d(tag, "stopTrack: stopTime=$stopTime")
        DatabaseUtils.executeTransaction(realm, Realm.Transaction {
            TrackRecordDao(it).stopTrack(track!!.id, stopTime)
        })
    }

    private fun saveLocation(realm: Realm, trackId: String, location: Location) {
        Log.d(
            tag,
            "saveLocation: trackId=$trackId, location=(" + location.latitude + ", " + location.longitude + ", " + location.time + ")"
        )
        DatabaseUtils.executeTransaction(realm, Realm.Transaction {
            TrackRecordDao(it).saveLocation(
                trackId,
                location.latitude,
                location.longitude,
                location.time
            )
        })
    }

    private fun saveFrequency(frequency: Long) {
        Log.d(tag, "saveFrequency: frequency=$frequency")
        settings.executeTransaction {
            settings.setFrequency(frequency)
        }
    }

    private fun updateAverageSpeed(realm: Realm, trackId: String, data: Pair<Long, Double>) {
        Log.d(
            tag,
            "updateAverageSpeed: trackId=$trackId, data=(" + data.first + ", " + data.second + ")"
        )
        val track = TrackRecordDao(realm).selectTrack(trackId)

        DatabaseUtils.executeTransaction(realm, Realm.Transaction {
            TrackRecordDao(it).updateAverageSpeed(
                track,
                track.totalTime + data.first,
                track.totalDistance + data.second
            )
        })
    }

    private fun notifyAverageSpeedChanged(averageSpeed: Double) {
        RxBus.publish(TrackAverageSpeed(averageSpeed))
    }

    private fun notifyTrackStarted(track: TrackRecord, started: Boolean) {
        RxBus.publish(TrackStatus(ModelAdapter.adaptTrack(track), started))
    }

    private fun notifyLocationChanged(id: String, location: Location) {
        RxBus.publish(TrackPoint(id, location.latitude, location.longitude, location.time))
    }

    internal fun putQueueStartTrack(startTime: Long) {
        val message = Message()
        message.what = TrackHandlerQueue.MSG_START_TRACK
        message.obj = startTime
        sendMessage(message)
    }

    internal fun putQueueStopTrack(stopTime: Long) {
        val message = Message()
        message.what = TrackHandlerQueue.MSG_STOP_TRACK
        message.obj = stopTime
        sendMessage(message)
    }

    internal fun putQueueStartRecognition() {
        val message = Message()
        message.what = TrackHandlerQueue.MSG_START_RECOGNITION
        sendMessage(message)
    }

    internal fun putQueueStopRecognition() {
        val message = Message()
        message.what = TrackHandlerQueue.MSG_STOP_RECOGNITION
        sendMessage(message)
    }

    internal fun putQueueSaveLocation(location: Location) {
        val message = Message()
        message.what = TrackHandlerQueue.MSG_SAVE_LOCATION
        message.obj = location
        sendMessage(message)
    }

    internal fun putQueueUpdateFrequency(frequency: Long) {
        val message = Message()
        message.what = TrackHandlerQueue.MSG_SAVE_FREQUENCY
        message.obj = frequency
        sendMessage(message)
    }

    fun putQueueUpdateAverageSpeed(data: Pair<Long, Double>) {
        val message = Message()
        message.what = TrackHandlerQueue.MSG_SAVE_AVERAGE_SPEED_DATA
        message.obj = data
        sendMessage(message)
    }

    fun putQueueNewRecognitionEvent() {
        sendEmptyMessage(MSG_NEW_RECOGNITION_EVENT)
    }


    private fun putQueuePending(
        putStart: Boolean = false,
        putStop: Boolean = false,
        putStopFast: Boolean = false
    ) {
        if (putStart) {
            val bluetoothCase = BluetoothUtils.enableBluetooth()
            val waitingSecs = if (bluetoothCase) 20L else 10L
            sendEmptyMessageDelayed(MSG_PENDING_START, TimeUnit.SECONDS.toMillis(waitingSecs))
        } else {
            removeMessages(MSG_PENDING_START)
        }
        if (putStop) {
            sendEmptyMessageDelayed(MSG_PENDING_STOP, TimeUnit.SECONDS.toMillis(30))
        } else {
            removeMessages(MSG_PENDING_STOP)
        }
        if (putStopFast) {
            sendEmptyMessageDelayed(MSG_PENDING_STOP_FAST, TimeUnit.SECONDS.toMillis(120))
        } else {
            removeMessages(MSG_PENDING_STOP_FAST)
        }
    }
}