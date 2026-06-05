package com.ascendy.app

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ascendy.app.data.AscendyRepo
import com.ascendy.app.data.Blocklist
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import org.junit.Test

/**
 * Real-device/emulator smoke layer. Proves the app actually builds, launches, and that Room opens
 * and round-trips on each API level in the CI emulator matrix — the things JVM/Robolectric can't
 * vouch for (real SQLite, real resources, real Compose first frame).
 */
@RunWith(AndroidJUnit4::class)
class SmokeTest {

    @Test fun mainActivity_launchesToResumed() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            assertEquals(androidx.lifecycle.Lifecycle.State.RESUMED, scenario.state)
        }
    }

    @Test fun room_opensAndRoundTrips() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val repo = AscendyRepo(ctx)
        val id = repo.upsertList(Blocklist(name = "smoke-list"))
        assertTrue(id > 0)
        val back = repo.list(id)
        assertNotNull(back)
        assertEquals("smoke-list", back!!.name)
    }

    @Test fun targetPackage_isCorrect() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        // foss flavor keeps the base applicationId.
        assertEquals("com.ascendy.app", ctx.packageName)
    }
}
