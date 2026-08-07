package com.ascendy.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ascendy.app.R
import com.ascendy.app.ui.components.Badge
import com.ascendy.app.ui.components.PageColumn
import com.ascendy.app.ui.components.ScreenHeader
import com.ascendy.app.ui.components.SectionLabel
import com.ascendy.app.ui.components.SelectableChip
import com.ascendy.app.ui.components.SoftCard
import com.ascendy.app.ui.theme.HSpace
import com.ascendy.app.ui.theme.Space
import com.ascendy.app.ui.theme.ThemeVariant
import com.ascendy.app.ui.theme.VSpace
import com.ascendy.app.ui.theme.palette
import com.ascendy.app.ui.theme.vocab

private val safetyChoices = listOf(60, 120, 240, 480, 720, 1440)
private val goalChoices = listOf(30, 60, 120, 180, 240, 360)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    current: ThemeVariant,
    safetyMinutes: Int,
    dailyGoalMinutes: Int,
    lockdownEnabled: Boolean,
    lockdownLocked: Boolean,
    onPickTheme: (ThemeVariant) -> Unit,
    onPickSafetyMinutes: (Int) -> Unit,
    onPickGoalMinutes: (Int) -> Unit,
    onToggleLockdown: (Boolean) -> Unit,
    onOpenStats: () -> Unit,
    onOpenSchedules: () -> Unit,
    onOpenPomodoro: () -> Unit,
    onOpenUpdates: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenTags: () -> Unit,
    onOpenLists: () -> Unit,
    onOpenPermissions: () -> Unit,
    onBack: () -> Unit,
) {
    var showLockdownConfirm by rememberSaveable { mutableStateOf(false) }

    // Anti-uninstall is consent-gated: turning Lockdown ON requires reading exactly what it does
    // (device-admin + Settings-bounce) and confirming. Turning it OFF stays one tap.
    if (showLockdownConfirm) {
        AlertDialog(
            onDismissRequest = { showLockdownConfirm = false },
            title = { Text(vocab.lockdownConfirmTitle) },
            text = { Text(vocab.lockdownConfirmBody) },
            confirmButton = {
                Button(onClick = {
                    showLockdownConfirm = false
                    onToggleLockdown(true)
                }) { Text(vocab.lockdownConfirmYes) }
            },
            dismissButton = {
                TextButton(onClick = { showLockdownConfirm = false }) {
                    Text(vocab.lockdownConfirmNo)
                }
            }
        )
    }

    PageColumn {
        ScreenHeader(title = vocab.settingsTitle, onBack = onBack)

        VSpace(Space.sm)

        val currentLabel = when (current) {
            ThemeVariant.Kawaii -> vocab.settingsKawaiiLabel
            ThemeVariant.Tough -> vocab.settingsToughLabel
            ThemeVariant.Neutral -> vocab.settingsNeutralLabel
        }
        Text(
            vocab.settingsCurrentLabelFmt.format(currentLabel),
            style = MaterialTheme.typography.titleLarge,
            color = palette.Smoke
        )
        VSpace(Space.sm)

        ThemeCard(
            variant = ThemeVariant.Kawaii,
            label = vocab.settingsKawaiiLabel,
            tagline = vocab.settingsKawaiiTagline,
            selected = current == ThemeVariant.Kawaii,
            onClick = { onPickTheme(ThemeVariant.Kawaii) }
        )
        VSpace(Space.sm)
        ThemeCard(
            variant = ThemeVariant.Tough,
            label = vocab.settingsToughLabel,
            tagline = vocab.settingsToughTagline,
            selected = current == ThemeVariant.Tough,
            onClick = { onPickTheme(ThemeVariant.Tough) }
        )
        VSpace(Space.sm)
        ThemeCard(
            variant = ThemeVariant.Neutral,
            label = vocab.settingsNeutralLabel,
            tagline = vocab.settingsNeutralTagline,
            selected = current == ThemeVariant.Neutral,
            onClick = { onPickTheme(ThemeVariant.Neutral) }
        )

        VSpace(Space.xl)

        SectionLabel(vocab.settingsSectionMore)
        VSpace(Space.sm)

        SettingsRow(label = vocab.rowPairTagLabel, onClick = onOpenTags)
        VSpace(Space.sm)
        SettingsRow(label = vocab.rowFocusListLabel, onClick = onOpenLists)
        VSpace(Space.sm)
        SettingsRow(label = vocab.rowPermissionsLabel, onClick = onOpenPermissions)
        VSpace(Space.sm)
        SettingsRow(label = vocab.settingsRowStats, onClick = onOpenStats)
        VSpace(Space.sm)
        SettingsRow(label = vocab.settingsRowSchedules, onClick = onOpenSchedules)
        VSpace(Space.sm)
        SettingsRow(label = vocab.settingsRowPomodoro, onClick = onOpenPomodoro)
        if (com.ascendy.app.BuildConfig.HAS_INAPP_UPDATER) {
            VSpace(Space.sm)
            SettingsRow(label = vocab.settingsRowUpdate, onClick = onOpenUpdates)
        }
        VSpace(Space.sm)
        SettingsRow(label = vocab.settingsRowAbout, onClick = onOpenAbout)

        VSpace(Space.xl)

        SoftCard(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
            Column {
                Text(vocab.goalTitle,
                    style = MaterialTheme.typography.titleMedium, color = palette.Ink)
                VSpace(Space.xs)
                Text(vocab.goalBody,
                    style = MaterialTheme.typography.bodySmall, color = palette.Smoke)
                VSpace(Space.md)
                for (rowIdx in 0 until 2) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(Space.sm)) {
                        for (colIdx in 0 until 3) {
                            val mins = goalChoices[rowIdx * 3 + colIdx]
                            SelectableChip(
                                label = if (mins % 60 == 0) "${mins / 60}h" else "${mins}m",
                                selected = dailyGoalMinutes == mins,
                                onClick = { onPickGoalMinutes(mins) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    if (rowIdx == 0) VSpace(Space.sm)
                }
            }
        }

        VSpace(Space.sm)

        SoftCard(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
            Column {
                Text(vocab.safetyTimerTitle,
                    style = MaterialTheme.typography.titleMedium, color = palette.Ink)
                VSpace(Space.xs)
                Text(vocab.safetyTimerBody,
                    style = MaterialTheme.typography.bodySmall, color = palette.Smoke)
                VSpace(Space.md)
                for (rowIdx in 0 until 2) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(Space.sm)) {
                        for (colIdx in 0 until 3) {
                            val mins = safetyChoices[rowIdx * 3 + colIdx]
                            SelectableChip(
                                label = if (mins % 60 == 0) "${mins / 60}h" else "${mins}m",
                                selected = safetyMinutes == mins,
                                onClick = { onPickSafetyMinutes(mins) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    if (rowIdx == 0) VSpace(Space.sm)
                }
            }
        }

        VSpace(Space.sm)

        SoftCard(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(vocab.lockdownTitle,
                            style = MaterialTheme.typography.titleMedium, color = palette.Ink)
                        VSpace(Space.xs)
                        Text(vocab.lockdownBody,
                            style = MaterialTheme.typography.bodySmall, color = palette.Smoke)
                    }
                    HSpace(Space.md)
                    Switch(
                        checked = lockdownEnabled,
                        onCheckedChange = { on ->
                            if (on) showLockdownConfirm = true else onToggleLockdown(false)
                        },
                        enabled = !lockdownLocked,
                    )
                }
                if (lockdownLocked) {
                    VSpace(Space.sm)
                    Text(vocab.lockdownLockedNote,
                        style = MaterialTheme.typography.bodySmall, color = palette.Smoke)
                }
            }
        }

        VSpace(Space.xxl)
        SoftCard(modifier = Modifier.fillMaxWidth(), color = palette.Cloud) {
            Text(
                vocab.settingsFooter,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.Smoke
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsRow(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = Space.xl, vertical = Space.lg),
            style = MaterialTheme.typography.titleMedium,
            color = palette.Ink
        )
    }
}

/** Themed app-icon artwork shown as each theme's preview tile. */
private fun themeIconRes(variant: ThemeVariant): Int = when (variant) {
    ThemeVariant.Kawaii -> R.drawable.theme_icon_kawaii
    ThemeVariant.Tough -> R.drawable.theme_icon_tough
    ThemeVariant.Neutral -> R.drawable.theme_icon_neutral
}

/**
 * Each card shows that variant's themed icon artwork as a preview tile. The chrome
 * (background, text colour, badge) stays on the active theme.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeCard(
    variant: ThemeVariant,
    label: String,
    tagline: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Space.lg, vertical = Space.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Variant-themed preview tile — the icon artwork carries its own themed background
            Image(
                painter = painterResource(id = themeIconRes(variant)),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(52.dp)
                    .clip(MaterialTheme.shapes.medium)
            )
            HSpace(Space.lg)
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleMedium, color = palette.Ink)
                Text(tagline, style = MaterialTheme.typography.bodySmall, color = palette.Smoke)
            }
            if (selected) Badge(label = vocab.settingsBadgeActive, color = palette.Sage)
            else Badge(label = vocab.settingsBadgeSelect, color = palette.Mint)
        }
    }
}
