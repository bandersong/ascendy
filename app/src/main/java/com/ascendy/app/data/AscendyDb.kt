package com.ascendy.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [BoundTag::class, Blocklist::class, BlockedPackage::class, BlockSession::class],
    version = 1,
    exportSchema = false
)
abstract class AscendyDb : RoomDatabase() {
    abstract fun tagDao(): TagDao
    abstract fun blocklistDao(): BlocklistDao
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile private var INSTANCE: AscendyDb? = null

        fun get(context: Context): AscendyDb {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AscendyDb::class.java,
                    "ascendy.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
