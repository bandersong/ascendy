package com.ascendy.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        BoundTag::class, Blocklist::class, BlockedPackage::class, BlockedDomain::class,
        BlockSession::class, SessionLog::class, Schedule::class,
    ],
    version = 7,
    exportSchema = true   // schemas/ JSON feeds MigrationTest; commit the generated file on bump
)
abstract class AscendyDb : RoomDatabase() {
    abstract fun tagDao(): TagDao
    abstract fun blocklistDao(): BlocklistDao
    abstract fun sessionDao(): SessionDao
    abstract fun sessionLogDao(): SessionLogDao
    abstract fun scheduleDao(): ScheduleDao

    companion object {
        @Volatile private var INSTANCE: AscendyDb? = null

        /**
         * ── HOW TO CHANGE THE SCHEMA (read before bumping `version` above) ──
         *
         * We do NOT use fallbackToDestructiveMigration() any more: it silently DROPS every table
         * (tags, lists, schedules, streak history) whenever no migration is found for a version
         * bump. That is a data-loss landmine. Instead:
         *
         *   1. Bump `version` in @Database.
         *   2. Add a `MIGRATION_<old>_<new>` object below with the exact ALTER TABLE / CREATE
         *      statements for the schema delta. If the bump genuinely changes nothing in the
         *      schema (rare — e.g. a code-only default change), the migrate() body may stay empty
         *      like MIGRATION_6_7. But an EMPTY body when the schema actually changed will make
         *      Room throw on open (schema-hash mismatch) — that loud failure in testing is the
         *      point; it can never silently wipe a real user.
         *   3. Register it in the .addMigrations(...) chain below.
         *
         * fallbackToDestructiveMigrationOnDowngrade() only fires when the on-disk version is
         * NEWER than the app (a downgrade) — which never happens through a normal Play Store /
         * GitHub-release update — so it can't cause data loss on the upgrade path.
         *
         * MIGRATION_6_7 is pre-wired scaffolding: version is still 6, so it does not run yet. It
         * exists as the worked example for the next person. If version 7 introduces real schema
         * changes, replace its empty body with the matching SQL.
         */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // v7 adds BlockSession.scheduleId (nullable) so a schedule's END alarm only ends
                // the session it actually started, not any session that shares the list.
                db.execSQL("ALTER TABLE block_session ADD COLUMN scheduleId INTEGER")
            }
        }

        fun get(context: Context): AscendyDb {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AscendyDb::class.java,
                    "ascendy.db"
                )
                    .addMigrations(MIGRATION_6_7)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
