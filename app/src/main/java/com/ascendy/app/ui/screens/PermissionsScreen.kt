package com.ascendy.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ascendy.app.ui.components.Badge
import com.ascendy.app.ui.components.PageColumn
import com.ascendy.app.ui.components.SoftCard
import com.ascendy.app.ui.theme.palette
import com.ascendy.app.ui.theme.vocab

data class PermissionStatus(
    val accessibility: Boolean,
    val usageStats: Boolean,
    val overlay: Boolean,
    val notifications: Boolean,
    val batteryExempt: Boolean,
    val vpnConsented: Boolean,
)

@Composable
fun PermissionsScreen(
    status: PermissionStatus,
    a11yDisclosureAccepted: Boolean,
    onAcceptA11yDisclosure: () -> Unit,
    onBack: () -> Unit,
    onRequestNotifications: () -> Unit,
    onRequestVpn: () -> Unit,
) {
    val context = LocalContext.current
    var showA11yDisclosure by remember { mutableStateOf(false) }

    val openA11ySettings = {
        context.startActivity(
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    // Play prominent-disclosure: the service reads screen content (foreground app + browser URL
    // bar), so consent must be collected in-app BEFORE sending the user to enable it.
    if (showA11yDisclosure) {
        AlertDialog(
            onDismissRequest = { showA11yDisclosure = false },
            title = { Text(vocab.a11yDisclosureTitle) },
            text = { Text(vocab.a11yDisclosureBody) },
            confirmButton = {
                Button(onClick = {
                    showA11yDisclosure = false
                    onAcceptA11yDisclosure()
                    openA11ySettings()
                }) { Text(vocab.a11yDisclosureAgree) }
            },
            dismissButton = {
                TextButton(onClick = { showA11yDisclosure = false }) {
                    Text(vocab.a11yDisclosureDecline)
                }
            }
        )
    }

    PageColumn {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = vocab.backLabel, tint = palette.Ink)
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
                if (a11yDisclosureAccepted) openA11ySettings()
                else showA11yDisclosure = true
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
        Spacer(Modifier.height(8.dp))

        PermissionCard(
            emoji = vocab.permsBatteryEmoji,
            title = vocab.permsBatteryTitle,
            body = vocab.permsBatteryBody,
            granted = status.batteryExempt,
            actionLabel = vocab.permsBatteryAction,
            onClick = {
                context.startActivity(
                    Intent(
                        android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        android.net.Uri.parse("package:" + context.packageName)
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        )
        Spacer(Modifier.height(8.dp))

        PermissionCard(
            emoji = vocab.permsVpnEmoji,
            title = vocab.permsVpnTitle,
            body = vocab.permsVpnBody,
            granted = status.vpnConsented,
            actionLabel = vocab.permsVpnAction,
            onClick = onRequestVpn
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
