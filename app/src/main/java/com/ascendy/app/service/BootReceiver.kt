package com.ascendy.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ascendy.app.AscendyApp
import com.ascendy.app.blocking.SessionController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val app = context.applicationContext as AscendyApp
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // restoreOnBoot re-arms the active session's END alarm + heartbeat (or ends it if
                // its window passed while powered off). Guard it: a transient DB/IO hiccup at boot
                // must not crash the receiver and drop the schedule re-arm below.
                try {
                    SessionController(context.applicationContext, app.repo, app.themePrefs).restoreOnBoot()
                } catch (_: Exception) {}
                // Re-arm both the START and END daily triggers for every enabled schedule. Alarms
                // don't survive reboot; without re-arming the END, a device that boots DURING a
                // window would never fire the scheduled end (only the safety timer would save it).
                app.repo.allEnabledSchedules().forEach { schedule ->
                    AlarmScheduler.scheduleDailyTrigger(context, schedule, isStart = true)
                    AlarmScheduler.scheduleDailyTrigger(context, schedule, isStart = false)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
