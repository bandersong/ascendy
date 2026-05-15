package com.ascendy.app.ui.screens

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ascendy.app.ui.components.Badge
import com.ascendy.app.ui.components.MiniMascot
import com.ascendy.app.ui.components.SoftCard
import com.ascendy.app.ui.theme.ThemeVariant
import com.ascendy.app.ui.theme.palette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    current: ThemeVariant,
    onPickTheme: (ThemeVariant) -> Unit,
    onBack: () -> Unit,
) {
    val insets = WindowInsets.systemBars.asPaddingValues()

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
            Text("settings", style = MaterialTheme.typography.headlineMedium, color = palette.Ink)
        }

        Spacer(Modifier.height(8.dp))

        Text(
            when (current) {
                ThemeVariant.Kawaii -> "theme: kawaii ♡"
                ThemeVariant.Tough -> "theme: tough ⛓"
            },
            style = MaterialTheme.typography.titleLarge,
            color = palette.Smoke
        )
        Spacer(Modifier.height(8.dp))

        ThemeCard(
            label = "kawaii",
            tagline = "soft pink, blush cheeks, soothing curves ♡",
            variant = ThemeVariant.Kawaii,
            selected = current == ThemeVariant.Kawaii,
            onClick = { onPickTheme(ThemeVariant.Kawaii) }
        )
        Spacer(Modifier.height(8.dp))
        ThemeCard(
            label = "tough",
            tagline = "iron chains, hard edges, scowling mascot ⛓",
            variant = ThemeVariant.Tough,
            selected = current == ThemeVariant.Tough,
            onClick = { onPickTheme(ThemeVariant.Tough) }
        )

        Spacer(Modifier.height(24.dp))
        SoftCard(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant) {
            Text(
                "more themes coming soon — drop ideas via the github repo.",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.Smoke
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeCard(
    label: String,
    tagline: String,
    variant: ThemeVariant,
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
            Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                // tiny live preview of the variant's mascot
                MiniMascot(locked = false, modifier = Modifier.fillMaxSize())
            }
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleMedium, color = palette.Ink)
                Text(tagline, style = MaterialTheme.typography.bodySmall, color = palette.Smoke)
            }
            if (selected) Badge(label = "active", color = palette.Sage)
            else Badge(label = "tap", color = palette.Mint)
        }
    }
}
