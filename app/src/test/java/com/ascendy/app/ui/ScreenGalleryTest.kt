package com.ascendy.app.ui

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.ascendy.app.data.Blocklist
import com.ascendy.app.data.BoundTag
import com.ascendy.app.data.Schedule
import com.ascendy.app.data.SessionLog
import com.ascendy.app.ui.screens.AboutScreen
import com.ascendy.app.ui.screens.AppPickerScreen
import com.ascendy.app.ui.screens.BlocklistScreen
import com.ascendy.app.ui.screens.HomeScreen
import com.ascendy.app.ui.screens.OnboardingScreen
import com.ascendy.app.ui.screens.PairTagScreen
import com.ascendy.app.ui.screens.PermissionStatus
import com.ascendy.app.ui.screens.PermissionsScreen
import com.ascendy.app.ui.screens.PomodoroScreen
import com.ascendy.app.ui.screens.SchedulesScreen
import com.ascendy.app.ui.screens.SettingsScreen
import com.ascendy.app.ui.screens.StatsScreen
import com.ascendy.app.ui.theme.AscendyTheme
import com.ascendy.app.ui.theme.ThemeVariant
import com.ascendy.app.ui.theme.palette
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Full-screen renders of the REAL screens (not just primitives) with fabricated data,
 * so the composed UI can actually be eyeballed across themes. Animations are frozen
 * (mainClock.autoAdvance = false) so the infinite mascot bob can't block idle.
 *   ./gradlew :app:recordRoborazziFossDebug
 * Output: src/test/snapshots/screens/ (one PNG per screen)
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp")
class ScreenGalleryTest {

    @get:Rule
    val compose = createComposeRule()

    private val opts = RoborazziOptions(
        compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.01f),
    )

    private fun snap(
        name: String,
        variant: ThemeVariant = ThemeVariant.Neutral,
        dark: Boolean = false,
        content: @Composable () -> Unit,
    ) {
        compose.mainClock.autoAdvance = false   // freeze infinite animations (mascot bob)
        RuntimeEnvironment.setQualifiers(if (dark) "+night" else "+notnight")
        compose.setContent {
            AscendyTheme(variant) {
                Surface(color = palette.Cream) { content() }
            }
        }
        compose.onRoot().captureRoboImage("src/test/snapshots/screens/$name.png", roborazziOptions = opts)
    }

    // ── fabricated data ──
    private val lists = listOf(
        Blocklist(id = 1, name = "Deep Work", isDefault = true, isStrict = true),
        Blocklist(id = 2, name = "Study Only", isAllowList = true),
        Blocklist(id = 3, name = "Evening Wind-down"),
    )
    private val tags = listOf(
        BoundTag(tagId = "uuid-desk", nickname = "Desk", createdAt = 0L, listId = 1, kind = "nfc"),
        BoundTag(tagId = "uuid-bed", nickname = "Bedroom QR", createdAt = 0L, listId = null, kind = "qr"),
    )
    private val recent = listOf(
        SessionLog(id = 1, listId = 1, startedAt = 1_700_000_000_000L, endedAt = 1_700_001_500_000L, source = "nfc"),
        SessionLog(id = 2, listId = 3, startedAt = 1_699_900_000_000L, endedAt = 1_699_903_000_000L, source = "pomodoro"),
        SessionLog(id = 3, listId = 2, startedAt = 1_699_800_000_000L, endedAt = 1_699_801_200_000L, source = "scheduled"),
    )
    private val schedules = listOf(
        Schedule(id = 1, listId = 1, daysOfWeek = 0b0111110, startMinuteOfDay = 540, endMinuteOfDay = 1020, nickname = "Work hours"),
        Schedule(id = 2, listId = 3, daysOfWeek = 0b1000001, startMinuteOfDay = 1230, endMinuteOfDay = 1380, enabled = false, nickname = "Weekend nights"),
    )
    private val perms = PermissionStatus(
        accessibility = true, usageStats = true, overlay = false,
        notifications = true, batteryExempt = false, vpnConsented = true,
    )

    @Composable
    private fun home() {
        HomeScreen(
            tagCount = 2, listCount = 3, permissionsReady = true, streakDays = 12,
            todayFocusedMinutes = 45, dailyGoalMinutes = 90,
            onPairTag = {}, onOpenLists = {}, onOpenPermissions = {}, onOpenSettings = {},
            onOpenStats = {}, onOpenPomodoro = {}, onScanQr = {}, onManualToggle = {}, onEmergencyUnlock = {},
        )
    }

    @Test fun home_neutral() = snap("home_neutral") { home() }
    @Test fun home_kawaii() = snap("home_kawaii", ThemeVariant.Kawaii) { home() }
    @Test fun home_tough_dark() = snap("home_tough_dark", ThemeVariant.Tough, dark = true) { home() }

    @Test fun settings() = snap("settings") {
        SettingsScreen(
            current = ThemeVariant.Neutral, safetyMinutes = 480, dailyGoalMinutes = 90,
            lockdownEnabled = false, lockdownLocked = false,
            onPickTheme = {}, onPickSafetyMinutes = {}, onPickGoalMinutes = {}, onToggleLockdown = {},
            onOpenStats = {}, onOpenSchedules = {}, onOpenPomodoro = {}, onOpenUpdates = {},
            onOpenAbout = {}, onOpenTags = {}, onOpenLists = {}, onOpenPermissions = {}, onBack = {},
        )
    }

    @Test fun blocklist() = snap("blocklist") {
        BlocklistScreen(
            lists = lists, appCountFor = { 7 }, onOpenList = {}, onCreateList = {}, onDeleteList = {},
            onToggleStrict = { _, _ -> }, onToggleAllowList = { _, _ -> }, onBack = {},
        )
    }

    @Test fun apppicker() = snap("apppicker") {
        AppPickerScreen(
            listName = "Deep Work", blockedPackages = setOf("com.android.chrome"),
            blockedDomains = listOf("reddit.com", "youtube.com", "news.ycombinator.com"),
            onTogglePackage = { _, _ -> }, onAddDomain = {}, onRemoveDomain = {}, onBack = {},
        )
    }

    @Test fun pairtag() = snap("pairtag") {
        PairTagScreen(
            waiting = false, detectedTagId = null, knownTags = tags, lists = lists,
            nfcSupported = true, nfcEnabled = true,
            onOpenNfcSettings = {}, onStartPairing = {}, onCancelPairing = {}, onSavePairing = {},
            onDeleteTag = {}, onAssignList = { _, _ -> }, onSaveQrAnchor = { _, _ -> },
            onSaveQrToGallery = {}, onShareQr = {}, onBack = {},
        )
    }

    @Test fun schedules() = snap("schedules") {
        SchedulesScreen(
            schedules = schedules, lists = lists, onSave = {}, onDelete = {}, onToggle = { _, _ -> }, onBack = {},
        )
    }

    @Test fun permissions() = snap("permissions") {
        PermissionsScreen(
            status = perms, a11yDisclosureAccepted = true,
            onAcceptA11yDisclosure = {}, onBack = {}, onRequestNotifications = {}, onRequestVpn = {},
        )
    }

    @Test fun stats() = snap("stats") {
        StatsScreen(
            todayMs = 45 * 60_000L, weekMs = 8 * 3_600_000L, allTimeMs = 120 * 3_600_000L,
            streakDays = 12, recent = recent, onBack = {},
        )
    }

    @Test fun pomodoro() = snap("pomodoro") {
        PomodoroScreen(lists = lists, onStart = { _, _ -> }, onBack = {})
    }

    @Test fun onboarding() = snap("onboarding") {
        OnboardingScreen(initialSafetyMinutes = 480, onFinish = {})
    }

    @Test fun about() = snap("about") {
        AboutScreen(onBack = {})
    }
}
