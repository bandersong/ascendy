package com.ascendy.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ascendy.app.blocking.BlockState
import com.ascendy.app.ui.components.Badge
import com.ascendy.app.ui.components.GoalRing
import com.ascendy.app.ui.components.HairlineDivider
import com.ascendy.app.ui.components.Mascot
import com.ascendy.app.ui.components.PageColumn
import com.ascendy.app.ui.components.SectionLabel
import com.ascendy.app.ui.components.SoftCard
import com.ascendy.app.ui.theme.Elev
import com.ascendy.app.ui.theme.HSpace
import com.ascendy.app.ui.theme.Space
import com.ascendy.app.ui.theme.VSpace
import com.ascendy.app.ui.theme.palette
import com.ascendy.app.ui.theme.pressScale
import com.ascendy.app.ui.theme.vocab
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    tagCount: Int,
    listCount: Int,
    permissionsReady: Boolean,
    streakDays: Int,
    todayFocusedMinutes: Int,
    dailyGoalMinutes: Int,
    onPairTag: () -> Unit,
    onOpenLists: () -> Unit,
    onOpenPermissions: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenPomodoro: () -> Unit,
    onScanQr: () -> Unit,
    onManualToggle: () -> Unit,
    onEmergencyUnlock: () -> Unit,
) {
    val active by BlockState.active.collectAsState()
    val startedAt by BlockState.startedAt.collectAsState()
    val blockedSet by BlockState.blocked.collectAsState()
    val blockedDomains by BlockState.blockedDomains.collectAsState()
    val emergencyAvailable by BlockState.emergencyAvailable.collectAsState()
    val strict by BlockState.strict.collectAsState()
    var showFrictionDialog by remember { mutableStateOf(false) }

    // re-tick every 30s while active to refresh the elapsed-minutes display
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(active) {
        if (active) {
            while (true) {
                nowMs = System.currentTimeMillis()
                delay(30_000L)
            }
        }
    }

    val setupAllDone = tagCount > 0 && listCount > 0 && permissionsReady

    PageColumn(centerWhenShort = true) {
        // Header — title + icon actions only
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                vocab.appTitle,
                style = MaterialTheme.typography.headlineMedium,
                color = palette.Ink,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onScanQr) {
                Icon(Icons.Rounded.QrCodeScanner, contentDescription = vocab.homeScanLabel, tint = palette.Ink)
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Rounded.Settings, contentDescription = vocab.settingsLabel, tint = palette.Ink)
            }
        }

        VSpace(Space.lg)

        // Hero card — mascot, status badge inline, hero text, timer (when active)
        SoftCard(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().animateContentSize()
            ) {
                Box(
                    modifier = Modifier
                        .size(176.dp)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = onManualToggle
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Daily-goal halo around the mascot — shows only when a goal is set.
                    GoalRing(
                        progress = if (dailyGoalMinutes > 0)
                            todayFocusedMinutes.toFloat() / dailyGoalMinutes else 0f,
                        show = dailyGoalMinutes > 0,
                        modifier = Modifier.matchParentSize(),
                    ) {
                        Mascot(locked = active, streakDays = streakDays)
                    }
                }
                VSpace(Space.sm)
                // Same reason as SetupRow: three unweighted pills in a plain Row means the last
                // ones get whatever width the first left behind. Wrap them instead of crushing.
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Space.sm, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(Space.sm),
                    itemVerticalAlignment = Alignment.CenterVertically,
                ) {
                    Badge(
                        label = if (active) vocab.statusFocusing else vocab.statusReady,
                        color = if (active) palette.Lilac else palette.Sage
                    )
                    if (active && strict) {
                        Badge(label = vocab.strictBadge, color = palette.Petal)
                    }
                    if (streakDays > 0) {
                        Badge(label = vocab.homeStreakBadgeFmt.format(streakDays), color = palette.Mint)
                    }
                }
                // Daily goal progress
                if (dailyGoalMinutes > 0) {
                    VSpace(Space.sm)
                    val goalHit = todayFocusedMinutes >= dailyGoalMinutes
                    Text(
                        if (goalHit) vocab.goalReached
                        else vocab.goalProgressFmt.format(todayFocusedMinutes, dailyGoalMinutes),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (goalHit) palette.Sage else palette.Smoke
                    )
                }
                VSpace(Space.md)
                // When active, the live timer is the hero: big + Ink, with the prompt demoted
                // to a quiet caption below it. When idle, the prompt itself is the focal line.
                AnimatedContent(
                    targetState = active,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "heroBlock"
                ) { isActive ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (isActive) {
                            Text(
                                formatElapsed(startedAt, nowMs),
                                style = MaterialTheme.typography.headlineMedium,
                                color = palette.Ink,
                                textAlign = TextAlign.Center
                            )
                            VSpace(Space.xs)
                            Text(
                                vocab.homeHeroActive,
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.Smoke,
                                textAlign = TextAlign.Center
                            )
                            if (blockedSet.size + blockedDomains.size > 0) {
                                VSpace(Space.sm)
                                Text(
                                    vocab.homeAppsSitesFmt.format(blockedSet.size, blockedDomains.size),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.Smoke
                                )
                            }
                        } else {
                            Text(
                                vocab.homeHeroIdle,
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.Ink,
                                textAlign = TextAlign.Center
                            )
                            if (!setupAllDone) {
                                VSpace(Space.xs)
                                Text(
                                    vocab.toastLongPressHint,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.Smoke,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        VSpace(Space.xl)

        // Setup section — single grouped card with check marks; collapses when all done.
        // Stays collapsed during an active session too, so starting a session never expands it.
        AnimatedContent(
            targetState = setupAllDone,
            transitionSpec = {
                (fadeIn() + expandVertically()) togetherWith (fadeOut() + shrinkVertically())
            },
            label = "setupSection"
        ) { allDone ->
        if (allDone) {
            SoftCard(modifier = Modifier.fillMaxWidth(), color = palette.Cloud) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint = palette.Sage,
                        modifier = Modifier.size(20.dp)
                    )
                    HSpace(Space.sm)
                    Text(
                        vocab.setupAllDone,
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.Ink
                    )
                }
            }
        } else {
            Column {
            SectionLabel(vocab.sectionSetup)
            VSpace(Space.sm)
            SoftCard(modifier = Modifier.fillMaxWidth(), color = palette.Surface) {
                Column {
                    SetupRow(
                        emoji = vocab.rowPairTagEmoji,
                        title = vocab.rowPairTagLabel,
                        done = tagCount > 0,
                        badge = if (tagCount > 0) "$tagCount" else vocab.badgeTodo,
                        onClick = onPairTag
                    )
                    HairlineDivider()
                    SetupRow(
                        emoji = vocab.rowFocusListEmoji,
                        title = vocab.rowFocusListLabel,
                        done = listCount > 0,
                        badge = if (active) vocab.homeBadgeBlockedFmt.format(blockedSet.size)
                                else if (listCount > 0) "$listCount"
                                else vocab.badgeTodo,
                        onClick = onOpenLists
                    )
                    HairlineDivider()
                    SetupRow(
                        emoji = vocab.rowPermissionsEmoji,
                        title = vocab.rowPermissionsLabel,
                        done = permissionsReady,
                        badge = if (permissionsReady) vocab.badgeOk else vocab.badgeTodo,
                        onClick = onOpenPermissions
                    )
                }
            }
            }
        }
        }

        VSpace(Space.lg)

        // Tools row — both tiles always present so the layout never re-justifies when a session
        // starts. Pomodoro is disabled (dimmed) during a session: starting a timer mid-session
        // makes no sense and would otherwise overwrite the running session.
        Row(modifier = Modifier.fillMaxWidth()) {
            HomeTile(
                title = vocab.statsTitle,
                subtitle = if (streakDays > 0) vocab.statsStreakFmt.format(streakDays) else "—",
                onClick = onOpenStats,
                modifier = Modifier.weight(1f)
            )
            HSpace(Space.sm)
            HomeTile(
                title = vocab.pomodoroTitle,
                subtitle = vocab.pomodoro25,
                onClick = onOpenPomodoro,
                enabled = !active,
                modifier = Modifier.weight(1f)
            )
        }

        // Strict notice or emergency-override card — fades/expands in when a session starts
        AnimatedVisibility(
            visible = active && strict,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                VSpace(Space.lg)
                SoftCard(modifier = Modifier.fillMaxWidth(), color = palette.Cloud) {
                    Text(
                        vocab.strictModeNote,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.Smoke
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = active && !strict && emergencyAvailable,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                VSpace(Space.lg)
                SoftCard(modifier = Modifier.fillMaxWidth(), color = palette.Cloud) {
                    Column {
                        Text(vocab.emergencyTitle, style = MaterialTheme.typography.titleMedium, color = palette.Ink)
                        VSpace(Space.xs)
                        Text(
                            vocab.emergencyBody,
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.Smoke
                        )
                        VSpace(Space.xs)
                        TextButton(onClick = { showFrictionDialog = true }) {
                            Text(vocab.emergencyButton)
                        }
                    }
                }
            }
        }
    }

    if (showFrictionDialog) {
        FrictionTaxDialog(
            sentence = vocab.frictionSentence,
            onDismiss = { showFrictionDialog = false },
            onConfirm = {
                showFrictionDialog = false
                onEmergencyUnlock()
            }
        )
    }
}

@Composable
private fun formatElapsed(startedAt: Long?, now: Long): String {
    if (startedAt == null) return vocab.timerJustStarted
    val elapsedMs = (now - startedAt).coerceAtLeast(0L)
    val totalMin = (elapsedMs / 60_000L).toInt()
    return when {
        totalMin < 1 -> vocab.timerJustStarted
        totalMin < 60 -> vocab.timerMinFmt.format(totalMin)
        else -> vocab.timerHourMinFmt.format(totalMin / 60, totalMin % 60)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTile(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        enabled = enabled,
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        interactionSource = interaction,
        modifier = modifier.pressScale(interaction)
    ) {
        Column(modifier = Modifier.padding(Space.lg).alpha(if (enabled) 1f else Elev.disabledAlpha)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = palette.Ink)
            VSpace(Space.xs)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = palette.Smoke)
        }
    }
}

/**
 * One "Setup" row: marker + title on the left, status pill on the right.
 *
 * A [FlowRow], not a Row — at fontScale 2.0 the old plain Row measured the unweighted title first,
 * so it swallowed the width and the pill was crushed to a 43dp column that rendered "TODO" as
 * T/O/D/O stacked vertically (and clipped the last letter of the title). Here the pill always keeps
 * its intrinsic width and simply reflows onto its own line when the title needs the full row.
 * SpaceBetween keeps the familiar title-left / pill-right look whenever both do fit on one line.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetupRow(emoji: String, title: String, done: Boolean, badge: String, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        color = androidx.compose.ui.graphics.Color.Transparent,
        interactionSource = interaction,
        modifier = Modifier.fillMaxWidth().pressScale(interaction)
    ) {
        FlowRow(
            modifier = Modifier.padding(horizontal = Space.xs, vertical = Space.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(Space.sm),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (done) {
                    Icon(Icons.Rounded.Check, contentDescription = null, tint = palette.Sage,
                        modifier = Modifier.size(20.dp))
                    HSpace(Space.sm)
                } else if (emoji.isNotEmpty()) {
                    Text(emoji, style = MaterialTheme.typography.titleLarge)
                    HSpace(Space.sm)
                }
                Text(title, style = MaterialTheme.typography.titleMedium, color = palette.Ink)
            }
            Badge(
                label = badge,
                color = if (done) palette.Sage else palette.Petal
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FrictionTaxDialog(
    sentence: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    var input by remember { mutableStateOf("") }
    val matches = input == sentence

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(vocab.emergencyConfirmTitle) },
        text = {
            Column {
                Text(vocab.frictionPrompt,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.Smoke)
                VSpace(Space.sm)
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        sentence,
                        modifier = Modifier.padding(Space.md),
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.Ink
                    )
                }
                VSpace(Space.md)
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text(vocab.frictionInputLabel) },
                    modifier = Modifier.fillMaxWidth()
                )
                if (matches) {
                    VSpace(Space.sm)
                    Text(vocab.frictionMatchOk,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.Sage)
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = matches) {
                Text(vocab.emergencyConfirmYes)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(vocab.emergencyConfirmNo)
            }
        }
    )
}
