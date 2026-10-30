package com.example.myapplication.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.myapplication.R

val InterFontFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)

private val base = Typography()

val Typography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = InterFontFamily),
    displayMedium = base.displayMedium.copy(fontFamily = InterFontFamily),
    displaySmall = base.displaySmall.copy(fontFamily = InterFontFamily),
    headlineLarge = base.headlineLarge.copy(fontFamily = InterFontFamily),
    headlineMedium = base.headlineMedium.copy(fontFamily = InterFontFamily),
    headlineSmall = base.headlineSmall.copy(fontFamily = InterFontFamily),
    titleLarge = base.titleLarge.copy(fontFamily = InterFontFamily),
    titleMedium = base.titleMedium.copy(fontFamily = InterFontFamily),
    titleSmall = base.titleSmall.copy(fontFamily = InterFontFamily),
    bodyLarge = base.bodyLarge.copy(fontFamily = InterFontFamily, lineHeight = 1.3.em, letterSpacing = 0.sp),
    bodyMedium = base.bodyMedium.copy(fontFamily = InterFontFamily, lineHeight = 1.3.em, letterSpacing = 0.sp),
    bodySmall = base.bodySmall.copy(fontFamily = InterFontFamily, lineHeight = 1.3.em, letterSpacing = 0.sp),
    labelLarge = base.labelLarge.copy(fontFamily = InterFontFamily),
    labelMedium = base.labelMedium.copy(fontFamily = InterFontFamily),
    labelSmall = base.labelSmall.copy(fontFamily = InterFontFamily)
)