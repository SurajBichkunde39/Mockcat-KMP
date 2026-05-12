package com.mockcat.logger.ui

import com.mockcat.logger.HttpLogReaderRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private var notificationsInstalled = false

internal fun installNotificationsForIos() {
    if (notificationsInstalled) return
    notificationsInstalled = true
    CoroutineScope(Dispatchers.Default + SupervisorJob()).launch {
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
