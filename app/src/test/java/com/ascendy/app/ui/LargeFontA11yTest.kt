package com.ascendy.app.ui

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import com.ascendy.app.data.SessionLog
import com.ascendy.app.ui.screens.HomeScreen
import com.ascendy.app.ui.screens.PermissionStatus
import com.ascendy.app.ui.screens.PermissionsScreen
import com.ascendy.app.ui.screens.StatsScreen
import com.ascendy.app.ui.theme.AscendyTheme
import com.ascendy.app.ui.theme.NeutralVocab
import com.ascendy.app.ui.theme.ThemeVariant
import com.ascendy.app.ui.theme.ToughVocab
import com.ascendy.app.ui.theme.palette
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Regression guard for the accessibility defects measured on a real Z Flip 7 at font scale 2.0
 * (.autoloop/rounds/r1/realdevice). Every assertion here failed before this change:
 *
 *  - the Home setup-row status pill was starved to 43x146dp and rendered "TODO" as T/O/D/O
 *    stacked one letter per line;
 *  - the Permissions Granted/Missing badge lost its label entirely — the accessibility tree
 *    showed the badge with ZERO text nodes, leaving the state conveyed by colour alone;
 *  - the 7-day chart was a raw Canvas with an empty contentDescription.
 *
 * A pill is WIDER THAN TALL. That single invariant is what a letter-tower violates, and it holds
 * at every font scale, so it is the assertion rather than a pixel width.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp")
class LargeFontA11yTest {

    @get:Rule
    val compose = createComposeRule()

    /** Renders [content] as if the user had set system font size to 2.0x. */
    private fun atFontScale2(variant: ThemeVariant, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            AscendyTheme(variant) {
                CompositionLocalProvider(
                    LocalDensity provides Density(density = 2.75f, fontScale = 2f)
                ) {
                    Surface(color = palette.Cream) { content() }
                }
            }
        }
    }

    @Test fun homeSetupPills_keepTheirLabelShapeAtFontScale2() {
        atFontScale2(ThemeVariant.Tough) {
            HomeScreen(
                tagCount = 0, listCount = 0, permissionsReady = false, streakDays = 0,
                todayFocusedMinutes = 0, dailyGoalMinutes = 0,
                onPairTag = {}, onOpenLists = {}, onOpenPermissions = {}, onOpenSettings = {},
                onOpenStats = {}, onOpenPomodoro = {}, onScanQr = {}, onManualToggle = {},
                onEmergencyUnlock = {},
            )
        }
        // All three setup rows are incomplete, so all three show the "todo" pill.
        val pills = compose.onAllNodesWithContentDescription(ToughVocab.badgeTodo, useUnmergedTree = true)
        val found = pills.fetchSemanticsNodes().size
        assertTrue("expected 3 setup pills, found $found", found == 3)
        repeat(found) { i ->
            val b = pills[i].getUnclippedBoundsInRoot()
            assertTrue(
                "setup pill $i is ${(b.right - b.left)} x ${(b.bottom - b.top)} — taller than wide means the label " +
                    "wrapped one letter per line",
                (b.right - b.left) > (b.bottom - b.top),
            )
        }
    }

    @Test fun permissionBadge_keepsItsLabelInTheA11yTreeAtFontScale2() {
        atFontScale2(ThemeVariant.Neutral) {
            PermissionsScreen(
                status = PermissionStatus(
                    accessibility = true, usageStats = true, overlay = false,
                    notifications = false, batteryExempt = false, vpnConsented = false,
                ),
                a11yDisclosureAccepted = true, onAcceptA11yDisclosure = {}, onBack = {},
                onRequestNotifications = {}, onRequestVpn = {},
            )
        }
        // "Display over other apps" is the long title whose badge vanished on the real device.
        // assertExists, not assertIsDisplayed: at 2.0 the page is far taller than the viewport.
        compose.onNodeWithText(NeutralVocab.permsOverlayTitle).assertExists()
        val badges = compose.onAllNodesWithContentDescription(
            NeutralVocab.permsBadgeMissing, useUnmergedTree = true,
        )
        val found = badges.fetchSemanticsNodes().size
        // overlay, notifications, battery and VPN are all ungranted in this fixture.
        assertTrue("expected 4 Missing badges in the accessibility tree, found $found", found == 4)
        repeat(found) { i ->
            val b = badges[i].getUnclippedBoundsInRoot()
            assertTrue("Missing badge $i is ${(b.right - b.left)} x ${(b.bottom - b.top)} — collapsed to a sliver", (b.right - b.left) > (b.bottom - b.top))
        }
    }

    @Test fun weekChart_describesItsRealDataToAScreenReader() {
        // Two sessions inside the last 7 days so the summary has real numbers to report.
        val now = System.currentTimeMillis()
        val recent = listOf(
            SessionLog(id = 1, listId = 1, startedAt = now - 3_600_000L, endedAt = now, source = "nfc"),
            SessionLog(id = 2, listId = 1, startedAt = now - 90_000_000L,
                endedAt = now - 88_200_000L, source = "pomodoro"),
        )
        atFontScale2(ThemeVariant.Neutral) {
            StatsScreen(
                todayMs = 3_600_000L, weekMs = 5_400_000L, allTimeMs = 9_000_000L,
                streakDays = 3, recent = recent, onBack = {},
            )
        }
        val desc = compose.onNode(
            SemanticsMatcher.keyIsDefined(SemanticsProperties.ContentDescription) and
                SemanticsMatcher("chart summary") { node ->
                    node.config.getOrNull(SemanticsProperties.ContentDescription)
                        ?.firstOrNull()?.startsWith("Focus by day") == true
                },
            useUnmergedTree = true,
        ).fetchSemanticsNode().config[SemanticsProperties.ContentDescription].first()

        // Every one of the seven days, plus the week total — not just seven bare letters.
        assertTrue("chart summary has no per-day values: $desc", Regex("\\d+m").findAll(desc).count() >= 7)
        assertTrue("chart summary reports no total: $desc", desc.contains("Total"))
        assertTrue("chart summary reports no average: $desc", desc.contains("average"))
    }

    @Test fun weekChart_saysSoWhenTheWeekIsEmpty() {
        atFontScale2(ThemeVariant.Neutral) {
            StatsScreen(
                todayMs = 0L, weekMs = 0L, allTimeMs = 0L,
                streakDays = 0, recent = emptyList(), onBack = {},
            )
        }
        compose.onNodeWithContentDescription(NeutralVocab.chart.empty, useUnmergedTree = true)
            .assertExists()
    }
}

private fun androidx.compose.ui.semantics.SemanticsConfiguration.getOrNull(
    key: androidx.compose.ui.semantics.SemanticsPropertyKey<List<String>>,
): List<String>? = if (contains(key)) this[key] else null
