package com.mockcat.logger.ui

import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

internal fun showShareSheet(text: String) {
    val activityVC = UIActivityViewController(
        activityItems = listOf(text),
        applicationActivities = null,
    )

    // Traverse connected scenes to find the active key window's root view controller (iOS 13+).
    // Falls back to the deprecated keyWindow for edge cases.
    @Suppress("DEPRECATION")
    val rootVC = UIApplication.sharedApplication
        .connectedScenes
        .filterIsInstance<platform.UIKit.UIWindowScene>()
        .firstOrNull()
        ?.windows
        ?.firstOrNull { it.isKeyWindow }
        ?.rootViewController
        ?: UIApplication.sharedApplication.keyWindow?.rootViewController

    // iPad: UIActivityViewController must have a popover anchor or it crashes.
    activityVC.popoverPresentationController?.let { popover ->
        popover.sourceView = rootVC?.view
        popover.permittedArrowDirections = 0u
    }

    rootVC?.presentViewController(activityVC, animated = true, completion = null)
}
