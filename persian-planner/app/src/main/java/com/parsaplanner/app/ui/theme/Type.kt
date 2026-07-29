package com.parsaplanner.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Uses the system's serif face for display/headline text (a classic, premium feel that
// also renders Persian script beautifully via the platform's Noto fallback), and the
// system sans-serif for body text (clean and highly legible at small sizes).
// This avoids bundling font files, so the project builds with zero extra setup.
// To swap in a custom Persian display font like Vazirmatn later, see README.md.
val ParsaTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 36.sp, letterSpacing = 0.2.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 25.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold, fontSize = 21.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 13.sp, letterSpacing = 0.3.sp)
)
