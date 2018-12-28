package com.trackinglibrary.database

import io.realm.RealmObject

class TrackLocations(
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val date: Long
) : RealmObject()