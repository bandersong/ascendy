package com.ascendy.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.ascendy.app.R
import com.ascendy.app.blocking.BlockState
import com.ascendy.app.blocking.BlockerActivity

class BlockingAccessibilityService : AccessibilityService() {

    private val lastBlockedAt = HashMap<String, Long>()
    private val debounceMs = 1500L
    private val tag = "AscendyA11y"

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (!BlockState.isActive()) return

        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return
        if (pkg in IGNORED_PACKAGES) return

        // Path 0: Lockdown — bounce out of any Settings screen that shows Ascendy. Those are the
        // only screens that can disable the blocker mid-session: the accessibility-service toggle,
        // our app-info/uninstall page, and the device-admin deactivation page (all of which display
        // the app's name). Narrowly scoped — other Settings screens stay usable. Device-admin
        // separately blocks the uninstall itself; this closes the "just toggle it off" hole.
        if (BlockState.isLockdown() &&
            event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            isSettingsPackage(pkg) &&
            windowMentionsSelf(rootInActiveWindow)
        ) {
            Log.d(tag, "lockdown bounce from settings pkg=$pkg")
            bounceHome("lockdown")
            return
        }

        // Path 1: app-level blocking on activity switch
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (BlockState.isBlocked(pkg)) {
                bounceHome(pkg)
                return
            }
        }

        // Path 2: URL blocking inside browsers
        if (BlockState.blockedDomains.value.isEmpty()) return
        if (!BROWSER_PACKAGES.contains(pkg)) return

        // Try the known URL-bar resource ID first
        val knownId = BROWSER_URL_BAR_IDS[pkg]
        val urlFromKnownId = knownId?.let { findFirstText(rootInActiveWindow, it) }
        val candidates = mutableListOf<String>()
        if (urlFromKnownId != null) candidates += urlFromKnownId

        // Also walk for any EditText / TextView whose id looks like a url bar — covers newer
        // browser versions where the resource id changed.
        candidates += scanForUrlLikeNodes(rootInActiveWindow)

        // Also consider event.text — TYPE_VIEW_TEXT_CHANGED includes the current text directly
        event.text?.forEach { cs -> cs?.toString()?.takeIf { it.isNotBlank() }?.let { candidates += it } }

        for (raw in candidates) {
            val host = extractHost(raw) ?: continue
            if (BlockState.isDomainBlocked(host)) {
                Log.d(tag, "blocking host=$host in pkg=$pkg (raw=$raw)")
                bounceHome("$pkg:$host")
                return
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

    private fun findFirstText(root: AccessibilityNodeInfo?, resourceId: String): String? {
        if (root == null) return null
        val nodes = try { root.findAccessibilityNodeInfosByViewId(resourceId) } catch (_: Exception) { null }
        if (nodes.isNullOrEmpty()) return null
        for (n in nodes) {
            val text = n.text?.toString() ?: continue
            if (text.isNotBlank()) return text
        }
        return null
    }

    /** The app's display label, lowercased, used to spot Settings screens that are about Ascendy. */
    private val selfLabel: String by lazy { getString(R.string.app_name).trim().lowercase() }

    private fun isSettingsPackage(pkg: String): Boolean =
        pkg == "com.android.settings" || pkg.endsWith(".settings") || pkg.contains("settings")

    /**
     * Bounded scan of the active window for any node whose text names Ascendy — by display label or
     * by package name. True only for the accessibility-toggle, app-info, and device-admin screens
     * (and the lists that lead to them), keeping the lockdown bounce narrow.
     */
    private fun windowMentionsSelf(root: AccessibilityNodeInfo?): Boolean {
        if (root == null) return false
        val stack = ArrayDeque<AccessibilityNodeInfo>()
        stack.addLast(root)
        var visited = 0
        val limit = 300
        while (stack.isNotEmpty() && visited < limit) {
            val n = stack.removeFirst()
            visited++
            val text = n.text?.toString()?.lowercase()
            if (text != null && (text.contains(selfLabel) || text.contains(packageName))) return true
            for (i in 0 until n.childCount) {
                n.getChild(i)?.let { stack.addLast(it) }
            }
        }
        return false
    }

    /** Walk the entire active-window tree for nodes whose view-id resource name hints at a URL bar. */
    private fun scanForUrlLikeNodes(root: AccessibilityNodeInfo?): List<String> {
        if (root == null) return emptyList()
        val out = mutableListOf<String>()
        val stack = ArrayDeque<AccessibilityNodeInfo>()
        stack.addLast(root)
        var visited = 0
        val limit = 400        // hard cap to keep main thread responsive
        while (stack.isNotEmpty() && visited < limit) {
            val n = stack.removeFirst()
            visited++
            val viewId = n.viewIdResourceName?.lowercase()
            val text = n.text?.toString()
            if (viewId != null && (viewId.endsWith("url_bar") ||
                                   viewId.endsWith("url_field") ||
                                   viewId.endsWith("location_bar_edit_text") ||
                                   viewId.contains("omnibar") ||
                                   viewId.contains("mozac_browser_toolbar_url_view"))
                && !text.isNullOrBlank()) {
                out += text
            }
            for (i in 0 until n.childCount) {
                n.getChild(i)?.let { stack.addLast(it) }
            }
        }
        return out
    }

    /** Extract a host from a URL bar string. Bars show full URLs, or just hosts, or even search strings. */
    private fun extractHost(raw: String): String? = UrlHost.fromUrlBar(raw)

    override fun onInterrupt() = Unit

    companion object {
        private val IGNORED_PACKAGES = setOf(
            "com.android.systemui",
            "android",
            "com.google.android.inputmethod.latin",
            "com.android.launcher",
            "com.android.launcher3"
        )

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

        /** Treat any of these as a browser for the purposes of running the URL scan. */
        private val BROWSER_PACKAGES: Set<String> = BROWSER_URL_BAR_IDS.keys + setOf(
            "com.kiwibrowser.browser",
            "com.UCMobile.intl",
            "com.yandex.browser",
            "com.ecosia.android",
        )
    }
}
