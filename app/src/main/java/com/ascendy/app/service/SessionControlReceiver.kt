package com.ascendy.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.ascendy.app.AscendyApp
import com.ascendy.app.blocking.ManualEndResult
import com.ascendy.app.blocking.SessionController
import com.ascendy.app.ui.theme.vocabFor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Receives manual toggle requests from the notification action button + Quick Settings tile.
 * Toggles a manual session via [SessionController.toggleManual] (which honors strict mode).
 */
class SessionControlReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TOGGLE) return
        val app = context.applicationContext as AscendyApp
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val controller = SessionController(context.applicationContext, app.repo, app.themePrefs)
                val result = controller.toggleManual()
                val vocab = vocabFor(app.currentVariant)
                val msg = when (result) {
                    ManualEndResult.BlockedStrict -> vocab.strictManualBlockedToast
                    ManualEndResult.Ended -> vocab.toastManualEnded
                    ManualEndResult.NoSession -> vocab.toastManualStarted
                }
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_TOGGLE = "com.ascendy.app.action.SESSION_TOGGLE"
    }
}
