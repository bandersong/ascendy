package com.ascendy.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ascendy.app.R
import com.ascendy.app.ui.components.Badge
import com.ascendy.app.ui.components.SoftCard
import com.ascendy.app.ui.theme.ThemeVariant
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
    onPickTheme: (ThemeVariant) -> Unit,
    onPickSafetyMinutes: (Int) -> Unit,
    onPickGoalMinutes: (Int) -> Unit,
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
    val insets = WindowInsets.systemBars.asPaddingValues()
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(top = insets.calculateTopPadding(),
                     bottom = insets.calculateBottomPadding(),
                     start = 16.dp, end = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "back", tint = palette.Ink)
            }
            Text(vocab.settingsTitle, style = MaterialTheme.typography.headlineMedium, color = palette.Ink)
        }

        Spacer(Modifier.height(8.dp))

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
        Spacer(Modifier.height(8.dp))

        ThemeCard(
            variant = ThemeVariant.Kawaii,
            label = vocab.settingsKawaiiLabel,
            tagline = vocab.settingsKawaiiTagline,
            selected = current == ThemeVariant.Kawaii,
            onClick = { onPickTheme(ThemeVariant.Kawaii) }
        )
        Spacer(Modifier.height(8.dp))
        ThemeCard(
            variant = ThemeVariant.Tough,
            label = vocab.settingsToughLabel,
            tagline = vocab.settingsToughTagline,
            selected = current == ThemeVariant.Tough,
            onClick = { onPickTheme(ThemeVariant.Tough) }
        )
        Spacer(Modifier.height(8.dp))
        ThemeCard(
            variant = ThemeVariant.Neutral,
            label = vocab.settingsNeutralLabel,
            tagline = vocab.settingsNeutralTagline,
            selected = current == ThemeVariant.Neutral,
            onClick = { onPickTheme(ThemeVariant.Neutral) }
        )

        Spacer(Modifier.height(20.dp))

        Text(
            "more",
            style = MaterialTheme.typography.titleLarge,
            color = palette.Smoke
        )
        Spacer(Modifier.height(8.dp))

        SettingsRow(label = vocab.rowPairTagLabel, onClick = onOpenTags)
        Spacer(Modifier.height(8.dp))
        SettingsRow(label = vocab.rowFocusListLabel, onClick = onOpenLists)
        Spacer(Modifier.height(8.dp))
        SettingsRow(label = vocab.rowPermissionsLabel, onClick = onOpenPermissions)
        Spacer(Modifier.height(8.dp))
        SettingsRow(label = vocab.settingsRowStats, onClick = onOpenStats)
        Spacer(Modifier.height(8.dp))
        SettingsRow(label = vocab.settingsRowSchedules, onClick = onOpenSchedules)
        Spacer(Modifier.height(8.dp))
        SettingsRow(label = vocab.settingsRowPomodoro, onClick = onOpenPomodoro)
        if (com.ascendy.app.BuildConfig.HAS_INAPP_UPDATER) {
            Spacer(Modifier.height(8.dp))
            SettingsRow(label = vocab.settingsRowUpdate, onClick = onOpenUpdates)
        }
        Spacer(Modifier.height(8.dp))
        SettingsRow(label = vocab.settingsRowAbout, onClick = onOpenAbout)

        Spacer(Modifier.height(20.dp))

        SoftCard(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
            Column {
                Text(vocab.goalTitle,
                    style = MaterialTheme.typography.titleMedium, color = palette.Ink)
                Spacer(Modifier.height(4.dp))
                Text(vocab.goalBody,
                    style = MaterialTheme.typography.bodySmall, color = palette.Smoke)
                Spacer(Modifier.height(10.dp))
                for (rowIdx in 0 until 2) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)) {
                        for (colIdx in 0 until 3) {
                            val mins = goalChoices[rowIdx * 3 + colIdx]
                            val sel = dailyGoalMinutes == mins
                            Surface(
                                onClick = { onPickGoalMinutes(mins) },
                                color = if (sel) palette.Petal else palette.Mist,
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    if (mins % 60 == 0) "${mins / 60}h" else "${mins}m",
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.Ink,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                    if (rowIdx == 0) Spacer(Modifier.height(6.dp))
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        SoftCard(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
            Column {
                Text(vocab.safetyTimerTitle,
                    style = MaterialTheme.typography.titleMedium, color = palette.Ink)
                Spacer(Modifier.height(4.dp))
                Text(vocab.safetyTimerBody,
                    style = MaterialTheme.typography.bodySmall, color = palette.Smoke)
                Spacer(Modifier.height(10.dp))
                for (rowIdx in 0 until 2) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)) {
                        for (colIdx in 0 until 3) {
                            val mins = safetyChoices[rowIdx * 3 + colIdx]
                            val sel = safetyMinutes == mins
                            Surface(
                                onClick = { onPickSafetyMinutes(mins) },
                                color = if (sel) palette.Petal else palette.Mist,
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    if (mins % 60 == 0) "${mins / 60}h" else "${mins}m",
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.Ink,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                    if (rowIdx == 0) Spacer(Modifier.height(6.dp))
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        SoftCard(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant) {
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
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
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
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
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
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleMedium, color = palette.Ink)
                Text(tagline, style = MaterialTheme.typography.bodySmall, color = palette.Smoke)
            }
            if (selected) Badge(label = vocab.settingsBadgeActive, color = palette.Sage)
            else Badge(label = vocab.settingsBadgeSelect, color = palette.Mint)
        }
    }
}
