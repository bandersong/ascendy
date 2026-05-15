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
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.ascendy.app.data.BoundTag
import com.ascendy.app.ui.components.Mascot
import com.ascendy.app.ui.components.SoftCard
import com.ascendy.app.ui.theme.palette
import com.ascendy.app.ui.theme.vocab

@Composable
fun PairTagScreen(
    waiting: Boolean,
    detectedTagId: String?,
    knownTags: List<BoundTag>,
    lists: List<Blocklist>,
    onStartPairing: () -> Unit,
    onCancelPairing: () -> Unit,
    onSavePairing: (nickname: String) -> Unit,
    onDeleteTag: (BoundTag) -> Unit,
    onAssignList: (BoundTag, Long?) -> Unit,
    onBack: () -> Unit,
) {
    var nickname by remember { mutableStateOf("") }
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
            Text(vocab.tagsTitle, style = MaterialTheme.typography.headlineMedium, color = palette.Ink)
        }

        Spacer(Modifier.height(8.dp))

        SoftCard(modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(110.dp), contentAlignment = Alignment.Center) {
                    Mascot(locked = waiting)
                }
                Spacer(Modifier.height(8.dp))
                when {
                    !waiting && detectedTagId == null -> {
                        Text(
                            vocab.tagsIntro,
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.Smoke
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = onStartPairing) { Text(vocab.tagsStartPairing) }
                    }
                    waiting && detectedTagId == null -> {
                        Text(
                            vocab.tagsWaiting,
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.Ink
                        )
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = onCancelPairing) { Text(vocab.tagsCancel) }
                    }
                    detectedTagId != null -> {
                        Text(vocab.tagsFound, style = MaterialTheme.typography.titleMedium, color = palette.Ink)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = nickname,
                            onValueChange = { nickname = it },
                            label = { Text(vocab.tagsNicknameLabel) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = onCancelPairing) { Text(vocab.tagsCancel) }
                            Button(
                                onClick = { if (nickname.isNotBlank()) onSavePairing(nickname.trim()) }
                            ) { Text(vocab.tagsSave) }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(vocab.tagsListHeader, style = MaterialTheme.typography.titleLarge, color = palette.Smoke)
        Spacer(Modifier.height(8.dp))

        if (knownTags.isEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                com.ascendy.app.ui.components.MiniMascot(
                    locked = false,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(Modifier.size(10.dp))
                Text(
                    vocab.tagsEmpty,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.Smoke
                )
            }
        } else {
            knownTags.forEach { tag ->
                SoftCard(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(tag.nickname, style = MaterialTheme.typography.titleMedium, color = palette.Ink)
                                Text(
                                    tag.tagId.take(10) + "…",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.Smoke
                                )
                            }
                            TextButton(onClick = { onDeleteTag(tag) }) { Text(vocab.tagsRemove) }
                        }
                        if (lists.size > 1 || tag.listId != null) {
                            Spacer(Modifier.height(6.dp))
                            Text(vocab.tagListPickerLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.Smoke)
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                ListChip(
                                    text = vocab.tagListPickerDefault,
                                    selected = tag.listId == null,
                                    onClick = { onAssignList(tag, null) }
                                )
                                lists.forEach { l ->
                                    ListChip(
                                        text = l.name,
                                        selected = tag.listId == l.id,
                                        onClick = { onAssignList(tag, l.id) }
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun ListChip(text: String, selected: Boolean, onClick: () -> Unit) {
    androidx.compose.material3.Surface(
        onClick = onClick,
        color = if (selected) palette.Petal else palette.Mist,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            color = palette.Ink
        )
    }
}
