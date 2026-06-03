package com.ascendy.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ascendy.app.AscendyApp
import com.ascendy.app.blocking.SessionController
import com.ascendy.app.blocking.SessionSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScheduleAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val app = context.applicationContext as AscendyApp
        val pending = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val controller = SessionController(context.applicationContext, app.repo, app.themePrefs)
                when (action) {
                    ACTION_END_SESSION -> {
                        controller.endSession()
                    }
                    ACTION_SCHEDULE_START -> {
                        val id = intent.getLongExtra(EXTRA_SCHEDULE_ID, -1L)
                        val schedule = if (id >= 0) app.repo.scheduleById(id) else null
                        if (schedule != null && schedule.enabled) {
                            controller.startSession(
                                listId = schedule.listId,
                                tagId = null,
                                source = SessionSource.Scheduled,
                                endsAt = null,
                            )
                            // re-arm for next week
                            AlarmScheduler.scheduleDailyTrigger(context, schedule, isStart = true)
                            // arm the end alarm for the same fire window
                            AlarmScheduler.scheduleDailyTrigger(context, schedule, isStart = false)
                        }
                    }
                    ACTION_SCHEDULE_END -> {
                        val id = intent.getLongExtra(EXTRA_SCHEDULE_ID, -1L)
                        val schedule = if (id >= 0) app.repo.scheduleById(id) else null
                        val current = app.repo.currentSession()
                        if (schedule != null && current?.active == true && current.listId == schedule.listId) {
                            controller.endSession()
                        }
                        if (schedule != null && schedule.enabled) {
                            AlarmScheduler.scheduleDailyTrigger(context, schedule, isStart = false)
                        }
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_END_SESSION = "com.ascendy.app.action.END_SESSION"
        const val ACTION_SCHEDULE_START = "com.ascendy.app.action.SCHEDULE_START"
        const val ACTION_SCHEDULE_END = "com.ascendy.app.action.SCHEDULE_END"
        const val EXTRA_SCHEDULE_ID = "schedule_id"
    }
}
