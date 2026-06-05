package com.ascendy.app.data

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
            .addMigrations(AscendyDb.MIGRATION_6_7)
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
}
