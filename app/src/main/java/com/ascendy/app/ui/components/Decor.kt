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
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ascendy.app.R
import com.ascendy.app.ui.theme.Elev
import com.ascendy.app.ui.theme.HSpace
import com.ascendy.app.ui.theme.Motion
import com.ascendy.app.ui.theme.Space
import com.ascendy.app.ui.theme.ThemeVariant
import com.ascendy.app.ui.theme.palette
import com.ascendy.app.ui.theme.pressScale
import com.ascendy.app.ui.theme.vocab

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
            animation = tween(durationMillis = Motion.mascotBob, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Crossfade(
            targetState = locked,
            animationSpec = tween(durationMillis = Motion.emphasized, easing = Motion.emphasizedEasing),
            label = "mascotArt"
        ) { isLocked ->
            Image(
                painter = painterResource(id = mascotRes(variant, isLocked)),
                contentDescription = if (isLocked) "Focusing mascot" else "Idle mascot",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Space.sm)
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
    centerWhenShort: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val insets = WindowInsets.systemBars.asPaddingValues()
    val topPad = Space.lg + insets.calculateTopPadding()
    val botPad = Space.xxl + insets.calculateBottomPadding()
    // BoxWithConstraints exposes the viewport height so [centerWhenShort] screens can sit
    // in the optical center when their content is shorter than the screen, while taller
    // content still grows past it and scrolls. A calm screen owns its empty space instead
    // of clinging to the top edge.
    BoxWithConstraints(modifier = Modifier.fillMaxSize().then(modifier)) {
        val minContentHeight = (maxHeight - topPad - botPad).coerceAtLeast(0.dp)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (scroll) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                .padding(top = topPad, bottom = botPad),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = PageMaxWidth)
                    .fillMaxWidth()
                    .then(if (centerWhenShort) Modifier.heightIn(min = minContentHeight) else Modifier)
                    .padding(horizontal = Space.xl),
                verticalArrangement = if (centerWhenShort) Arrangement.Center else Arrangement.Top,
                content = content,
            )
        }
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
                top = Space.lg + insets.calculateTopPadding(),
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
                    .padding(horizontal = Space.xl),
                content = content,
            )
            if (floating != null) {
                Box(Modifier.align(Alignment.BottomEnd).padding(end = Space.xl, bottom = Space.xl)) {
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
        shadowElevation = if (palette.isDark) Elev.cardRestDark else Elev.cardRestLight,
        border = BorderStroke(Elev.hairline, palette.Mist),
    ) {
        // fillMaxWidth so content lays out against the card's real width — a wrap-content box
        // here left every centered hero/column hugging the left edge on tablets.
        Box(Modifier.fillMaxWidth().padding(Space.xl)) { content() }
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
            modifier = Modifier.padding(horizontal = Space.md, vertical = Space.sm),
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
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        color = if (selected) palette.Petal else palette.Cloud,
        contentColor = if (selected) palette.onPetal else palette.Ink,
        shape = MaterialTheme.shapes.medium,
        border = if (selected) null else BorderStroke(Elev.hairline, palette.Mist),
        interactionSource = interaction,
        modifier = modifier.pressScale(interaction),
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
fun Dot(color: Color, size: Dp = Space.sm) {
    // Density-correct: radius derived in px from a Dp size, not a raw float px literal.
    Canvas(modifier = Modifier.size(size)) {
        drawCircle(color, radius = size.toPx() / 2f)
    }
}

/**
 * The one divider primitive — a 1dp [palette.Mist] hairline. Replaces every
 * hand-rolled Box+Surface divider. [inset] keeps the line clear of card edges.
 */
@Composable
fun HairlineDivider(modifier: Modifier = Modifier, inset: Dp = Space.xs) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = inset)
            .height(Elev.hairline)
            .background(palette.Mist)
    )
}

/** Section header label — muted titleLarge. One look for every "Setup"/"Tools" heading. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = palette.Smoke,
        modifier = modifier,
    )
}

/**
 * The one screen-title header: a back chevron + headline title, with optional trailing
 * [actions]. Replaces the hand-rolled IconButton + Text row that was copy-pasted across
 * ten screens, so the back hit-target, title style, and alignment stay identical in every
 * theme — and a long title wraps within the column instead of shoving past the edge.
 */
@Composable
fun ScreenHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable (RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = vocab.backLabel,
                tint = palette.Ink,
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = palette.Ink,
            // Centralizing the header lets every screen's title announce as a heading to
            // TalkBack in one place (headlineMedium is purely visual on its own).
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        actions?.invoke(this)
    }
}

/**
 * One look for every "nothing here yet" state — a static [MiniMascot] beside a muted
 * line, in a [SoftCard]. Replaces five ad-hoc per-screen treatments so an empty list
 * reads as intentional, not broken, in all three themes. [text] is the screen's vocab
 * empty string. Static mascot (no animation) keeps it deterministic for snapshots.
 */
@Composable
fun EmptyState(text: String, modifier: Modifier = Modifier) {
    SoftCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MiniMascot(locked = false, modifier = Modifier.size(EmptyStateMascotSize))
            HSpace(Space.md)
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.Smoke,
                // weight so a long / localized string wraps in the remaining width
                // instead of shoving the mascot or clipping.
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private val EmptyStateMascotSize = 40.dp

/**
 * Daily-goal halo for the hero mascot. Draws a faint full track and a [palette.Petal]
 * progress arc — turning [palette.Sage] once the goal is met — sweeping clockwise from
 * 12 o'clock around its [content]. Decorative only; [content] (the mascot) sits centered
 * inside. With [show] false it renders just the content (no goal set). Static, so it
 * stays deterministic in snapshots.
 */
@Composable
fun GoalRing(
    progress: Float,
    modifier: Modifier = Modifier,
    show: Boolean = true,
    content: @Composable () -> Unit,
) {
    val p = progress.coerceIn(0f, 1f)
    val arcColor = if (p >= 1f) palette.Sage else palette.Petal
    val trackColor = palette.Mist
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (show) {
            Canvas(modifier = Modifier.matchParentSize().padding(GoalRingInset)) {
                val stroke = GoalRingStroke.toPx()
                val d = size.minDimension - stroke
                val tl = Offset((size.width - d) / 2f, (size.height - d) / 2f)
                val arcSize = Size(d, d)
                // Track as a full circle (not a 360° arc) so there is no Round-cap seam
                // at 12 o'clock; the progress arc keeps rounded ends.
                drawCircle(trackColor, radius = d / 2f, center = center, style = Stroke(width = stroke))
                if (p > 0f) {
                    drawArc(arcColor, -90f, p * 360f, useCenter = false, topLeft = tl, size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round))
                }
            }
        }
        content()
    }
}

private val GoalRingStroke = 6.dp
private val GoalRingInset = 2.dp
