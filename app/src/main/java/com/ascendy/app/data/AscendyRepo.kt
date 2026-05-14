package com.ascendy.app.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class AscendyRepo(context: Context) {
    private val db = AscendyDb.get(context)
    private val tags = db.tagDao()
    private val lists = db.blocklistDao()
    private val sessions = db.sessionDao()

    fun observeTags(): Flow<List<BoundTag>> = tags.observeAll()
    suspend fun allTags(): List<BoundTag> = tags.all()
    suspend fun tagById(id: String): BoundTag? = tags.byId(id)
    suspend fun saveTag(t: BoundTag) = tags.upsert(t)
    suspend fun deleteTag(t: BoundTag) = tags.delete(t)

    fun observeLists(): Flow<List<Blocklist>> = lists.observeAll()
    suspend fun list(id: Long): Blocklist? = lists.byId(id)
    suspend fun defaultList(): Blocklist? = lists.defaultList()
    suspend fun upsertList(l: Blocklist): Long = lists.insert(l)
    suspend fun deleteList(id: Long) = lists.delete(id)

    fun observePackages(listId: Long): Flow<List<String>> = lists.observePackages(listId)
    suspend fun packages(listId: Long): List<String> = lists.packages(listId)
    suspend fun addPackage(listId: Long, pkg: String) = lists.addPackage(BlockedPackage(listId, pkg))
    suspend fun removePackage(listId: Long, pkg: String) = lists.removePackage(listId, pkg)

    fun observeSession(): Flow<BlockSession?> = sessions.observe()
    suspend fun currentSession(): BlockSession? = sessions.current()
    suspend fun saveSession(s: BlockSession) = sessions.upsert(s)

    suspend fun ensureDefaultList(): Blocklist {
        defaultList()?.let { return it }
        val id = upsertList(Blocklist(name = "focus", isDefault = true))
        return list(id)!!
    }
}
