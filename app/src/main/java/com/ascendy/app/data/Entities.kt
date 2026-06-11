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
    /** "nfc" or "qr" — determines the input modality. */
    val kind: String = "nfc",
)

@Entity(tableName = "blocklist")
data class Blocklist(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    /** Strict lists: no emergency override, no manual-end. Only tag/QR scan or safety timer. */
    val isStrict: Boolean = false,
    /**
     * Allow-list mode: invert the semantics. When true, the listed apps/domains are the
     * ONLY ones allowed during a session. Everything else gets bounced. Great for study-only
     * setups where you whitelist your textbook + class apps and block the rest of the phone.
     */
    val isAllowList: Boolean = false,
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
    /**
     * SystemClock.elapsedRealtime() captured when this session's monotonic anchor was set (at
     * start, or re-anchored on boot). The safety timer is gated against this MONOTONIC clock, not
     * the wall clock — so winding Settings → Date & time forward can't fire the END early and
     * shorten a strict session. null on rows written before the anchor existed; elapsedRealtime
     * resets to ~0 on reboot, so restoreOnBoot detects a reboot (current elapsed < stored) and
     * re-anchors against the wall-clock window. See SessionController.
     */
    val startedAtElapsed: Long? = null,
    /**
     * Settings.Global.BOOT_COUNT at the time [startedAtElapsed] was set. The definitive reboot
     * signal: a magnitude check on elapsedRealtime alone misfires when a session starts moments
     * after boot (small anchor) and the device reboots (uptime climbs back past the small anchor),
     * which would over-credit the monotonic window. Comparing the boot count instead is exact.
     * null on legacy rows / devices that don't expose it (falls back to the elapsed magnitude check).
     */
    val startedAtBootCount: Long? = null,
    /**
     * The [Schedule] that started this session, if any. null for manual/NFC/QR/pomodoro sessions.
     * A schedule's END alarm only ends the session whose scheduleId matches it, so a manual session
     * the user happened to start on the same list is never killed out from under them.
     */
    val scheduleId: Long? = null,
    /**
     * Row id of THIS session's open [SessionLog]. Closing the log by id (not by startedAt) means a
     * crash-orphaned open log that happens to share the same startedAt millisecond can never be
     * mis-closed as — or mistaken for — this session's log. null on pre-v9 rows; those fall back
     * to resolving the newest open log with a matching startedAt. Stats integrity only.
     */
    val openLogId: Long? = null,
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
