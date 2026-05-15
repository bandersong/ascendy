package com.ascendy.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.ascendy.app.blocking.BlockState
import com.ascendy.app.blocking.BlockerActivity

class BlockingAccessibilityService : AccessibilityService() {

    private val lastBlockedAt = HashMap<String, Long>()
    private val debounceMs = 1500L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (!BlockState.isActive()) return

        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return
        if (pkg in IGNORED_PACKAGES) return

        // Path 1: app-level blocking on activity switch
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (BlockState.isBlocked(pkg)) {
                bounceHome(pkg)
                return
            }
        }

        // Path 2: URL blocking inside browsers — check on content/text changes too
        val browserUrlBarId = BROWSER_URL_BAR_IDS[pkg]
        if (browserUrlBarId != null) {
            val url = findUrlInTree(rootInActiveWindow, browserUrlBarId)
            if (url != null) {
                val host = extractHost(url)
                if (host != null && BlockState.isDomainBlocked(host)) {
                    bounceHome("$pkg:$host")
                }
            }
        }
    }

    private fun bounceHome(key: String) {
        val now = SystemClock.uptimeMillis()
        val last = lastBlockedAt[key] ?: 0L
        if (now - last < debounceMs) return
        lastBlockedAt[key] = now

        performGlobalAction(GLOBAL_ACTION_HOME)
        startActivity(
            Intent(this, BlockerActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    private fun findUrlInTree(root: AccessibilityNodeInfo?, resourceId: String): String? {
        if (root == null) return null
        val nodes = root.findAccessibilityNodeInfosByViewId(resourceId)
        if (nodes.isNullOrEmpty()) return null
        for (n in nodes) {
            val text = n.text?.toString() ?: continue
            if (text.isNotBlank()) return text
        }
        return null
    }

    /** Extract the host from a URL bar string. Bars sometimes show the host only, sometimes the full URL. */
    private fun extractHost(raw: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null
        // strip scheme
        var s = trimmed.removePrefix("https://").removePrefix("http://")
        // some bars show "search query" instead of a URL — bail if no dot
        if ('.' !in s.substringBefore('/').substringBefore(' ')) return null
        // host portion only
        s = s.substringBefore('/').substringBefore('?').substringBefore('#').substringBefore(' ')
        s = s.removePrefix("www.").lowercase()
        return s.ifBlank { null }
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

        /** Known Android browser packages → their URL-bar accessibility view-IDs. */
        private val BROWSER_URL_BAR_IDS = mapOf(
            "com.android.chrome" to "com.android.chrome:id/url_bar",
            "com.chrome.beta" to "com.chrome.beta:id/url_bar",
            "com.chrome.dev" to "com.chrome.dev:id/url_bar",
            "com.chrome.canary" to "com.chrome.canary:id/url_bar",
            "org.mozilla.firefox" to "org.mozilla.firefox:id/mozac_browser_toolbar_url_view",
            "org.mozilla.firefox_beta" to "org.mozilla.firefox_beta:id/mozac_browser_toolbar_url_view",
            "org.mozilla.fenix" to "org.mozilla.fenix:id/mozac_browser_toolbar_url_view",
            "com.sec.android.app.sbrowser" to "com.sec.android.app.sbrowser:id/location_bar_edit_text",
            "com.microsoft.emmx" to "com.microsoft.emmx:id/url_bar",
            "com.brave.browser" to "com.brave.browser:id/url_bar",
            "com.opera.browser" to "com.opera.browser:id/url_field",
            "com.opera.mini.native" to "com.opera.mini.native:id/url_field",
            "com.duckduckgo.mobile.android" to "com.duckduckgo.mobile.android:id/omnibarTextInput",
            "org.torproject.torbrowser" to "org.torproject.torbrowser:id/mozac_browser_toolbar_url_view",
            "com.vivaldi.browser" to "com.vivaldi.browser:id/url_bar",
        )
    }
}
