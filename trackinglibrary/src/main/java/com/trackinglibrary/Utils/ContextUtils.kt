package com.trackinglibrary.Utils

import android.content.Context
import android.content.Intent
import android.os.Build

object ContextUtils {

    fun startService(context: Context, serviceClass: Class<*>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(Intent(context, serviceClass))
        } else {
            context.startService(Intent(context, serviceClass))
        }
    }

    fun stopService(context: Context, serviceClass: Class<*>) {
        context.stopService(Intent(context, serviceClass))
    }
}