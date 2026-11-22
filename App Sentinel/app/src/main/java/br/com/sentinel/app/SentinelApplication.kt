package br.com.sentinel.app

import android.app.Application
import br.com.sentinel.app.data.SentinelState
import br.com.sentinel.app.services.NotificationHelper

class SentinelApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SentinelState.initialize(this)
        NotificationHelper.createChannel(this)
    }
}
