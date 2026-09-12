package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val HoloDarkColorScheme = darkColorScheme(
    primary = HoloBlueLight,
    onPrimary = Color.Black,
    primaryContainer = HoloBlueDark,
    onPrimaryContainer = Color.White,
    secondary = HoloBlueDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1F2E3D),
    onSecondaryContainer = HoloBlueLight,
    tertiary = HoloGreen,
    onTertiary = Color.Black,
    background = HoloDarkBg,
    onBackground = HoloTextPrimary,
    surface = HoloDarkSurface,
    onSurface = HoloTextPrimary,
    surfaceVariant = HoloDarkSurfaceVariant,
    onSurfaceVariant = HoloTextSecondary,
    outline = HoloDivider,
    outlineVariant = Color(0xFF383838),
    error = HoloRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to true as requested
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = HoloDarkColorScheme,
        typography = Typography,
        content = content
    )
}

