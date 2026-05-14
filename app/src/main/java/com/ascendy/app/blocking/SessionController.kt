package com.ascendy.app.blocking

import android.content.Context
import android.content.Intent
import android.os.Build
import com.ascendy.app.data.AscendyRepo
import com.ascendy.app.data.BlockSession
import com.ascendy.app.service.BlockingForegroundService

sealed class TapResult {
    data class Locked(val listName: String) : TapResult()
    object Unlocked : TapResult()
    data class UnknownTag(val tagId: String) : TapResult()
    data class WrongTag(val expected: String) : TapResult()
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
            val list = repo.defaultList() ?: repo.ensureDefaultList()
            startSession(list.id, tag.tagId)
            TapResult.Locked(list.name)
        }
    }

    suspend fun startSession(listId: Long, tagId: String?) {
        val packages = repo.packages(listId).toSet()
        val session = BlockSession(
            id = 1L,
            active = true,
            startedAt = System.currentTimeMillis(),
            listId = listId,
            tagId = tagId,
            emergencyUnlocksLeft = 1
        )
        repo.saveSession(session)
        BlockState.set(active = true, blocked = packages)
        startForegroundService()
    }

    suspend fun endSession() {
        val current = repo.currentSession() ?: return
        repo.saveSession(current.copy(active = false))
        BlockState.clear()
        stopForegroundService()
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
        BlockState.set(active = true, blocked = packages)
        startForegroundService()
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
