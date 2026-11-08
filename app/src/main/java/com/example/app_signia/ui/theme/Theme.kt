package com.example.app_signia.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = SigniaBluePrimary,
    secondary = SigniaSoftLilac,
    tertiary = SigniaBlueSecondary,
    background = SigniaDarkBg,
    surface = SigniaDarkCardBg,
    onPrimary = SigniaWhite,
    onSecondary = SigniaWhite,
    onBackground = SigniaDarkTextLight,
    onSurface = SigniaDarkTextLight
)

private val LightColorScheme = lightColorScheme(
    primary = SigniaBluePrimary,
    secondary = SigniaSoftLilac,
    tertiary = SigniaBlueSecondary,
    background = SigniaLightLavender,
    surface = SigniaWhite,
    onPrimary = SigniaWhite,
    onSecondary = SigniaDarkPurple,
    onBackground = SigniaDarkPurple,
    onSurface = SigniaDarkPurple
)

@Composable
fun APPSIGNIATheme(
    darkTheme: Boolean = ThemeManager.isDarkMode,
    // Dynamic color deshabilitado para mantener la coherencia de la marca SIGNIA
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}