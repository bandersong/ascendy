package com.ascendy.app.blocking

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.net.VpnService
import android.util.Log
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
    // Clock seams — injected so the monotonic safety-timer logic (clock-jump resistance, reboot
    // reconciliation) is unit-testable without an emulator. Production uses the real clocks.
    private val wallClock: () -> Long = { System.currentTimeMillis() },
    private val elapsedClock: () -> Long = { SystemClock.elapsedRealtime() },
    // Reboot identity (Settings.Global.BOOT_COUNT). Increments once per boot; lets restore tell a
    // same-boot clock jump apart from a real reboot. -1 when the device doesn't expose it.
    private val bootCount: () -> Long = {
        try {
            android.provider.Settings.Global.getInt(
                context.contentResolver, android.provider.Settings.Global.BOOT_COUNT, -1
            ).toLong()
        } catch (_: Exception) { -1L }
    },
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
        val now = wallClock()
        val nowElapsed = elapsedClock()

        // Safety timer: force every session to have an auto-end. Explicit endsAt (pomodoro,
        // scheduled) wins if shorter; otherwise we use the user's configured max duration.
        val safetyEndsAt = now + maxSessionMs()
        val effectiveEndsAt = if (endsAt != null) minOf(endsAt, safetyEndsAt) else safetyEndsAt
        // The session's intended real duration, captured now so a later wall-clock change can't
        // alter it. The END is enforced against the MONOTONIC deadline below, not endsAt.
        val durationMs = (effectiveEndsAt - now).coerceAtLeast(0)
        val endsElapsed = nowElapsed + durationMs

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
            startedAtElapsed = nowElapsed,
            startedAtBootCount = bootCount(),
            scheduleId = scheduleId,
        )
        repo.saveSession(session)
        repo.startLog(list.id, now, source.tag)
        BlockState.set(
            active = true,
            blocked = packages,
            blockedDomains = domains,
            startedAt = now,
            endsAtElapsed = endsElapsed,
            emergencyAvailable = !isStrict && unlocksLeft > 0,
            strict = isStrict,
            inverted = list.isAllowList,
            lockdown = lockdown,
        )
        startForegroundService()
        // Cancel any orphaned END alarm before arming this session's (defensive — the request code
        // is shared, so a stale one from a crash-skipped end could otherwise linger). The trigger is
        // a MONOTONIC instant: immune to system-clock changes.
        AlarmScheduler.cancelSessionEnd(context)
        AlarmScheduler.scheduleSessionEnd(context, endsElapsed, now)
        AlarmScheduler.scheduleHeartbeat(context)
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
        // `now` can be BELOW startedAt if the wall clock was wound backward since the session began.
        // Clamp the ceiling up to startedAt so coerceIn never gets inverted bounds (min > max), which
        // would throw IllegalArgumentException and abort the end — the most dangerous failure here,
        // since this is the funnel for EVERY auto-end path (END alarm, heartbeat, FGS guarantor). A
        // throw would leave the session active and trap the user. This makes ending exception-free.
        val now = maxOf(wallClock(), current.startedAt)
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
        AlarmScheduler.cancelHeartbeat(context)
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
        val now = wallClock()
        val activeStartedAt = repo.currentSession()?.takeIf { it.active }?.startedAt ?: -1L
        repo.closeStaleOpenLogs(now, maxSessionMs(), exceptStartedAt = activeStartedAt)
    }

    /**
     * Re-assert enforcement from the persisted session row. Idempotent and safe to call from any
     * entry point (app start, boot, the self-heal heartbeat, an accessibility re-bind). It either
     * (a) ends a session whose window has passed, (b) clears in-memory state that outlived its DB
     * row, or (c) rehydrates the active session — re-anchoring the MONOTONIC safety timer and
     * re-arming the (elapsed-realtime) END alarm + heartbeat. It can never EXTEND a session past
     * its safety cap, so a watchdog routed through here can't trap a user.
     */
    suspend fun restoreOnBoot(): Unit = transitionMutex.withLock {
        val current = repo.currentSession()?.takeIf { it.active }
        if (current == null) {
            // NH-01: an in-memory BlockState that outlived its DB session (partial end / DB ended
            // out-of-band) would keep enforcing with no backing row and no alarm. Tear it down so
            // in-memory state can never outlive the persisted session in EITHER direction.
            if (BlockState.isActive()) {
                BlockState.clear()
                stopForegroundService()
                stopVpnService()
                AlarmScheduler.cancelSessionEnd(context)
                AlarmScheduler.cancelHeartbeat(context)
            }
            return
        }
        val now = wallClock()
        val nowElapsed = elapsedClock()
        val cap = maxSessionMs()

        // Re-derive the auto-end instant. Schedule-sourced sessions rely on a daily END alarm
        // that does NOT survive a reboot, so recompute this window's end from the schedule.
        val schedule = current.scheduleId?.let { repo.scheduleById(it) }
        val windowEndMs = schedule?.let {
            val windowMin = (it.endMinuteOfDay - it.startMinuteOfDay).mod(1440)
                .let { m -> if (m == 0) 1440 else m }
            current.startedAt + windowMin * 60_000L
        }
        // AF-04: every restored session MUST end up with a finite armed end. A pre-safety-timer
        // legacy row can have endsAt == null AND scheduleId == null; without this fallback the
        // expiry check and the re-arm below are both skipped, leaving an unbounded block with no
        // safety timer. Anchor the fallback on startedAt (not now) so a long-stale row dies
        // immediately at the expiry check rather than getting a fresh full window.
        val endsAtWall = listOfNotNull(current.endsAt, windowEndMs).minOrNull()
            ?: (current.startedAt + cap)

        // AF-02: decide remaining time on the MONOTONIC clock when we can. Crucially there is NO
        // early wall-clock "now >= endsAtWall → end" check here: the heartbeat calls this every ~90s,
        // and a wall check would let a forward system-clock jump end a strict session early via the
        // heartbeat. The intended real duration is endsAtWall-startedAt (both wall, captured
        // consistently → immune to a later clock change); the time we enforce is monotonic.
        val intendedDurationMs = (endsAtWall - current.startedAt).coerceIn(0, cap)
        val anchor = current.startedAtElapsed
        // Same boot iff we have an anchor, uptime hasn't gone backwards, AND the boot identity still
        // matches. The boot-count check is what stops a reboot-soon-after-start (small anchor, uptime
        // already climbed past it) from being mistaken for the same boot and over-crediting the
        // window. When BOOT_COUNT is unavailable (-1 on both sides) we fall back to the elapsed check.
        val storedBoot = current.startedAtBootCount
        val nowBoot = bootCount()
        val sameBoot = anchor != null && nowElapsed >= anchor &&
            (storedBoot == null || storedBoot == nowBoot)
        val remainingMs: Long = if (sameBoot) {
            // Trust the monotonic clock outright — a wall-clock jump since start changes nothing here.
            (intendedDurationMs - (nowElapsed - anchor!!)).coerceIn(0, cap)
        } else {
            // Reboot detected (elapsed reset / boot count changed) or a legacy row with no anchor: we
            // can't carry monotonic time across a reboot, so fall back to the wall window, clamped to
            // the cap as a hard ceiling so a backward clock change while powered off can't over-extend.
            (endsAtWall - now).coerceIn(0, cap)
        }
        // If the window has genuinely elapsed (monotonically, or by the wall fallback on reboot),
        // end now and credit focus only up to the intended end — never the powered-off / jumped gap.
        if (remainingMs <= 0) {
            endSessionLocked(logEndMs = minOf(endsAtWall, now))
            return
        }
        // Re-anchor so the persisted/in-memory monotonic deadline are consistent for this boot:
        // endsElapsed == newAnchor + intendedDuration, and remaining == endsElapsed - nowElapsed.
        // Also stamp the current boot count so subsequent restores in this boot read as same-boot.
        val endsElapsed = nowElapsed + remainingMs
        val newAnchor = endsElapsed - intendedDurationMs
        if (newAnchor != anchor || storedBoot != nowBoot) {
            repo.saveSession(current.copy(startedAtElapsed = newAnchor, startedAtBootCount = nowBoot))
        }

        val list = repo.list(current.listId)
        val packages = repo.packages(current.listId).toSet()
        val domains = repo.domains(current.listId).toSet()
        BlockState.set(
            active = true,
            blocked = packages,
            blockedDomains = domains,
            startedAt = current.startedAt,
            endsAtElapsed = endsElapsed,
            emergencyAvailable = list?.isStrict != true && current.emergencyUnlocksLeft > 0,
            strict = list?.isStrict == true,
            inverted = list?.isAllowList == true,
            lockdown = themePrefs.lockdownEnabled.first(),
        )
        startForegroundService()
        AlarmScheduler.cancelSessionEnd(context)
        AlarmScheduler.scheduleSessionEnd(context, endsElapsed, current.startedAt)
        AlarmScheduler.scheduleHeartbeat(context)
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
        val endsAt = wallClock() + durationMs
        startSessionLocked(list.id, tagId = null, source = SessionSource.Pomodoro, endsAt = endsAt)
    }

    // AF-12: a background startForegroundService() can throw ForegroundServiceStartNotAllowedException
    // (Android 12+) or IllegalStateException from the AscendyApp process-recreation path. Swallow it so
    // the coroutine that already marked the session active in the DB doesn't die mid-start — the END
    // alarm and the independently-enabled accessibility service still carry enforcement, and the
    // heartbeat will retry the FGS start on its next tick.
    private fun startForegroundService() {
        val intent = Intent(context, BlockingForegroundService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            Log.w(TAG, "startForegroundService blocked: ${e.message}")
        }
    }

    private fun stopForegroundService() {
        try {
            context.stopService(Intent(context, BlockingForegroundService::class.java))
        } catch (_: Exception) {}
    }

    private fun startVpnService() {
        val intent = Intent(context, AscendyVpnService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            Log.w(TAG, "startVpnService blocked: ${e.message}")
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

        private const val TAG = "AscendySession"
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
