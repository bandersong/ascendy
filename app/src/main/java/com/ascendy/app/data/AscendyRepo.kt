package com.ascendy.app.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class AscendyRepo(context: Context) {
    private val db = AscendyDb.get(context)
    private val tags = db.tagDao()
    private val lists = db.blocklistDao()
    private val sessions = db.sessionDao()
    private val logs = db.sessionLogDao()
    private val schedules = db.scheduleDao()

    fun observeTags(): Flow<List<BoundTag>> = tags.observeAll()
    suspend fun allTags(): List<BoundTag> = tags.all()
    suspend fun tagById(id: String): BoundTag? = tags.byId(id)
    suspend fun saveTag(t: BoundTag) = tags.upsert(t)
    suspend fun deleteTag(t: BoundTag) = tags.delete(t)

    fun observeLists(): Flow<List<Blocklist>> = lists.observeAll()
    suspend fun list(id: Long): Blocklist? = lists.byId(id)
    suspend fun defaultList(): Blocklist? = lists.defaultList()
    suspend fun upsertList(l: Blocklist): Long = lists.insert(l)
    suspend fun updateStrict(id: Long, on: Boolean) = lists.updateStrict(id, on)
    suspend fun updateAllowList(id: Long, on: Boolean) = lists.updateAllowList(id, on)
    suspend fun deleteList(id: Long) = lists.delete(id)

    fun observePackages(listId: Long): Flow<List<String>> = lists.observePackages(listId)
    suspend fun packages(listId: Long): List<String> = lists.packages(listId)
    suspend fun addPackage(listId: Long, pkg: String) = lists.addPackage(BlockedPackage(listId, pkg))
    suspend fun removePackage(listId: Long, pkg: String) = lists.removePackage(listId, pkg)

    fun observeDomains(listId: Long): Flow<List<String>> = lists.observeDomains(listId)
    suspend fun domains(listId: Long): List<String> = lists.domains(listId)
    suspend fun addDomain(listId: Long, domain: String) =
        lists.addDomain(BlockedDomain(listId, normalizeDomain(domain)))
    suspend fun removeDomain(listId: Long, domain: String) = lists.removeDomain(listId, domain)

    private fun normalizeDomain(raw: String): String = Domains.normalize(raw)

    fun observeSession(): Flow<BlockSession?> = sessions.observe()
    suspend fun currentSession(): BlockSession? = sessions.current()
    suspend fun saveSession(s: BlockSession) = sessions.upsert(s)

    suspend fun ensureDefaultList(): Blocklist {
        defaultList()?.let { return it }
        val id = upsertList(Blocklist(name = "focus", isDefault = true))
        return list(id)!!
    }

    // ── session log (stats) ──
    suspend fun startLog(listId: Long, startedAt: Long, source: String): Long =
        logs.insert(SessionLog(listId = listId, startedAt = startedAt, endedAt = null, source = source))

    suspend fun finishLog(logId: Long, endedAt: Long) = logs.finishLog(logId, endedAt)
    suspend fun openLogIdFor(startedAt: Long): Long? = logs.openLogIdFor(startedAt)
    suspend fun closeStaleOpenLogs(nowMs: Long, maxMs: Long, exceptLogId: Long = -1L) =
        logs.closeStaleOpenLogs(nowMs, maxMs, exceptLogId)
    suspend fun latestLog(): SessionLog? = logs.latest()
    fun observeFocusMsSince(sinceMs: Long, nowMs: Long): kotlinx.coroutines.flow.Flow<Long> =
        logs.observeFocusMsSince(sinceMs, nowMs)
    fun observeLogsSince(sinceMs: Long): kotlinx.coroutines.flow.Flow<List<SessionLog>> =
        logs.observeSince(sinceMs)
    suspend fun distinctSessionDates(): List<String> = logs.distinctDates()

    // ── schedules ──
    fun observeSchedules(): kotlinx.coroutines.flow.Flow<List<Schedule>> = schedules.observeAll()
    suspend fun allEnabledSchedules(): List<Schedule> = schedules.allEnabled()
    suspend fun upsertSchedule(s: Schedule): Long = schedules.upsert(s)
    suspend fun deleteSchedule(id: Long) = schedules.delete(id)
    suspend fun scheduleById(id: Long): Schedule? = schedules.byId(id)
}
