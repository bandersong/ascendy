package com.ascendy.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import com.ascendy.app.blocking.BlockState
import com.ascendy.app.blocking.BlockerActivity

class BlockingAccessibilityService : AccessibilityService() {

    private val lastBlockedAt = HashMap<String, Long>()
    private val debounceMs = 1500L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        if (!BlockState.isActive()) return

        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return
        if (pkg in IGNORED_PACKAGES) return
        if (!BlockState.isBlocked(pkg)) return

        val now = SystemClock.uptimeMillis()
        val last = lastBlockedAt[pkg] ?: 0L
        if (now - last < debounceMs) return
        lastBlockedAt[pkg] = now

        performGlobalAction(GLOBAL_ACTION_HOME)
        startActivity(
            Intent(this, BlockerActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    override fun onInterrupt() = Unit

    companion object {
        private val IGNORED_PACKAGES = setOf(
            "com.android.systemui",
            "android",
            "com.google.android.inputmethod.latin",
            "com.android.launcher",
            "com.android.launcher3"
        )
    }
}
