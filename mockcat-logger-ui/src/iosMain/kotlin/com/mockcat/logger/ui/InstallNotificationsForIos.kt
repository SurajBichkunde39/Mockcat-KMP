package com.mockcat.logger.ui

import com.mockcat.logger.HttpLogReaderRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import platform.Foundation.NSNotificationCenter

private var notificationsInstalled = false
private val notificationScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
private var observerToken: Any? = null

internal fun installNotificationsForIos() {
    if (notificationsInstalled) return
    notificationsInstalled = true

    // NSNotificationCenter is an ObjC singleton from Foundation.framework — it is truly
    // process-wide and not duplicated per Kotlin framework (unlike Kotlin `object` singletons
    // which are per-dylib). The URLSession framework posts here after each store.emit(); we
    // receive it here in the UI framework and do a one-shot SQL read to get the full call list.
    observerToken = NSNotificationCenter.defaultCenter().addObserverForName(
        name = "com.mockcat.httpLogWritten",
        `object` = null,
        queue = null,
    ) { _ ->
        notificationScope.launch {
            val reader = HttpLogReaderRegistry.currentOrNull() ?: return@launch
            // One-shot collect: store.emit() already committed the row before the notification
            // was posted, so this SELECT is guaranteed to include the new entry.
            val calls = reader.observeLogs().first()
            if (calls.isNotEmpty()) {
                val latestCall = calls.last()
                HttpLogNotificationHelperIos.show(latestCall, calls.size)
            }
        }
    }
}
