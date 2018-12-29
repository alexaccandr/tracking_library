package com.trackinglibrary.model

import com.trackinglibrary.Utils.SpeedUtils
import com.trackinglibrary.database.TrackRecord

object ModelAdapter {

    fun adaptTrack(item: TrackRecord): Track {
        return Track(
            item.id,
            item.startDate,
            item.finishDate,
            SpeedUtils.calcAverageSpeed(item.totalTime, item.totalDistance),
            item.locations.map {
                TrackPoint(
                    item.id,
                    it.lat,
                    it.lon,
                    it.date
                )
            }.toTypedArray()
        )
    }

    fun adaptTracks(item: List<TrackRecord>): Array<Track> {
        return item.map {
            adaptTrack(it)
        }.toTypedArray()
    }
}