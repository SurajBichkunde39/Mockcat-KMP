package com.mockcat.logger.ui

import androidx.compose.runtime.Composable

@Composable
internal expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
