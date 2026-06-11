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
    private const val HEARTBEAT_REQ = 1002
    private const val SCHEDULE_BASE_REQ = 10_000

    /** How often the self-heal heartbeat re-checks that enforcement is alive (monotonic). */
    const val HEARTBEAT_INTERVAL_MS = 90_000L

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

    /**
     * canScheduleExactAlarms() can flip to false between the check and the set (user revokes the
     * special access mid-flight) — setExactAndAllowWhileIdle then throws SecurityException. An
     * inexact alarm is always better than crashing after the session row was already marked active.
     *
     * [type] selects the clock base. The session-end / heartbeat alarms use ELAPSED_REALTIME_WAKEUP
     * (device uptime) so a wall-clock change can't fire them early; the daily schedule triggers use
     * RTC_WAKEUP because "block at 9pm" is genuinely a wall-clock-of-day event.
     */
    private fun setAlarm(am: AlarmManager, type: Int, triggerAtMs: Long, pi: PendingIntent) {
        val inexactType =
            if (type == AlarmManager.ELAPSED_REALTIME_WAKEUP) AlarmManager.ELAPSED_REALTIME_WAKEUP
            else AlarmManager.RTC_WAKEUP
        if (canScheduleExact(am)) {
            try {
                am.setExactAndAllowWhileIdle(type, triggerAtMs, pi)
                return
            } catch (_: SecurityException) {
                // fall through to inexact
            }
        }
        am.setAndAllowWhileIdle(inexactType, triggerAtMs, pi)
    }

    /**
     * [sessionStartedAt] identifies the session this alarm is for. The request code is shared
     * across sessions, so a delayed delivery armed for a PREVIOUS session can still fire after a
     * new one starts — the receiver compares this extra against the active session and ignores
     * mismatches instead of ending whatever happens to be running.
     *
     * [triggerAtElapsedMs] is on the SystemClock.elapsedRealtime() timeline (monotonic), not the
     * wall clock — winding the system clock forward therefore cannot make this fire early and tear
     * down a strict session before its real duration has elapsed.
     */
    fun scheduleSessionEnd(context: Context, triggerAtElapsedMs: Long, sessionStartedAt: Long) {
        val am = alarmManager(context)
        val intent = Intent(context, ScheduleAlarmReceiver::class.java).apply {
            action = ScheduleAlarmReceiver.ACTION_END_SESSION
            putExtra(ScheduleAlarmReceiver.EXTRA_SESSION_STARTED_AT, sessionStartedAt)
        }
        val pi = pending(context, POMODORO_END_REQ, intent)
        setAlarm(am, AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtElapsedMs, pi)
    }

    fun cancelSessionEnd(context: Context) {
        val intent = Intent(context, ScheduleAlarmReceiver::class.java).apply {
            action = ScheduleAlarmReceiver.ACTION_END_SESSION
        }
        alarmManager(context).cancel(pending(context, POMODORO_END_REQ, intent))
    }

    /**
     * Self-heal heartbeat. A single exact ELAPSED_REALTIME alarm ~[HEARTBEAT_INTERVAL_MS] out; its
     * receiver re-asserts enforcement (via SessionController.restoreOnBoot, which honors the safety
     * timer and re-ends an over-due session) and re-arms the next heartbeat. This is the only
     * re-arm vector that survives a force-stop together with the accessibility-service auto-rebind:
     * exact alarms are redelivered to a stopped package, whereas WorkManager jobs are cancelled.
     * Cheap and self-cancelling — it's a no-op when no session is active, then stops re-arming.
     */
    fun scheduleHeartbeat(context: Context) {
        val am = alarmManager(context)
        val pi = pending(context, HEARTBEAT_REQ, heartbeatIntent(context))
        setAlarm(
            am,
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            android.os.SystemClock.elapsedRealtime() + HEARTBEAT_INTERVAL_MS,
            pi,
        )
    }

    fun cancelHeartbeat(context: Context) {
        alarmManager(context).cancel(pending(context, HEARTBEAT_REQ, heartbeatIntent(context)))
    }

    private fun heartbeatIntent(context: Context): Intent =
        Intent(context, ScheduleAlarmReceiver::class.java).apply {
            action = ScheduleAlarmReceiver.ACTION_HEARTBEAT
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
        setAlarm(am, AlarmManager.RTC_WAKEUP, nextMs, pi)
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
        // Overnight window (end <= start, e.g. Mon 22:00→02:00): the END fires on the calendar day
        // AFTER the enabled start day, so its day-bit check must look one day back — otherwise the
        // Tue-02:00 end only matches if Tue is enabled, and a Mon-only schedule's end lands a week
        // late (the safety timer was the only thing saving it).
        val matchDayOffset =
            if (!isStart && schedule.endMinuteOfDay <= schedule.startMinuteOfDay) 1 else 0
        return nextFiringFrom(
            schedule.daysOfWeek, schedule.enabled, minutes, Calendar.getInstance(), matchDayOffset
        )
    }

    /**
     * Pure, deterministic core of [computeNextFiring] — [now] is injected so the day-bitmask /
     * wrap-around math can be unit-tested without depending on the wall clock. Returns the next
     * instant strictly after [now] at time-of-day [minuteOfDay] whose weekday — shifted back by
     * [matchDayOffset] days — has its bit set in [daysOfWeek], or null if [daysOfWeek] is empty or
     * the schedule is disabled. [matchDayOffset] = 1 expresses "this firing belongs to the window
     * that STARTED yesterday" (overnight schedule ends).
     */
    internal fun nextFiringFrom(
        daysOfWeek: Int,
        enabled: Boolean,
        minuteOfDay: Int,
        now: Calendar,
        matchDayOffset: Int = 0,
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
            val matchDay = (cand.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -matchDayOffset) }
            // Calendar.DAY_OF_WEEK: Sun=1, Mon=2, …, Sat=7. Our bit 0 = Sun.
            val bit = matchDay.get(Calendar.DAY_OF_WEEK) - 1
            if ((daysOfWeek shr bit) and 1 == 1 && cand.timeInMillis > now.timeInMillis) {
                return cand.timeInMillis
            }
        }
        return null
    }
}
