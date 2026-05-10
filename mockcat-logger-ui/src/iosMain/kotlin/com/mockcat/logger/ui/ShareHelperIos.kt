package com.mockcat.logger.ui

import platform.UIKit.UIPasteboard

internal fun showShareSheet(text: String) {
    UIPasteboard.generalPasteboard.string = text
}
