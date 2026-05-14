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
import com.ascendy.app.data.BoundTag
import com.ascendy.app.ui.components.Mascot
import com.ascendy.app.ui.components.SoftCard
import com.ascendy.app.ui.theme.AscendyColors

@Composable
fun PairTagScreen(
    waiting: Boolean,
    detectedTagId: String?,
    knownTags: List<BoundTag>,
    onStartPairing: () -> Unit,
    onCancelPairing: () -> Unit,
    onSavePairing: (nickname: String) -> Unit,
    onDeleteTag: (BoundTag) -> Unit,
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
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "back", tint = AscendyColors.Ink)
            }
            Text("tags", style = MaterialTheme.typography.headlineMedium, color = AscendyColors.Ink)
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
                            "pair a blank ntag21x sticker. you can keep it on the fridge, in a drawer, or in your bag.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AscendyColors.Smoke
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = onStartPairing) { Text("start pairing") }
                    }
                    waiting && detectedTagId == null -> {
                        Text(
                            "hold the back of your phone against the tag ✨",
                            style = MaterialTheme.typography.titleMedium,
                            color = AscendyColors.Ink
                        )
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = onCancelPairing) { Text("cancel") }
                    }
                    detectedTagId != null -> {
                        Text("tag found 🌸", style = MaterialTheme.typography.titleMedium, color = AscendyColors.Ink)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = nickname,
                            onValueChange = { nickname = it },
                            label = { Text("give it a name (e.g. kitchen)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = onCancelPairing) { Text("cancel") }
                            Button(
                                onClick = { if (nickname.isNotBlank()) onSavePairing(nickname.trim()) }
                            ) { Text("save") }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text("your tags", style = MaterialTheme.typography.titleLarge, color = AscendyColors.Smoke)
        Spacer(Modifier.height(8.dp))

        if (knownTags.isEmpty()) {
            Text(
                "no tags yet ♡",
                style = MaterialTheme.typography.bodyMedium,
                color = AscendyColors.Smoke
            )
        } else {
            knownTags.forEach { tag ->
                SoftCard(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(tag.nickname, style = MaterialTheme.typography.titleMedium, color = AscendyColors.Ink)
                            Text(
                                tag.tagId.take(10) + "…",
                                style = MaterialTheme.typography.bodySmall,
                                color = AscendyColors.Smoke
                            )
                        }
                        TextButton(onClick = { onDeleteTag(tag) }) { Text("remove") }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
