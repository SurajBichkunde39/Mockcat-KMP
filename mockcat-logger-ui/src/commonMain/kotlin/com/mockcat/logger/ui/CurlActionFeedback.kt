package com.mockcat.logger.ui

// Non-null on platforms that copy to clipboard (iOS); null on platforms that open a share sheet (Android).
internal expect val curlActionFeedback: String?
