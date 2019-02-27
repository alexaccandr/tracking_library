package com.trackinglibrary.prefs


import android.content.Context
import android.content.SharedPreferences
import java.util.*

class SettingsController(
        private val context: Context,
        private val listener: SettingsControllerListener,
        private val prefs: SharedPreferences,
        vararg keys: String
) : SharedPreferences.OnSharedPreferenceChangeListener {

    private val keysSet = HashSet<String>()

    init {
        keysSet.addAll(Arrays.asList(*keys))
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        for (k in keysSet) {
            if (key.equals(k, true)) {
                try {
                    listener.onChange(context, sharedPreferences, key)
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
                break
            }
        }
    }

    fun unregisterListeners() {
        try {
            prefs.unregisterOnSharedPreferenceChangeListener(this)
        } catch (e: Throwable) {
            e.printStackTrace()
            //ignored
        }
    }
}
