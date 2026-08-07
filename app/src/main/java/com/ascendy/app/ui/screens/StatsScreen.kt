package com.ascendy.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ascendy.app.data.SessionLog
import com.ascendy.app.data.Stats
import com.ascendy.app.ui.components.EmptyState
import com.ascendy.app.ui.components.PageColumn
import com.ascendy.app.ui.components.ScreenHeader
import com.ascendy.app.ui.components.SectionLabel
import com.ascendy.app.ui.components.SoftCard
import com.ascendy.app.ui.theme.HSpace
import com.ascendy.app.ui.theme.Space
import com.ascendy.app.ui.theme.ThemeVariant
import com.ascendy.app.ui.theme.VSpace
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
        ScreenHeader(title = vocab.statsTitle, onBack = onBack)

        VSpace(Space.sm)

        SoftCard(modifier = Modifier.fillMaxWidth(), color = palette.Cloud) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()) {
                Text(
                    vocab.statsStreakFmt.format(streakDays),
                    style = MaterialTheme.typography.headlineSmall,
                    color = palette.Ink,
                    textAlign = TextAlign.Center
                )
                VSpace(Space.xs)
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

        VSpace(Space.md)

        // IntrinsicSize.Max + fillMaxHeight gives all three tiles the tallest tile's height. At
        // fontScale 2.0 the middle caption wrapped to an extra line and the row ended up with a
        // 36dp height mismatch and ragged card bottoms.
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)) {
            StatTile(vocab.statsToday, Stats.formatMinutes(Stats.msToMinutes(todayMs)),
                Modifier.weight(1f).fillMaxHeight())
            HSpace(Space.sm)
            StatTile(vocab.statsWeek, Stats.formatMinutes(Stats.msToMinutes(weekMs)),
                Modifier.weight(1f).fillMaxHeight())
            HSpace(Space.sm)
            StatTile(vocab.statsAllTime, Stats.formatMinutes(Stats.msToMinutes(allTimeMs)),
                Modifier.weight(1f).fillMaxHeight())
        }

        VSpace(Space.xl)

        // 7-day bar chart
        SectionLabel(vocab.statsChartLabel)
        VSpace(Space.sm)
        SoftCard(modifier = Modifier.fillMaxWidth(), color = palette.Surface) {
            WeekChart(weekBuckets)
        }

        VSpace(Space.xl)

        SectionLabel(vocab.statsRecent)
        VSpace(Space.sm)

        if (recent.isEmpty()) {
            EmptyState(vocab.statsEmpty)
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
                VSpace(Space.sm)
            }
        }
    }
}

@Composable
private fun WeekChart(buckets: LongArray) {
    val maxMs = (buckets.maxOrNull() ?: 0L).coerceAtLeast(1L)
    val bestIdx = buckets.indices.maxByOrNull { buckets[it] } ?: -1
    // Full short day names ("Mon") for the spoken summary; their initials for the drawn axis.
    val dayNames = remember {
        val now = java.util.Calendar.getInstance().timeInMillis
        val fmt = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
        List(7) { i ->
            val c = java.util.Calendar.getInstance().apply {
                timeInMillis = now
                add(java.util.Calendar.DAY_OF_YEAR, -(6 - i))
            }
            fmt.format(c.time)
        }
    }
    val dayLabels = remember(dayNames) { dayNames.map { it.first().uppercase() } }

    // The chart is a raw Canvas: without this it exposed an empty contentDescription and its only
    // accessible children were seven single letters, so a screen-reader user learned nothing from
    // ~40% of the screen. Built from the exact buckets the bars are drawn from.
    val chartVocab = vocab.chart
    val chartSummary = remember(buckets, dayNames, chartVocab) {
        val totalMs = buckets.sum()
        if (totalMs <= 0L) chartVocab.empty
        else chartVocab.summaryFmt.format(
            dayNames.indices.joinToString(", ") {
                chartVocab.dayFmt.format(dayNames[it], Stats.formatMinutes(Stats.msToMinutes(buckets[it])))
            },
            Stats.formatMinutes(Stats.msToMinutes(totalMs)),
            Stats.formatMinutes(Stats.msToMinutes(totalMs / 7)),
        )
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
                .padding(vertical = Space.xs)
                .semantics { contentDescription = chartSummary }
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
        // The seven initials are a visual axis for the bars above; the chart's own description
        // already names every day in full, so leaving them in the tree just makes a screen reader
        // read out "S S M T W T F".
        Row(modifier = Modifier.fillMaxWidth().clearAndSetSemantics { }) {
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
            VSpace(Space.sm)
            Text(
                "${vocab.statsBestDay}: ${dayLabels[bestIdx]} · ${Stats.formatMinutes(Stats.msToMinutes(buckets[bestIdx]))}",
                style = MaterialTheme.typography.bodySmall,
                color = palette.Smoke
            )
        }
    }
}

/**
 * Three of these share one Row, so each gets ~1/3 of the page. The card's default Space.xl padding
 * left the caption only 61dp of text width, which is narrower than "WEEK" at fontScale 2.0 — so it
 * broke mid-word. Space.md buys 16dp back per tile, enough for the longest caption word in all
 * three themes to fit and wrap on the space instead.
 * ponytail: a fixed padding step, not a measured fit — if a future theme adds a longer single-word
 * caption, stack the tiles vertically above ~fontScale 1.5 rather than shaving padding further.
 */
@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    SoftCard(modifier = modifier, color = palette.Surface, padding = Space.md) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()) {
            Text(value, style = MaterialTheme.typography.titleLarge, color = palette.Ink,
                textAlign = TextAlign.Center)
            VSpace(Space.xxs)
            Text(label, style = MaterialTheme.typography.bodySmall, color = palette.Smoke,
                textAlign = TextAlign.Center)
        }
    }
}
