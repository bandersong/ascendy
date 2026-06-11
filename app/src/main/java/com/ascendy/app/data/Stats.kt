package com.ascendy.app.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object Stats {

    /** Local-midnight timestamp for [offsetDays] days before today (today = 0, negative = future). */
    fun localMidnightDaysAgo(offsetDays: Int): Long {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        c.add(Calendar.DAY_OF_YEAR, -offsetDays)
        return c.timeInMillis
    }

    fun startOfTodayMs(): Long = localMidnightDaysAgo(0)
    fun startOfWeekMs(): Long {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        val firstDow = c.firstDayOfWeek
        while (c.get(Calendar.DAY_OF_WEEK) != firstDow) c.add(Calendar.DAY_OF_YEAR, -1)
        return c.timeInMillis
    }

    /**
     * Compute the current consecutive-day streak from a list of "YYYY-MM-DD" strings
     * (newest-first). A streak is the number of consecutive days back from today
     * that contain at least one session.
     */
    fun streakDays(distinctDatesNewestFirst: List<String>): Int {
        if (distinctDatesNewestFirst.isEmpty()) return 0
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getDefault() }
        val today = fmt.format(Date())
        val set = distinctDatesNewestFirst.toHashSet()
        if (today !in set) return 0
        var streak = 0
        val c = Calendar.getInstance()
        while (true) {
            val d = fmt.format(c.time)
            if (d in set) {
                streak += 1
                c.add(Calendar.DAY_OF_YEAR, -1)
            } else break
        }
        return streak
    }

    fun formatMinutes(totalMin: Int): String = when {
        totalMin < 60 -> "${totalMin}m"
        else -> "${totalMin / 60}h ${totalMin % 60}m"
    }

    fun msToMinutes(ms: Long): Int = (ms / 60_000L).toInt()
}
