package com.ascendy.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ascendy.app.data.SessionLog
import com.ascendy.app.data.Stats
import com.ascendy.app.ui.components.PageColumn
import com.ascendy.app.ui.components.SoftCard
import com.ascendy.app.ui.theme.ThemeVariant
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
    // Bucket recent logs into the last 7 local days (index 0 = 6 days ago, 6 = today).
    // Each log contributes its overlap with each day window, so sessions spanning midnight
    // split correctly instead of crediting their full length to the start day.
    val weekBuckets = remember(recent) {
        // midnights[0..6] = start of each bucket day, midnights[7] = upcoming midnight
        val midnights = LongArray(8) { Stats.localMidnightDaysAgo(6 - it) }
        val now = System.currentTimeMillis()
        val out = LongArray(7)
        recent.forEach { log ->
            val end = log.endedAt ?: now
            for (i in 0..6) {
                val overlap = minOf(end, midnights[i + 1]) - maxOf(log.startedAt, midnights[i])
                if (overlap > 0) out[i] += overlap
            }
        }
        out
    }

    PageColumn {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = vocab.backLabel, tint = palette.Ink)
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

        // 7-day bar chart
        Text(vocab.statsChartLabel, style = MaterialTheme.typography.titleLarge, color = palette.Smoke)
        Spacer(Modifier.height(8.dp))
        SoftCard(modifier = Modifier.fillMaxWidth(), color = palette.Surface) {
            WeekChart(weekBuckets)
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
private fun WeekChart(buckets: LongArray) {
    val maxMs = (buckets.maxOrNull() ?: 0L).coerceAtLeast(1L)
    val bestIdx = buckets.indices.maxByOrNull { buckets[it] } ?: -1
    val dayLabels = remember {
        val cal = java.util.Calendar.getInstance()
        val fmt = java.text.SimpleDateFormat("EE", java.util.Locale.getDefault())
        val out = Array(7) { "" }
        for (i in 0 until 7) {
            val c = java.util.Calendar.getInstance().apply {
                timeInMillis = cal.timeInMillis
                add(java.util.Calendar.DAY_OF_YEAR, -(6 - i))
            }
            out[i] = fmt.format(c.time).first().uppercase()
        }
        out
    }
    val barColor = palette.Petal
    val highlightColor = palette.Lilac
    val textColor = palette.Smoke
    val labelInk = palette.Ink
    // Bar corners follow the theme's shape language: Tough is hard-edged, the others soft.
    val barRadiusCap = if (palette.variant == ThemeVariant.Tough) 3.dp else 12.dp

    Column {
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(vertical = 4.dp)
        ) {
            val w = size.width
            val h = size.height
            // One pitch shared with the label row below: 7 equal slots, bar centered in each.
            // Bar width is capped so wide screens get slim bars with breathing room, not slabs.
            val slotW = w / 7f
            val barW = minOf(slotW * 0.55f, 44.dp.toPx())
            for (i in 0..6) {
                val pct = (buckets[i].toFloat() / maxMs).coerceIn(0f, 1f)
                val barH = (h * 0.82f) * pct
                val left = i * slotW + (slotW - barW) / 2f
                val top = (h * 0.82f) - barH
                val r = minOf(barW * 0.3f, barRadiusCap.toPx())
                drawRoundRect(
                    color = if (i == bestIdx && buckets[i] > 0) highlightColor else barColor,
                    topLeft = androidx.compose.ui.geometry.Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(barW, barH.coerceAtLeast(2.dp.toPx())),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r)
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            for (i in 0..6) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        dayLabels[i],
                        style = MaterialTheme.typography.bodySmall,
                        color = if (i == bestIdx && buckets[i] > 0) labelInk else textColor
                    )
                }
            }
        }
        if (bestIdx >= 0 && buckets[bestIdx] > 0) {
            Spacer(Modifier.height(6.dp))
            Text(
                "${vocab.statsBestDay}: ${dayLabels[bestIdx]} · ${Stats.formatMinutes(Stats.msToMinutes(buckets[bestIdx]))}",
                style = MaterialTheme.typography.bodySmall,
                color = palette.Smoke
            )
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
