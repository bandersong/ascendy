package com.ascendy.app.ui.screens

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.ascendy.app.ui.theme.palette
import com.ascendy.app.ui.theme.vocab

data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Painter?
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
    val insets = WindowInsets.systemBars.asPaddingValues()

    var tab by remember { mutableStateOf(0) }     // 0=apps, 1=sites
    var apps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var newDomain by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        apps = loadApps(context.packageManager)
    }

    val filtered by remember(query, apps) {
        derivedStateOf {
            if (query.isBlank()) apps
            else apps.filter { it.label.contains(query, ignoreCase = true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = insets.calculateTopPadding(),
                     bottom = insets.calculateBottomPadding())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "back", tint = palette.Ink)
            }
            Text(listName, style = MaterialTheme.typography.headlineMedium, color = palette.Ink)
        }

        // Tabs
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
            TabChip(label = vocab.pickerTabApps, selected = tab == 0, onClick = { tab = 0 }, modifier = Modifier.weight(1f))
            Spacer(Modifier.size(8.dp))
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                items(filtered, key = { it.packageName }) { app ->
                    AppRow(
                        app = app,
                        checked = app.packageName in blockedPackages,
                        onCheckedChange = { onTogglePackage(app.packageName, it) }
                    )
                    Spacer(Modifier.height(6.dp))
                }
            }
        } else {
            // Sites tab
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
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
                Spacer(Modifier.size(8.dp))
                Button(onClick = {
                    val cleaned = newDomain.trim()
                    if (cleaned.isNotEmpty() && '.' in cleaned) {
                        onAddDomain(cleaned)
                        newDomain = ""
                    }
                }) { Text(vocab.pickerSitesAdd) }
            }

            Spacer(Modifier.size(4.dp))
            Text(
                vocab.pickerSitesNote,
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = palette.Smoke
            )
            Spacer(Modifier.size(8.dp))

            if (blockedDomains.isEmpty()) {
                Text(
                    vocab.pickerSitesEmpty,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.Smoke
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    items(blockedDomains, key = { it }) { d ->
                        DomainRow(domain = d, onRemove = { onRemoveDomain(d) })
                        Spacer(Modifier.height(6.dp))
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
        color = if (selected) palette.Petal else palette.Mist,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
    ) {
        Text(
            label,
            modifier = Modifier.padding(vertical = 10.dp),
            style = MaterialTheme.typography.titleMedium,
            color = palette.Ink,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
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
private fun AppRow(app: AppInfo, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        onClick = { onCheckedChange(!checked) },
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            if (app.icon != null) {
                Image(painter = app.icon, contentDescription = null, modifier = Modifier.size(36.dp))
                Spacer(Modifier.size(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(app.label, style = MaterialTheme.typography.titleMedium, color = palette.Ink)
                Text(app.packageName, style = MaterialTheme.typography.bodySmall, color = palette.Smoke)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

private fun loadApps(pm: PackageManager): List<AppInfo> {
    val flags = PackageManager.GET_META_DATA
    val packages = try {
        pm.getInstalledApplications(flags)
    } catch (_: Exception) { return emptyList() }

    return packages.asSequence()
        .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 || pm.getLaunchIntentForPackage(it.packageName) != null }
        .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
        .map { info ->
            val label = pm.getApplicationLabel(info).toString()
            val drawable = try { pm.getApplicationIcon(info) } catch (_: Exception) { null }
            val painter: Painter? = drawable?.let {
                try {
                    val bmp = it.toBitmap(width = 72, height = 72)
                    BitmapPainter(bmp.asImageBitmap())
                } catch (_: Exception) { null }
            }
            AppInfo(info.packageName, label, painter)
        }
        .sortedBy { it.label.lowercase() }
        .toList()
}
