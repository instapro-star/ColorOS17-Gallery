package com.coloros.gallery17.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

enum class AppThemeMode {
    SYSTEM,
    FORCE_LIGHT,
    FORCE_DARK
}

val LocalThemeMode = compositionLocalOf { mutableStateOf(AppThemeMode.SYSTEM) }

private val ColorOSLightScheme = lightColorScheme(
    primary = Color(0xFF0066FF),
    onPrimary = Color.White,
    background = Color(0xFFF7F8FA),
    onBackground = Color(0xFF111318),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111318),
    surfaceVariant = Color(0xFFECEFF4),
    onSurfaceVariant = Color(0xFF5A606E)
)

private val ColorOSDarkScheme = darkColorScheme(
    primary = Color(0xFF3B82F6),
    onPrimary = Color.White,
    background = Color(0xFF0A0B0E),
    onBackground = Color(0xFFF3F4F6),
    surface = Color(0xFF151820),
    onSurface = Color(0xFFF3F4F6),
    surfaceVariant = Color(0xFF222632),
    onSurfaceVariant = Color(0xFF9CA3AF)
)

@Composable
fun ColorOS17Theme(
    themeMode: AppThemeMode,
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.FORCE_LIGHT -> false
        AppThemeMode.FORCE_DARK -> true
    }

    MaterialTheme(
        colorScheme = if (useDark) ColorOSDarkScheme else ColorOSLightScheme,
        content = content
    )
}
