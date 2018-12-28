package com.trackinglibrary.database

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

class TrackRecord(

    @PrimaryKey
    var id: String = "",
    var startDate: Long = 0L,
    var finishDate: Long? = null,
    var totalDistance: Double = 0.0,
    var totalTime: Long = 0L,
    var locations: RealmList<TrackLocations> = RealmList()
) : RealmObject() {
    // The Kotlin compiler generates standard getters and setters.
    // Realm will overload them and code inside them is ignored.
    // So if you prefer you can also just have empty abstract methods.
}