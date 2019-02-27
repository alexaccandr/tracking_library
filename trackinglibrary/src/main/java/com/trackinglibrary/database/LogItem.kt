package com.trackinglibrary.database

import io.realm.RealmObject

open class LogItem(
    var date: Long = 0L,
    var type: Int = 0,
    var data: String = ""
) : RealmObject() {
    companion object {
        enum class Type(val typeId: Int) {
            TYPE_ACCELEROMETER(0),
            TYPE_TRIP_STATUS(1), /*trip start/stop info*/
            TYPE_SPEED(2) /*trip start/stop info*/
        }
    }
}