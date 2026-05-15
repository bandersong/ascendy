package com.ascendy.app.blocking

import android.content.Context
import android.content.Intent
import android.os.Build
import com.ascendy.app.data.AscendyRepo
import com.ascendy.app.data.BlockSession
import com.ascendy.app.service.AlarmScheduler
import com.ascendy.app.service.BlockingForegroundService

sealed class TapResult {
    data class Locked(val listName: String) : TapResult()
    object Unlocked : TapResult()
    data class UnknownTag(val tagId: String) : TapResult()
    data class WrongTag(val expected: String) : TapResult()
}

enum class SessionSource(val tag: String) {
    Nfc("nfc"), Manual("manual"), Pomodoro("pomodoro"), Scheduled("scheduled");
}

class SessionController(private val context: Context, private val repo: AscendyRepo) {

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
            // Per-tag binding: if the tag has a listId, use it; else fall back to default
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
        val packages = repo.packages(listId).toSet()
        val now = System.currentTimeMillis()
        val session = BlockSession(
            id = 1L,
            active = true,
            startedAt = now,
            listId = listId,
            tagId = tagId,
            emergencyUnlocksLeft = 1,
            endsAt = endsAt,
        )
        repo.saveSession(session)
        repo.startLog(listId, now, source.tag)
        BlockState.set(active = true, blocked = packages, startedAt = now)
        startForegroundService()

        // Pomodoro / scheduled auto-end alarm
        if (endsAt != null) {
            AlarmScheduler.scheduleSessionEnd(context, endsAt)
        }
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
        val packages = repo.packages(current.listId).toSet()
        BlockState.set(active = true, blocked = packages, startedAt = current.startedAt)
        startForegroundService()
        current.endsAt?.let { AlarmScheduler.scheduleSessionEnd(context, it) }
    }

    /** Manual toggle — no tag involved. Used by the long-press shortcut. */
    suspend fun toggleManual() {
        val current = repo.currentSession()
        if (current?.active == true) {
            endSession()
        } else {
            val list = repo.defaultList() ?: repo.ensureDefaultList()
            startSession(list.id, tagId = null, source = SessionSource.Manual)
        }
    }

    /** Pomodoro / quick-lock: start a session that auto-ends after [durationMs]. */
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
