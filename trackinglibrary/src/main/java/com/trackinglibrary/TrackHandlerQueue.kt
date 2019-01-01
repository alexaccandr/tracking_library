package com.trackinglibrary

import android.location.Location
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.kite.model.settings.TrackerSettings
import com.trackinglibrary.database.TrackRecord
import com.trackinglibrary.database.TrackRecordDao
import com.trackinglibrary.model.ModelAdapter
import com.trackinglibrary.model.TrackAverageSpeed
import com.trackinglibrary.model.TrackPoint
import com.trackinglibrary.model.TrackStatus
import com.trackinglibrary.utils.DatabaseUtils
import com.trackinglibrary.utils.RxBus
import com.trackinglibrary.utils.SpeedUtils
import io.realm.Realm

internal class TrackHandlerQueue constructor(val settings: TrackerSettings, looper: Looper) :
    Handler(looper) {

    val tag = TrackHandlerQueue::class.java.simpleName

    private companion object {
        const val MSG_START_TRACK = 0
        const val MSG_STOP_TRACK = 1
        const val MSG_SAVE_LOCATION = 2
        const val MSG_SAVE_FREQUENCY = 3
        const val MSG_SAVE_AVERAGE_SPEED_DATA = 4
    }

    private var realm: Realm? = null
    private var track: TrackRecord? = null

    init {
        post {
            realm = Realm.getDefaultInstance()
            track = TrackRecordDao(realm!!).lastTrack()
            if (track == null) {
                DatabaseUtils.close(realm)
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
        }
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
}