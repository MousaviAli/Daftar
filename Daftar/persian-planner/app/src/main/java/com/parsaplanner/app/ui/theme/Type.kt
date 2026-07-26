package com.parsaplanner.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.parsaplanner.app.R

// Vazirmatn: a free, modern, very high-quality Persian variable font family
// (designed for UI, excellent readability, strong display weights)
// Place the .ttf files under res/font/ — see README for download instructions.
val VazirmatnFamily = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_semibold, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
    Font(R.font.vazirmatn_black, FontWeight.Black)
)

val ParsaTypography = Typography(
    displayLarge = TextStyle(fontFamily = VazirmatnFamily, fontWeight = FontWeight.Black, fontSize = 34.sp),
    headlineMedium = TextStyle(fontFamily = VazirmatnFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = VazirmatnFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    titleMedium = TextStyle(fontFamily = VazirmatnFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = VazirmatnFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = VazirmatnFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge = TextStyle(fontFamily = VazirmatnFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp)
)
