package com.trackinglibrary.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import com.trackinglibrary.R

object NotificationUtils {

    const val FOREGROUND_SERVICE_ID = 123

    fun createOrUpdateTrackerNotification(context: Context): Notification {

        val icon1 = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)

        var mBuilder: Notification.Builder = Notification.Builder(context)
            .setContentTitle("Tracker")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(icon1)
            .setOngoing(true)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            val CHANNEL_ONE_ID = "channel_id_1"
            val CHANNEL_ONE_NAME = "channel_id_name_1"
            val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            var notificationChannel = manager.getNotificationChannel(CHANNEL_ONE_ID)
            if (notificationChannel == null) {
                notificationChannel = NotificationChannel(
                    CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME,
                    IMPORTANCE_LOW
                )
                notificationChannel.enableLights(true)
                notificationChannel.lightColor = Color.RED
                notificationChannel.setShowBadge(true)
                notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                manager.createNotificationChannel(notificationChannel)
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                mBuilder = mBuilder.setChannelId(CHANNEL_ONE_ID)
            }
        }

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        mBuilder = mBuilder.setSound(soundUri)


        val mNotificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val myNotification: Notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            myNotification = mBuilder.build()
        } else {
            myNotification = mBuilder.notification
        }

        mNotificationManager.notify(FOREGROUND_SERVICE_ID, myNotification)
        return myNotification
    }
}