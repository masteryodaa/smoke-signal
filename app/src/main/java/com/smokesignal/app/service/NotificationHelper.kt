package com.smokesignal.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.smokesignal.app.MainActivity
import com.smokesignal.app.R

object NotificationHelper {

    const val SERVICE_NOTIFICATION_ID = 1
    private const val SIGNAL_NOTIFICATION_ID = 100

    private const val SERVICE_CHANNEL = "smoke_signal_service"
    private const val ALERT_CHANNEL = "smoke_signal_alerts"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = context.getSystemService(NotificationManager::class.java)

            mgr.createNotificationChannel(NotificationChannel(
                SERVICE_CHANNEL, "Background Service", NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps SmokeSignal connected to nearby friends"
                setShowBadge(false)
            })

            mgr.createNotificationChannel(NotificationChannel(
                ALERT_CHANNEL, "Smoke Signals", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when a friend sends a smoke signal"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 150, 80, 150, 80, 400)
            })
        }
    }

    fun createPersistentNotification(context: Context): Notification {
        createChannels(context)
        val pi = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(context, SERVICE_CHANNEL)
            .setContentTitle("SmokeSignal")
            .setContentText("Listening for friends nearby\u2026")
            .setSmallIcon(R.drawable.ic_smoke)
            .setContentIntent(pi)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    fun showSmokeSignal(context: Context, fromNickname: String, message: String) {
        createChannels(context)
        val pi = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val n = NotificationCompat.Builder(context, ALERT_CHANNEL)
            .setContentTitle("\uD83D\uDD25 $fromNickname")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_smoke)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        runCatching { NotificationManagerCompat.from(context).notify(SIGNAL_NOTIFICATION_ID, n) }
    }
}
