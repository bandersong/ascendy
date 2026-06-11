package com.ascendy.app.data

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.Test

/**
 * Instrumented migration safety net. Builds a real on-disk v6 database from the exported schema,
 * writes a row, then reopens it through the SAME Room configuration the app ships with — proving
 * the documented "no silent wipe on open" guarantee actually holds on a device. When the schema
 * is bumped to v7, add a migrate-and-validate case here for the 6→7 path.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val TEST_DB = "migration-test.db"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AscendyDb::class.java,
    )

    @Test fun openingExistingV6Database_preservesData() {
        // Create the v6 schema and insert a row through raw SQL.
        helper.createDatabase(TEST_DB, 6).apply {
            execSQL(
                "INSERT INTO blocklist (name, isDefault, createdAt, isStrict, isAllowList) " +
                    "VALUES ('keep-me', 0, 0, 0, 0)"
            )
            close()
        }

        // Reopen via the production builder (same migrations + downgrade fallback). Must not wipe.
        val db = Room.databaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AscendyDb::class.java,
            TEST_DB,
        )
            .addMigrations(AscendyDb.MIGRATION_6_7, AscendyDb.MIGRATION_7_8)
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()

        try {
            val row = runBlocking { db.blocklistDao().byId(1) }
            assertNotNull("row survived reopen", row)
            assertEquals("keep-me", row!!.name)
        } finally {
            db.close()
        }
    }

    @Test fun migrate6To7_addsNullableScheduleId_preservingData() {
        // Seed a v6 block_session (no scheduleId column yet).
        helper.createDatabase(TEST_DB, 6).apply {
            execSQL(
                "INSERT INTO block_session (id, active, startedAt, listId, tagId, emergencyUnlocksLeft, endsAt) " +
                    "VALUES (1, 1, 1000, 5, NULL, 1, NULL)"
            )
            close()
        }

        // Runs MIGRATION_6_7 and validates the result matches the exported v7 schema exactly —
        // a wrong ALTER (type/nullability mismatch) fails right here.
        val db = helper.runMigrationsAndValidate(TEST_DB, 7, true, AscendyDb.MIGRATION_6_7)

        db.query("SELECT listId, scheduleId FROM block_session WHERE id = 1").use { c ->
            assertTrue("seeded row survived migration", c.moveToFirst())
            assertEquals("existing data preserved", 5L, c.getLong(0))
            assertTrue("new scheduleId is null for pre-existing rows", c.isNull(1))
        }
        db.close()
    }

    @Test fun migrate7To8_addsNullableMonotonicColumns_preservingData() {
        // Seed a v7 block_session (no startedAtElapsed / startedAtBootCount columns yet).
        helper.createDatabase(TEST_DB, 7).apply {
            execSQL(
                "INSERT INTO block_session (id, active, startedAt, listId, tagId, emergencyUnlocksLeft, endsAt, scheduleId) " +
                    "VALUES (1, 1, 1000, 5, NULL, 1, 9000, NULL)"
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 8, true, AscendyDb.MIGRATION_7_8)

        db.query("SELECT endsAt, startedAtElapsed, startedAtBootCount FROM block_session WHERE id = 1").use { c ->
            assertTrue("seeded row survived migration", c.moveToFirst())
            assertEquals("existing data preserved", 9000L, c.getLong(0))
            assertTrue("new startedAtElapsed is null for pre-existing rows", c.isNull(1))
            assertTrue("new startedAtBootCount is null for pre-existing rows", c.isNull(2))
        }
        db.close()
    }
}
