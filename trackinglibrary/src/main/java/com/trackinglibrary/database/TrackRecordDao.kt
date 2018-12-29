package com.trackinglibrary.database

import io.realm.Realm
import io.realm.RealmResults
import java.util.*

internal class TrackRecordDao(val realm: Realm) {

    fun lastTrackId(): String? = realm.where(TrackRecord::class.java).isNull("finishDate").findFirst()?.id

    fun hasStartedTrack(): Boolean {
        val it = realm.where(TrackRecord::class.java).isNull("finishDate").findFirst()
        return it != null
    }

    fun createTrack(startTime: Long): TrackRecord {
        val idStr = UUID.randomUUID().toString()
        val item = TrackRecord(id = idStr, startDate = startTime)
        return realm.copyToRealm(item)
    }

    fun stopTrack(id: String, stopTime: Long) {
        val it = realm.where(TrackRecord::class.java).equalTo("id", id).findFirst()
        it!!.finishDate = stopTime
    }

    fun saveLocation(id: String, latitude: Double, longitude: Double, time: Long) {

        val newLocation = realm.copyToRealm(TrackLocations(latitude, longitude, time))

        val it = realm.where(TrackRecord::class.java).equalTo("id", id).findFirst()
        it!!.locations.add(newLocation)
    }

    fun updateAverageSpeed(track: TrackRecord, totalTime: Long, totalDistance: Double) {
        track.totalTime = totalTime
        track.totalDistance = totalDistance
    }

    fun selectTrack(id: String): TrackRecord {
        return realm.where(TrackRecord::class.java).equalTo("id", id).findFirst()!!
    }

    fun selectTracks(): RealmResults<TrackRecord> {
        return realm.where(TrackRecord::class.java).findAll()
    }

    fun lastTrack(): TrackRecord? = realm.where(TrackRecord::class.java).isNull("finishDate").findFirst()

    fun lastLocationTime(): Long {
        val lastTrack = lastTrack()
        return lastTrack?.locations?.maxBy { it.date }?.date ?: 0L
    }
}