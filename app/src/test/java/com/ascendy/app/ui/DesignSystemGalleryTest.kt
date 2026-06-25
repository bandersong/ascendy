package com.ascendy.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.ascendy.app.ui.components.Badge
import com.ascendy.app.ui.components.EmptyState
import com.ascendy.app.ui.components.HairlineDivider
import com.ascendy.app.ui.components.SectionLabel
import com.ascendy.app.ui.components.SelectableChip
import com.ascendy.app.ui.components.SoftCard
import com.ascendy.app.ui.theme.AscendyTheme
import com.ascendy.app.ui.theme.HSpace
import com.ascendy.app.ui.theme.Space
import com.ascendy.app.ui.theme.ThemeVariant
import com.ascendy.app.ui.theme.VSpace
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
 * Design-system gallery snapshots — the visual regression gate the campaign needed.
 * Renders the token-driven primitives across all three themes x light/dark on the
 * JVM (Robolectric, no emulator), so any drift in spacing/type/color/component
 * state shows up as a PNG diff in CI.
 *
 *   ./gradlew :app:recordRoborazziFossDebug   # write/refresh goldens
 *   ./gradlew :app:verifyRoborazziFossDebug    # CI gate against committed goldens
 *
 * Deliberately excludes the animated Mascot (infinite animation is non-deterministic
 * for a static snapshot). See docs/UI_MASTERPIECE.md.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp")
class DesignSystemGalleryTest {

    @get:Rule
    val compose = createComposeRule()

    @Test fun kawaii_light() = snap(ThemeVariant.Kawaii, dark = false)
    @Test fun kawaii_dark() = snap(ThemeVariant.Kawaii, dark = true)
    @Test fun neutral_light() = snap(ThemeVariant.Neutral, dark = false)
    @Test fun neutral_dark() = snap(ThemeVariant.Neutral, dark = true)
    @Test fun tough_light() = snap(ThemeVariant.Tough, dark = false)
    @Test fun tough_dark() = snap(ThemeVariant.Tough, dark = true)

    private fun snap(variant: ThemeVariant, dark: Boolean) {
        // AscendyTheme reads isSystemInDarkTheme(); flip the night qualifier to drive it.
        RuntimeEnvironment.setQualifiers(if (dark) "+night" else "+notnight")
        compose.setContent {
            AscendyTheme(variant) { Gallery() }
        }
        val mode = if (dark) "dark" else "light"
        compose.onRoot().captureRoboImage(
            filePath = "src/test/snapshots/gallery_${variant.name.lowercase()}_$mode.png",
            // Tolerate sub-pixel anti-aliasing / font-hinting diffs between a local
            // record and a CI verify (different OS/JBR). 1% changed-pixel budget.
            roborazziOptions = RoborazziOptions(
                compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.01f),
            ),
        )
    }
}

/** Static showcase of the token-driven primitives — no animated content. */
@Composable
private fun Gallery() {
    Column(
        Modifier
            .background(palette.Cream)
            .fillMaxWidth()
            .padding(Space.xl)
    ) {
        SectionLabel("Design System")
        VSpace(Space.md)
        SoftCard(Modifier.fillMaxWidth()) {
            Column {
                Text("Headline", style = MaterialTheme.typography.headlineMedium, color = palette.Ink)
                VSpace(Space.xs)
                Text("Title large", style = MaterialTheme.typography.titleLarge, color = palette.Ink)
                VSpace(Space.xs)
                Text("Body medium — the quick brown fox.", style = MaterialTheme.typography.bodyMedium, color = palette.Ink)
                VSpace(Space.xs)
                Text("Caption · bodySmall", style = MaterialTheme.typography.bodySmall, color = palette.Smoke)
            }
        }
        VSpace(Space.lg)
        Row {
            Badge("Ready", color = palette.Sage)
            HSpace(Space.sm)
            Badge("Focusing", color = palette.Lilac)
            HSpace(Space.sm)
            Badge("Strict", color = palette.Petal)
            HSpace(Space.sm)
            Badge("Streak 3", color = palette.Mint)
        }
        VSpace(Space.lg)
        Row {
            SelectableChip("Selected", selected = true, onClick = {}, modifier = Modifier.width(140.dp))
            HSpace(Space.sm)
            SelectableChip("Idle", selected = false, onClick = {}, modifier = Modifier.width(140.dp))
        }
        VSpace(Space.lg)
        HairlineDivider()
        VSpace(Space.lg)
        Row {
            Button(onClick = {}) { Text("Primary") }
            HSpace(Space.sm)
            TextButton(onClick = {}) { Text("Text") }
        }
        VSpace(Space.lg)
        EmptyState("No lists yet — tap + to start")
    }
}
