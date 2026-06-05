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
import org.junit.Assert.assertNull
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

    // Singletons: ThemePrefs wraps a real DataStore whose "one instance per file" guard would trip
    // if we rebuilt it every @Before. Room is already a singleton via AscendyDb.get(). Per-method
    // hygiene (session row + BlockState + maxSession) is reset in [reset] below.
    companion object {
        private val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        private val repo = AscendyRepo(ctx)
        private val prefs = ThemePrefs(ctx)
        private val controller = SessionController(ctx, repo, prefs)
    }

    @Before fun reset() = runTest {
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

    @Test fun manualSource_neverInheritsStrict_andIsEndable() = runTest {
        // A manual session on a STRICT list must NOT become strict, and must stay endable via
        // the long-press toggle (tagId == null). This is the "trapped in strict" regression guard.
        val listId = newList(strict = true)
        controller.startSession(listId, tagId = null, source = SessionSource.Manual)
        assertTrue("session active", BlockState.active.value)
        assertFalse("manual session is never strict", BlockState.strict.value)

        assertEquals(ManualEndResult.Ended, controller.toggleManual())
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

    @Test fun safetyTimer_keepsShortPomodoroUnchanged() = runTest {
        prefs.setMaxSessionMinutes(480)
        val listId = newList(strict = false)
        val before = System.currentTimeMillis()
        controller.startTimedSession(durationMs = 25L * 60 * 1000, listId = listId)   // 25-min pomodoro

        val endsAt = repo.currentSession()!!.endsAt!!
        // The explicit 25-min request is far below the 8h cap, so it must win unchanged.
        assertTrue("pomodoro window preserved", endsAt <= before + 25 * 60_000L + 5_000)
    }

    @Test fun allowList_setsInvertedFlag() = runTest {
        val listId = newList(strict = false, allow = true)
        controller.startSession(listId, tagId = null, source = SessionSource.Nfc)
        assertTrue("allow-only session is inverted", BlockState.inverted.value)
    }

    @Test fun lockdownPref_propagatesToBlockState() = runTest {
        prefs.setLockdownEnabled(true)
        try {
            val listId = newList(strict = false)
            controller.startSession(listId, tagId = null, source = SessionSource.Nfc)
            assertTrue("lockdown flag carried into BlockState", BlockState.lockdown.value)
        } finally {
            prefs.setLockdownEnabled(false)
        }
    }

    @Test fun scheduledSource_canBeStrict_unlikeManual() = runTest {
        // Only Manual is strict-exempt; a Scheduled session on a strict list IS strict.
        val listId = newList(strict = true)
        controller.startSession(listId, tagId = null, source = SessionSource.Scheduled)
        assertTrue("scheduled honors strict", BlockState.strict.value)
        assertFalse("strict session offers no emergency unlock", BlockState.emergencyAvailable.value)
    }

    @Test fun scheduledSession_recordsItsScheduleId() = runTest {
        val listId = newList(strict = false)
        controller.startSession(listId, tagId = null, source = SessionSource.Scheduled, scheduleId = 42L)
        assertEquals("schedule ownership tracked", 42L, repo.currentSession()!!.scheduleId)
    }

    @Test fun manualSessionOnScheduledList_hasNoScheduleId() = runTest {
        // The fix: a manual session on a list that also has a schedule is NOT owned by it, so a
        // schedule END alarm (which matches on scheduleId) can never end this session.
        val listId = newList(strict = false)
        controller.startSession(listId, tagId = null, source = SessionSource.Manual)
        assertNull("manual session unowned by any schedule", repo.currentSession()!!.scheduleId)
    }

    @Test fun restoreOnBoot_rehydratesActiveSession() = runTest {
        val listId = newList(strict = false)
        controller.startSession(listId, tagId = null, source = SessionSource.Nfc)
        // Simulate process death: in-memory state gone, DB row remains active.
        BlockState.clear()
        assertFalse(BlockState.active.value)

        controller.restoreOnBoot()
        assertTrue("session restored from DB", BlockState.active.value)
    }
}
