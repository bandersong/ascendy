package com.ascendy.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ascendy.app.data.Blocklist
import com.ascendy.app.ui.components.SelectableChip
import com.ascendy.app.ui.components.SoftCard
import com.ascendy.app.ui.theme.palette
import com.ascendy.app.ui.theme.vocab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    lists: List<Blocklist>,
    onStart: (durationMs: Long, listId: Long) -> Unit,
    onBack: () -> Unit,
) {
    val insets = WindowInsets.systemBars.asPaddingValues()
    val durations = listOf(
        15 to vocab.pomodoro15,
        25 to vocab.pomodoro25,
        50 to vocab.pomodoro50,
        90 to vocab.pomodoro90,
    )
    var selectedMin by remember { mutableIntStateOf(25) }
    val defaultList = lists.firstOrNull { it.isDefault } ?: lists.firstOrNull()
    var selectedListId by remember { mutableStateOf(defaultList?.id ?: 0L) }

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
            Text(vocab.pomodoroTitle, style = MaterialTheme.typography.headlineMedium, color = palette.Ink)
        }
        Spacer(Modifier.height(8.dp))

        Text(
            vocab.pomodoroIntro,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.Smoke
        )
        Spacer(Modifier.height(16.dp))

        // duration chips
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            durations.forEach { (min, label) ->
                SelectableChip(
                    label = label,
                    selected = selectedMin == min,
                    onClick = { selectedMin = min },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // list picker
        Text(vocab.schedulesList, style = MaterialTheme.typography.titleMedium, color = palette.Ink)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            lists.forEach { l ->
                SelectableChip(
                    label = l.name,
                    selected = selectedListId == l.id,
                    onClick = { selectedListId = l.id }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (selectedListId != 0L) {
                    onStart(selectedMin.toLong() * 60_000L, selectedListId)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(vocab.pomodoroStart)
        }

        Spacer(Modifier.height(12.dp))

        SoftCard(modifier = Modifier.fillMaxWidth(), color = palette.Cloud) {
            Text(
                vocab.pomodoroSelectedFmt.format(selectedMin),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.Smoke
            )
        }
    }
}
