package br.com.sentinel.app.services

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object GmailScheduler {

    fun schedule(context: Context) {
        val restrictions = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<GmailCheckWorker>(15, TimeUnit.MINUTES)
            .setConstraints(restrictions)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "gmail_check",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
