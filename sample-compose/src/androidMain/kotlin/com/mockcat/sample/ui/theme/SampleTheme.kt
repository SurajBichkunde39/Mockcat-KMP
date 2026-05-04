package com.mockcat.sample.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Simple brand tint for a polished sample (not a design system)
private val Primary = Color(0xFF1565C0)
private val Secondary = Color(0xFF5C6BC0)

private val LightColors =
    lightColorScheme(
        primary = Primary,
        onPrimary = Color.White,
        primaryContainer = Color(0xFFBBDEFB),
        onPrimaryContainer = Color(0xFF0D47A1),
        secondary = Secondary,
        surface = Color(0xFFFAFAFA),
        onSurface = Color(0xFF1C1B1F),
        error = Color(0xFFB00020),
    )

private val DarkColors =
    darkColorScheme(
        primary = Color(0xFF90CAF9),
        onPrimary = Color(0xFF0D3B5C),
        primaryContainer = Color(0xFF1565C0),
        onPrimaryContainer = Color(0xFFE3F2FD),
        secondary = Color(0xFF9FA8DA),
        surface = Color(0xFF121212),
    )

@Composable
fun SampleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors: ColorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content,
    )
}
