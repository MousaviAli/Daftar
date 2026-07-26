package com.parsaplanner.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = AccentTerracotta,
    secondary = AccentOlive,
    tertiary = AccentGold,
    background = PaperCream,
    surface = PaperCard,
    onPrimary = PaperCream,
    onBackground = InkBrown,
    onSurface = InkBrown,
    outline = DividerSoft
)

private val DarkColors = darkColorScheme(
    primary = NightAccent,
    secondary = AccentOlive,
    tertiary = AccentGold,
    background = NightBg,
    surface = NightCard,
    onPrimary = NightBg,
    onBackground = NightInk,
    onSurface = NightInk,
    outline = InkSoft
)

@Composable
fun ParsaPlannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = ParsaTypography,
        content = content
    )
}
