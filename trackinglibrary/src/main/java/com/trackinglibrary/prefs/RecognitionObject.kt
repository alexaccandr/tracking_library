package com.trackinglibrary.prefs

import com.google.android.gms.location.DetectedActivity
import java.util.*

data class RecognitionObject(val time: Long, val list: ArrayList<DetectedActivity>)