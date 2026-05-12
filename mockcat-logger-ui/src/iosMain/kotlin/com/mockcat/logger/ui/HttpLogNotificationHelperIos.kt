package com.mockcat.logger.ui

import com.mockcat.api.http.LoggedHttpCall
import platform.Foundation.NSError
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter

internal object HttpLogNotificationHelperIos {
    private const val NOTIFICATION_ID = "mockcat_http_log"

    // K/N exposes title/body as val (inherited from readonly UNNotificationContent).
    // setTitle:/setBody: ObjC selectors are available as direct methods on the mutable subclass.
    private val center = UNUserNotificationCenter.currentNotificationCenter()

    fun show(calls: List<LoggedHttpCall>) {
        center.removeDeliveredNotificationsWithIdentifiers(listOf(NOTIFICATION_ID))

        val count = calls.size
        val content = UNMutableNotificationContent().apply {
            setTitle("Mockcat — $count request${if (count == 1) "" else "s"}")
            setBody(calls.first().notificationLine())
            setSound(UNNotificationSound.defaultSound())
        }

        val request = UNNotificationRequest.requestWithIdentifier(NOTIFICATION_ID, content, null)
        center.addNotificationRequest(request) { error: NSError? ->
            if (error != null) println("Mockcat: notification post failed — ${error.localizedDescription}")
        }
    }

    fun cancel() {
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
