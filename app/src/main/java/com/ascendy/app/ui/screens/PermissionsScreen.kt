package com.ascendy.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.ascendy.app.service.OemBattery
import com.ascendy.app.service.SettingsLauncher
import com.ascendy.app.ui.components.Badge
import com.ascendy.app.ui.components.PageColumn
import com.ascendy.app.ui.components.ScreenHeader
import com.ascendy.app.ui.components.SoftCard
import com.ascendy.app.ui.theme.HSpace
import com.ascendy.app.ui.theme.Space
import com.ascendy.app.ui.theme.VSpace
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

    // Every settings hop goes through SettingsLauncher: these screens are missing on some ROMs and
    // a bare startActivity would throw ActivityNotFoundException right in the user's face.
    val openA11ySettings = {
        SettingsLauncher.open(context, Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
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
        ScreenHeader(title = vocab.permsTitle, onBack = onBack)

        VSpace(Space.sm)

        Text(
            vocab.permsIntro,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.Smoke
        )

        VSpace(Space.lg)

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
        VSpace(Space.sm)

        PermissionCard(
            emoji = vocab.permsUsageEmoji,
            title = vocab.permsUsageTitle,
            body = vocab.permsUsageBody,
            granted = status.usageStats,
            actionLabel = vocab.permsOpenSettings,
            onClick = {
                SettingsLauncher.open(context, Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
        )
        VSpace(Space.sm)

        PermissionCard(
            emoji = vocab.permsOverlayEmoji,
            title = vocab.permsOverlayTitle,
            body = vocab.permsOverlayBody,
            granted = status.overlay,
            actionLabel = vocab.permsOpenSettings,
            onClick = {
                SettingsLauncher.open(
                    context,
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + context.packageName)
                    ),
                )
            }
        )
        VSpace(Space.sm)

        PermissionCard(
            emoji = vocab.permsNotificationsEmoji,
            title = vocab.permsNotificationsTitle,
            body = vocab.permsNotificationsBody,
            granted = status.notifications,
            actionLabel = vocab.permsAllow,
            onClick = onRequestNotifications
        )
        VSpace(Space.sm)

        PermissionCard(
            emoji = vocab.permsBatteryEmoji,
            title = vocab.permsBatteryTitle,
            body = vocab.permsBatteryBody,
            granted = status.batteryExempt,
            actionLabel = vocab.permsBatteryAction,
            onClick = {
                SettingsLauncher.open(
                    context,
                    Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:" + context.packageName)
                    ),
                    // Some ROMs drop the per-app dialog but keep the global list.
                    Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS),
                )
            }
        )
        VSpace(Space.sm)

        PermissionCard(
            emoji = vocab.permsVpnEmoji,
            title = vocab.permsVpnTitle,
            body = vocab.permsVpnBody,
            granted = status.vpnConsented,
            actionLabel = vocab.permsVpnAction,
            onClick = onRequestVpn
        )

        // NH-04: on aggressive OEM skins the battery exemption alone doesn't stop the OS freezing the
        // service — point the user to the vendor auto-start / protected-apps screen. Only shown there.
        if (OemBattery.isAggressiveOem()) {
            VSpace(Space.sm)
            ActionCard(
                title = vocab.permsOemTitle,
                body = vocab.permsOemBody,
                actionLabel = vocab.permsOemAction,
                onClick = { OemBattery.openBestSettings(context) },
            )
        }

        VSpace(Space.xxl)

        // NH-03: surface Lockdown where hardening-minded users will see it (it lives in Settings).
        SoftCard(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant) {
            Column {
                Text(vocab.permsLockdownTitle, style = MaterialTheme.typography.titleMedium, color = palette.Ink)
                VSpace(Space.sm)
                Text(vocab.permsLockdownBody, style = MaterialTheme.typography.bodyMedium, color = palette.Smoke)
            }
        }

        VSpace(Space.md)

        SoftCard(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant) {
            Column {
                Text(vocab.permsAapmHeader, style = MaterialTheme.typography.titleMedium, color = palette.Ink)
                VSpace(Space.sm)
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
private fun ActionCard(
    title: String,
    body: String,
    actionLabel: String,
    onClick: () -> Unit,
) {
    SoftCard(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, color = palette.Ink)
            VSpace(Space.sm)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = palette.Smoke)
            VSpace(Space.xs)
            TextButton(onClick = onClick) { Text(actionLabel) }
        }
    }
}

/**
 * Granted/Missing is the whole point of this card, so the badge must never lose its label.
 * In a plain Row the unweighted title was measured first and took 85% of the width; at fontScale
 * 2.0 the badge collapsed to a bare coloured sliver with ZERO text nodes in the accessibility
 * tree, leaving the permission's state conveyed by colour alone. A [FlowRow] lets the pill keep
 * its intrinsic width and drop to its own line instead of being crushed.
 */
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
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(Space.sm),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (emoji.isNotEmpty()) {
                        Text(emoji, style = MaterialTheme.typography.headlineSmall)
                        HSpace(Space.md)
                    }
                    Text(title, style = MaterialTheme.typography.titleMedium, color = palette.Ink)
                }
                Badge(
                    label = if (granted) vocab.permsBadgeOk else vocab.permsBadgeMissing,
                    color = if (granted) palette.Sage else palette.Petal
                )
            }
            VSpace(Space.sm)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = palette.Smoke)
            if (!granted) {
                VSpace(Space.xs)
                TextButton(onClick = onClick) { Text(actionLabel) }
            }
        }
    }
}
