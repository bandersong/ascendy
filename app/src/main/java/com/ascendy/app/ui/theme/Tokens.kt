package com.ascendy.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Ascendy spacing scale — a strict 4pt grid. Every gap, pad, and inset in the UI
 * resolves to one of these tokens; screens carry no raw spacing `.dp` literals.
 * Variant-agnostic on purpose — rhythm stays identical across Kawaii/Neutral/Tough.
 * See docs/UI_MASTERPIECE.md §1.
 */
object Space {
    val none: Dp = 0.dp
    val xxs: Dp = 2.dp    // hairline nudge only
    val xs: Dp = 4.dp     // label↔control, tight inline gaps
    val sm: Dp = 8.dp     // chip gaps, icon↔text, badge spacing
    val md: Dp = 12.dp    // inside-row padding, dialog inner blocks
    val lg: Dp = 16.dp    // block gap, card↔card default
    val xl: Dp = 20.dp    // page margin, section gap, card padding
    val xxl: Dp = 24.dp   // page bottom, large section breaks
    val xxxl: Dp = 32.dp  // hero breathing room
    val huge: Dp = 40.dp  // empty-state / splash vertical
    val xhuge: Dp = 48.dp // macro layout: major section seams, FAB clearance
    val mega: Dp = 64.dp  // screen-level hero / empty-state vertical centering
}

/** Vertical spacer on the grid, e.g. `VSpace(Space.lg)`. */
@Composable
fun VSpace(height: Dp) = Spacer(Modifier.height(height))

/** Horizontal spacer on the grid, e.g. `HSpace(Space.sm)`. */
@Composable
fun HSpace(width: Dp) = Spacer(Modifier.width(width))

/**
 * Motion tokens — one source for durations + easings so every transition in the
 * app shares a coherent feel. No raw `durationMillis` ints in screens/components.
 * See docs/UI_MASTERPIECE.md §4.
 */
object Motion {
    const val quick: Int = 150        // taps, toggles, micro-feedback
    const val standard: Int = 250     // content swaps, enter/exit
    const val emphasized: Int = 400   // hero / state changes
    const val mascotBob: Int = 2400   // ambient idle loop

    /** Material "standard" decelerate — most enter/exit. */
    val standardEasing: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    /** Material "emphasized" — hero/state changes that deserve a beat. */
    val emphasizedEasing: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
}

/**
 * Elevation / pressable-state tokens. Dark mode favors hairline borders over
 * shadows (handled in SoftCard). See docs/UI_MASTERPIECE.md §5–6.
 */
object Elev {
    val cardRestLight: Dp = 1.dp
    val cardRestDark: Dp = 0.dp
    val hairline: Dp = 1.dp
    const val pressedScale: Float = 0.97f
    const val disabledAlpha: Float = 0.4f
}
