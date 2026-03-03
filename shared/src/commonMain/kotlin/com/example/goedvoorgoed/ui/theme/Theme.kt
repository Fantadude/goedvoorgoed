package com.example.goedvoorgoed.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = LightBlue,
    secondary = LightBlueDark,
    tertiary = SkyBlue,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = DarkText,
    onSecondary = DarkText,
    onTertiary = DarkText,
    onBackground = DarkThemeText,
    onSurface = DarkThemeText,
    onSurfaceVariant = DarkThemeTextSecondary,
    outline = DarkThemeTextTertiary,
    error = Color(0xFFCF6679),
    onError = White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = PrimaryBlueDark,
    tertiary = AccentBlue,
    background = BackgroundWhite,
    surface = SurfaceWhite,
    onPrimary = DarkText,
    onSecondary = DarkText,
    onTertiary = White,
    onBackground = DarkText,
    onSurface = DarkText
)

@Composable
fun GoedvoorgoedTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
