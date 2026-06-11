package com.ascendy.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.ascendy.app.AscendyApp
import com.ascendy.app.MainActivity
import com.ascendy.app.R
import com.ascendy.app.blocking.BlockState
import com.ascendy.app.blocking.BlockerActivity
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
        startForeground(NOTIF_ID, buildNotification())
        startPolling()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
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
            val exempt = exemptPackages()
            while (isActive) {
                if (BlockState.isActive() && usage != null) {
                    val now = System.currentTimeMillis()
                    val events = try {
                        usage.queryEvents(now - 5_000L, now)
                    } catch (_: SecurityException) { null }

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
                delay(700L)
            }
        }
    }

    private fun tryBlock(pkg: String) {
        val now = SystemClock.uptimeMillis()
        if (pkg == lastBlockedPkg && now - lastBlockedAt < 1500L) return
        lastBlockedPkg = pkg
        lastBlockedAt = now

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
        private const val CHANNEL_ID = "ascendy.focus"
        private const val NOTIF_ID = 4242
    }
}
