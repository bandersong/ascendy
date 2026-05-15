package com.ascendy.app.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ascendy.app.ui.components.Badge
import com.ascendy.app.ui.components.MiniMascot
import com.ascendy.app.ui.components.SoftCard
import com.ascendy.app.ui.theme.LocalPalette
import com.ascendy.app.ui.theme.LocalVocab
import com.ascendy.app.ui.theme.ThemeVariant
import com.ascendy.app.ui.theme.palette
import com.ascendy.app.ui.theme.paletteFor
import com.ascendy.app.ui.theme.vocab
import com.ascendy.app.ui.theme.vocabFor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    current: ThemeVariant,
    onPickTheme: (ThemeVariant) -> Unit,
    onOpenStats: () -> Unit,
    onOpenSchedules: () -> Unit,
    onOpenPomodoro: () -> Unit,
    onBack: () -> Unit,
) {
    val insets = WindowInsets.systemBars.asPaddingValues()
    val dark = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
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
            dark = dark,
            label = vocab.settingsKawaiiLabel,
            tagline = vocab.settingsKawaiiTagline,
            selected = current == ThemeVariant.Kawaii,
            onClick = { onPickTheme(ThemeVariant.Kawaii) }
        )
        Spacer(Modifier.height(8.dp))
        ThemeCard(
            variant = ThemeVariant.Tough,
            dark = dark,
            label = vocab.settingsToughLabel,
            tagline = vocab.settingsToughTagline,
            selected = current == ThemeVariant.Tough,
            onClick = { onPickTheme(ThemeVariant.Tough) }
        )
        Spacer(Modifier.height(8.dp))
        ThemeCard(
            variant = ThemeVariant.Neutral,
            dark = dark,
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

        SettingsRow(label = vocab.settingsRowStats, onClick = onOpenStats)
        Spacer(Modifier.height(8.dp))
        SettingsRow(label = vocab.settingsRowSchedules, onClick = onOpenSchedules)
        Spacer(Modifier.height(8.dp))
        SettingsRow(label = vocab.settingsRowPomodoro, onClick = onOpenPomodoro)

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

/**
 * Each card renders its mascot under that variant's palette so the user sees three
 * different mascots. The chrome (background, text colour, badge) stays on the active
 * theme — only what's inside the per-variant provider swaps.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeCard(
    variant: ThemeVariant,
    dark: Boolean,
    label: String,
    tagline: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val variantPalette = paletteFor(variant, dark)
    val variantVocab = vocabFor(variant)

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
            // Variant-themed preview tile — palette swapped only for this Box's subtree
            Box(
                Modifier
                    .size(52.dp),
                contentAlignment = Alignment.Center
            ) {
                CompositionLocalProvider(
                    LocalPalette provides variantPalette,
                    LocalVocab provides variantVocab,
                ) {
                    Surface(
                        color = variantPalette.Cloud,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(Modifier.padding(4.dp), contentAlignment = Alignment.Center) {
                            MiniMascot(locked = false, modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }
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
