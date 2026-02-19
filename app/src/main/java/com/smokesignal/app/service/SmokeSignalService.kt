package com.smokesignal.app.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class SmokeSignalService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationHelper.createPersistentNotification(this)
        startForeground(NotificationHelper.SERVICE_NOTIFICATION_ID, notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
