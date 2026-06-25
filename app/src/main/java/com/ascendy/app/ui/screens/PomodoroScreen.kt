package com.ascendy.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.ascendy.app.data.Blocklist
import com.ascendy.app.ui.components.PageColumn
import com.ascendy.app.ui.components.SelectableChip
import com.ascendy.app.ui.components.SoftCard
import com.ascendy.app.ui.theme.Space
import com.ascendy.app.ui.theme.VSpace
import com.ascendy.app.ui.theme.palette
import com.ascendy.app.ui.theme.vocab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    lists: List<Blocklist>,
    onStart: (durationMs: Long, listId: Long) -> Unit,
    onBack: () -> Unit,
) {
    val durations = listOf(
        15 to vocab.pomodoro15,
        25 to vocab.pomodoro25,
        50 to vocab.pomodoro50,
        90 to vocab.pomodoro90,
    )
    var selectedMin by remember { mutableIntStateOf(25) }
    val defaultList = lists.firstOrNull { it.isDefault } ?: lists.firstOrNull()
    var selectedListId by remember { mutableStateOf(defaultList?.id ?: 0L) }

    PageColumn {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = vocab.backLabel, tint = palette.Ink)
            }
            Text(vocab.pomodoroTitle, style = MaterialTheme.typography.headlineMedium, color = palette.Ink)
        }
        VSpace(Space.sm)

        Text(
            vocab.pomodoroIntro,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.Smoke
        )
        VSpace(Space.lg)

        // duration chips
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
            durations.forEach { (min, label) ->
                SelectableChip(
                    label = label,
                    selected = selectedMin == min,
                    onClick = { selectedMin = min },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        VSpace(Space.lg)

        // list picker
        Text(vocab.schedulesList, style = MaterialTheme.typography.titleMedium, color = palette.Ink)
        VSpace(Space.sm)
        Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
            lists.forEach { l ->
                SelectableChip(
                    label = l.name,
                    selected = selectedListId == l.id,
                    onClick = { selectedListId = l.id }
                )
            }
        }

        VSpace(Space.xxl)

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

        VSpace(Space.md)

        SoftCard(modifier = Modifier.fillMaxWidth(), color = palette.Cloud) {
            Text(
                vocab.pomodoroSelectedFmt.format(selectedMin),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.Smoke
            )
        }
    }
}
