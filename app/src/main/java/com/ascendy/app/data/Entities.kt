package com.ascendy.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "bound_tag")
data class BoundTag(
    @PrimaryKey val tagId: String,
    val nickname: String,
    val createdAt: Long,
    /** Optional override: when set, this tag triggers this list instead of the default. */
    val listId: Long? = null,
)

@Entity(tableName = "blocklist")
data class Blocklist(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "blocked_package",
    primaryKeys = ["listId", "packageName"],
    foreignKeys = [
        ForeignKey(
            entity = Blocklist::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("listId")]
)
data class BlockedPackage(
    val listId: Long,
    val packageName: String
)

@Entity(
    tableName = "blocked_domain",
    primaryKeys = ["listId", "domain"],
    foreignKeys = [
        ForeignKey(
            entity = Blocklist::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("listId")]
)
data class BlockedDomain(
    val listId: Long,
    val domain: String,           // normalized: lowercase, no scheme, no path, no leading "www."
)

@Entity(tableName = "block_session")
data class BlockSession(
    @PrimaryKey val id: Long = 1L,
    val active: Boolean,
    val startedAt: Long,
    val listId: Long,
    val tagId: String?,
    val emergencyUnlocksLeft: Int,
    /** Auto-end timestamp for pomodoro / scheduled sessions. null = open-ended (NFC-bound). */
    val endsAt: Long? = null,
)

@Entity(tableName = "session_log")
data class SessionLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val listId: Long,
    val startedAt: Long,
    val endedAt: Long?,
    val source: String,           // "nfc", "manual", "pomodoro", "scheduled"
)

@Entity(tableName = "schedule")
data class Schedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val listId: Long,
    /** Bitmask: bit 0=Sun, 1=Mon, …, 6=Sat. */
    val daysOfWeek: Int,
    val startMinuteOfDay: Int,     // 0..1439
    val endMinuteOfDay: Int,       // 0..1439, must be > start for same-day schedules
    val enabled: Boolean = true,
    val nickname: String = "",
)
