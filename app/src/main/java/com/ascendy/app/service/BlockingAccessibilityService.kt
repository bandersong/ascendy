package com.ascendy.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.ascendy.app.AscendyApp
import com.ascendy.app.R
import com.ascendy.app.blocking.BlockState
import com.ascendy.app.blocking.BlockerActivity
import com.ascendy.app.blocking.SessionController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BlockingAccessibilityService : AccessibilityService() {

    private val lastBlockedAt = HashMap<String, Long>()
    private val debounceMs = 1500L
    private val tag = "AscendyA11y"

    // AF-07: per-package throttle for the expensive node walk, so a chatty app firing rapid
    // content-change events can't run a 400-node scan on every event.
    private val lastDeepScanAt = HashMap<String, Long>()
    private val deepScanThrottleMs = 800L

    // AF-01: when the system re-binds this service after a force-stop (the user reopens the app, or
    // the heartbeat alarm wakes it), re-assert enforcement from the persisted session. restoreOnBoot
    // honors the safety timer, so this can only heal an active session — never resurrect an expired
    // one. This is the auto-rebind half of the force-stop self-heal.
    override fun onServiceConnected() {
        super.onServiceConnected()
        val app = applicationContext as? AscendyApp ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                SessionController(applicationContext, app.repo, app.themePrefs).restoreOnBoot()
            } catch (e: Exception) {
                Log.w(tag, "onServiceConnected heal failed: ${e.message}")
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (!BlockState.isActive()) return

        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return
        if (pkg in IGNORED_PACKAGES) return
        // The actual default launcher is OEM-specific (Pixel/Samsung/etc. aren't in the static
        // list) — bouncing it in allow-list mode would throw the user into a home-bounce loop.
        if (pkg == defaultLauncherPackage) return

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

        // Path 2: URL blocking inside browsers.
        if (BlockState.blockedDomains.value.isEmpty()) return

        val candidates = mutableListOf<String>()

        // Try the known URL-bar resource ID first (cheap, exact).
        BROWSER_URL_BAR_IDS[pkg]?.let { findFirstText(rootInActiveWindow, it) }
            ?.let { candidates += it }

        // event.text is cheap and always present on text-change events — covers in-app webviews /
        // custom tabs whose host package isn't itself a browser.
        event.text?.forEach { cs -> cs?.toString()?.takeIf { it.isNotBlank() }?.let { candidates += it } }

        // Heuristic node walk for any url-bar-like view. Run it for ANY package the system reports
        // as a browser — the static list PLUS every app that handles http(s) VIEW intents (Cromite,
        // Mull, Vivaldi forks, vendor browsers) — not just a hardcoded 18. Throttled per package to
        // keep the main thread responsive; the visited-node cap inside the walk bounds it further.
        if (isBrowserPackage(pkg) && shouldDeepScan(pkg)) {
            candidates += scanForUrlLikeNodes(rootInActiveWindow)
        }

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
        // Drop entries past their debounce window so the map stays bounded over a long session
        // instead of accumulating one row per distinct blocked package/host forever.
        lastBlockedAt.entries.removeAll { now - it.value >= debounceMs }
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

    private val defaultLauncherPackage: String? by lazy {
        packageManager.resolveActivity(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),
            android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
        )?.activityInfo?.packageName
    }

    /** The accessibility-service entry's label in Settings, lowercased. */
    private val serviceLabel: String by lazy {
        getString(R.string.accessibility_service_label).trim().lowercase()
    }

    /** The device-admin entry's label in Settings, lowercased. */
    private val adminLabel: String by lazy {
        getString(R.string.device_admin_label).trim().lowercase()
    }

    // Exact package match only — `contains("settings")` also caught arbitrary third-party apps
    // with "settings" anywhere in their package name.
    private fun isSettingsPackage(pkg: String): Boolean =
        pkg == "com.android.settings" || pkg.endsWith(".settings")

    /**
     * Bounded scan of the active window for a node whose text IS one of Ascendy's labels (app
     * label, accessibility-service label, device-admin label) — the titles/rows shown on the
     * screens that can disable the blocker. Exact equality, not contains: incidental mentions in
     * composite strings (battery usage rows, notification history, search suggestions) must not
     * bounce the user out of unrelated Settings screens.
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
            val text = n.text?.toString()?.trim()?.lowercase()
            if (text != null &&
                (text == selfLabel || text == serviceLabel || text == adminLabel || text == packageName)
            ) return true
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

    /**
     * Every package the system reports as a web browser: the static list (fast known-id paths) plus
     * everything that resolves an http(s) VIEW + BROWSABLE intent. Resolved once and cached — covers
     * unlisted browsers without QUERY_ALL_PACKAGES (the manifest <queries> declares the web intent).
     */
    private val dynamicBrowsers: Set<String> by lazy {
        val out = HashSet<String>(BROWSER_PACKAGES)
        try {
            val probe = Intent(Intent.ACTION_VIEW, Uri.parse("http://example.com"))
                .addCategory(Intent.CATEGORY_BROWSABLE)
            val resolves = packageManager.queryIntentActivities(probe, PackageManager.MATCH_ALL)
            resolves.forEach { it.activityInfo?.packageName?.let(out::add) }
        } catch (e: Exception) {
            Log.w(tag, "browser enumeration failed: ${e.message}")
        }
        out
    }

    private fun isBrowserPackage(pkg: String): Boolean =
        pkg in BROWSER_PACKAGES || pkg in dynamicBrowsers

    /** Per-package throttle gate for the node walk. Drops stale entries so the map stays bounded. */
    private fun shouldDeepScan(pkg: String): Boolean {
        val now = SystemClock.uptimeMillis()
        val last = lastDeepScanAt[pkg] ?: 0L
        if (now - last < deepScanThrottleMs) return false
        lastDeepScanAt.entries.removeAll { now - it.value >= 60_000L }
        lastDeepScanAt[pkg] = now
        return true
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
