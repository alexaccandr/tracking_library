package com.trackinglibrary.settings

import android.content.Context
import android.content.SharedPreferences
import com.trackinglibrary.prefs.BaseSettings
import java.util.concurrent.TimeUnit

class TrackerSettings(context: Context) : BaseSettings(context, "common_settings") {

    companion object {
        const val KEY_FREQUENCY = "key_frequency"
        const val KEY_RECOGNITION_STARTED = "key_recognition_started"
        const val KEY_GEOFENCE_STR = "key_geofence"
        const val KEY_GEOFENCE_PATHSENSE_STR = "key_geofence_pathsense"
        const val KEY_GEOFENCE_RADIUS = "key_geofence_radius"
        const val KEY_STILL = "key_still"
        const val KEY_STILL_REGISTERED = "key_still_registered"
    }

    private val listeners = mutableListOf<SharedPreferences.OnSharedPreferenceChangeListener>()

    fun setFrequency(freq: Long) {
        set(KEY_FREQUENCY, freq)
    }

    fun getFrequency(): Long = get(KEY_FREQUENCY, TimeUnit.MINUTES.toMillis(1))

    fun registerFrequencyChangedListener(listener: (Long) -> Unit) {
        val prefListener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == KEY_FREQUENCY) {
                    listener(getFrequency())
                }
            }
        preferences.registerOnSharedPreferenceChangeListener(prefListener)
        listeners.add(prefListener)
    }


    fun unregisterListeners() {
        listeners.forEach {
            preferences.unregisterOnSharedPreferenceChangeListener(it)
        }
    }

    fun isRecognitionStarted(): Boolean {
        return preferences.getBoolean(KEY_RECOGNITION_STARTED, false)
    }

    fun setRecognitionStarted(value: Boolean) {
        set(KEY_RECOGNITION_STARTED, value)
    }

    fun setGeofenceStr(value: String) {
        set(KEY_GEOFENCE_STR, value)
    }

    fun getGeofenceStr(): String {
        return get(KEY_GEOFENCE_STR, "")
    }

    fun setGeofencePathsenseStr(value: String) {
        set(KEY_GEOFENCE_PATHSENSE_STR, value)
    }

    fun getGeofencePathsenseStr(): String {
        return get(KEY_GEOFENCE_PATHSENSE_STR, "")
    }

    fun getGeofenceRadius(): Int {
        return get(KEY_GEOFENCE_RADIUS, 100)
    }

    fun setGeofenceRadius(value: Int) {
        set(KEY_GEOFENCE_RADIUS, value)
    }

    fun getStill(): Int {
        return get(KEY_STILL, 7)
    }

    fun setStill(value: Int) {
        set(KEY_STILL, value)
    }

    fun isStillRegistered(): Boolean {
        return get(KEY_STILL_REGISTERED, false)
    }

    fun setStillRegistered(value: Boolean) {
        set(KEY_STILL_REGISTERED, value)
    }
}