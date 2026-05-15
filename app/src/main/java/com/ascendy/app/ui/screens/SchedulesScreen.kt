package com.ascendy.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ascendy.app.data.Blocklist
import com.ascendy.app.data.Schedule
import com.ascendy.app.ui.components.Badge
import com.ascendy.app.ui.components.SoftCard
import com.ascendy.app.ui.theme.palette
import com.ascendy.app.ui.theme.vocab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulesScreen(
    schedules: List<Schedule>,
    lists: List<Blocklist>,
    onSave: (Schedule) -> Unit,
    onDelete: (Schedule) -> Unit,
    onToggle: (Schedule, Boolean) -> Unit,
    onBack: () -> Unit,
) {
    var showAdd by remember { mutableStateOf(false) }
    val insets = WindowInsets.systemBars.asPaddingValues()

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(top = insets.calculateTopPadding(),
                 bottom = insets.calculateBottomPadding())
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "back", tint = palette.Ink)
                }
                Text(vocab.schedulesTitle, style = MaterialTheme.typography.headlineMedium, color = palette.Ink)
            }
            Spacer(Modifier.height(8.dp))

            if (schedules.isEmpty()) {
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Text(vocab.schedulesEmpty,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.Smoke)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(schedules, key = { it.id }) { s ->
                        ScheduleCard(
                            schedule = s,
                            list = lists.find { it.id == s.listId },
                            onToggle = { en -> onToggle(s, en) },
                            onDelete = { onDelete(s) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAdd = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = palette.Ink
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "new")
        }
    }

    if (showAdd) {
        ScheduleDialog(
            initial = null,
            lists = lists,
            onDismiss = { showAdd = false },
            onConfirm = { s ->
                showAdd = false
                onSave(s)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleCard(
    schedule: Schedule,
    list: Blocklist?,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    SoftCard(modifier = Modifier.fillMaxWidth(), color = palette.Surface) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        schedule.nickname.ifBlank { list?.name ?: "schedule" },
                        style = MaterialTheme.typography.titleMedium, color = palette.Ink
                    )
                    Text(
                        "${minutesToHHmm(schedule.startMinuteOfDay)} – ${minutesToHHmm(schedule.endMinuteOfDay)}",
                        style = MaterialTheme.typography.bodyMedium, color = palette.Smoke
                    )
                }
                Switch(checked = schedule.enabled, onCheckedChange = onToggle)
            }
            Spacer(Modifier.height(6.dp))
            DayDots(daysOfWeek = schedule.daysOfWeek)
            Spacer(Modifier.height(6.dp))
            Row {
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDelete) { Text(vocab.schedulesDelete) }
            }
        }
    }
}

@Composable
private fun DayDots(daysOfWeek: Int) {
    Row {
        vocab.daysShort.forEachIndexed { i, label ->
            val on = (daysOfWeek shr i) and 1 == 1
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .padding(end = 4.dp)
                    .clip(CircleShape)
                    .background(if (on) palette.Petal else palette.Mist),
                contentAlignment = Alignment.Center
            ) {
                Text(label,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (on) palette.Ink else palette.Smoke)
            }
        }
    }
}

@Composable
private fun ScheduleDialog(
    initial: Schedule?,
    lists: List<Blocklist>,
    onDismiss: () -> Unit,
    onConfirm: (Schedule) -> Unit,
) {
    val defaultList = lists.firstOrNull { it.isDefault } ?: lists.firstOrNull()
    var nickname by remember { mutableStateOf(initial?.nickname ?: "") }
    var listId by remember { mutableStateOf(initial?.listId ?: defaultList?.id ?: 0L) }
    var daysOfWeek by remember { mutableIntStateOf(initial?.daysOfWeek ?: 0b0111110) } // Mon-Fri by default
    var startMin by remember { mutableIntStateOf(initial?.startMinuteOfDay ?: (9 * 60)) }
    var endMin by remember { mutableIntStateOf(initial?.endMinuteOfDay ?: (12 * 60)) }
    var enabled by remember { mutableStateOf(initial?.enabled ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(vocab.schedulesNewDialogTitle) },
        text = {
            Column {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text(vocab.schedulesNickname) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(vocab.schedulesList, style = MaterialTheme.typography.titleMedium, color = palette.Ink)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    lists.forEach { l ->
                        val sel = listId == l.id
                        Surface(
                            onClick = { listId = l.id },
                            color = if (sel) palette.Petal else palette.Mist,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(l.name,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.Ink)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(vocab.schedulesDays, style = MaterialTheme.typography.titleMedium, color = palette.Ink)
                Row {
                    vocab.daysShort.forEachIndexed { i, label ->
                        val on = (daysOfWeek shr i) and 1 == 1
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .padding(end = 4.dp)
                                .clip(CircleShape)
                                .background(if (on) palette.Petal else palette.Mist)
                                .clickable {
                                    daysOfWeek = daysOfWeek xor (1 shl i)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, style = MaterialTheme.typography.bodySmall,
                                color = if (on) palette.Ink else palette.Smoke)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TimeAdjustField(label = vocab.schedulesStartTime, value = startMin, onChange = { startMin = it }, modifier = Modifier.weight(1f))
                    Spacer(Modifier.size(8.dp))
                    TimeAdjustField(label = vocab.schedulesEndTime, value = endMin, onChange = { endMin = it }, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(vocab.schedulesEnabled, style = MaterialTheme.typography.titleMedium, color = palette.Ink)
                    Spacer(Modifier.weight(1f))
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (listId != 0L && daysOfWeek != 0 && endMin > startMin) {
                    onConfirm(
                        Schedule(
                            id = initial?.id ?: 0,
                            listId = listId,
                            daysOfWeek = daysOfWeek,
                            startMinuteOfDay = startMin,
                            endMinuteOfDay = endMin,
                            enabled = enabled,
                            nickname = nickname.trim(),
                        )
                    )
                }
            }) { Text(vocab.schedulesSave) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(vocab.schedulesCancel) }
        }
    )
}

@Composable
private fun TimeAdjustField(label: String, value: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = palette.Smoke)
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { onChange((value - 15).coerceAtLeast(0)) }) { Text("-") }
            Text(minutesToHHmm(value), style = MaterialTheme.typography.titleMedium, color = palette.Ink)
            TextButton(onClick = { onChange((value + 15).coerceAtMost(24 * 60 - 15)) }) { Text("+") }
        }
    }
}

private fun minutesToHHmm(m: Int): String =
    "%02d:%02d".format(m / 60, m % 60)
