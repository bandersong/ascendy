package com.ascendy.app.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

/**
 * Pure-JVM tests for the daily-schedule firing math ([AlarmScheduler.nextFiringFrom]). `now` is
 * injected so the weekday-bitmask and wrap-around logic are deterministic regardless of wall clock.
 * Bit 0 = Sunday … bit 6 = Saturday.
 */
class ScheduleTimingTest {

    private fun at(year: Int, month: Int, day: Int, hour: Int, min: Int): Calendar =
        Calendar.getInstance().apply {
            clear()
            set(year, month, day, hour, min, 0)
            set(Calendar.MILLISECOND, 0)
        }

    private fun cal(ms: Long): Calendar = Calendar.getInstance().apply { timeInMillis = ms }

    private val now = at(2024, Calendar.JANUARY, 3, 10, 0)   // a Wednesday, 10:00
    private val todayBit = 1 shl (now.get(Calendar.DAY_OF_WEEK) - 1)

    @Test fun noDaysSelected_isNull() =
        assertNull(AlarmScheduler.nextFiringFrom(daysOfWeek = 0, enabled = true, minuteOfDay = 600, now = now))

    @Test fun disabled_isNull() =
        assertNull(AlarmScheduler.nextFiringFrom(daysOfWeek = 0x7F, enabled = false, minuteOfDay = 600, now = now))

    @Test fun laterToday_firesToday() {
        val ms = AlarmScheduler.nextFiringFrom(todayBit, true, 11 * 60, now)
        assertNotNull(ms)
        val c = cal(ms!!)
        assertEquals("same calendar day", now.get(Calendar.DAY_OF_YEAR), c.get(Calendar.DAY_OF_YEAR))
        assertEquals(11, c.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, c.get(Calendar.MINUTE))
    }

    @Test fun earlierToday_rollsToNextWeek() {
        // 09:00 has already passed at 10:00 → next occurrence is the same weekday, 7 days later.
        val ms = AlarmScheduler.nextFiringFrom(todayBit, true, 9 * 60, now)!!
        val c = cal(ms)
        assertEquals("same weekday", now.get(Calendar.DAY_OF_WEEK), c.get(Calendar.DAY_OF_WEEK))
        assertEquals("7 days ahead", now.get(Calendar.DAY_OF_YEAR) + 7, c.get(Calendar.DAY_OF_YEAR))
        assertEquals(9, c.get(Calendar.HOUR_OF_DAY))
    }

    @Test fun sundayOnly_findsUpcomingSunday() {
        val sundayBit = 1 shl (Calendar.SUNDAY - 1)
        val ms = AlarmScheduler.nextFiringFrom(sundayBit, true, 8 * 60, now)!!
        val c = cal(ms)
        assertEquals(Calendar.SUNDAY, c.get(Calendar.DAY_OF_WEEK))
        assertTrue("strictly in the future", ms > now.timeInMillis)
        assertEquals(8, c.get(Calendar.HOUR_OF_DAY))
    }

    @Test fun midnightAndEndOfDay_areValid() {
        val midnight = cal(AlarmScheduler.nextFiringFrom(0x7F, true, 0, now)!!)
        assertEquals(0, midnight.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, midnight.get(Calendar.MINUTE))
        val late = cal(AlarmScheduler.nextFiringFrom(0x7F, true, 1439, now)!!)
        assertEquals(23, late.get(Calendar.HOUR_OF_DAY))
        assertEquals(59, late.get(Calendar.MINUTE))
    }

    @Test fun allDays_laterToday_firesToday() {
        val ms = AlarmScheduler.nextFiringFrom(0x7F, true, 23 * 60, now)!!
        assertEquals(now.get(Calendar.DAY_OF_YEAR), cal(ms).get(Calendar.DAY_OF_YEAR))
    }

    @Test fun overnightEnd_matchesStartDayBit_notEndDay() {
        // Wed-only schedule 22:00→02:00: the END fires Thu 02:00. With matchDayOffset = 1 the
        // Thu candidate is checked against Wednesday's bit, so it fires the very next morning —
        // not a week later (the old bug: Thu's bit unset → scan rolled to next Wed's 02:00).
        val wedBit = todayBit   // `now` is a Wednesday
        val ms = AlarmScheduler.nextFiringFrom(wedBit, true, 2 * 60, now, matchDayOffset = 1)!!
        val c = cal(ms)
        assertEquals(Calendar.THURSDAY, c.get(Calendar.DAY_OF_WEEK))
        assertEquals("the morning right after the Wed window", now.get(Calendar.DAY_OF_YEAR) + 1, c.get(Calendar.DAY_OF_YEAR))
        assertEquals(2, c.get(Calendar.HOUR_OF_DAY))
    }

    @Test fun overnightEnd_insideWindow_firesWithinHours() {
        // It's Wed 23:30, inside the Wed 22:00→02:00 window: the END must be Thu 02:00.
        val lateNow = at(2024, Calendar.JANUARY, 3, 23, 30)
        val ms = AlarmScheduler.nextFiringFrom(todayBit, true, 2 * 60, lateNow, matchDayOffset = 1)!!
        val c = cal(ms)
        assertEquals(Calendar.THURSDAY, c.get(Calendar.DAY_OF_WEEK))
        assertTrue("fires within 3 hours", ms - lateNow.timeInMillis <= 3 * 60 * 60_000L)
    }

    // ── nextTimeOfDayAfter (restoreOnBoot's scheduled-window end recomputation) ──

    @Test fun nextTimeOfDayAfter_laterToday_staysToday() {
        val after = at(2024, Calendar.JANUARY, 3, 10, 0).timeInMillis
        val c = cal(AlarmScheduler.nextTimeOfDayAfter(11 * 60, after))
        assertEquals(3, c.get(Calendar.DAY_OF_MONTH))
        assertEquals(11, c.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, c.get(Calendar.MINUTE))
    }

    @Test fun nextTimeOfDayAfter_timeAlreadyPassed_rollsToTomorrow() {
        val after = at(2024, Calendar.JANUARY, 3, 10, 0).timeInMillis
        val c = cal(AlarmScheduler.nextTimeOfDayAfter(9 * 60, after))
        assertEquals(4, c.get(Calendar.DAY_OF_MONTH))
        assertEquals(9, c.get(Calendar.HOUR_OF_DAY))
    }

    @Test fun nextTimeOfDayAfter_exactInstant_isStrictlyAfter() {
        // end == start minute-of-day expresses a full 24h window, never a zero-length one.
        val after = at(2024, Calendar.JANUARY, 3, 10, 0).timeInMillis
        val c = cal(AlarmScheduler.nextTimeOfDayAfter(10 * 60, after))
        assertEquals(4, c.get(Calendar.DAY_OF_MONTH))
        assertEquals(10, c.get(Calendar.HOUR_OF_DAY))
    }

    @Test fun nextTimeOfDayAfter_springForward_tracksWallClockNotElapsed() {
        val before = java.util.TimeZone.getDefault()
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("America/New_York"))
        try {
            // Sat 2026-03-07 22:00 EST → next 06:00 local is 06:00 EDT: 7 REAL hours, not 8.
            val start = at(2026, Calendar.MARCH, 7, 22, 0).timeInMillis
            val end = AlarmScheduler.nextTimeOfDayAfter(6 * 60, start)
            assertEquals("skipped DST hour is not blocked-through", 7 * 3_600_000L, end - start)
        } finally {
            java.util.TimeZone.setDefault(before)
        }
    }
}
