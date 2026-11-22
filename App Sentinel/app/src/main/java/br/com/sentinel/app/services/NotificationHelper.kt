package br.com.sentinel.app.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import br.com.sentinel.app.R

object NotificationHelper {

    private const val CHANNEL_ID_ALERT = "sentinel_alertas"
    private const val CHANNEL_ID_OFFICIAL = "sentinel_oficial"
    private var counter = 1000

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID_ALERT,
                    context.getString(R.string.channel_alerts_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = context.getString(R.string.channel_alerts_description) }
            )

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID_OFFICIAL,
                    context.getString(R.string.channel_official_name),
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = context.getString(R.string.channel_official_description)
                }
            )
        }
    }

    fun sendNotification(context: Context, source: String, reasons: List<String>, originalContent: String) {
        val reasonsList = reasons.joinToString("\n") { "- $it" }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ALERT)
            .setSmallIcon(R.drawable.sentinel_logo)
            .setContentTitle(context.getString(R.string.notification_scam_title, source))
            .setContentText(reasons.firstOrNull() ?: context.getString(R.string.notification_scam_fallback))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(context.getString(R.string.notification_scam_body, originalContent, reasonsList))
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setColor(Color.parseColor("#3857F5"))
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        if (hasPermission(context)) {
            NotificationManagerCompat.from(context).notify(counter++, notification)
        }
    }

    fun sendOfficialNotification(context: Context, source: String, institution: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_OFFICIAL)
            .setSmallIcon(R.drawable.sentinel_logo)
            .setContentTitle(context.getString(R.string.notification_official_title))
            .setContentText(
                context.getString(R.string.notification_official_body, source, institution)
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setColor(Color.parseColor("#2E7D32"))
            .setAutoCancel(true)
            .build()

        if (hasPermission(context)) {
            NotificationManagerCompat.from(context).notify(counter++, notification)
        }
    }

    private fun hasPermission(context: Context): Boolean = ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED ||
        Build.VERSION.SDK_INT < 33
}
