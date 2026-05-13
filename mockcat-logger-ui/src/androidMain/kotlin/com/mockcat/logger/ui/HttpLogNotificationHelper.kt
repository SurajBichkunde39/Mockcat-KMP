package com.mockcat.logger.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mockcat.api.http.LoggedHttpCall

internal object HttpLogNotificationHelper {
    private const val CHANNEL_ID = "mockcat_http_log_v2"
    private const val NOTIFICATION_ID = 7734

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Mockcat HTTP Logs",
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    fun show(context: Context, calls: List<LoggedHttpCall>) {
        val nm = NotificationManagerCompat.from(context)
        if (!nm.areNotificationsEnabled()) return

        val launchIntent = MockcatLoggerUi.createLaunchIntent(context)
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val displayCalls = calls.take(10)
        val inboxStyle = NotificationCompat.InboxStyle()
        displayCalls.forEach { inboxStyle.addLine(it.notificationLine()) }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Mockcat — ${calls.size} request${if (calls.size == 1) "" else "s"}")
            .setContentText(displayCalls.first().notificationLine())
            .setStyle(inboxStyle)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setLocalOnly(true)
            .setOnlyAlertOnce(true)
            .build()

        nm.notify(NOTIFICATION_ID, notification)
    }

    fun cancel(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    private fun LoggedHttpCall.notificationLine(): String {
        val status = when {
            response != null -> "${response!!.statusCode}"
            error != null -> "ERR"
            else -> "···"
        }
        val path = "/" + request.url.substringAfter("://").substringAfter("/", "")
        return "$status ${request.method} $path"
    }
}
