package com.mockcat.logger.ui

import com.mockcat.logger.HttpLogReaderRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import platform.Foundation.NSNotificationCenter

private var overlayInstalled = false
private val observerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
private var observerToken: Any? = null

internal fun installOverlayForIos() {
    if (overlayInstalled) return
    overlayInstalled = true

    // Receive the cross-framework write signal posted by the URLSession module and update the
    // in-app overlay. NSNotificationCenter.defaultCenter() is a genuine ObjC process-wide
    // singleton — it bridges across the two separate Kotlin runtime instances safely.
    observerToken = NSNotificationCenter.defaultCenter().addObserverForName(
        name = "com.mockcat.httpLogWritten",
        `object` = null,
        queue = null,
    ) { _ ->
        observerScope.launch {
            val reader = HttpLogReaderRegistry.currentOrNull() ?: return@launch
            val calls = reader.observeLogs().first()
            if (calls.isNotEmpty()) {
                HttpLogOverlayIos.show(calls.size)
            }
        }
    }
}
