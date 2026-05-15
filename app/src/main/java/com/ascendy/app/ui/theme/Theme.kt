package com.ascendy.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ThemeVariant { Kawaii, Tough }

/** Per-theme palette. Same field names so screens are theme-agnostic. */
data class Palette(
    val variant: ThemeVariant,
    val isDark: Boolean,
    val Cream: Color,          // background
    val Cloud: Color,          // soft secondary surface
    val Petal: Color,          // primary accent
    val Lilac: Color,          // secondary accent
    val Mint: Color,           // tertiary accent
    val Sage: Color,           // success-ish
    val Ink: Color,            // primary text
    val Smoke: Color,          // muted text
    val Mist: Color,           // dividers / variant surface
    val Surface: Color,        // card surface
)

// ───── Kawaii (default — soft, pastel, friendly) ─────
val KawaiiLight = Palette(
    variant = ThemeVariant.Kawaii,
    isDark = false,
    Cream = Color(0xFFFFFBFD),
    Cloud = Color(0xFFFAF1F7),
    Petal = Color(0xFFFFD4E5),
    Lilac = Color(0xFFE0CFEC),
    Mint = Color(0xFFCFEAFE),
    Sage = Color(0xFFD4ECD5),
    Ink = Color(0xFF2E2538),
    Smoke = Color(0xFF8278A0),
    Mist = Color(0xFFF0E8F3),
    Surface = Color(0xFFFFFFFF),
)

val KawaiiDark = Palette(
    variant = ThemeVariant.Kawaii,
    isDark = true,
    Cream = Color(0xFF1F1A24),
    Cloud = Color(0xFF2A2330),
    Petal = Color(0xFFFFC8DD),
    Lilac = Color(0xFFCDB4DB),
    Mint = Color(0xFFBDE0FE),
    Sage = Color(0xFFC8E6C9),
    Ink = Color(0xFFF5EBFF),
    Smoke = Color(0xFFB8AFC4),
    Mist = Color(0xFF3A3140),
    Surface = Color(0xFF2A2330),
)

// ───── Tough (gritty, monochrome with iron accents) ─────
val ToughLight = Palette(
    variant = ThemeVariant.Tough,
    isDark = false,
    Cream = Color(0xFFEAE6E0),       // cool stone
    Cloud = Color(0xFFD8D2C9),       // dusty concrete
    Petal = Color(0xFF1A1A1C),       // black is the primary accent
    Lilac = Color(0xFF55504A),       // gunmetal
    Mint = Color(0xFFB4B5A3),        // olive-tinged gray
    Sage = Color(0xFF7F8B7A),        // muted moss
    Ink = Color(0xFF0E0E10),
    Smoke = Color(0xFF55504A),
    Mist = Color(0xFFCEC8BE),
    Surface = Color(0xFFF3EFE9),
)

val ToughDark = Palette(
    variant = ThemeVariant.Tough,
    isDark = true,
    Cream = Color(0xFF0B0B0D),
    Cloud = Color(0xFF1A1A1C),
    Petal = Color(0xFFEDE7DA),       // bone white as primary in dark tough
    Lilac = Color(0xFF8A8378),       // tarnished brass
    Mint = Color(0xFF6B7066),
    Sage = Color(0xFF5C685D),
    Ink = Color(0xFFEDE7DA),
    Smoke = Color(0xFF9A938A),
    Mist = Color(0xFF2A2A2D),
    Surface = Color(0xFF161618),
)

val LocalPalette = staticCompositionLocalOf { KawaiiLight }

/** Top-level composable accessor so screens just write `palette.Ink` etc. */
val palette: Palette
    @Composable @ReadOnlyComposable
    get() = LocalPalette.current

private fun colorSchemeFor(p: Palette) = if (p.isDark) {
    darkColorScheme(
        primary = p.Petal,
        onPrimary = if (p.variant == ThemeVariant.Tough) p.Cream else p.Ink,
        primaryContainer = p.Cloud,
        onPrimaryContainer = p.Ink,
        secondary = p.Lilac,
        onSecondary = p.Ink,
        tertiary = p.Mint,
        onTertiary = p.Ink,
        background = p.Cream,
        onBackground = p.Ink,
        surface = p.Surface,
        onSurface = p.Ink,
        surfaceVariant = p.Mist,
        onSurfaceVariant = p.Smoke,
        outline = p.Mist,
    )
} else {
    lightColorScheme(
        primary = p.Petal,
        onPrimary = if (p.variant == ThemeVariant.Tough) p.Cream else p.Ink,
        primaryContainer = p.Cloud,
        onPrimaryContainer = p.Ink,
        secondary = p.Lilac,
        onSecondary = p.Ink,
        tertiary = p.Mint,
        onTertiary = p.Ink,
        background = p.Cream,
        onBackground = p.Ink,
        surface = p.Surface,
        onSurface = p.Ink,
        surfaceVariant = p.Mist,
        onSurfaceVariant = p.Smoke,
        outline = p.Mist,
    )
}

private val KawaiiShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

// Tough mode: hard edges (smaller radii)
private val ToughShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(10.dp),
    extraLarge = RoundedCornerShape(12.dp),
)

private val KawaiiTypography = Typography(
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

// Tough typography — heavier, all-caps feel via tracking
private val ToughTypography = Typography(
    displayLarge = TextStyle(fontSize = 56.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, lineHeight = 60.sp),
    displayMedium = TextStyle(fontSize = 44.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp, lineHeight = 48.sp),
    displaySmall = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp, lineHeight = 40.sp),
    headlineLarge = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp, lineHeight = 30.sp),
    headlineSmall = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, lineHeight = 26.sp),
    titleLarge = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, lineHeight = 24.sp),
    titleMedium = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp, lineHeight = 21.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 18.sp),
    labelLarge = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, lineHeight = 18.sp),
)

private fun paletteFor(variant: ThemeVariant, dark: Boolean): Palette = when (variant) {
    ThemeVariant.Kawaii -> if (dark) KawaiiDark else KawaiiLight
    ThemeVariant.Tough -> if (dark) ToughDark else ToughLight
}

@Composable
fun AscendyTheme(variant: ThemeVariant = ThemeVariant.Kawaii, content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val p = paletteFor(variant, dark)
    CompositionLocalProvider(LocalPalette provides p) {
        MaterialTheme(
            colorScheme = colorSchemeFor(p),
            typography = if (variant == ThemeVariant.Tough) ToughTypography else KawaiiTypography,
            shapes = if (variant == ThemeVariant.Tough) ToughShapes else KawaiiShapes,
            content = content
        )
    }
}
