package com.ascendy.app.blocking

import androidx.test.core.app.ApplicationProvider
import com.ascendy.app.data.AscendyRepo
import com.ascendy.app.data.BlockSession
import com.ascendy.app.data.Blocklist
import com.ascendy.app.data.BoundTag
import com.ascendy.app.data.ThemePrefs
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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

    @Test fun startSession_neverClobbersActiveSession() = runTest {
        // A scheduled/pomodoro start during a STRICT session must not overwrite it — that was
        // an escape hatch out of strict mode and orphaned the prior open log.
        val strictList = newList(strict = true)
        repo.saveTag(BoundTag(tagId = "tagS", nickname = "S", createdAt = 0, listId = strictList))
        controller.handleTagTap("tagS")
        assertTrue(BlockState.strict.value)

        val weakList = newList(strict = false)
        val started = controller.startSession(weakList, tagId = null, source = SessionSource.Scheduled)
        assertFalse("second start refused while a session is active", started)
        assertTrue("original strict session untouched", BlockState.strict.value)
        assertEquals("session row still the strict list", strictList, repo.currentSession()!!.listId)
    }

    @Test fun endSession_closesEveryOpenLog() = runTest {
        val listId = newList(strict = false)
        // Orphaned open log from a (pre-fix) clobbered session.
        repo.startLog(listId, startedAt = 1L, source = "manual")
        controller.startSession(listId, tagId = null, source = SessionSource.Nfc)
        controller.endSession()

        val open = repo.observeLogsSince(0).first().filter { it.endedAt == null }
        assertTrue("no open log remains after endSession, found: $open", open.isEmpty())
    }

    @Test fun endSessionIfStartedAt_ignoresStaleAlarmIdentity() = runTest {
        val listId = newList(strict = false)
        controller.startSession(listId, tagId = null, source = SessionSource.Nfc)
        val realStart = repo.currentSession()!!.startedAt

        // A delayed END alarm armed for a previous session carries that session's startedAt.
        controller.endSessionIfStartedAt(realStart - 99_999L)
        assertTrue("stale alarm ignored", BlockState.active.value)

        controller.endSessionIfStartedAt(realStart)
        assertFalse("matching alarm ends the session", BlockState.active.value)
    }

    @Test fun restoreOnBoot_endsSessionWhoseWindowPassed() = runTest {
        val listId = newList(strict = false)
        controller.startTimedSession(durationMs = 25L * 60 * 1000, listId = listId)
        // Simulate reboot after the window expired: backdate the session so endsAt is in the past.
        // Null the monotonic anchor to model the reboot (elapsedRealtime reset → wall fallback path).
        val s = repo.currentSession()!!
        repo.saveSession(s.copy(
            startedAt = s.startedAt - 10 * 60 * 60_000L,
            endsAt = System.currentTimeMillis() - 1L,
            startedAtElapsed = null,
        ))
        BlockState.clear()

        controller.restoreOnBoot()
        assertFalse("expired session not resurrected", BlockState.active.value)
        assertFalse("DB row closed too", repo.currentSession()!!.active)
    }

    // ───────────────────────── Safety-timer invariant regression set (NH-18) ─────────────────────

    @Test fun restoreOnBoot_clampsNullEndsAtToSafetyTimer() = runTest {
        // AF-04: a pre-safety-timer legacy row can have endsAt == null AND scheduleId == null. The
        // restore MUST still arm a finite end — never leave an unbounded block with no safety timer.
        prefs.setMaxSessionMinutes(60)
        val listId = newList(strict = false)
        repo.saveSession(BlockSession(
            id = 1L, active = true, startedAt = System.currentTimeMillis(),
            listId = listId, tagId = null, emergencyUnlocksLeft = 0,
            endsAt = null, startedAtElapsed = null, scheduleId = null,
        ))
        BlockState.clear()

        controller.restoreOnBoot()
        assertTrue("recent null-endsAt session restored", BlockState.active.value)
        val deadline = BlockState.endsAtElapsed.value
        assertNotNull("a finite monotonic deadline was armed", deadline)
        // Bounded by the 60-min safety cap (+ a little slack), never open-ended.
        assertTrue(
            "deadline within the safety cap",
            deadline!! <= android.os.SystemClock.elapsedRealtime() + 61 * 60_000L,
        )
    }

    @Test fun restoreOnBoot_endsStaleNullEndsAtRow() = runTest {
        // AF-04 (other half): a null-endsAt row whose startedAt is older than the cap must die
        // immediately, not get a fresh full window.
        prefs.setMaxSessionMinutes(60)
        val listId = newList(strict = false)
        repo.saveSession(BlockSession(
            id = 1L, active = true, startedAt = System.currentTimeMillis() - 10 * 60 * 60_000L,
            listId = listId, tagId = null, emergencyUnlocksLeft = 0,
            endsAt = null, startedAtElapsed = null, scheduleId = null,
        ))
        BlockState.clear()

        controller.restoreOnBoot()
        assertFalse("stale null-endsAt row not resurrected", BlockState.active.value)
        assertFalse("DB row closed", repo.currentSession()!!.active)
    }

    @Test fun restoreOnBoot_clearsStaleBlockStateWithNoDbSession() = runTest {
        // NH-01: in-memory BlockState must never outlive its DB row. If the row is inactive but
        // BlockState was left active (partial end / out-of-band end), restore tears it down.
        BlockState.set(active = true, blocked = setOf("com.app"))
        assertTrue(BlockState.active.value)
        // reset() already left the id=1 row inactive.
        controller.restoreOnBoot()
        assertFalse("stale in-memory state cleared", BlockState.active.value)
    }

    @Test fun monotonicTimer_forwardWallClockJump_doesNotEndStrictSessionEarly() = runTest {
        // AF-02: the crux. Start a 60-min session, then jump ONLY the wall clock forward by 10h
        // (uptime unchanged, as a real Settings → Date & time change does). The session must survive.
        prefs.setMaxSessionMinutes(60)
        var wall = 1_000_000_000_000L
        var elapsed = 5_000_000L
        val clk = SessionController(ctx, repo, prefs, wallClock = { wall }, elapsedClock = { elapsed })
        val listId = newList(strict = true)
        clk.startSession(listId, tagId = "anchor", source = SessionSource.Nfc)
        assertTrue(BlockState.active.value)

        wall += 10L * 60 * 60_000L            // wind the clock forward 10 hours
        clk.restoreOnBoot()                   // the heartbeat path runs this every ~90s
        assertTrue("forward clock jump did NOT end the session early", BlockState.active.value)
    }

    @Test fun monotonicTimer_realUptimeElapsed_endsSession() = runTest {
        // The flip side: once real device uptime passes the duration, the session ends — immune to
        // the wall clock entirely (here the wall barely moves but uptime crosses the window).
        prefs.setMaxSessionMinutes(60)
        var wall = 1_000_000_000_000L
        var elapsed = 5_000_000L
        val clk = SessionController(ctx, repo, prefs, wallClock = { wall }, elapsedClock = { elapsed })
        val listId = newList(strict = true)
        clk.startSession(listId, tagId = "anchor", source = SessionSource.Nfc)

        elapsed += 61L * 60_000L              // 61 min of real uptime
        wall += 61L * 60_000L
        clk.restoreOnBoot()
        assertFalse("session ends once real uptime passes the window", BlockState.active.value)
    }

    @Test fun backwardWallClockJump_belowStart_stillEndsWithoutThrowing() = runTest {
        // Regression for the critical coerceIn(min>max) trap: a wall clock wound BACK below startedAt
        // must not throw out of endSessionLocked (which would abort every auto-end vector and trap
        // the user). Uptime passes the window so the session is genuinely due; the backward wall
        // clock must not stop it ending.
        prefs.setMaxSessionMinutes(60)
        var wall = 1_000_000_000_000L
        var elapsed = 5_000_000L
        val clk = SessionController(ctx, repo, prefs, wallClock = { wall }, elapsedClock = { elapsed })
        val listId = newList(strict = true)
        clk.startSession(listId, tagId = "anchor", source = SessionSource.Nfc)

        elapsed += 61L * 60_000L                       // real uptime passes the 60-min window
        wall -= 10L * 60 * 60_000L                     // but the wall clock is wound 10h BACKWARD
        clk.restoreOnBoot()                            // must not throw
        assertFalse("session ends despite a backward wall clock", BlockState.active.value)
        assertFalse("DB row closed", repo.currentSession()!!.active)
        // And the END-alarm path (logEndMs=null) must also be exception-free under the same condition.
        clk.endSession()
    }

    @Test fun rebootSoonAfterStart_doesNotOverExtend_usesWallFallback() = runTest {
        // Regression for the reboot-misdetection: a session started moments after boot1 (small
        // anchor) then a reboot — where uptime has already climbed back past the small anchor — must
        // NOT be read as same-boot (which would re-credit nearly the whole window). The boot-count
        // change forces the wall fallback, so the restored window reflects real remaining time.
        prefs.setMaxSessionMinutes(120)
        var wall = 1_000_000_000_000L
        var elapsed = 1_000L                            // tiny anchor: started ~1s after boot
        var boot = 7L
        val clk = SessionController(
            ctx, repo, prefs,
            wallClock = { wall }, elapsedClock = { elapsed }, bootCount = { boot },
        )
        val listId = newList(strict = true)
        clk.startSession(listId, tagId = "anchor", source = SessionSource.Nfc)  // 120-min window

        // Simulate: 110 min pass, then reboot. Wall advances 110 min; uptime resets then climbs to
        // 90s; boot count increments. With only the elapsed check this would look same-boot
        // (90_000 >= 1_000) and re-credit ~120 min. The boot-count change must force wall fallback.
        wall += 110L * 60_000L
        elapsed = 90_000L
        boot = 8L
        clk.restoreOnBoot()

        assertTrue("session still active (within the 120-min cap)", BlockState.active.value)
        val deadline = BlockState.endsAtElapsed.value!!
        val remainingMin = (deadline - elapsed) / 60_000.0
        // Wall fallback credits the consumed 110 min, leaving ~10 min — NOT a fresh ~120-min window.
        assertTrue("remaining reflects real time (~10 min), not a fresh window: $remainingMin",
            remainingMin in 5.0..20.0)
    }

    @Test fun scheduleEnd_endsOnlyItsOwnSession() = runTest {
        // endSessionIfScheduleId must end only the session a schedule started.
        val listId = newList(strict = false)
        controller.startSession(listId, tagId = null, source = SessionSource.Scheduled, scheduleId = 7L)
        assertTrue(BlockState.active.value)

        controller.endSessionIfScheduleId(99L)
        assertTrue("a different schedule's END leaves it running", BlockState.active.value)
        controller.endSessionIfScheduleId(7L)
        assertFalse("its own schedule's END ends it", BlockState.active.value)
    }

    @Test fun scheduleEnd_ignoresManualSessionOnSameList() = runTest {
        // A manual session on a list that also has a schedule is unowned — a schedule END can't kill it.
        val listId = newList(strict = false)
        controller.startSession(listId, tagId = null, source = SessionSource.Manual)
        controller.endSessionIfScheduleId(7L)
        assertTrue("manual session survives a schedule END", BlockState.active.value)
    }
}
