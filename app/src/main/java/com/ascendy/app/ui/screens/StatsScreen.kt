package com.ascendy.app.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ascendy.app.data.SessionLog
import com.ascendy.app.data.Stats
import com.ascendy.app.ui.components.Badge
import com.ascendy.app.ui.components.SoftCard
import com.ascendy.app.ui.theme.palette
import com.ascendy.app.ui.theme.vocab
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatsScreen(
    todayMs: Long,
    weekMs: Long,
    allTimeMs: Long,
    streakDays: Int,
    recent: List<SessionLog>,
    onBack: () -> Unit,
) {
    val insets = WindowInsets.systemBars.asPaddingValues()
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(top = insets.calculateTopPadding(),
                     bottom = insets.calculateBottomPadding(),
                     start = 16.dp, end = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "back", tint = palette.Ink)
            }
            Text(vocab.statsTitle, style = MaterialTheme.typography.headlineMedium, color = palette.Ink)
        }

        Spacer(Modifier.height(8.dp))

        SoftCard(modifier = Modifier.fillMaxWidth(), color = palette.Cloud) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()) {
                Text(
                    vocab.statsStreakFmt.format(streakDays),
                    style = MaterialTheme.typography.headlineSmall,
                    color = palette.Ink,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                val achievement = when {
                    streakDays >= 100 -> vocab.statsAchievement100
                    streakDays >= 30 -> vocab.statsAchievement30
                    streakDays >= 7 -> vocab.statsAchievement7
                    else -> ""
                }
                if (achievement.isNotEmpty()) {
                    Text(achievement,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.Smoke,
                        textAlign = TextAlign.Center)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            StatTile(vocab.statsToday, Stats.formatMinutes(Stats.msToMinutes(todayMs)), Modifier.weight(1f))
            Spacer(Modifier.size(8.dp))
            StatTile(vocab.statsWeek, Stats.formatMinutes(Stats.msToMinutes(weekMs)), Modifier.weight(1f))
            Spacer(Modifier.size(8.dp))
            StatTile(vocab.statsAllTime, Stats.formatMinutes(Stats.msToMinutes(allTimeMs)), Modifier.weight(1f))
        }

        Spacer(Modifier.height(20.dp))

        Text(vocab.statsRecent, style = MaterialTheme.typography.titleLarge, color = palette.Smoke)
        Spacer(Modifier.height(8.dp))

        if (recent.isEmpty()) {
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                Text(vocab.statsEmpty, style = MaterialTheme.typography.bodyMedium, color = palette.Smoke)
            }
        } else {
            val fmt = SimpleDateFormat("EEE MMM d, HH:mm", Locale.getDefault())
            recent.take(20).forEach { log ->
                SoftCard(modifier = Modifier.fillMaxWidth(), color = palette.Surface) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(fmt.format(Date(log.startedAt)),
                                style = MaterialTheme.typography.titleMedium, color = palette.Ink)
                            val durMin = if (log.endedAt != null)
                                Stats.msToMinutes(log.endedAt - log.startedAt) else 0
                            val durLabel = if (log.endedAt == null) "—" else Stats.formatMinutes(durMin)
                            Text("$durLabel · ${log.source}",
                                style = MaterialTheme.typography.bodySmall, color = palette.Smoke)
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    SoftCard(modifier = modifier, color = palette.Surface) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()) {
            Text(value, style = MaterialTheme.typography.titleLarge, color = palette.Ink)
            Spacer(Modifier.height(2.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = palette.Smoke)
        }
    }
}
