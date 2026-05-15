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

    @Query("SELECT * FROM session_log ORDER BY startedAt DESC LIMIT 1")
    suspend fun latest(): SessionLog?

    @Query("SELECT COALESCE(SUM(COALESCE(endedAt, :nowMs) - startedAt), 0) FROM session_log WHERE startedAt >= :sinceMs")
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
