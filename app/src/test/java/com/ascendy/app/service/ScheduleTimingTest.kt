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
}
