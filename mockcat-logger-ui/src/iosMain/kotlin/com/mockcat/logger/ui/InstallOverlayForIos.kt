package com.mockcat.logger.ui

import platform.Foundation.NSNotificationCenter

private var overlayInstalled = false
private var observerToken: Any? = null

internal fun installOverlayForIos() {
    if (overlayInstalled) return
    overlayInstalled = true

    // NSNotificationCenter.defaultCenter() is a genuine ObjC process-wide singleton — it bridges
    // across the two separate Kotlin runtime instances (URLSession dylib and UI dylib) safely.
    // The write signal itself is enough to show the badge; no log read is needed.
    observerToken = NSNotificationCenter.defaultCenter().addObserverForName(
        name = "com.mockcat.httpLogWritten",
        `object` = null,
        queue = null,
    ) { _ ->
        HttpLogOverlayIos.show()
    }
}
