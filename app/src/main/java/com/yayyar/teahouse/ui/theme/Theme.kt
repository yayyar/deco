package com.yayyar.teahouse.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryLight,
    onPrimary = Color.Black,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = Color.White,
    secondary = SecondaryLight,
    onSecondary = Color.Black,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceCardDark,
    onSurface = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF94A3B8),
    error = AccentRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryDark,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = PrimaryDark,
    secondary = SecondaryDark,
    onSecondary = Color.White,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceCardLight,
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF475569),
    error = AccentRed,
    onError = Color.White
)

@Composable
fun DecoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeSeed: String = "TEAL",
    themeStyle: String = "TONAL_SPOT",
    content: @Composable () -> Unit
) {
    val seed = ThemeSeed.fromId(themeSeed)
    val style = ThemeStyle.fromId(themeStyle)
    val colorScheme = ThemePalette.generateColorScheme(seed, style, darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}