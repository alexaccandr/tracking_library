package com.trackinglibrary.database

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class MyTrackTransition(

    @PrimaryKey
    var id: String = "",
    var date: Long = 0L,
    var type: Int = 0
) : RealmObject()