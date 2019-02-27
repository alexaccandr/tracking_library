package com.trackinglibrary.prefs

import android.content.Context
import android.content.SharedPreferences

interface SettingsControllerListener {
    fun onChange(context: Context, sharedPreferences: SharedPreferences, key: String)
}