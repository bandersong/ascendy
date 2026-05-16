package com.ascendy.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ascendy.app.blocking.BlockState
import com.ascendy.app.ui.components.Badge
import com.ascendy.app.ui.components.Mascot
import com.ascendy.app.ui.components.SoftCard
import com.ascendy.app.ui.theme.palette
import com.ascendy.app.ui.theme.vocab
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    tagCount: Int,
    listCount: Int,
    permissionsReady: Boolean,
    streakDays: Int,
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
    val insets = WindowInsets.systemBars.asPaddingValues()
    val scroll = rememberScrollState()
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(top = 20.dp + insets.calculateTopPadding(),
                     bottom = 24.dp + insets.calculateBottomPadding(),
                     start = 20.dp, end = 20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                vocab.appTitle,
                style = MaterialTheme.typography.headlineMedium,
                color = palette.Ink
            )
            Spacer(Modifier.weight(1f))
            Badge(
                label = if (active) vocab.statusFocusing else vocab.statusReady,
                color = if (active) palette.Lilac else palette.Sage
            )
            Spacer(Modifier.size(8.dp))
            IconButton(onClick = onScanQr) {
                Icon(Icons.Rounded.QrCodeScanner, contentDescription = vocab.homeScanLabel, tint = palette.Ink)
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Rounded.Settings, contentDescription = "settings", tint = palette.Ink)
            }
        }

        Spacer(Modifier.height(24.dp))

        SoftCard(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = onManualToggle
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Mascot(locked = active, streakDays = streakDays)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    if (active) vocab.homeHeroActive else vocab.homeHeroIdle,
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.Ink
                )
                if (active) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        formatElapsed(startedAt, nowMs),
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.Smoke
                    )
                } else {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        vocab.toastLongPressHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.Smoke
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            vocab.sectionSetup,
            style = MaterialTheme.typography.titleLarge,
            color = palette.Smoke
        )
        Spacer(Modifier.height(8.dp))

        SetupRow(
            emoji = vocab.rowPairTagEmoji,
            title = vocab.rowPairTagLabel,
            badge = if (tagCount > 0) "$tagCount" else vocab.badgeTodo,
            badgeColor = if (tagCount > 0) palette.Sage else palette.Petal,
            onClick = onPairTag
        )
        Spacer(Modifier.height(8.dp))
        SetupRow(
            emoji = vocab.rowFocusListEmoji,
            title = vocab.rowFocusListLabel,
            badge = if (active) vocab.homeBadgeBlockedFmt.format(blockedSet.size)
                    else if (listCount > 0) "$listCount"
                    else vocab.badgeTodo,
            badgeColor = if (active) palette.Lilac
                         else if (listCount > 0) palette.Sage
                         else palette.Petal,
            onClick = onOpenLists
        )
        Spacer(Modifier.height(8.dp))
        SetupRow(
            emoji = vocab.rowPermissionsEmoji,
            title = vocab.rowPermissionsLabel,
            badge = if (permissionsReady) vocab.badgeOk else vocab.badgeTodo,
            badgeColor = if (permissionsReady) palette.Sage else palette.Petal,
            onClick = onOpenPermissions
        )

        Spacer(Modifier.height(20.dp))

        // Stats + Pomodoro tiles
        Row(modifier = Modifier.fillMaxWidth()) {
            HomeTile(
                title = vocab.statsTitle,
                subtitle = if (streakDays > 0) vocab.statsStreakFmt.format(streakDays) else "",
                onClick = onOpenStats,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.size(8.dp))
            HomeTile(
                title = vocab.pomodoroTitle,
                subtitle = vocab.pomodoro25,
                onClick = onOpenPomodoro,
                modifier = Modifier.weight(1f)
            )
        }

        // Live session diagnostics — helps verify domain/app counts and accessibility status
        if (active) {
            Spacer(Modifier.height(12.dp))
            SoftCard(modifier = Modifier.fillMaxWidth(), color = palette.Cloud) {
                Column {
                    Text(
                        "blocked: ${blockedSet.size} app${if (blockedSet.size == 1) "" else "s"} · ${blockedDomains.size} site${if (blockedDomains.size == 1) "" else "s"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.Ink
                    )
                    if (blockedDomains.isNotEmpty()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            blockedDomains.take(5).joinToString(", ") +
                                if (blockedDomains.size > 5) "…" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.Smoke
                        )
                    }
                }
            }
        }

        if (active && strict) {
            Spacer(Modifier.height(12.dp))
            SoftCard(color = MaterialTheme.colorScheme.surfaceVariant) {
                Text(
                    vocab.strictModeNote,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.Smoke
                )
            }
        } else if (active && emergencyAvailable) {
            Spacer(Modifier.height(24.dp))
            SoftCard(color = MaterialTheme.colorScheme.surfaceVariant) {
                Column {
                    Text(vocab.emergencyTitle, style = MaterialTheme.typography.titleMedium, color = palette.Ink)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        vocab.emergencyBody,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.Smoke
                    )
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { showFrictionDialog = true }) {
                        Text(vocab.emergencyButton)
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
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        sentence,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.Ink
                    )
                }
                Spacer(Modifier.height(12.dp))
                androidx.compose.material3.OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text(vocab.frictionInputLabel) },
                    modifier = Modifier.fillMaxWidth()
                )
                if (matches) {
                    Spacer(Modifier.height(6.dp))
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
private fun HomeTile(title: String, subtitle: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = palette.Ink)
            if (subtitle.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = palette.Smoke)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetupRow(emoji: String, title: String, badge: String, badgeColor: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (emoji.isNotEmpty()) {
                Text(emoji, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.size(12.dp))
            }
            Text(title, style = MaterialTheme.typography.titleMedium, color = palette.Ink)
            Spacer(Modifier.weight(1f))
            Badge(label = badge, color = badgeColor)
        }
    }
}
