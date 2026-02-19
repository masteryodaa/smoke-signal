package com.smokesignal.app

import android.app.Application
import com.smokesignal.app.service.NotificationHelper

class SmokeSignalApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
    }
}
