package com.ascendy.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ascendy.app.ui.components.Badge
import com.ascendy.app.ui.components.SoftCard
import com.ascendy.app.ui.theme.AscendyColors

data class PermissionStatus(
    val accessibility: Boolean,
    val usageStats: Boolean,
    val overlay: Boolean,
    val notifications: Boolean,
)

@Composable
fun PermissionsScreen(
    status: PermissionStatus,
    onBack: () -> Unit,
    onRequestNotifications: () -> Unit,
) {
    val context = LocalContext.current
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
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "back", tint = AscendyColors.Ink)
            }
            Text("permissions", style = MaterialTheme.typography.headlineMedium, color = AscendyColors.Ink)
        }

        Spacer(Modifier.height(8.dp))

        Text(
            "ascendy needs these to block apps. nothing leaves your device.",
            style = MaterialTheme.typography.bodyMedium,
            color = AscendyColors.Smoke
        )

        Spacer(Modifier.height(16.dp))

        PermissionCard(
            title = "accessibility",
            body = "the primary blocking path. watches the foreground app and bounces you home.",
            granted = status.accessibility,
            actionLabel = "open settings",
            onClick = {
                context.startActivity(
                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        )
        Spacer(Modifier.height(8.dp))

        PermissionCard(
            title = "usage access",
            body = "fallback path used if accessibility is unavailable (e.g. android 17 advanced protection).",
            granted = status.usageStats,
            actionLabel = "open settings",
            onClick = {
                context.startActivity(
                    Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        )
        Spacer(Modifier.height(8.dp))

        PermissionCard(
            title = "display over other apps",
            body = "lets the kawaii blocker overlay show on top of blocked apps.",
            granted = status.overlay,
            actionLabel = "open settings",
            onClick = {
                context.startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + context.packageName)
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        )
        Spacer(Modifier.height(8.dp))

        PermissionCard(
            title = "notifications",
            body = "the focus session shows a persistent notification while active (android 13+).",
            granted = status.notifications,
            actionLabel = "allow",
            onClick = onRequestNotifications
        )

        Spacer(Modifier.height(24.dp))

        SoftCard(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant) {
            Column {
                Text("a heads-up about android 17", style = MaterialTheme.typography.titleMedium, color = AscendyColors.Ink)
                Spacer(Modifier.height(6.dp))
                Text(
                    "advanced protection mode disables accessibility services for apps not categorised as accessibility tools. ascendy isn't one — so when aapm is on, only the usage-stats path runs.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AscendyColors.Smoke
                )
            }
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    body: String,
    granted: Boolean,
    actionLabel: String,
    onClick: () -> Unit,
) {
    SoftCard(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = AscendyColors.Ink)
                Spacer(Modifier.weight(1f))
                Badge(
                    label = if (granted) "ok" else "missing",
                    color = if (granted) AscendyColors.Sage else AscendyColors.Petal
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium, color = AscendyColors.Smoke)
            if (!granted) {
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = onClick) { Text(actionLabel) }
            }
        }
    }
}
