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
    val Cream = Color(0xFFFFF8F3)
    val Cloud = Color(0xFFFDF2F8)
    val Petal = Color(0xFFFFC8DD)
    val Lilac = Color(0xFFCDB4DB)
    val Mint = Color(0xFFBDE0FE)
    val Sage = Color(0xFFC8E6C9)
    val Ink = Color(0xFF1A1224)
    val Smoke = Color(0xFF4F4258)
    val Mist = Color(0xFFE6DAEC)

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
    surface = AscendyColors.Cream,
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
    displayLarge = TextStyle(fontSize = 56.sp, fontWeight = FontWeight.Black, letterSpacing = (-1).sp, lineHeight = 60.sp),
    displayMedium = TextStyle(fontSize = 44.sp, fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp, lineHeight = 48.sp),
    displaySmall = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 40.sp),
    headlineLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 32.sp),
    headlineSmall = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp),
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 26.sp),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 18.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp),
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
