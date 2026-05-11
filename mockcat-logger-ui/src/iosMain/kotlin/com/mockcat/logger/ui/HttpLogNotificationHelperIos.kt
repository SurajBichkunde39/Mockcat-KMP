package com.mockcat.logger.ui

import com.mockcat.api.http.LoggedHttpCall
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter

internal object HttpLogNotificationHelperIos {
    private const val NOTIFICATION_ID = "mockcat_http_log"

    fun show(calls: List<LoggedHttpCall>) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.removeDeliveredNotificationsWithIdentifiers(listOf(NOTIFICATION_ID))

        val content = UNMutableNotificationContent()
        val count = calls.size
        // K/N exposes title/body as val (inherited from readonly parent UNNotificationContent).
        // The ObjC setTitle:/setBody: selectors are available as direct methods.
        content.setTitle("Mockcat — $count request${if (count == 1) "" else "s"}")
        content.setBody(calls.first().notificationLine())

        val request = UNNotificationRequest.requestWithIdentifier(NOTIFICATION_ID, content, null)
        center.addNotificationRequest(request) { error ->
            if (error != null) println("Mockcat: notification post failed — ${error.localizedDescription}")
        }
    }

    fun cancel() {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.removeDeliveredNotificationsWithIdentifiers(listOf(NOTIFICATION_ID))
        center.removePendingNotificationRequestsWithIdentifiers(listOf(NOTIFICATION_ID))
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
