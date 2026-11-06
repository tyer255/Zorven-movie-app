package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val ZorvenSplashBlack = Color(0xFF050505)
val ZorvenObsidian = Color(0xFF0A0A0C)
val ZorvenSurface = Color(0xFF141418)
val ZorvenSurfaceElevated = Color(0xFF1E1E26)
val ZorvenSurfaceHigh = Color(0xFF282833)
val ZorvenBorderSubtle = Color(0xFF2C2C38)

val ZorvenCrimson = Color(0xFFE50914)
val ZorvenCrimsonDark = Color(0xFFB20710)
val ZorvenWarmGold = Color(0xFFD4A359)
val ZorvenCoolSilver = Color(0xFF98A6B8)

val ZorvenTextPrimary = Color(0xFFF5F5F7)
val ZorvenTextSecondary = Color(0xFFA5A5B0)
val ZorvenTextMuted = Color(0xFF6E6E7A)

val ZorvenSuccessGreen = Color(0xFF46D369)
val ZorvenWarningAmber = Color(0xFFF5A623)
val ZorvenErrorRed = Color(0xFFFF453A)

fun parseHexColor(hex: String, fallback: Color = ZorvenCrimson): Color {
    return try {
        val cleaned = hex.removePrefix("#")
        val longVal = cleaned.toLong(16)
        if (cleaned.length == 6) {
            Color(longVal or 0xFF000000L)
        } else {
            Color(longVal)
        }
    } catch (_: Exception) {
        fallback
    }
}
