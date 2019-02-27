package com.trackinglibrary.prefs

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

abstract class BaseSettings(context: Context, settingsName: String) {


    companion object {
        val INT_NULL_VALUE = -1
        val LONG_NULL_VALUE = -1L
        val FLOAT_NULL_VALUE = -1f
        val SETTING_VIBRATION = "setting.vibration"
        val SETTING_DATA = "setting.data"
        val SETTING_LOCATION_v2 = "setting.location2"
        val SETTING_LAST_TRANSITION = "setting.last_transition"

        val SETTING_LAST_IN_VEHICLE_CONF = "setting.last_in_vehicle_conf"
        val SETTING_LAST_STILL_CONF = "setting.last_still_conf"
    }

    val preferences: SharedPreferences =
        context.applicationContext.getSharedPreferences(settingsName, Context.MODE_PRIVATE)

    protected lateinit var editor: SharedPreferences.Editor

    open fun clear() {
        editor.clear()
    }

    @SuppressLint("CommitPrefEdits")
    private fun beginTransaction() {
        editor = preferences.edit()
    }

    private fun commitTransaction() {
        editor.commit()
    }

    fun executeTransaction(block: (editor: SharedPreferences.Editor) -> Unit) {
        beginTransaction()
        block.invoke(editor)
        commitTransaction()
    }

    protected fun set(key: String, value: Any) {
        when (value) {
            is String -> editor.putString(key, value)
            is Int -> editor.putInt(key, value)
            is Boolean -> editor.putBoolean(key, value)
            is Float -> editor.putFloat(key, value)
            is Long -> editor.putLong(key, value)
            else -> throw UnsupportedOperationException("Not yet implemented type=" + value::class.java.simpleName)
        }
    }

    inline fun <reified T : Any> get(key: String, defaultValue: T? = null): T {
        return when (T::class) {
            String::class -> preferences.getString(key, defaultValue as? String ?: "") as T
            Int::class -> preferences.getInt(key, defaultValue as? Int ?: INT_NULL_VALUE) as T
            Boolean::class -> preferences.getBoolean(key, defaultValue as? Boolean ?: false) as T
            Float::class -> preferences.getFloat(key, defaultValue as? Float ?: FLOAT_NULL_VALUE) as T
            Long::class -> preferences.getLong(key, defaultValue as? Long ?: LONG_NULL_VALUE) as T
            else -> throw UnsupportedOperationException("Not yet implemented")
        }
    }
}