package com.mockcat.logger.ui

import com.mockcat.logger.HttpLogReaderRegistry
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private var notificationsInstalled = false

internal fun installNotificationsForIos() {
    if (notificationsInstalled) return
    notificationsInstalled = true
    MainScope().launch {
        val reader = HttpLogReaderRegistry.currentFlow.filterNotNull().first()
        reader.observeLogs().collect { calls ->
            if (calls.isNotEmpty()) {
                HttpLogNotificationHelperIos.show(calls)
            } else {
                HttpLogNotificationHelperIos.cancel()
            }
        }
    }
}
