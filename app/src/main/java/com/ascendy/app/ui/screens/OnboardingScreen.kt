package com.ascendy.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ascendy.app.ui.components.Mascot
import com.ascendy.app.ui.components.PageColumn
import com.ascendy.app.ui.components.SelectableChip
import com.ascendy.app.ui.theme.Space
import com.ascendy.app.ui.theme.VSpace
import com.ascendy.app.ui.theme.palette
import com.ascendy.app.ui.theme.vocab
import kotlinx.coroutines.launch

private val safetyChoices = listOf(60, 120, 240, 480, 720, 1440) // mins

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    initialSafetyMinutes: Int,
    onFinish: (safetyMinutes: Int) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()
    var safetyMin by remember { mutableIntStateOf(initialSafetyMinutes) }

    PageColumn(scroll = false) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { onFinish(safetyMin) }) { Text(vocab.onboardSkip) }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) { page ->
            when (page) {
                3 -> SafetyTimerPage(
                    selected = safetyMin,
                    onSelect = { safetyMin = it }
                )
                else -> {
                    val (title, body) = when (page) {
                        0 -> vocab.onboardP1Title to vocab.onboardP1Body
                        1 -> vocab.onboardP2Title to vocab.onboardP2Body
                        else -> vocab.onboardP3Title to vocab.onboardP3Body
                    }
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(Modifier.size(180.dp), contentAlignment = Alignment.Center) {
                            Mascot(locked = false)
                        }
                        VSpace(Space.xxl)
                        Text(title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = palette.Ink,
                            textAlign = TextAlign.Center)
                        VSpace(Space.md)
                        Text(body,
                            style = MaterialTheme.typography.bodyLarge,
                            color = palette.Smoke,
                            textAlign = TextAlign.Center)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = Space.lg),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(4) { i ->
                val active = i == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .padding(horizontal = Space.xs)
                        .size(if (active) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (active) palette.Petal else palette.Mist)
                )
            }
        }

        Button(
            onClick = {
                if (pagerState.currentPage < 3) {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                } else {
                    onFinish(safetyMin)
                }
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = Space.sm)
        ) {
            Text(if (pagerState.currentPage < 3) vocab.onboardNext else vocab.onboardStart)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SafetyTimerPage(selected: Int, onSelect: (Int) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = Space.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(Modifier.size(160.dp), contentAlignment = Alignment.Center) {
            Mascot(locked = false)
        }
        VSpace(Space.lg)
        Text(vocab.safetyTimerOnboardTitle,
            style = MaterialTheme.typography.headlineMedium,
            color = palette.Ink,
            textAlign = TextAlign.Center)
        VSpace(Space.sm)
        Text(vocab.safetyTimerOnboardBody,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.Smoke,
            textAlign = TextAlign.Center)
        VSpace(Space.lg)
        // Chip grid (2 rows of 3)
        for (rowIdx in 0 until 2) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                for (colIdx in 0 until 3) {
                    val mins = safetyChoices[rowIdx * 3 + colIdx]
                    SelectableChip(
                        label = formatMinutesLabel(mins),
                        selected = selected == mins,
                        onClick = { onSelect(mins) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            if (rowIdx == 0) VSpace(Space.sm)
        }
    }
}

private fun formatMinutesLabel(mins: Int): String =
    if (mins % 60 == 0) "${mins / 60}h" else "${mins}m"
