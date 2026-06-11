package com.ascendy.app.blocking

import android.content.Context
import android.content.Intent
import android.os.Build
import android.net.VpnService
import com.ascendy.app.widget.AscendyWidget
import com.ascendy.app.data.AscendyRepo
import com.ascendy.app.data.BlockSession
import com.ascendy.app.data.ThemePrefs
import com.ascendy.app.service.AlarmScheduler
import com.ascendy.app.service.BlockingForegroundService
import com.ascendy.app.vpn.AscendyVpnService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed class TapResult {
    data class Locked(val listName: String) : TapResult()
    object Unlocked : TapResult()
    data class UnknownTag(val tagId: String) : TapResult()
    data class WrongTag(val expected: String) : TapResult()
}

enum class SessionSource(val tag: String) {
    Nfc("nfc"), Manual("manual"), Pomodoro("pomodoro"), Scheduled("scheduled");
}

enum class ManualEndResult { Ended, BlockedStrict, NoSession }

class SessionController(
    private val context: Context,
    private val repo: AscendyRepo,
    private val themePrefs: ThemePrefs,
) {

    suspend fun handleTagTap(tagId: String): TapResult = transitionMutex.withLock {
        val tag = repo.tagById(tagId) ?: return TapResult.UnknownTag(tagId)
        val current = repo.currentSession()

        return if (current?.active == true) {
            if (current.tagId != null && current.tagId != tag.tagId) {
                TapResult.WrongTag(current.tagId)
            } else {
                endSessionLocked()
                TapResult.Unlocked
            }
        } else {
            val list = tag.listId?.let { repo.list(it) }
                ?: repo.defaultList()
                ?: repo.ensureDefaultList()
            startSessionLocked(list.id, tag.tagId, SessionSource.Nfc)
            TapResult.Locked(list.name)
        }
    }

    /** @return true if a session was started; false if an active session already exists. */
    suspend fun startSession(
        listId: Long,
        tagId: String?,
        source: SessionSource = SessionSource.Nfc,
        endsAt: Long? = null,
        scheduleId: Long? = null,
    ): Boolean = transitionMutex.withLock {
        startSessionLocked(listId, tagId, source, endsAt, scheduleId)
    }

    private suspend fun startSessionLocked(
        listId: Long,
        tagId: String?,
        source: SessionSource = SessionSource.Nfc,
        endsAt: Long? = null,
        scheduleId: Long? = null,
    ): Boolean {
        // Never clobber a running session: a scheduled or pomodoro start while (say) a STRICT
        // session is active would overwrite session row id=1 with a weaker one — an escape hatch —
        // and orphan the prior open log, inflating stats.
        if (repo.currentSession()?.active == true) return false

        val list = repo.list(listId) ?: repo.ensureDefaultList()
        val packages = repo.packages(list.id).toSet()
        val domains = repo.domains(list.id).toSet()
        val now = System.currentTimeMillis()

        // Safety timer: force every session to have an auto-end. Explicit endsAt (pomodoro,
        // scheduled) wins if shorter; otherwise we use the user's configured max duration.
        val safetyEndsAt = now + maxSessionMs()
        val effectiveEndsAt = if (endsAt != null) minOf(endsAt, safetyEndsAt) else safetyEndsAt

        // Manual sessions (no anchor) never inherit strict mode — strict is reserved for
        // sessions you've explicitly committed to with a tag/QR anchor. Otherwise long-pressing
        // the mascot can accidentally trap the user with no escape.
        val isStrict = list.isStrict && source != SessionSource.Manual
        val unlocksLeft = if (isStrict) 0 else 1
        val lockdown = themePrefs.lockdownEnabled.first()

        val session = BlockSession(
            id = 1L,
            active = true,
            startedAt = now,
            listId = list.id,
            tagId = tagId,
            emergencyUnlocksLeft = unlocksLeft,
            endsAt = effectiveEndsAt,
            scheduleId = scheduleId,
        )
        repo.saveSession(session)
        repo.startLog(list.id, now, source.tag)
        BlockState.set(
            active = true,
            blocked = packages,
            blockedDomains = domains,
            startedAt = now,
            emergencyAvailable = !isStrict && unlocksLeft > 0,
            strict = isStrict,
            inverted = list.isAllowList,
            lockdown = lockdown,
        )
        startForegroundService()
        AlarmScheduler.scheduleSessionEnd(context, effectiveEndsAt, now)
        // Auto-start VPN sinkhole if user has consented AND any domains are configured
        if (domains.isNotEmpty() && VpnService.prepare(context) == null) {
            startVpnService()
        }
        // Public broadcast for Tasker / other automation
        context.sendBroadcast(
            Intent(ACTION_SESSION_STARTED)
                .setPackage(context.packageName)
                .putExtra(EXTRA_LIST_NAME, list.name)
                .putExtra(EXTRA_LIST_ID, list.id)
                .putExtra(EXTRA_SOURCE, source.tag)
                .putExtra(EXTRA_ENDS_AT, effectiveEndsAt)
                .putExtra(EXTRA_STRICT, isStrict)
        )
        AscendyWidget.refresh(context)
        return true
    }

    suspend fun endSession() = transitionMutex.withLock { endSessionLocked() }

    /**
     * End the session only if it is the one identified by [startedAt] — used by the END alarm so
     * a stale delivery armed for a previous session can never kill the current one.
     */
    suspend fun endSessionIfStartedAt(startedAt: Long) = transitionMutex.withLock {
        val current = repo.currentSession()
        if (current?.active == true && current.startedAt == startedAt) endSessionLocked()
    }

    /**
     * End the session only if it was started by schedule [scheduleId]. The ownership check lives
     * inside the lock: checking in the receiver and then calling endSession() would race a
     * concurrent transition and could end a session the schedule never owned.
     */
    suspend fun endSessionIfScheduleId(scheduleId: Long) = transitionMutex.withLock {
        val current = repo.currentSession()
        if (current?.active == true && current.scheduleId == scheduleId) endSessionLocked()
    }

    /**
     * [logEndMs] overrides the focus time credited to the session's log — used when the session
     * actually ended earlier than this call (e.g. it expired while the device was powered off),
     * so the dead gap is never counted as focus.
     */
    private suspend fun endSessionLocked(logEndMs: Long? = null) {
        val current = repo.currentSession() ?: return
        val now = System.currentTimeMillis()
        val logEnd = (logEndMs ?: now).coerceIn(current.startedAt, now)
        repo.saveSession(current.copy(active = false))
        // Close this session's log at the true end time, then sweep any older dangling logs
        // with a clamped duration — closing orphans at `now` would credit the whole gap
        // since their crash as focus time.
        repo.finishOpenLogStartedAt(current.startedAt, logEnd)
        repo.closeStaleOpenLogs(now, maxSessionMs())
        val durationMs = logEnd - current.startedAt
        BlockState.clear()
        stopForegroundService()
        stopVpnService()
        AlarmScheduler.cancelSessionEnd(context)
        context.sendBroadcast(
            Intent(ACTION_SESSION_ENDED)
                .setPackage(context.packageName)
                .putExtra(EXTRA_LIST_ID, current.listId)
                .putExtra(EXTRA_DURATION_MS, durationMs)
        )
        AscendyWidget.refresh(context)
    }

    suspend fun useEmergencyUnlock(): Boolean = transitionMutex.withLock {
        val current = repo.currentSession() ?: return false
        if (!current.active) return false
        if (current.emergencyUnlocksLeft <= 0) return false
        repo.saveSession(current.copy(emergencyUnlocksLeft = current.emergencyUnlocksLeft - 1))
        endSessionLocked()
        return true
    }

    private suspend fun maxSessionMs(): Long =
        themePrefs.maxSessionMinutes.first().coerceIn(60, 24 * 60) * 60_000L

    /**
     * Close any session logs left open by a crash or force-stop, crediting each with at most
     * the safety-timer duration. The active session's own log (if any) is left open — it is
     * still genuinely running. Called once at app start.
     */
    suspend fun reconcileStaleLogs(): Unit = transitionMutex.withLock {
        val now = System.currentTimeMillis()
        val activeStartedAt = repo.currentSession()?.takeIf { it.active }?.startedAt ?: -1L
        repo.closeStaleOpenLogs(now, maxSessionMs(), exceptStartedAt = activeStartedAt)
    }

    suspend fun restoreOnBoot(): Unit = transitionMutex.withLock {
        val current = repo.currentSession() ?: return
        if (!current.active) return
        val now = System.currentTimeMillis()

        // Re-derive the auto-end instant. Schedule-sourced sessions rely on a daily END alarm
        // that does NOT survive a reboot, so recompute this window's end from the schedule.
        val schedule = current.scheduleId?.let { repo.scheduleById(it) }
        val windowEndMs = schedule?.let {
            val windowMin = (it.endMinuteOfDay - it.startMinuteOfDay).mod(1440)
                .let { m -> if (m == 0) 1440 else m }
            current.startedAt + windowMin * 60_000L
        }
        val endsAt = listOfNotNull(current.endsAt, windowEndMs).minOrNull()

        // If the end already passed while the device was off, end now instead of resurrecting
        // a session that should have died hours ago — crediting focus only up to the scheduled
        // end, not the powered-off gap since.
        if (endsAt != null && now >= endsAt) {
            endSessionLocked(logEndMs = endsAt)
            return
        }

        val list = repo.list(current.listId)
        val packages = repo.packages(current.listId).toSet()
        val domains = repo.domains(current.listId).toSet()
        BlockState.set(
            active = true,
            blocked = packages,
            blockedDomains = domains,
            startedAt = current.startedAt,
            emergencyAvailable = list?.isStrict != true && current.emergencyUnlocksLeft > 0,
            strict = list?.isStrict == true,
            inverted = list?.isAllowList == true,
            lockdown = themePrefs.lockdownEnabled.first(),
        )
        startForegroundService()
        endsAt?.let { AlarmScheduler.scheduleSessionEnd(context, it, current.startedAt) }
        // The VPN tunnel does not survive reboot/process death — restart it or site blocking
        // is silently off for the rest of the session.
        if (domains.isNotEmpty() && VpnService.prepare(context) == null) {
            startVpnService()
        }
    }

    /**
     * Manual long-press toggle. Starts a session if none is active. If an active session is
     * strict, [ManualEndResult.BlockedStrict] is returned and the session is NOT ended — the user
     * must use the bound tag/QR or wait for the safety timer.
     */
    suspend fun toggleManual(): ManualEndResult = transitionMutex.withLock {
        val current = repo.currentSession()
        return if (current?.active == true) {
            val list = repo.list(current.listId)
            // Strict only blocks the manual exit for tag/QR-bound sessions — those are the
            // ones the user committed to with a physical anchor. Manual sessions
            // (tagId == null) are always endable via the long-press toggle.
            if (list?.isStrict == true && current.tagId != null) {
                ManualEndResult.BlockedStrict
            } else {
                endSessionLocked()
                ManualEndResult.Ended
            }
        } else {
            val list = repo.defaultList() ?: repo.ensureDefaultList()
            startSessionLocked(list.id, tagId = null, source = SessionSource.Manual)
            ManualEndResult.NoSession  // semantically "started fresh"
        }
    }

    suspend fun startTimedSession(durationMs: Long, listId: Long? = null): Boolean = transitionMutex.withLock {
        val list = listId?.let { repo.list(it) } ?: repo.defaultList() ?: repo.ensureDefaultList()
        val endsAt = System.currentTimeMillis() + durationMs
        startSessionLocked(list.id, tagId = null, source = SessionSource.Pomodoro, endsAt = endsAt)
    }

    private fun startForegroundService() {
        val intent = Intent(context, BlockingForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private fun stopForegroundService() {
        context.stopService(Intent(context, BlockingForegroundService::class.java))
    }

    private fun startVpnService() {
        val intent = Intent(context, AscendyVpnService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private fun stopVpnService() {
        context.startService(
            Intent(context, AscendyVpnService::class.java)
                .setAction(AscendyVpnService.ACTION_STOP)
        )
    }

    companion object {
        /**
         * Controllers are constructed independently in MainActivity, the QS tile, the
         * notification receiver, the schedule alarm receiver, and the boot receiver — so the
         * lock that serializes session transitions must be process-wide, not per-instance.
         * Every public mutating entry point takes it; private *Locked methods assume it's held.
         */
        private val transitionMutex = Mutex()

        const val ACTION_SESSION_STARTED = "com.ascendy.app.SESSION_STARTED"
        const val ACTION_SESSION_ENDED = "com.ascendy.app.SESSION_ENDED"
        const val EXTRA_LIST_ID = "list_id"
        const val EXTRA_LIST_NAME = "list_name"
        const val EXTRA_SOURCE = "source"
        const val EXTRA_ENDS_AT = "ends_at"
        const val EXTRA_STRICT = "strict"
        const val EXTRA_DURATION_MS = "duration_ms"
    }
}
