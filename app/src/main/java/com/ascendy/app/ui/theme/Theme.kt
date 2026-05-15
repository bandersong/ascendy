package com.ascendy.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object AscendyColors {
    val Cream = Color(0xFFFFFBFD)          // background — almost white, breath of pink
    val Cloud = Color(0xFFFAF1F7)          // secondary surface
    val Petal = Color(0xFFFFD4E5)          // primary, slightly softer pink
    val Lilac = Color(0xFFE0CFEC)          // lighter lavender
    val Mint = Color(0xFFCFEAFE)
    val Sage = Color(0xFFD4ECD5)
    val Ink = Color(0xFF2E2538)            // purple-charcoal, not jet black
    val Smoke = Color(0xFF8278A0)          // lighter muted for secondary text
    val Mist = Color(0xFFF0E8F3)

    val Night = Color(0xFF1F1A24)
    val NightSurface = Color(0xFF2A2330)
    val NightMid = Color(0xFF3A3140)
    val Moon = Color(0xFFF5EBFF)
}

private val Light = lightColorScheme(
    primary = AscendyColors.Petal,
    onPrimary = AscendyColors.Ink,
    primaryContainer = AscendyColors.Cloud,
    onPrimaryContainer = AscendyColors.Ink,
    secondary = AscendyColors.Lilac,
    onSecondary = AscendyColors.Ink,
    tertiary = AscendyColors.Mint,
    onTertiary = AscendyColors.Ink,
    background = AscendyColors.Cream,
    onBackground = AscendyColors.Ink,
    surface = Color(0xFFFFFFFF),                          // pure white cards float above bg
    onSurface = AscendyColors.Ink,
    surfaceVariant = AscendyColors.Mist,
    onSurfaceVariant = AscendyColors.Smoke,
    outline = AscendyColors.Mist,
)

private val Dark = darkColorScheme(
    primary = AscendyColors.Petal,
    onPrimary = AscendyColors.Ink,
    primaryContainer = AscendyColors.NightMid,
    onPrimaryContainer = AscendyColors.Moon,
    secondary = AscendyColors.Lilac,
    onSecondary = AscendyColors.Ink,
    tertiary = AscendyColors.Mint,
    onTertiary = AscendyColors.Ink,
    background = AscendyColors.Night,
    onBackground = AscendyColors.Moon,
    surface = AscendyColors.NightSurface,
    onSurface = AscendyColors.Moon,
    surfaceVariant = AscendyColors.NightMid,
    onSurfaceVariant = AscendyColors.Moon,
    outline = AscendyColors.NightMid,
)

private val AscendyShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

private val AscendyTypography = Typography(
    displayLarge = TextStyle(fontSize = 56.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-1).sp, lineHeight = 60.sp),
    displayMedium = TextStyle(fontSize = 44.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp, lineHeight = 48.sp),
    displaySmall = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.SemiBold, lineHeight = 40.sp),
    headlineLarge = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.SemiBold, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold, lineHeight = 30.sp),
    headlineSmall = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium, lineHeight = 26.sp),
    titleLarge = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp),
    titleMedium = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, lineHeight = 21.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 18.sp),
    labelLarge = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, lineHeight = 18.sp),
)

@Composable
fun AscendyTheme(content: @Composable () -> Unit) {
    val scheme = if (isSystemInDarkTheme()) Dark else Light
    MaterialTheme(
        colorScheme = scheme,
        typography = AscendyTypography,
        shapes = AscendyShapes,
        content = content
    )
}
