package com.trackinglibrary.database

import io.realm.RealmObject

open class TrackLocations(
    var lat: Double = 0.0,
    var lon: Double = 0.0,
    var date: Long = 0
) : RealmObject()