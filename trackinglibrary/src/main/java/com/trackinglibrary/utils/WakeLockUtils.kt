package com.trackinglibrary.utils

import android.content.Context
import android.os.PowerManager

object WakeLockUtils {

    fun acquireWakeLock(context: Context, lockTag: String): PowerManager.WakeLock? {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, lockTag)
        if (!wakeLock.isHeld) {
            wakeLock.acquire()
        }
        return wakeLock
    }

    fun releaseWakeLock(wakeLock: PowerManager.WakeLock) {
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }
}