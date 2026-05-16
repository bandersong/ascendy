package com.ascendy.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ascendy.app.data.Blocklist
import com.ascendy.app.ui.components.Badge
import com.ascendy.app.ui.components.SoftCard
import com.ascendy.app.ui.theme.palette
import com.ascendy.app.ui.theme.vocab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlocklistScreen(
    lists: List<Blocklist>,
    appCountFor: (Long) -> Int,
    onOpenList: (Blocklist) -> Unit,
    onCreateList: (String) -> Unit,
    onDeleteList: (Blocklist) -> Unit,
    onToggleStrict: (Blocklist, Boolean) -> Unit,
    onBack: () -> Unit,
) {
    var showCreate by remember { mutableStateOf(false) }
    val insets = WindowInsets.systemBars.asPaddingValues()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = insets.calculateTopPadding(),
                     bottom = insets.calculateBottomPadding())
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "back", tint = palette.Ink)
                }
                Text(vocab.listsTitle, style = MaterialTheme.typography.headlineMedium, color = palette.Ink)
            }
            Spacer(Modifier.height(8.dp))

            if (lists.isEmpty()) {
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        com.ascendy.app.ui.components.MiniMascot(
                            locked = false,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.size(12.dp))
                        Text(
                            vocab.listsEmpty,
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.Smoke
                        )
                    }
                }
            } else {
                lists.forEach { list ->
                    Surface(
                        onClick = { onOpenList(list) },
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(list.name, style = MaterialTheme.typography.titleMedium, color = palette.Ink)
                                        if (list.isStrict) {
                                            Spacer(Modifier.size(8.dp))
                                            Badge(label = vocab.strictBadge, color = palette.Petal)
                                        }
                                    }
                                    Text(
                                        vocab.listsAppCountFmt.format(appCountFor(list.id)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = palette.Smoke
                                    )
                                }
                                if (list.isDefault) Badge(label = vocab.listsBadgeDefault, color = palette.Mint)
                                else TextButton(onClick = { onDeleteList(list) }) { Text(vocab.tagsRemove) }
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(vocab.strictToggleLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.Smoke)
                                Spacer(Modifier.weight(1f))
                                Switch(checked = list.isStrict,
                                    onCheckedChange = { onToggleStrict(list, it) })
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        FloatingActionButton(
            onClick = { showCreate = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = palette.Ink
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "new list")
        }
    }

    if (showCreate) {
        CreateListDialog(
            onDismiss = { showCreate = false },
            onConfirm = { name ->
                showCreate = false
                if (name.isNotBlank()) onCreateList(name.trim())
            }
        )
    }
}

@Composable
private fun CreateListDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(vocab.listsNewDialogTitle) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(vocab.listsNewNameLabel) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = { Button(onClick = { onConfirm(name) }) { Text(vocab.listsCreate) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(vocab.listsCancel) } }
    )
}
