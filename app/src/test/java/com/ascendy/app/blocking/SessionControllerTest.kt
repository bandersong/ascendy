package com.ascendy.app.blocking

import androidx.test.core.app.ApplicationProvider
import com.ascendy.app.data.AscendyRepo
import com.ascendy.app.data.BlockSession
import com.ascendy.app.data.Blocklist
import com.ascendy.app.data.BoundTag
import com.ascendy.app.data.ThemePrefs
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.runner.RunWith
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * State-machine edge cases for [SessionController], driven through the REAL [AscendyRepo] (Room,
 * in-process) and [ThemePrefs] under Robolectric — services/alarms/widgets are shadowed so we can
 * assert the decisions without an emulator. This is where the "trapped in strict" / "manual exit"
 * class of bugs gets caught before it ever ships to a device.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SessionControllerTest {

    private lateinit var repo: AscendyRepo
    private lateinit var prefs: ThemePrefs
    private lateinit var controller: SessionController

    @Before fun setUp() = runTest {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        repo = AscendyRepo(ctx)
        prefs = ThemePrefs(ctx)
        controller = SessionController(ctx, repo, prefs)
        // Clean slate: deactivate any leftover session and clear in-memory state.
        repo.saveSession(BlockSession(id = 1L, active = false, startedAt = 0, listId = 0, tagId = null, emergencyUnlocksLeft = 0))
        BlockState.clear()
        prefs.setMaxSessionMinutes(60)
    }

    private suspend fun newList(strict: Boolean = false, allow: Boolean = false): Long {
        val id = repo.upsertList(Blocklist(name = "t", isStrict = strict, isAllowList = allow))
        return id
    }

    @Test fun unknownTag_returnsUnknown() = runTest {
        val res = controller.handleTagTap("no-such-tag")
        assertTrue(res is TapResult.UnknownTag)
    }

    @Test fun manualStart_neverInheritsStrict_andIsEndable() = runTest {
        // toggleManual on a STRICT default list must NOT produce a strict session.
        val id = newList(strict = true)
        repo.saveSession(repo.currentSession()!!.copy(active = false))
        repo.upsertList(Blocklist(id = id, name = "t", isDefault = true, isStrict = true))

        controller.toggleManual()                       // start
        assertTrue("session active", BlockState.active.value)
        assertFalse("manual session is never strict", BlockState.strict.value)

        val end = controller.toggleManual()             // end
        assertEquals(ManualEndResult.Ended, end)
        assertFalse(BlockState.active.value)
    }

    @Test fun tagBoundStrict_blocksManualExit_butSameTagUnlocks() = runTest {
        val listId = newList(strict = true)
        repo.saveTag(BoundTag(tagId = "tagA", nickname = "A", createdAt = 0, listId = listId))

        val locked = controller.handleTagTap("tagA")
        assertTrue(locked is TapResult.Locked)
        assertTrue("tag-bound strict IS strict", BlockState.strict.value)

        // Manual long-press cannot escape a strict tag session.
        assertEquals(ManualEndResult.BlockedStrict, controller.toggleManual())
        assertTrue("still locked", BlockState.active.value)

        // Re-tapping the bound tag ends it.
        val unlocked = controller.handleTagTap("tagA")
        assertTrue(unlocked is TapResult.Unlocked)
        assertFalse(BlockState.active.value)
    }

    @Test fun wrongTag_doesNotEndActiveSession() = runTest {
        val listId = newList(strict = false)
        repo.saveTag(BoundTag(tagId = "tagA", nickname = "A", createdAt = 0, listId = listId))
        repo.saveTag(BoundTag(tagId = "tagB", nickname = "B", createdAt = 0, listId = listId))

        controller.handleTagTap("tagA")
        val res = controller.handleTagTap("tagB")
        assertTrue(res is TapResult.WrongTag)
        assertTrue("session survives wrong tag", BlockState.active.value)
    }

    @Test fun emergencyUnlock_consumesTheSingleUnlock() = runTest {
        val listId = newList(strict = false)
        controller.startSession(listId, tagId = null, source = SessionSource.Nfc)
        assertTrue(BlockState.emergencyAvailable.value)

        assertTrue("first unlock succeeds", controller.useEmergencyUnlock())
        assertFalse("session ended", BlockState.active.value)
        assertFalse("no session to unlock", controller.useEmergencyUnlock())
    }

    @Test fun safetyTimer_clampsOverlongSessionToMaxDuration() = runTest {
        prefs.setMaxSessionMinutes(60)
        val listId = newList(strict = false)
        val before = System.currentTimeMillis()
        controller.startTimedSession(durationMs = 100L * 60 * 60 * 1000)   // request 100h

        val endsAt = repo.currentSession()!!.endsAt!!
        val cap = before + 60 * 60_000L
        assertTrue("clamped to <= max session window", endsAt <= cap + 5_000)
        assertTrue("but still a real future window", endsAt > before)
    }
}
