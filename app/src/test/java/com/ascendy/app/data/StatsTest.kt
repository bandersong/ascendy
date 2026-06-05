package com.ascendy.app.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/** Pure-JVM tests for streak math and duration formatting (java.util only — no Android). */
class StatsTest {

    private val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getDefault() }
    private fun daysAgo(n: Int): String {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_YEAR, -n)
        return fmt.format(c.time)
    }

    @Test fun streak_emptyIsZero() = assertEquals(0, Stats.streakDays(emptyList()))

    @Test fun streak_todayOnlyIsOne() =
        assertEquals(1, Stats.streakDays(listOf(daysAgo(0))))

    @Test fun streak_consecutiveDaysCount() =
        assertEquals(3, Stats.streakDays(listOf(daysAgo(0), daysAgo(1), daysAgo(2))))

    @Test fun streak_breaksOnGap() =
        assertEquals(1, Stats.streakDays(listOf(daysAgo(0), daysAgo(2), daysAgo(3))))

    @Test fun streak_zeroWhenTodayMissing() =
        assertEquals(0, Stats.streakDays(listOf(daysAgo(1), daysAgo(2))))

    @Test fun streak_toleratesDuplicatesAndOrder() =
        assertEquals(2, Stats.streakDays(listOf(daysAgo(1), daysAgo(0), daysAgo(0))))

    @Test fun formatMinutes_underHour() = assertEquals("45m", Stats.formatMinutes(45))
    @Test fun formatMinutes_exactHour() = assertEquals("1h 0m", Stats.formatMinutes(60))
    @Test fun formatMinutes_hourAndMinutes() = assertEquals("2h 5m", Stats.formatMinutes(125))
    @Test fun formatMinutes_zero() = assertEquals("0m", Stats.formatMinutes(0))

    @Test fun msToMinutes_truncatesDown() {
        assertEquals(1, Stats.msToMinutes(119_999))
        assertEquals(2, Stats.msToMinutes(120_000))
        assertEquals(0, Stats.msToMinutes(0))
    }
}
