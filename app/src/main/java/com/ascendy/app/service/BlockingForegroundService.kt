package com.ascendy.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.ascendy.app.AscendyApp
import com.ascendy.app.MainActivity
import com.ascendy.app.R
import com.ascendy.app.blocking.BlockState
import com.ascendy.app.blocking.BlockerActivity
import com.ascendy.app.blocking.SessionController
import com.ascendy.app.ui.theme.vocabFor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class BlockingForegroundService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var pollJob: Job? = null
    private var lastBlockedPkg: String? = null
    private var lastBlockedAt: Long = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        // NH-06: a denied specialUse start / revoked POST_NOTIFICATIONS / missed deadline must not
        // hard-crash into a START_STICKY restart loop. If foreground promotion fails, the alarm +
        // accessibility service still carry enforcement and the heartbeat retries the start.
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceCompat.startForeground(
                    this, NOTIF_ID, buildNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
                )
            } else {
                startForeground(NOTIF_ID, buildNotification())
            }
        } catch (e: Exception) {
            android.util.Log.w(TAG, "startForeground failed: ${e.message}")
        }
        startPolling()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    // AF-01: a recents swipe-away invokes this. While a session is genuinely active, relaunch self
    // so the poller comes back. stopWithTask=false (manifest) keeps us alive; this is the belt to
    // that suspenders for OEMs that still deliver onTaskRemoved.
    override fun onTaskRemoved(rootIntent: Intent?) {
        if (BlockState.isActive()) {
            val restart = Intent(applicationContext, BlockingForegroundService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(restart)
                } else {
                    startService(restart)
                }
            } catch (_: Exception) {}
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        pollJob?.cancel()
        scope.cancel()
    }

    /**
     * Packages that must never be bounced, no matter what the list says. In allow-list
     * (inverted) mode everything outside the list counts as blocked — without these exemptions
     * the blocker itself, the launcher, and system UI get "blocked", which relaunches
     * BlockerActivity in a loop and bounces the user off their own home screen.
     */
    private fun exemptPackages(): Set<String> {
        val launcher = packageManager.resolveActivity(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),
            android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
        )?.activityInfo?.packageName
        return setOfNotNull(packageName, launcher, "com.android.systemui", "android")
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = scope.launch {
            val usage = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            val power = getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
            val exempt = exemptPackages()
            var tick = 0L
            while (isActive) {
                // NH-05: nothing the user can launch happens while the screen is off, and the a11y
                // service (event-driven, always on) catches the screen-on moment instantly — so back
                // the poll way off while non-interactive to cut the battery drain aggressive OEMs
                // cite when they freeze the FGS. The monotonic end check still runs every tick (and
                // the ELAPSED_REALTIME alarm fires in Doze regardless), so auto-end is unaffected.
                val interactive = power?.isInteractive ?: true
                // One bad tick (an OEM startActivity throw, a notify RemoteException, an unexpected
                // end-path error) must NEVER tear down this loop: it owns app-reblocking and the
                // degradation alerts (no redundant path) and is a safety-timer guarantor. Swallow,
                // log, and keep ticking.
                try {
                    // AF-03: the always-running enforcer is also the SAFETY-TIMER guarantor. If the
                    // AlarmManager END alarm is ever dropped (exact-alarm revoked under Doze, OEM
                    // battery manager, overwrite race) enforceMonotonicEndIfDue independently ends
                    // the session at its MONOTONIC deadline — immune to a wall-clock jump in either
                    // direction. It can only ever END, never extend. When it ends one this tick, skip
                    // the poll work below (the session is now inactive anyway).
                    if (BlockState.isActive() && !enforceMonotonicEndIfDue() && interactive) {
                        // AF-10 / AF-11: detect a mid-session loss of either enforcement grant and
                        // warn loudly (every ~7s) instead of silently doing nothing. Checked before
                        // the poll so a revoked usage grant is surfaced even though queryEvents no-ops.
                        if (tick % 10L == 0L) checkEnforcementHealth(usage)

                        if (usage != null) {
                            val now = System.currentTimeMillis()
                            val events = try {
                                usage.queryEvents(now - 5_000L, now)
                            } catch (_: SecurityException) {
                                warnUsageAccessRevoked(); null
                            }

                            if (events != null) {
                                val event = android.app.usage.UsageEvents.Event()
                                var latestForeground: String? = null
                                while (events.hasNextEvent()) {
                                    events.getNextEvent(event)
                                    if (event.eventType == android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED ||
                                        event.eventType == android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND) {
                                        latestForeground = event.packageName
                                    }
                                }
                                if (latestForeground != null && latestForeground !in exempt &&
                                    BlockState.isBlocked(latestForeground)) {
                                    tryBlock(latestForeground)
                                }
                            }
                        }
                    }
                } catch (c: kotlinx.coroutines.CancellationException) {
                    throw c   // never swallow cooperative cancellation
                } catch (e: Exception) {
                    android.util.Log.w(TAG, "poll tick error (continuing): ${e.message}")
                }
                tick++
                delay(if (interactive) POLL_INTERVAL_MS else SCREEN_OFF_POLL_MS)
            }
        }
    }

    /**
     * Ends the active session iff its monotonic deadline has passed. Routed through
     * SessionController.endSessionIfStartedAt so the ownership guard prevents ending a freshly
     * started session. Returns true if it ended one.
     */
    private suspend fun enforceMonotonicEndIfDue(): Boolean {
        val deadline = BlockState.endsAtElapsed() ?: return false
        if (SystemClock.elapsedRealtime() < deadline) return false
        val startedAt = BlockState.startedAt.value ?: return false
        val app = application as AscendyApp
        SessionController(applicationContext, app.repo, app.themePrefs)
            .endSessionIfStartedAt(startedAt)
        return true
    }

    /** AF-10: accessibility (the only URL-blocking path) turned off mid-session. */
    private fun checkEnforcementHealth(usage: UsageStatsManager?) {
        val nm = getSystemService(NotificationManager::class.java) ?: return
        val a11yOn = EnforcementHealth.isAccessibilityEnabled(this)
        if (!a11yOn) {
            nm.notify(ALERT_A11Y_ID, buildAlert(
                getString(R.string.alert_a11y_title), getString(R.string.alert_a11y_body),
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS),
            ))
        } else {
            nm.cancel(ALERT_A11Y_ID)
        }
        if (usage != null && !EnforcementHealth.hasUsageAccess(this)) {
            warnUsageAccessRevoked()
        } else {
            nm.cancel(ALERT_USAGE_ID)
        }
        // NH-11: with accessibility off, this poller is the only app-blocking path — and on
        // API 29+ the OS only lets it start BlockerActivity from the background while the
        // overlay (SYSTEM_ALERT_WINDOW) grant is held. Both missing means tryBlock() below is
        // being silently suppressed: detection still runs but nothing visible happens. Warn as
        // loudly as the other degradations instead of letting the user believe they're blocked.
        val overlayDead = !a11yOn &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !Settings.canDrawOverlays(this)
        if (overlayDead) {
            nm.notify(ALERT_OVERLAY_ID, buildAlert(
                getString(R.string.alert_overlay_title), getString(R.string.alert_overlay_body),
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:$packageName"),
                ),
            ))
        } else {
            nm.cancel(ALERT_OVERLAY_ID)
        }
    }

    private fun warnUsageAccessRevoked() {
        val nm = getSystemService(NotificationManager::class.java) ?: return
        nm.notify(ALERT_USAGE_ID, buildAlert(
            getString(R.string.alert_usage_title), getString(R.string.alert_usage_body),
            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
        ))
    }

    /** A loud, persistent diagnostic alert that taps through to the relevant system settings screen. */
    private fun buildAlert(title: String, body: String, settingsIntent: Intent): Notification {
        ensureAlertChannel()
        val tap = PendingIntent.getActivity(
            this, settingsIntent.action?.hashCode() ?: 0,
            settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(tap)
            .build()
    }

    private fun ensureAlertChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(NotificationManager::class.java) ?: return
        if (nm.getNotificationChannel(ALERT_CHANNEL_ID) != null) return
        nm.createNotificationChannel(
            NotificationChannel(
                ALERT_CHANNEL_ID,
                getString(R.string.alert_channel_name),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply { description = getString(R.string.alert_channel_desc) }
        )
    }

    private fun tryBlock(pkg: String) {
        val now = SystemClock.uptimeMillis()
        if (pkg == lastBlockedPkg && now - lastBlockedAt < 1500L) return
        lastBlockedPkg = pkg
        lastBlockedAt = now

        // NH-11: on API 29+ this background start is only honored while the SYSTEM_ALERT_WINDOW
        // grant is held (or the a11y service is bound, which has its own exemption). When neither
        // applies the OS suppresses it silently — checkEnforcementHealth raises the alert for that.
        startActivity(
            Intent(this, BlockerActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    private fun buildNotification(): Notification {
        ensureChannel()
        val vocab = vocabFor((application as AscendyApp).currentVariant)
        val tap = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val statsTap = PendingIntent.getActivity(
            this, 1,
            Intent(this, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, "stats")
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val toggleAction = PendingIntent.getBroadcast(
            this, 2,
            Intent(this, SessionControlReceiver::class.java)
                .setAction(SessionControlReceiver.ACTION_TOGGLE)
                .setPackage(packageName),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle(vocab.notifTitle)
            .setContentText(vocab.notifText)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(tap)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(0, vocab.notifActionStats, statsTap)
            .addAction(0, vocab.notifActionEnd, toggleAction)
            .build()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(NotificationManager::class.java) ?: return
        if (nm.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notif_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notif_channel_desc)
            setShowBadge(false)
        }
        nm.createNotificationChannel(channel)
    }

    companion object {
        private const val TAG = "AscendyFgs"
        private const val POLL_INTERVAL_MS = 700L
        private const val SCREEN_OFF_POLL_MS = 5_000L
        private const val CHANNEL_ID = "ascendy.focus"
        private const val ALERT_CHANNEL_ID = "ascendy.alerts"
        private const val NOTIF_ID = 4242
        private const val ALERT_A11Y_ID = 4244
        private const val ALERT_USAGE_ID = 4245
        private const val ALERT_OVERLAY_ID = 4247   // 4246 is the VPN-revoked alert
    }
}
