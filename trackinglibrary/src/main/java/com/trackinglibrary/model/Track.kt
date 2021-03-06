package com.trackinglibrary.model

class Track(
    val id: String,
    var startDate: Long = 0L,
    var finishDate: Long? = null,
    val averageSpeed: Double = 0.0,
    var locations: Array<TrackPoint> = arrayOf()
)