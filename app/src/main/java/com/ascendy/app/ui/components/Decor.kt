package com.ascendy.app.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
 * Themed mascot, gently bobbing. [locked] selects the focusing/locked artwork over the idle one,
 * crossfading between the two so the state change reads as a mood shift, not a hard cut.
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
        Crossfade(
            targetState = locked,
            animationSpec = tween(durationMillis = 350),
            label = "mascotArt"
        ) { isLocked ->
            Image(
                painter = painterResource(id = mascotRes(variant, isLocked)),
                contentDescription = if (isLocked) "Focusing mascot" else "Idle mascot",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .graphicsLayer { translationY = bob }
            )
        }
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

/** Max readable width for a page's content column; wider screens center the column. */
val PageMaxWidth = 640.dp

/**
 * Standard page scaffold: fills the screen, pads for system bars, scrolls, and constrains
 * content to a readable column ([PageMaxWidth]) centered on wide screens. Phones are
 * unaffected; tablets and landscape stop stretching rows edge-to-edge.
 */
@Composable
fun PageColumn(
    modifier: Modifier = Modifier,
    scroll: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val insets = WindowInsets.systemBars.asPaddingValues()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(if (scroll) Modifier.verticalScroll(rememberScrollState()) else Modifier)
            .padding(
                top = 16.dp + insets.calculateTopPadding(),
                bottom = 24.dp + insets.calculateBottomPadding(),
            )
            .then(modifier),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = PageMaxWidth)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            content = content,
        )
    }
}

/**
 * Width-constraining frame for screens that manage their own scrolling (LazyColumn).
 * Same insets, horizontal padding, and centered [PageMaxWidth] column as [PageColumn],
 * but no scroll of its own and no extra bottom padding (lazy lists bring their own
 * contentPadding). [floating] is anchored to the bottom-end of the *content column*,
 * not the screen, so a FAB stays attached to the list it acts on even on tablets.
 */
@Composable
fun PageFrame(
    modifier: Modifier = Modifier,
    floating: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val insets = WindowInsets.systemBars.asPaddingValues()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = 16.dp + insets.calculateTopPadding(),
                bottom = insets.calculateBottomPadding(),
            )
            .then(modifier),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = PageMaxWidth)
                .fillMaxWidth()
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                content = content,
            )
            if (floating != null) {
                Box(Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 20.dp)) {
                    floating()
                }
            }
        }
    }
}

@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primaryContainer,
    content: @Composable () -> Unit
) {
    // A whisper of shadow in light mode (so surface-on-cream cards don't melt into the bg)
    // and a hairline border in palette.Mist — the border especially suits Tough's hard edges,
    // the soft shadow suits Kawaii.
    Surface(
        modifier = modifier,
        color = color,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 0.dp,
        shadowElevation = if (palette.isDark) 0.dp else 1.dp,
        border = BorderStroke(1.dp, palette.Mist),
    ) {
        // fillMaxWidth so content lays out against the card's real width — a wrap-content box
        // here left every centered hero/column hugging the left edge on tablets.
        Box(Modifier.fillMaxWidth().padding(20.dp)) { content() }
    }
}

/** Text/icon color that stays readable on an arbitrary [chip] background, inside the palette. */
@Composable
fun onChip(chip: Color): Color {
    val darkText = if (palette.isDark) palette.Cream else palette.Ink
    val lightText = if (palette.isDark) palette.Ink else palette.Cream
    return if (chip.luminance() > 0.45f) darkText else lightText
}

@Composable
fun Badge(label: String, color: Color = MaterialTheme.colorScheme.tertiary) {
    Surface(
        color = color,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = onChip(color)
        )
    }
}

/**
 * Shared selectable pill used across Onboarding, PairTag, AppPicker, Schedules, Pomodoro and
 * Settings. Keeping it in one place is the only way the selected-state contrast stays correct
 * in all three theme variants (the old per-screen copies all rendered Ink-on-Petal, which is
 * invisible in Tough and low-contrast in Neutral).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectableChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        color = if (selected) palette.Petal else palette.Cloud,
        contentColor = if (selected) palette.onPetal else palette.Ink,
        shape = MaterialTheme.shapes.medium,
        border = if (selected) null else BorderStroke(1.dp, palette.Mist),
        modifier = modifier,
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) palette.onPetal else palette.Ink,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun Dot(color: Color, sizeDp: Int = 8) {
    Canvas(modifier = Modifier.padding(0.dp)) {
        drawCircle(color, radius = sizeDp.toFloat())
    }
}
