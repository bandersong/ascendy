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

enum class ThemeVariant { Kawaii, Tough, Neutral }

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

// ───── Kawaii (Strawberry Milk + Sakura — cool pinks, lavender co-star, plum text) ─────
val KawaiiLight = Palette(
    variant = ThemeVariant.Kawaii,
    isDark = false,
    Cream = Color(0xFFFFF5F7),          // strawberry-milk background
    Cloud = Color(0xFFFCEAEF),           // soft pink secondary surface
    Petal = Color(0xFFFF8FB1),           // primary — sakura mochi pink
    Lilac = Color(0xFFC7B8EA),           // secondary — Kuromi/wisteria lavender
    Mint = Color(0xFFFFD6A5),            // tertiary — sun-warmed peach
    Sage = Color(0xFFA8D8B9),            // success — Cinnamoroll mint
    Ink = Color(0xFF4A2C3D),             // deep plum-maroon
    Smoke = Color(0xFF9B7A8A),           // dusty mauve for muted text
    Mist = Color(0xFFF5E1E8),            // pink fog dividers
    Surface = Color(0xFFFFFDFE),          // off-white with whisper of pink
)

val KawaiiDark = Palette(
    variant = ThemeVariant.Kawaii,
    isDark = true,
    Cream = Color(0xFF2A1A24),            // deep plum bg
    Cloud = Color(0xFF3A2530),
    Petal = Color(0xFFFFB7CC),            // softer pink for dark
    Lilac = Color(0xFFC7B8EA),
    Mint = Color(0xFFFFD6A5),
    Sage = Color(0xFFA8D8B9),
    Ink = Color(0xFFFFF5F7),
    Smoke = Color(0xFFC9A8B5),
    Mist = Color(0xFF4A2E3C),
    Surface = Color(0xFF321F2A),
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

// ───── Neutral (corporate, professional, restrained) ─────
val NeutralLight = Palette(
    variant = ThemeVariant.Neutral,
    isDark = false,
    Cream = Color(0xFFF7F8FA),       // crisp paper
    Cloud = Color(0xFFEDEFF3),       // panel
    Petal = Color(0xFF2B5BD7),       // corporate blue accent
    Lilac = Color(0xFF6B7280),       // slate
    Mint = Color(0xFFDDE5F1),
    Sage = Color(0xFFCDE5D0),
    Ink = Color(0xFF111827),         // neutral near-black
    Smoke = Color(0xFF6B7280),
    Mist = Color(0xFFE3E6EB),
    Surface = Color(0xFFFFFFFF),
)

val NeutralDark = Palette(
    variant = ThemeVariant.Neutral,
    isDark = true,
    Cream = Color(0xFF0F1115),
    Cloud = Color(0xFF181B22),
    Petal = Color(0xFF7B97FF),
    Lilac = Color(0xFF9AA3B2),
    Mint = Color(0xFF2D3344),
    Sage = Color(0xFF2D3D33),
    Ink = Color(0xFFE6E8EC),
    Smoke = Color(0xFF9AA3B2),
    Mist = Color(0xFF272B34),
    Surface = Color(0xFF1A1D24),
)

val LocalPalette = staticCompositionLocalOf { NeutralLight }

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

// Neutral: mid-radius, clean cards
private val NeutralShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp),
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

// Neutral typography — clean, modest weights, no character
private val NeutralTypography = Typography(
    displayLarge = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp, lineHeight = 56.sp),
    displayMedium = TextStyle(fontSize = 38.sp, fontWeight = FontWeight.SemiBold, lineHeight = 46.sp),
    displaySmall = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Medium, lineHeight = 40.sp),
    headlineLarge = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Medium, lineHeight = 32.sp),
    headlineMedium = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Medium, lineHeight = 28.sp),
    headlineSmall = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp),
    titleLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp),
    titleMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal, lineHeight = 19.sp),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 17.sp),
    labelLarge = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.3.sp, lineHeight = 16.sp),
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

fun paletteFor(variant: ThemeVariant, dark: Boolean): Palette = when (variant) {
    ThemeVariant.Kawaii -> if (dark) KawaiiDark else KawaiiLight
    ThemeVariant.Tough -> if (dark) ToughDark else ToughLight
    ThemeVariant.Neutral -> if (dark) NeutralDark else NeutralLight
}

private fun typographyFor(variant: ThemeVariant): Typography = when (variant) {
    ThemeVariant.Kawaii -> KawaiiTypography
    ThemeVariant.Tough -> ToughTypography
    ThemeVariant.Neutral -> NeutralTypography
}

private fun shapesFor(variant: ThemeVariant): Shapes = when (variant) {
    ThemeVariant.Kawaii -> KawaiiShapes
    ThemeVariant.Tough -> ToughShapes
    ThemeVariant.Neutral -> NeutralShapes
}

@Composable
fun AscendyTheme(variant: ThemeVariant = ThemeVariant.Neutral, content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val p = paletteFor(variant, dark)
    CompositionLocalProvider(
        LocalPalette provides p,
        LocalVocab provides vocabFor(variant),
    ) {
        MaterialTheme(
            colorScheme = colorSchemeFor(p),
            typography = typographyFor(variant),
            shapes = shapesFor(variant),
            content = content
        )
    }
}
