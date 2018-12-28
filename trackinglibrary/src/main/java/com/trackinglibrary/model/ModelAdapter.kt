package com.trackinglibrary.model

import com.trackinglibrary.Utils.SpeedUtils
import com.trackinglibrary.database.TrackRecord

object ModelAdapter {

    fun adaptTrack(item: TrackRecord): Track {
        return Track(
            item.startDate,
            item.finishDate,
            SpeedUtils.calcAverageSpeed(item.totalTime, item.totalDistance),
            item.locations.map {
                TrackPoint(
                    it.lat,
                    it.lon,
                    it.date
                )
            }.toTypedArray()
        )
    }

    fun adaptTracks(item: Collection<TrackRecord>): Array<Track> {
        return item.map {
            adaptTrack(it)
        }.toTypedArray()
    }
}