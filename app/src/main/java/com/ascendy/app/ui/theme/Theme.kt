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
import androidx.compose.ui.graphics.luminance
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
) {
    /**
     * Readable text/icon color to lay over [bg], staying inside the palette.
     * Picks Ink or Cream — whichever contrasts better with the background.
     * Works because in dark palettes Cream is the deep bg (reads as "dark text")
     * and Ink is near-white, while in light palettes the reverse holds.
     */
    fun on(bg: Color): Color =
        if (contrast(bg, Cream) >= contrast(bg, Ink)) Cream else Ink

    /** Correct content color for a filled [Petal] surface (button/FAB/selected chip). */
    val onPetal: Color get() = on(Petal)
}

/** WCAG relative-contrast ratio between two opaque colors. */
private fun contrast(a: Color, b: Color): Float {
    val la = a.luminance() + 0.05f
    val lb = b.luminance() + 0.05f
    return if (la > lb) la / lb else lb / la
}

// ───── Kawaii (Orchid Dream - lavender-forward, sakura-pink co-star, plum text) ─────
// Anchored to the mascot art: a lavender/orchid star with blush-pink cheeks.
val KawaiiLight = Palette(
    variant = ThemeVariant.Kawaii,
    isDark = false,
    Cream = Color(0xFFFBF7FF),          // lavender-white background
    Cloud = Color(0xFFF3E9FC),           // soft orchid secondary surface
    Petal = Color(0xFF9D40CE),           // primary - deep orchid (the mascot's body)
    Lilac = Color(0xFFFF9EC4),           // secondary - sakura blush pink
    Mint = Color(0xFFFFC59A),            // tertiary - sun-warmed peach
    Sage = Color(0xFF8FD0AC),            // success - soft mint
    Ink = Color(0xFF3D2453),             // deep plum-violet text
    Smoke = Color(0xFF7C5A82),           // dusty plum for muted text (AA on all surfaces)
    Mist = Color(0xFFECDDF7),            // lavender fog dividers
    Surface = Color(0xFFFFFDFF),          // off-white with a whisper of lavender
)

val KawaiiDark = Palette(
    variant = ThemeVariant.Kawaii,
    isDark = true,
    Cream = Color(0xFF1E1428),            // deep plum-violet bg
    Cloud = Color(0xFF2C1E3A),
    Petal = Color(0xFFDDA8F5),            // luminous soft orchid for dark
    Lilac = Color(0xFFFFB0CE),            // soft sakura pink
    Mint = Color(0xFFFFC59A),
    Sage = Color(0xFF9FD8B6),
    Ink = Color(0xFFF7EFFB),
    Smoke = Color(0xFFC4A8D4),
    Mist = Color(0xFF3D2A4F),
    Surface = Color(0xFF271934),
)

// ───── Tough (Ink & Bone - monochrome with iron accents) ─────
// Anchored to the mascot art: a bone-white star on pure black.
val ToughLight = Palette(
    variant = ThemeVariant.Tough,
    isDark = false,
    Cream = Color(0xFFECE9E1),       // warm bone-stone
    Cloud = Color(0xFFDAD5CB),       // dusty concrete
    Petal = Color(0xFF15151A),       // near-black is the primary accent
    Lilac = Color(0xFF52504B),       // gunmetal
    Mint = Color(0xFFB0B19F),        // olive-tinged gray
    Sage = Color(0xFF7C8A78),        // muted moss
    Ink = Color(0xFF0C0C0E),
    Smoke = Color(0xFF565249),
    Mist = Color(0xFFCFC9BF),
    Surface = Color(0xFFF4F1EA),
)

val ToughDark = Palette(
    variant = ThemeVariant.Tough,
    isDark = true,
    Cream = Color(0xFF070708),       // pure near-black (matches icon bg)
    Cloud = Color(0xFF161618),
    Petal = Color(0xFFF2EFE4),       // bone white as primary in dark tough
    Lilac = Color(0xFF8B8478),       // tarnished brass
    Mint = Color(0xFF7A7F71),        // pale olive-gray
    Sage = Color(0xFF5D695E),
    Ink = Color(0xFFF2EFE4),
    Smoke = Color(0xFF9C958A),
    Mist = Color(0xFF28282B),
    Surface = Color(0xFF121214),
)

// ───── Neutral (Slate Iris - professional, restrained, quietly indigo) ─────
// Anchored to the mascot art: a muted slate blue-gray robot.
val NeutralLight = Palette(
    variant = ThemeVariant.Neutral,
    isDark = false,
    Cream = Color(0xFFF6F7FA),       // crisp cool paper
    Cloud = Color(0xFFEAECF2),       // panel
    Petal = Color(0xFF4D5694),       // iris-slate accent (sophisticated, not loud blue)
    Lilac = Color(0xFF646A7C),       // slate
    Mint = Color(0xFFDEE2EE),
    Sage = Color(0xFFBFE0CA),
    Ink = Color(0xFF15161E),         // slate near-black
    Smoke = Color(0xFF5C6070),
    Mist = Color(0xFFE1E4EB),
    Surface = Color(0xFFFFFFFF),
)

val NeutralDark = Palette(
    variant = ThemeVariant.Neutral,
    isDark = true,
    Cream = Color(0xFF101117),
    Cloud = Color(0xFF191B23),
    Petal = Color(0xFF9AA2E0),       // luminous periwinkle-iris
    Lilac = Color(0xFF9AA0B2),
    Mint = Color(0xFF2C3344),
    Sage = Color(0xFF2C3D33),
    Ink = Color(0xFFE8E9F0),
    Smoke = Color(0xFF9DA1B2),
    Mist = Color(0xFF262932),
    Surface = Color(0xFF1A1C24),
)

val LocalPalette = staticCompositionLocalOf { NeutralLight }

/** Top-level composable accessor so screens just write `palette.Ink` etc. */
val palette: Palette
    @Composable @ReadOnlyComposable
    get() = LocalPalette.current

private fun colorSchemeFor(p: Palette) = if (p.isDark) {
    darkColorScheme(
        primary = p.Petal,
        onPrimary = p.onPetal,
        primaryContainer = p.Cloud,
        onPrimaryContainer = p.Ink,
        secondary = p.Lilac,
        onSecondary = p.on(p.Lilac),
        tertiary = p.Mint,
        onTertiary = p.on(p.Mint),
        background = p.Cream,
        onBackground = p.Ink,
        surface = p.Surface,
        onSurface = p.Ink,
        surfaceVariant = p.Mist,
        onSurfaceVariant = p.Smoke,
        outline = p.Mist,
        // surfaceContainer* drive dialogs, menus, bottom sheets — keep them in-palette
        surfaceContainerLowest = p.Cream,
        surfaceContainerLow = p.Surface,
        surfaceContainer = p.Surface,
        surfaceContainerHigh = p.Cloud,
        surfaceContainerHighest = p.Cloud,
        outlineVariant = p.Mist,
    )
} else {
    lightColorScheme(
        primary = p.Petal,
        onPrimary = p.onPetal,
        primaryContainer = p.Cloud,
        onPrimaryContainer = p.Ink,
        secondary = p.Lilac,
        onSecondary = p.on(p.Lilac),
        tertiary = p.Mint,
        onTertiary = p.on(p.Mint),
        background = p.Cream,
        onBackground = p.Ink,
        surface = p.Surface,
        onSurface = p.Ink,
        surfaceVariant = p.Mist,
        onSurfaceVariant = p.Smoke,
        outline = p.Mist,
        surfaceContainerLowest = p.Cream,
        surfaceContainerLow = p.Surface,
        surfaceContainer = p.Surface,
        surfaceContainerHigh = p.Cloud,
        surfaceContainerHighest = p.Cloud,
        outlineVariant = p.Mist,
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
