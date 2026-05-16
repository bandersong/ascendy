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
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_LOCKED_BOOT_COMPLETED) return

        val app = context.applicationContext as AscendyApp
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                SessionController(context.applicationContext, app.repo, app.themePrefs).restoreOnBoot()
                // re-arm every enabled schedule's next firing
                app.repo.allEnabledSchedules().forEach { schedule ->
                    AlarmScheduler.scheduleDailyTrigger(context, schedule, isStart = true)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
