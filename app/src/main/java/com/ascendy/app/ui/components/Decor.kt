package com.ascendy.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ascendy.app.R
import com.ascendy.app.ui.theme.ThemeVariant
import com.ascendy.app.ui.theme.palette

/** Picks the hand-drawn mascot art for the active theme and locked/unlocked state. */
private fun mascotRes(variant: ThemeVariant, locked: Boolean): Int = when (variant) {
    ThemeVariant.Kawaii -> if (locked) R.drawable.mascot_kawaii_locked else R.drawable.mascot_kawaii_unlocked
    ThemeVariant.Tough -> if (locked) R.drawable.mascot_tough_locked else R.drawable.mascot_tough_unlocked
    ThemeVariant.Neutral -> if (locked) R.drawable.mascot_neutral_locked else R.drawable.mascot_neutral_unlocked
}

/**
 * Themed mascot, gently bobbing. [locked] selects the focusing/locked artwork over the idle one.
 * [streakDays] is retained for call-site compatibility; streak decorations are not drawn over the art.
 */
@Composable
fun Mascot(locked: Boolean, streakDays: Int = 0, modifier: Modifier = Modifier.fillMaxWidth()) {
    val variant = palette.variant
    val transition = rememberInfiniteTransition(label = "mascot")
    val bob by transition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = mascotRes(variant, locked)),
            contentDescription = if (locked) "Focusing mascot" else "Idle mascot",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .graphicsLayer { translationY = bob }
        )
    }
}

/** A tiny static mascot for inline use (no animation). */
@Composable
fun MiniMascot(locked: Boolean, streakDays: Int = 0, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = mascotRes(palette.variant, locked)),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}

@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primaryContainer,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        color = color,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 0.dp,
    ) {
        Box(Modifier.padding(20.dp)) { content() }
    }
}

@Composable
fun Badge(label: String, color: Color = MaterialTheme.colorScheme.tertiary) {
    val onColor = if (palette.variant == ThemeVariant.Tough && color == palette.Petal) palette.Cream else palette.Ink
    Surface(
        color = color,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = onColor
        )
    }
}

@Composable
fun Dot(color: Color, sizeDp: Int = 8) {
    Canvas(modifier = Modifier.padding(0.dp)) {
        drawCircle(color, radius = sizeDp.toFloat())
    }
}
