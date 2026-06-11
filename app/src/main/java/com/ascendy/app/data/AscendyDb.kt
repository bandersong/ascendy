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
         *      schema (rare — e.g. a code-only default change), the migrate() body may stay
         *      empty. But an EMPTY body when the schema actually changed will make Room throw
         *      on open (schema-hash mismatch) — that loud failure in testing is the point; it
         *      can never silently wipe a real user.
         *   3. Add it to ALL_MIGRATIONS below (both the app and LegacyMigrationTest use it).
         *
         * fallbackToDestructiveMigrationOnDowngrade() only fires when the on-disk version is
         * NEWER than the app (a downgrade) — which never happens through a normal Play Store /
         * GitHub-release update — so it can't cause data loss on the upgrade path.
         *
         * Migrations 1→6 were reconstructed from the git history of Entities.kt (schema JSON was
         * only exported from v6 on); LegacyMigrationTest builds a real v1 database and walks the
         * whole chain to prove they produce exactly the schema Room expects.
         */

        /** v2 (stats + schedules): per-tag list bindings, session auto-end, log + schedule tables. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `bound_tag` ADD COLUMN `listId` INTEGER")
                db.execSQL("ALTER TABLE `block_session` ADD COLUMN `endsAt` INTEGER")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `session_log` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `listId` INTEGER NOT NULL, " +
                        "`startedAt` INTEGER NOT NULL, `endedAt` INTEGER, `source` TEXT NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `schedule` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `listId` INTEGER NOT NULL, " +
                        "`daysOfWeek` INTEGER NOT NULL, `startMinuteOfDay` INTEGER NOT NULL, " +
                        "`endMinuteOfDay` INTEGER NOT NULL, `enabled` INTEGER NOT NULL, " +
                        "`nickname` TEXT NOT NULL)"
                )
            }
        }

        /** v3 (website blocking): blocked_domain table. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `blocked_domain` (" +
                        "`listId` INTEGER NOT NULL, `domain` TEXT NOT NULL, " +
                        "PRIMARY KEY(`listId`, `domain`), " +
                        "FOREIGN KEY(`listId`) REFERENCES `blocklist`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE )"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_blocked_domain_listId` ON `blocked_domain` (`listId`)")
            }
        }

        /** v4 (QR anchors): BoundTag.kind distinguishes "nfc" from "qr". */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `bound_tag` ADD COLUMN `kind` TEXT NOT NULL DEFAULT 'nfc'")
            }
        }

        /** v5 (ascend mode): Blocklist.isStrict. */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `blocklist` ADD COLUMN `isStrict` INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** v6 (whitelist mode): Blocklist.isAllowList. */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `blocklist` ADD COLUMN `isAllowList` INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** v7: BlockSession.scheduleId so a schedule's END alarm only ends the session it started. */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE block_session ADD COLUMN scheduleId INTEGER")
            }
        }

        val ALL_MIGRATIONS = arrayOf(
            MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4,
            MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7,
        )

        fun get(context: Context): AscendyDb {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AscendyDb::class.java,
                    "ascendy.db"
                )
                    .addMigrations(*ALL_MIGRATIONS)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
