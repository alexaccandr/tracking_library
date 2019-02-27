package com.trackinglibrary.prefs

import android.content.Context

class RealTimeSettings(context: Context) : BaseSettings(context, "azaza") {

    fun setVibration(value: Boolean) {
        set(SETTING_VIBRATION, value)
    }


    fun setData(value: String) {
        set(SETTING_DATA, value)
    }

    fun getVibration(): Boolean = get(SETTING_VIBRATION, false)

    fun getData(): String = get(SETTING_DATA, "")

    fun setLocationV2(locationSerialized: String) {
        set(SETTING_LOCATION_v2, locationSerialized)
    }

    fun getLocationV2(): String {
        return get(SETTING_LOCATION_v2, "")
    }

    fun setLastTransition(type: Int) {
        set(SETTING_LAST_TRANSITION, type)
    }

    fun getLastTransition(): Int {
        return get(SETTING_LAST_TRANSITION, -1)
    }

    fun setLastInVehicleConf(value: Int) {
        set(SETTING_LAST_IN_VEHICLE_CONF, value)
    }

    fun setLastStillConf(value: Int) {
        set(SETTING_LAST_STILL_CONF, value)
    }

    fun getLastInVehicleConf(): Int {
        return get(SETTING_LAST_IN_VEHICLE_CONF, 0)
    }

    fun getLastStillConf(): Int {
        return get(SETTING_LAST_STILL_CONF, 0)
    }
}