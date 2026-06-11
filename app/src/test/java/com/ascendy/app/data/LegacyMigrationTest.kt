package com.ascendy.app.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Walks the full 1→7 migration chain against a REAL v1 database file. Schema JSON was only
 * exported from v6 on, so migrations 1→6 were reconstructed from git history — this test is the
 * proof they produce exactly the schema Room expects: after the migrations run, Room validates
 * every table against its @Entity definitions and throws "Migration didn't properly handle: …"
 * on any drift. Data seeded into v1 must also survive to the other side.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LegacyMigrationTest {

    @Test
    fun v1Database_migratesToCurrent_keepingData() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val dbFile = ctx.getDatabasePath("legacy-migration-test.db")
        dbFile.parentFile?.mkdirs()
        dbFile.delete()

        // Hand-built v1 schema, exactly as Room generated it at commit 7487daa (initial scaffold).
        SQLiteDatabase.openOrCreateDatabase(dbFile, null).use { db ->
            db.execSQL(
                "CREATE TABLE `bound_tag` (`tagId` TEXT NOT NULL, `nickname` TEXT NOT NULL, " +
                    "`createdAt` INTEGER NOT NULL, PRIMARY KEY(`tagId`))"
            )
            db.execSQL(
                "CREATE TABLE `blocklist` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT NOT NULL, `isDefault` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)"
            )
            db.execSQL(
                "CREATE TABLE `blocked_package` (`listId` INTEGER NOT NULL, `packageName` TEXT NOT NULL, " +
                    "PRIMARY KEY(`listId`, `packageName`), " +
                    "FOREIGN KEY(`listId`) REFERENCES `blocklist`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
            )
            db.execSQL("CREATE INDEX `index_blocked_package_listId` ON `blocked_package` (`listId`)")
            db.execSQL(
                "CREATE TABLE `block_session` (`id` INTEGER NOT NULL, `active` INTEGER NOT NULL, " +
                    "`startedAt` INTEGER NOT NULL, `listId` INTEGER NOT NULL, `tagId` TEXT, " +
                    "`emergencyUnlocksLeft` INTEGER NOT NULL, PRIMARY KEY(`id`))"
            )

            db.execSQL("INSERT INTO bound_tag (tagId, nickname, createdAt) VALUES ('tag-1', 'desk tag', 111)")
            db.execSQL("INSERT INTO blocklist (name, isDefault, createdAt) VALUES ('focus', 1, 222)")
            db.execSQL("INSERT INTO blocked_package (listId, packageName) VALUES (1, 'com.example.distraction')")
            db.execSQL(
                "INSERT INTO block_session (id, active, startedAt, listId, tagId, emergencyUnlocksLeft) " +
                    "VALUES (1, 0, 333, 1, NULL, 1)"
            )
            db.version = 1
        }

        val room = Room.databaseBuilder(ctx, AscendyDb::class.java, "legacy-migration-test.db")
            .addMigrations(*AscendyDb.ALL_MIGRATIONS)
            .build()
        try {
            runBlocking {
                // First query opens the db → runs the whole migration chain → Room validates the
                // resulting schema. Then prove the v1 rows survived.
                val tag = room.tagDao().byId("tag-1")
                assertNotNull("v1 tag survived migration", tag)
                assertEquals("desk tag", tag!!.nickname)
                assertEquals("new kind column defaulted", "nfc", tag.kind)

                val list = room.blocklistDao().defaultList()
                assertNotNull("v1 default list survived", list)
                assertEquals("focus", list!!.name)

                assertTrue(
                    "v1 blocked package survived",
                    room.blocklistDao().packages(list.id).contains("com.example.distraction")
                )

                val session = room.sessionDao().current()
                assertNotNull("v1 session row survived", session)
                assertEquals("new scheduleId column defaulted to null", null, session!!.scheduleId)

                // Tables created BY the migrations are usable.
                room.scheduleDao().allEnabled()
                room.blocklistDao().domains(list.id)
            }
        } finally {
            room.close()
        }
    }
}
