package com.ascendy.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "bound_tag")
data class BoundTag(
    @PrimaryKey val tagId: String,
    val nickname: String,
    val createdAt: Long
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

@Entity(tableName = "block_session")
data class BlockSession(
    @PrimaryKey val id: Long = 1L,
    val active: Boolean,
    val startedAt: Long,
    val listId: Long,
    val tagId: String?,
    val emergencyUnlocksLeft: Int
)
