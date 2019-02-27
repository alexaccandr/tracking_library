package com.kite.model.settings

import android.content.Context
import android.content.SharedPreferences
import java.util.concurrent.TimeUnit

class TrackerSettings(context: Context) : BaseSettings(context, "common_settings") {

    companion object {
        const val KEY_FREQUENCY = "key_frequency"
        const val KEY_RECOGNITION_STARTED = "key_recognition_started"
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
}