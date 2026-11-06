package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ZorvenDarkColorScheme = darkColorScheme(
    primary = ZorvenCrimson,
    onPrimary = ZorvenTextPrimary,
    primaryContainer = ZorvenCrimsonDark,
    onPrimaryContainer = ZorvenTextPrimary,
    secondary = ZorvenWarmGold,
    onSecondary = ZorvenObsidian,
    tertiary = ZorvenCoolSilver,
    background = ZorvenObsidian,
    onBackground = ZorvenTextPrimary,
    surface = ZorvenSurface,
    onSurface = ZorvenTextPrimary,
    surfaceVariant = ZorvenSurfaceElevated,
    onSurfaceVariant = ZorvenTextSecondary,
    outline = ZorvenBorderSubtle,
    error = ZorvenErrorRed,
    onError = ZorvenTextPrimary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ZorvenDarkColorScheme,
        typography = Typography,
        content = content
    )
}
