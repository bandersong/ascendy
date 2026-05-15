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
import androidx.compose.foundation.layout.size
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
import com.ascendy.app.ui.theme.palette
import com.ascendy.app.ui.theme.vocab

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
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "back", tint = palette.Ink)
            }
            Text(vocab.permsTitle, style = MaterialTheme.typography.headlineMedium, color = palette.Ink)
        }

        Spacer(Modifier.height(8.dp))

        Text(
            vocab.permsIntro,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.Smoke
        )

        Spacer(Modifier.height(16.dp))

        PermissionCard(
            emoji = vocab.permsAccessibilityEmoji,
            title = vocab.permsAccessibilityTitle,
            body = vocab.permsAccessibilityBody,
            granted = status.accessibility,
            actionLabel = vocab.permsOpenSettings,
            onClick = {
                context.startActivity(
                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        )
        Spacer(Modifier.height(8.dp))

        PermissionCard(
            emoji = vocab.permsUsageEmoji,
            title = vocab.permsUsageTitle,
            body = vocab.permsUsageBody,
            granted = status.usageStats,
            actionLabel = vocab.permsOpenSettings,
            onClick = {
                context.startActivity(
                    Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        )
        Spacer(Modifier.height(8.dp))

        PermissionCard(
            emoji = vocab.permsOverlayEmoji,
            title = vocab.permsOverlayTitle,
            body = vocab.permsOverlayBody,
            granted = status.overlay,
            actionLabel = vocab.permsOpenSettings,
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
            emoji = vocab.permsNotificationsEmoji,
            title = vocab.permsNotificationsTitle,
            body = vocab.permsNotificationsBody,
            granted = status.notifications,
            actionLabel = vocab.permsAllow,
            onClick = onRequestNotifications
        )

        Spacer(Modifier.height(24.dp))

        SoftCard(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant) {
            Column {
                Text(vocab.permsAapmHeader, style = MaterialTheme.typography.titleMedium, color = palette.Ink)
                Spacer(Modifier.height(6.dp))
                Text(
                    vocab.permsAapmBody,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.Smoke
                )
            }
        }
    }
}

@Composable
private fun PermissionCard(
    emoji: String,
    title: String,
    body: String,
    granted: Boolean,
    actionLabel: String,
    onClick: () -> Unit,
) {
    SoftCard(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (emoji.isNotEmpty()) {
                    Text(emoji, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.size(10.dp))
                }
                Text(title, style = MaterialTheme.typography.titleMedium, color = palette.Ink)
                Spacer(Modifier.weight(1f))
                Badge(
                    label = if (granted) vocab.permsBadgeOk else vocab.permsBadgeMissing,
                    color = if (granted) palette.Sage else palette.Petal
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium, color = palette.Smoke)
            if (!granted) {
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = onClick) { Text(actionLabel) }
            }
        }
    }
}
