package com.mockcat.logger.ui

private var overlayInstalled = false

internal fun installOverlayForIos() {
    if (overlayInstalled) return
    overlayInstalled = true
    HttpLogOverlayIos.show()
}
