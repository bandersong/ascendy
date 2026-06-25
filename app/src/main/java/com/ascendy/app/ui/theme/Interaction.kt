package com.ascendy.app.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Tactile press feedback — gently scales a pressable to [Elev.pressedScale] while
 * held, springing back on release. Purely visual ([graphicsLayer]); it never
 * changes layout or hit area. Pass the SAME [interactionSource] you hand the
 * clickable/Surface so the scale tracks real press state.
 *
 * One modifier for every interactive surface = one coherent "this is tappable"
 * feel across all three themes. See docs/UI_MASTERPIECE.md §6.
 */
@Composable
fun Modifier.pressScale(interactionSource: InteractionSource): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) Elev.pressedScale else 1f,
        animationSpec = tween(durationMillis = Motion.quick, easing = Motion.standardEasing),
        label = "pressScale",
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
