package com.trackinglibrary.database

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class TrackRecord(

    @PrimaryKey
    var id: String = "",
    var startDate: Long = 0L,
    var finishDate: Long? = null,
    var totalDistance: Double = 0.0,
    var totalTime: Long = 0L,
    var locations: RealmList<TrackLocations> = RealmList()
) : RealmObject()