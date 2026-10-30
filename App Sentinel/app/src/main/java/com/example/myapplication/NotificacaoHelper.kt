package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificacaoHelper {

    private const val CHANNEL_ID_ALERTA = "sentinel_alertas"
    private const val CHANNEL_ID_OFICIAL = "sentinel_oficial"
    private var contador = 1000

    fun criarCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID_ALERTA, "Alertas do Sentinel", NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Avisos sobre possíveis golpes detectados" }
            )

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID_OFICIAL, "Contatos oficiais", NotificationManager.IMPORTANCE_LOW
                ).apply { description = "Confirmações de contato oficial verificado" }
            )
        }
    }

    fun notificar(context: Context, origem: String, motivos: List<String>, textoOriginal: String) {
        val listaMotivos = motivos.joinToString("\n") { "- $it" }

        val notificacao = NotificationCompat.Builder(context, CHANNEL_ID_ALERTA)
            .setSmallIcon(R.drawable.sentinel_logo)
            .setContentTitle("⚠️ Possível golpe em $origem")
            .setContentText(motivos.firstOrNull() ?: "Verifique os detalhes")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Mensagem: \"$textoOriginal\"\n\nMotivo:\n$listaMotivos")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setColor(Color.parseColor("#3857F5"))
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        if (temPermissao(context)) {
            NotificationManagerCompat.from(context).notify(contador++, notificacao)
        }
    }

    fun notificarOficial(context: Context, origem: String, instituicao: String) {
        val notificacao = NotificationCompat.Builder(context, CHANNEL_ID_OFICIAL)
            .setSmallIcon(R.drawable.sentinel_logo)
            .setContentTitle("✅ Contato oficial verificado")
            .setContentText("$origem é um canal oficial de $instituicao")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setColor(Color.parseColor("#2E7D32"))
            .setAutoCancel(true)
            .build()

        if (temPermissao(context)) {
            NotificationManagerCompat.from(context).notify(contador++, notificacao)
        }
    }

    private fun temPermissao(context: Context): Boolean {
        return androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.POST_NOTIFICATIONS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < 33
    }
}