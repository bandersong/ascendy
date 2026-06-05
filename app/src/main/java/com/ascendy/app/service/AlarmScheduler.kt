package com.ascendy.app.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.ascendy.app.data.Schedule
import java.util.Calendar

/**
 * AlarmManager wrapper for:
 *  - one-shot pomodoro / timed-session end
 *  - daily scheduled session starts and ends
 */
object AlarmScheduler {

    private const val POMODORO_END_REQ = 1001
    private const val SCHEDULE_BASE_REQ = 10_000

    private fun alarmManager(context: Context): AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun canScheduleExact(am: AlarmManager): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) am.canScheduleExactAlarms() else true
    }

    private fun pending(context: Context, requestCode: Int, intent: Intent): PendingIntent {
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun scheduleSessionEnd(context: Context, triggerAtMs: Long) {
        val am = alarmManager(context)
        val intent = Intent(context, ScheduleAlarmReceiver::class.java).apply {
            action = ScheduleAlarmReceiver.ACTION_END_SESSION
        }
        val pi = pending(context, POMODORO_END_REQ, intent)
        if (canScheduleExact(am)) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pi)
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pi)
        }
    }

    fun cancelSessionEnd(context: Context) {
        val intent = Intent(context, ScheduleAlarmReceiver::class.java).apply {
            action = ScheduleAlarmReceiver.ACTION_END_SESSION
        }
        alarmManager(context).cancel(pending(context, POMODORO_END_REQ, intent))
    }

    /** Schedule the next firing of a daily Schedule's START or END. */
    fun scheduleDailyTrigger(context: Context, schedule: Schedule, isStart: Boolean) {
        val nextMs = computeNextFiring(schedule, isStart) ?: return
        val am = alarmManager(context)
        val intent = Intent(context, ScheduleAlarmReceiver::class.java).apply {
            action = if (isStart) ScheduleAlarmReceiver.ACTION_SCHEDULE_START
                     else ScheduleAlarmReceiver.ACTION_SCHEDULE_END
            putExtra(ScheduleAlarmReceiver.EXTRA_SCHEDULE_ID, schedule.id)
        }
        val req = (SCHEDULE_BASE_REQ + schedule.id.toInt() * 2 + if (isStart) 0 else 1)
        val pi = pending(context, req, intent)
        if (canScheduleExact(am)) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextMs, pi)
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextMs, pi)
        }
    }

    fun cancelDailyTrigger(context: Context, scheduleId: Long, isStart: Boolean) {
        val intent = Intent(context, ScheduleAlarmReceiver::class.java).apply {
            action = if (isStart) ScheduleAlarmReceiver.ACTION_SCHEDULE_START
                     else ScheduleAlarmReceiver.ACTION_SCHEDULE_END
        }
        val req = (SCHEDULE_BASE_REQ + scheduleId.toInt() * 2 + if (isStart) 0 else 1)
        alarmManager(context).cancel(pending(context, req, intent))
    }

    /** Find the next firing instant for a daily schedule. Returns null if no day is enabled. */
    private fun computeNextFiring(schedule: Schedule, isStart: Boolean): Long? {
        val minutes = if (isStart) schedule.startMinuteOfDay else schedule.endMinuteOfDay
        return nextFiringFrom(schedule.daysOfWeek, schedule.enabled, minutes, Calendar.getInstance())
    }

    /**
     * Pure, deterministic core of [computeNextFiring] — [now] is injected so the day-bitmask /
     * wrap-around math can be unit-tested without depending on the wall clock. Returns the next
     * instant strictly after [now] whose weekday bit is set in [daysOfWeek] and whose time-of-day
     * is [minuteOfDay], or null if [daysOfWeek] is empty or the schedule is disabled.
     */
    internal fun nextFiringFrom(
        daysOfWeek: Int,
        enabled: Boolean,
        minuteOfDay: Int,
        now: Calendar,
    ): Long? {
        if (daysOfWeek == 0 || !enabled) return null
        for (offset in 0..7) {
            val cand = (now.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, offset)
                set(Calendar.HOUR_OF_DAY, minuteOfDay / 60)
                set(Calendar.MINUTE, minuteOfDay % 60)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            // Calendar.DAY_OF_WEEK: Sun=1, Mon=2, …, Sat=7. Our bit 0 = Sun.
            val bit = cand.get(Calendar.DAY_OF_WEEK) - 1
            if ((daysOfWeek shr bit) and 1 == 1 && cand.timeInMillis > now.timeInMillis) {
                return cand.timeInMillis
            }
        }
        return null
    }
}
