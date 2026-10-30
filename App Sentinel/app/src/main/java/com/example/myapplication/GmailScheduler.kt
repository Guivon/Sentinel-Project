package com.example.myapplication

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object GmailScheduler {

    fun agendar(context: Context) {
        val request = PeriodicWorkRequestBuilder<GmailCheckWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "gmail_check",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}