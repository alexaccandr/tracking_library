package com.trackinglibrary

import android.location.Location
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.kite.model.settings.TrackerSettings
import com.trackinglibrary.Utils.DatabaseUtils
import com.trackinglibrary.Utils.RxBus
import com.trackinglibrary.Utils.SpeedUtils
import com.trackinglibrary.database.TrackRecordDao
import com.trackinglibrary.model.TrackAverageSpeed
import com.trackinglibrary.model.TrackStatus
import io.realm.Realm

internal class TrackHandlerQueue(val settings: TrackerSettings, looper: Looper) : Handler(looper) {

    private companion object {
        const val MSG_START_TRACK = 0
        const val MSG_STOP_TRACK = 1
        const val MSG_LOCATION = 2
        const val MSG_FREQUENCY = 3
        const val MSG_AVERAGE_SPEED_DATA = 4
    }

    private var realm: Realm? = null
    private var dao: TrackRecordDao? = null
    private var trackId: String? = null

    override fun handleMessage(msg: Message?) {
        super.handleMessage(msg)

        if (msg == null) {
            return
        }

        when (msg.what) {
            MSG_START_TRACK -> {
                if (trackId == null) {
                    realm = Realm.getDefaultInstance()
                    dao = TrackRecordDao(realm!!)
                    val startTime = msg.obj as Long

                    // open track
                    DatabaseUtils.executeTransaction(realm!!, Realm.Transaction {
                        trackId = dao!!.createTrack(startTime)
                    })

                    notifyTrackStarted(true)
                }
            }
            MSG_STOP_TRACK -> {
                if (trackId != null) {
                    val stopTime = msg.obj as Long

                    // close track
                    stopTrack(realm!!, stopTime)
                    trackId = null

                    // close database
                    DatabaseUtils.close(realm)
                    dao = null

                    notifyTrackStarted(false)
                }
            }
            MSG_LOCATION -> {
                if (trackId != null) {
                    val location = msg.obj as Location

                    // add location
                    saveLocation(realm!!, trackId!!, location)
                }
            }
            MSG_FREQUENCY -> {
                if (trackId != null) {
                    val frequency = msg.obj as Long

                    // update frequency
                    saveFrequency(frequency)
                }
            }
            MSG_AVERAGE_SPEED_DATA -> {
                if (trackId != null) {
                    val data = msg.obj as Pair<Long, Double>

                    // update average speed data
                    updateAverageSpeed(realm!!, trackId!!, data)

                    val track = dao!!.selectTrack(trackId!!)
                    val averageSpeed =
                        SpeedUtils.calcAverageSpeed(track.totalTime, track.totalDistance)
                    notifyAverageSpeedChanged(averageSpeed)
                }
            }
        }
    }

    private fun stopTrack(realm: Realm, stopTime: Long) {
        DatabaseUtils.executeTransaction(realm, Realm.Transaction {
            dao!!.stopTrack(trackId!!, stopTime)
        })
    }

    private fun saveLocation(realm: Realm, trackId: String, location: Location) {
        DatabaseUtils.executeTransaction(realm, Realm.Transaction {
            dao!!.saveLocation(trackId, location.latitude, location.longitude, location.time)
        })
    }

    private fun saveFrequency(frequency: Long) {
        settings.executeTransaction {
            settings.setFrequency(frequency)
        }
    }

    private fun updateAverageSpeed(realm: Realm, trackId: String, data: Pair<Long, Double>) {
        val track = dao!!.selectTrack(trackId)

        DatabaseUtils.executeTransaction(realm, Realm.Transaction {
            dao!!.updateAverageSpeed(
                track,
                track.totalTime + data.first,
                track.totalDistance + data.second
            )
        })
    }

    private fun notifyAverageSpeedChanged(averageSpeed: Double) {
        RxBus.publish(TrackAverageSpeed(averageSpeed))
    }

    private fun notifyTrackStarted(started: Boolean) {
        RxBus.publish(TrackStatus(started))
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
        message.what = TrackHandlerQueue.MSG_LOCATION
        message.obj = location
        sendMessage(message)
    }

    internal fun putQueueUpdateFrequency(frequency: Long) {
        val message = Message()
        message.what = TrackHandlerQueue.MSG_FREQUENCY
        message.obj = frequency
        sendMessage(message)
    }

    fun putQueueUpdateAverageSpeed(data: Pair<Long, Double>) {
        val message = Message()
        message.what = TrackHandlerQueue.MSG_AVERAGE_SPEED_DATA
        message.obj = data
        sendMessage(message)
    }
}