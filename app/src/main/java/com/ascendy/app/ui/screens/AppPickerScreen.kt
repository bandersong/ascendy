package com.ascendy.app.ui.screens

import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.ascendy.app.ui.components.EmptyState
import com.ascendy.app.ui.components.PageFrame
import com.ascendy.app.ui.components.ScreenHeader
import com.ascendy.app.ui.theme.HSpace
import com.ascendy.app.ui.theme.Space
import com.ascendy.app.ui.theme.VSpace
import com.ascendy.app.ui.theme.palette
import com.ascendy.app.ui.theme.vocab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppInfo(
    val packageName: String,
    val label: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerScreen(
    listName: String,
    blockedPackages: Set<String>,
    blockedDomains: List<String>,
    onTogglePackage: (pkg: String, blocked: Boolean) -> Unit,
    onAddDomain: (String) -> Unit,
    onRemoveDomain: (String) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current

    var tab by remember { mutableStateOf(0) }     // 0=apps, 1=sites
    var apps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var loadingApps by remember { mutableStateOf(true) }
    val iconCache = remember { mutableStateMapOf<String, Painter>() }
    var query by remember { mutableStateOf("") }
    var newDomain by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        // Phase 1: labels only, off the main thread. Fast — single IPC to system_server.
        val labels = withContext(Dispatchers.IO) { loadLaunchableAppLabels(context.packageManager) }
        apps = labels
        loadingApps = false

        // Phase 2: icons stream in. Each icon's row recomposes individually via stateMap.
        withContext(Dispatchers.IO) {
            for (info in labels) {
                if (iconCache.containsKey(info.packageName)) continue
                val painter = loadIcon(context.packageManager, info.packageName) ?: continue
                withContext(Dispatchers.Main) {
                    iconCache[info.packageName] = painter
                }
            }
        }
    }

    val filtered by remember(query, apps) {
        derivedStateOf {
            if (query.isBlank()) apps
            else apps.filter { it.label.contains(query, ignoreCase = true) }
        }
    }

    PageFrame {
        ScreenHeader(title = listName, onBack = onBack)

        Row(modifier = Modifier.fillMaxWidth().padding(vertical = Space.xs)) {
            TabChip(label = vocab.pickerTabApps, selected = tab == 0, onClick = { tab = 0 }, modifier = Modifier.weight(1f))
            HSpace(Space.sm)
            TabChip(label = vocab.pickerTabSites, selected = tab == 1, onClick = { tab = 1 }, modifier = Modifier.weight(1f))
        }

        if (tab == 0) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text(vocab.pickerSearch) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Space.sm),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            if (loadingApps) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filtered, key = { it.packageName }) { app ->
                    AppRow(
                        app = app,
                        icon = iconCache[app.packageName],
                        checked = app.packageName in blockedPackages,
                        onCheckedChange = { onTogglePackage(app.packageName, it) }
                    )
                    VSpace(Space.sm)
                }
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = Space.sm),
                verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newDomain,
                    onValueChange = { newDomain = it },
                    label = { Text(vocab.pickerSitesAddHint) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                HSpace(Space.sm)
                Button(onClick = {
                    val cleaned = newDomain.trim()
                    if (cleaned.isNotEmpty() && '.' in cleaned) {
                        onAddDomain(cleaned)
                        newDomain = ""
                    }
                }) { Text(vocab.pickerSitesAdd) }
            }

            VSpace(Space.xs)
            Text(
                vocab.pickerSitesNote,
                style = MaterialTheme.typography.bodySmall,
                color = palette.Smoke
            )
            VSpace(Space.sm)

            if (blockedDomains.isEmpty()) {
                EmptyState(vocab.pickerSitesEmpty)
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(blockedDomains, key = { it }) { d ->
                        DomainRow(domain = d, onRemove = { onRemoveDomain(d) })
                        VSpace(Space.sm)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        color = if (selected) palette.Petal else palette.Cloud,
        shape = MaterialTheme.shapes.medium,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, palette.Mist),
        modifier = modifier
    ) {
        Text(
            label,
            modifier = Modifier.padding(vertical = Space.md),
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) palette.onPetal else palette.Ink,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DomainRow(domain: String, onRemove: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = Space.lg, vertical = Space.lg)
        ) {
            Text(domain, style = MaterialTheme.typography.titleMedium, color = palette.Ink,
                modifier = Modifier.weight(1f))
            androidx.compose.material3.TextButton(onClick = onRemove) {
                Text(vocab.tagsRemove)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppRow(app: AppInfo, icon: Painter?, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        onClick = { onCheckedChange(!checked) },
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = Space.lg, vertical = Space.lg)
        ) {
            if (icon != null) {
                Image(painter = icon, contentDescription = null, modifier = Modifier.size(36.dp))
            } else {
                // Letter-avatar placeholder while the real icon decodes in the background
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(palette.Mist),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        app.label.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.Smoke
                    )
                }
            }
            HSpace(Space.md)
            Column(Modifier.weight(1f)) {
                Text(app.label, style = MaterialTheme.typography.titleMedium, color = palette.Ink)
                Text(app.packageName, style = MaterialTheme.typography.bodySmall, color = palette.Smoke)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

/** Fast label-only enumeration: one IPC for all launchable apps. */
private fun loadLaunchableAppLabels(pm: PackageManager): List<AppInfo> {
    return try {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        pm.queryIntentActivities(intent, 0)
            .asSequence()
            .map { it.activityInfo.applicationInfo }
            .distinctBy { it.packageName }
            .map { info ->
                AppInfo(
                    packageName = info.packageName,
                    label = try { pm.getApplicationLabel(info).toString() } catch (_: Exception) { info.packageName }
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    } catch (_: Exception) {
        emptyList()
    }
}

private fun loadIcon(pm: PackageManager, packageName: String): Painter? {
    return try {
        val drawable = pm.getApplicationIcon(packageName)
        val bmp = drawable.toBitmap(width = 72, height = 72)
        BitmapPainter(bmp.asImageBitmap())
    } catch (_: Exception) { null }
}
