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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ascendy.app.blocking.BlockState
import com.ascendy.app.ui.components.Badge
import com.ascendy.app.ui.components.Mascot
import com.ascendy.app.ui.components.SoftCard
import com.ascendy.app.ui.theme.palette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    tagCount: Int,
    listCount: Int,
    permissionsReady: Boolean,
    onPairTag: () -> Unit,
    onOpenLists: () -> Unit,
    onOpenPermissions: () -> Unit,
    onOpenSettings: () -> Unit,
    onEmergencyUnlock: () -> Unit,
) {
    val active by BlockState.active.collectAsState()
    val insets = WindowInsets.systemBars.asPaddingValues()
    val scroll = rememberScrollState()

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
                "ascendy ♡",
                style = MaterialTheme.typography.headlineMedium,
                color = palette.Ink
            )
            Spacer(Modifier.weight(1f))
            Badge(
                label = if (active) "focusing" else "ready",
                color = if (active) palette.Lilac else palette.Sage
            )
            Spacer(Modifier.size(8.dp))
            androidx.compose.material3.IconButton(onClick = onOpenSettings) {
                androidx.compose.material3.Icon(
                    androidx.compose.material.icons.Icons.Rounded.Settings,
                    contentDescription = "settings",
                    tint = palette.Ink
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        SoftCard(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.Center) {
                    Mascot(locked = active)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    if (active) "you're focusing — tap your tag to come back 🌙"
                    else "tap your tag whenever you're ready ✨",
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.Ink
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "setup",
            style = MaterialTheme.typography.titleLarge,
            color = palette.Smoke
        )
        Spacer(Modifier.height(8.dp))

        SetupRow(
            emoji = "🌸",
            title = "pair an nfc tag",
            badge = if (tagCount > 0) "$tagCount" else "todo",
            badgeColor = if (tagCount > 0) palette.Sage else palette.Petal,
            onClick = onPairTag
        )
        Spacer(Modifier.height(8.dp))
        SetupRow(
            emoji = "✨",
            title = "build your focus list",
            badge = if (listCount > 0) "$listCount" else "todo",
            badgeColor = if (listCount > 0) palette.Sage else palette.Petal,
            onClick = onOpenLists
        )
        Spacer(Modifier.height(8.dp))
        SetupRow(
            emoji = "🔒",
            title = "permissions",
            badge = if (permissionsReady) "ok" else "todo",
            badgeColor = if (permissionsReady) palette.Sage else palette.Petal,
            onClick = onOpenPermissions
        )

        if (active) {
            Spacer(Modifier.height(24.dp))
            SoftCard(color = MaterialTheme.colorScheme.surfaceVariant) {
                Column {
                    Text("emergency unlock", style = MaterialTheme.typography.titleMedium, color = palette.Ink)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "one-time per session. for true emergencies only.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.Smoke
                    )
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onEmergencyUnlock) {
                        Text("use unlock")
                    }
                }
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
            Text(emoji, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.size(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = palette.Ink)
            Spacer(Modifier.weight(1f))
            Badge(label = badge, color = badgeColor)
        }
    }
}
