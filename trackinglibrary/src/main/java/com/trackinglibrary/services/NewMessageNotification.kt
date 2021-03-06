package com.trackinglibrary.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.provider.Settings
import android.support.v4.app.NotificationCompat
import com.trackinglibrary.R


object NewMessageNotification {


    val NOTIFICATION_CHANNEL_ID = "10001"

    /**
     * Create and push the notification
     */
    fun createNotification(mContext: Context, title: String, message: String) {

        val mNotificationManager: NotificationManager?
        val mBuilder: NotificationCompat.Builder?

        /**Creates an explicit intent for an Activity in your app */
//        val resultIntent = Intent()
//        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//
//        val resultPendingIntent = PendingIntent.getActivity(
//            mContext,
//            0 /* Request code */, resultIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT
//        )
        val resultIntent = Intent("SUPER_ACTION")

        val resultPendingIntent = PendingIntent.getBroadcast(
            mContext,
            0 /* Request code */, resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        mBuilder = NotificationCompat.Builder(mContext)
        mBuilder.setSmallIcon(R.drawable.ic_stat_new_message)
        mBuilder.setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(false)
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
            .setContentIntent(resultPendingIntent)

        mNotificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel =
                NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
//            notificationChannel.enableVibration(true)
//            notificationChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
//            assert(mNotificationManager != null)
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID)
            mNotificationManager.createNotificationChannel(notificationChannel)
        }
        mNotificationManager.notify(0 /* Request Code */, mBuilder.build())
    }
}
