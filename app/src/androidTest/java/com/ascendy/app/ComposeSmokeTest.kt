package com.ascendy.app

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.Test

/**
 * Isolated Compose smoke test — the rule launches MainActivity, so it lives in its own class to
 * avoid double-launching alongside [SmokeTest]'s ActivityScenario tests. Proves the Compose tree
 * composes its first frame with real resources/graphics on each emulator API level.
 */
@RunWith(AndroidJUnit4::class)
class ComposeSmokeTest {

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test fun composesFirstFrameWithoutCrashing() {
        compose.onRoot().assertExists()
    }
}
