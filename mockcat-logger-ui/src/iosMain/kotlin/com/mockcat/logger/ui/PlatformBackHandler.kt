package com.mockcat.logger.ui

import androidx.compose.runtime.Composable

// iOS has no hardware back button; the ← button in TopAppBar handles navigation.
@Composable
internal actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit
