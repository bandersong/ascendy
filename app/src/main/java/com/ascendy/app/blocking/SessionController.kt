package com.ascendy.app.blocking

import android.content.Context
import android.content.Intent
import android.os.Build
import com.ascendy.app.data.AscendyRepo
import com.ascendy.app.data.BlockSession
import com.ascendy.app.data.ThemePrefs
import com.ascendy.app.service.AlarmScheduler
import com.ascendy.app.service.BlockingForegroundService
import kotlinx.coroutines.flow.first

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

    suspend fun handleTagTap(tagId: String): TapResult {
        val tag = repo.tagById(tagId) ?: return TapResult.UnknownTag(tagId)
        val current = repo.currentSession()

        return if (current?.active == true) {
            if (current.tagId != null && current.tagId != tag.tagId) {
                TapResult.WrongTag(current.tagId)
            } else {
                endSession()
                TapResult.Unlocked
            }
        } else {
            val list = tag.listId?.let { repo.list(it) }
                ?: repo.defaultList()
                ?: repo.ensureDefaultList()
            startSession(list.id, tag.tagId, SessionSource.Nfc)
            TapResult.Locked(list.name)
        }
    }

    suspend fun startSession(
        listId: Long,
        tagId: String?,
        source: SessionSource = SessionSource.Nfc,
        endsAt: Long? = null,
    ) {
        val list = repo.list(listId) ?: repo.ensureDefaultList()
        val packages = repo.packages(list.id).toSet()
        val domains = repo.domains(list.id).toSet()
        val now = System.currentTimeMillis()

        // Safety timer: force every session to have an auto-end. Explicit endsAt (pomodoro,
        // scheduled) wins if shorter; otherwise we use the user's configured max duration.
        val maxMin = themePrefs.maxSessionMinutes.first().coerceIn(60, 24 * 60)
        val safetyEndsAt = now + maxMin * 60_000L
        val effectiveEndsAt = if (endsAt != null) minOf(endsAt, safetyEndsAt) else safetyEndsAt

        val isStrict = list.isStrict
        val unlocksLeft = if (isStrict) 0 else 1

        val session = BlockSession(
            id = 1L,
            active = true,
            startedAt = now,
            listId = list.id,
            tagId = tagId,
            emergencyUnlocksLeft = unlocksLeft,
            endsAt = effectiveEndsAt,
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
        )
        startForegroundService()
        AlarmScheduler.scheduleSessionEnd(context, effectiveEndsAt)
    }

    suspend fun endSession() {
        val current = repo.currentSession() ?: return
        val now = System.currentTimeMillis()
        repo.saveSession(current.copy(active = false))
        repo.latestLog()?.let { latest ->
            if (latest.endedAt == null) repo.finishLog(latest.id, now)
        }
        BlockState.clear()
        stopForegroundService()
        AlarmScheduler.cancelSessionEnd(context)
    }

    suspend fun useEmergencyUnlock(): Boolean {
        val current = repo.currentSession() ?: return false
        if (!current.active) return false
        if (current.emergencyUnlocksLeft <= 0) return false
        repo.saveSession(current.copy(emergencyUnlocksLeft = current.emergencyUnlocksLeft - 1))
        endSession()
        return true
    }

    suspend fun restoreOnBoot() {
        val current = repo.currentSession() ?: return
        if (!current.active) return
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
        )
        startForegroundService()
        current.endsAt?.let { AlarmScheduler.scheduleSessionEnd(context, it) }
    }

    /**
     * Manual long-press toggle. Starts a session if none is active. If an active session is
     * strict, [ManualEndResult.BlockedStrict] is returned and the session is NOT ended — the user
     * must use the bound tag/QR or wait for the safety timer.
     */
    suspend fun toggleManual(): ManualEndResult {
        val current = repo.currentSession()
        return if (current?.active == true) {
            val list = repo.list(current.listId)
            if (list?.isStrict == true) {
                ManualEndResult.BlockedStrict
            } else {
                endSession()
                ManualEndResult.Ended
            }
        } else {
            val list = repo.defaultList() ?: repo.ensureDefaultList()
            startSession(list.id, tagId = null, source = SessionSource.Manual)
            ManualEndResult.NoSession  // semantically "started fresh"
        }
    }

    suspend fun startTimedSession(durationMs: Long, listId: Long? = null) {
        val list = listId?.let { repo.list(it) } ?: repo.defaultList() ?: repo.ensureDefaultList()
        val endsAt = System.currentTimeMillis() + durationMs
        startSession(list.id, tagId = null, source = SessionSource.Pomodoro, endsAt = endsAt)
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
}
