package com.mockcat.logger.ui

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * Presents the same [HttpLogListContentFromRegistry] as the Android [HttpLogListActivity], for SwiftUI
 * or UIKit: `present(createHttpLogListViewController(), animated: true)`.
 */
// TODO: Verify curl share sheet works on a real iOS device — not yet tested.
fun createHttpLogListViewController(): UIViewController = ComposeUIViewController {
    HttpLogListContentFromRegistry(onShareCurl = { text -> showShareSheet(text) })
}
