package com.ascendy.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.ascendy.app.ui.components.PageFrame
import com.ascendy.app.ui.components.SoftCard
import com.ascendy.app.ui.theme.Elev
import com.ascendy.app.ui.theme.HSpace
import com.ascendy.app.ui.theme.Space
import com.ascendy.app.ui.theme.VSpace
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
    onToggleAllowList: (Blocklist, Boolean) -> Unit,
    onBack: () -> Unit,
) {
    var showCreate by remember { mutableStateOf(false) }

    PageFrame(
        floating = {
            FloatingActionButton(
                onClick = { showCreate = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Rounded.Add, contentDescription = vocab.listsNewDialogTitle)
            }
        }
    ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = vocab.backLabel, tint = palette.Ink)
                }
                Text(vocab.listsTitle, style = MaterialTheme.typography.headlineMedium, color = palette.Ink)
            }
            VSpace(Space.sm)

            if (lists.isEmpty()) {
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        com.ascendy.app.ui.components.MiniMascot(
                            locked = false,
                            modifier = Modifier.size(40.dp)
                        )
                        HSpace(Space.md)
                        Text(
                            vocab.listsEmpty,
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.Smoke
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = Space.mega + Space.xxxl),
                    verticalArrangement = Arrangement.spacedBy(Space.sm)
                ) {
                items(lists, key = { it.id }) { list ->
                    Surface(
                        onClick = { onOpenList(list) },
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.large,
                        border = androidx.compose.foundation.BorderStroke(Elev.hairline, palette.Mist),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = Space.xl, vertical = Space.lg)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(list.name, style = MaterialTheme.typography.titleMedium, color = palette.Ink)
                                        if (list.isStrict) {
                                            HSpace(Space.sm)
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
                            VSpace(Space.xs)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(vocab.strictToggleLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.Smoke)
                                Spacer(Modifier.weight(1f))
                                Switch(checked = list.isStrict,
                                    onCheckedChange = { onToggleStrict(list, it) })
                            }
                            // The tradeoff, BEFORE committing — the toast version only appears once
                            // a strict session is already running, which is too late to inform.
                            Text(
                                vocab.strictToggleHint,
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.Smoke
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(vocab.allowListToggleLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.Smoke)
                                Spacer(Modifier.weight(1f))
                                Switch(checked = list.isAllowList,
                                    onCheckedChange = { onToggleAllowList(list, it) })
                            }
                        }
                    }
                }
                }
            }
    }

    if (showCreate) {
        CreateListDialog(
            onDismiss = { showCreate = false },
            onConfirm = { name ->
                showCreate = false
                val final = name.trim().ifBlank { randomListName() }
                onCreateList(final)
            }
        )
    }
}

private val randomListNames = listOf(
    "willow", "ember", "drift", "comet", "petal", "moss", "lumen", "halo",
    "tide", "nest", "echo", "saffron", "fern", "harbor", "lark", "cinder",
    "spruce", "atlas", "veil", "ridge", "bloom", "river", "haven", "kite",
    "marble", "stillwater", "afterglow", "meadow", "sable", "linden",
    "juniper", "starling", "thistle", "lattice", "halcyon", "vellum",
    "indigo", "alcove", "aurora", "ember", "quartz", "wisteria"
)

private fun randomListName(): String = randomListNames.random()

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
