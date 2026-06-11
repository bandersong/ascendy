package com.ascendy.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM bound_tag ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<BoundTag>>

    @Query("SELECT * FROM bound_tag")
    suspend fun all(): List<BoundTag>

    @Query("SELECT * FROM bound_tag WHERE tagId = :id LIMIT 1")
    suspend fun byId(id: String): BoundTag?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(tag: BoundTag)

    @Delete
    suspend fun delete(tag: BoundTag)
}

@Dao
interface BlocklistDao {
    @Query("SELECT * FROM blocklist ORDER BY isDefault DESC, createdAt ASC")
    fun observeAll(): Flow<List<Blocklist>>

    @Query("SELECT * FROM blocklist WHERE id = :id LIMIT 1")
    suspend fun byId(id: Long): Blocklist?

    @Query("SELECT * FROM blocklist WHERE isDefault = 1 LIMIT 1")
    suspend fun defaultList(): Blocklist?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: Blocklist): Long

    @Query("UPDATE blocklist SET isStrict = :on WHERE id = :id")
    suspend fun updateStrict(id: Long, on: Boolean)

    @Query("UPDATE blocklist SET isAllowList = :on WHERE id = :id")
    suspend fun updateAllowList(id: Long, on: Boolean)

    @Query("DELETE FROM blocklist WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT packageName FROM blocked_package WHERE listId = :listId")
    fun observePackages(listId: Long): Flow<List<String>>

    @Query("SELECT packageName FROM blocked_package WHERE listId = :listId")
    suspend fun packages(listId: Long): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addPackage(pkg: BlockedPackage)

    @Query("DELETE FROM blocked_package WHERE listId = :listId AND packageName = :pkg")
    suspend fun removePackage(listId: Long, pkg: String)

    // ── domains ──
    @Query("SELECT domain FROM blocked_domain WHERE listId = :listId ORDER BY domain ASC")
    fun observeDomains(listId: Long): Flow<List<String>>

    @Query("SELECT domain FROM blocked_domain WHERE listId = :listId")
    suspend fun domains(listId: Long): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addDomain(d: BlockedDomain)

    @Query("DELETE FROM blocked_domain WHERE listId = :listId AND domain = :d")
    suspend fun removeDomain(listId: Long, d: String)
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM block_session WHERE id = 1 LIMIT 1")
    fun observe(): Flow<BlockSession?>

    @Query("SELECT * FROM block_session WHERE id = 1 LIMIT 1")
    suspend fun current(): BlockSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(s: BlockSession)
}

@Dao
interface SessionLogDao {
    @Insert
    suspend fun insert(log: SessionLog): Long

    @Query("UPDATE session_log SET endedAt = :endedAt WHERE id = :id")
    suspend fun finishLog(id: Long, endedAt: Long)

    @Query("UPDATE session_log SET endedAt = :endedAt WHERE endedAt IS NULL AND startedAt = :startedAt")
    suspend fun finishOpenLogStartedAt(startedAt: Long, endedAt: Long)

    /**
     * Close every dangling open log, crediting each with at most [maxMs] of focus time.
     * Closing orphans at "now" instead would retroactively count days of crash-orphaned
     * time as focus — the source of absurd 100h+ "best day" stats.
     */
    @Query("UPDATE session_log SET endedAt = MIN(:nowMs, startedAt + :maxMs) WHERE endedAt IS NULL AND startedAt != :exceptStartedAt")
    suspend fun closeStaleOpenLogs(nowMs: Long, maxMs: Long, exceptStartedAt: Long)

    @Query("SELECT * FROM session_log ORDER BY startedAt DESC LIMIT 1")
    suspend fun latest(): SessionLog?

    /**
     * Total focus ms overlapping the window [sinceMs, nowMs]. Overlap attribution (not
     * started-in-window) so a session crossing midnight credits each day its own slice —
     * matching how the stats screen's week chart buckets days.
     */
    @Query(
        "SELECT COALESCE(SUM(MAX(0, MIN(COALESCE(endedAt, :nowMs), :nowMs) - MAX(startedAt, :sinceMs))), 0) " +
        "FROM session_log WHERE COALESCE(endedAt, :nowMs) > :sinceMs"
    )
    fun observeFocusMsSince(sinceMs: Long, nowMs: Long): Flow<Long>

    @Query("SELECT * FROM session_log WHERE startedAt >= :sinceMs ORDER BY startedAt DESC")
    fun observeSince(sinceMs: Long): Flow<List<SessionLog>>

    @Query("SELECT DISTINCT date(startedAt / 1000, 'unixepoch', 'localtime') FROM session_log ORDER BY 1 DESC")
    suspend fun distinctDates(): List<String>
}

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule ORDER BY startMinuteOfDay ASC")
    fun observeAll(): Flow<List<Schedule>>

    @Query("SELECT * FROM schedule WHERE enabled = 1")
    suspend fun allEnabled(): List<Schedule>

    @Query("SELECT * FROM schedule WHERE id = :id")
    suspend fun byId(id: Long): Schedule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(s: Schedule): Long

    @Query("DELETE FROM schedule WHERE id = :id")
    suspend fun delete(id: Long)
}
