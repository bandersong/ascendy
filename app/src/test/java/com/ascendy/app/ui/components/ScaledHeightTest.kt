package com.ascendy.app.ui.components

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Hero art used to be fixed dp, which on a folded cover screen left no room for the copy.
 * The replacement scales with the window and clamps — these are the three cases that matter:
 * a short viewport must land on the floor, a tall one on the ceiling, and the middle must
 * actually track the viewport instead of quietly pinning to an edge.
 */
class ScaledHeightTest {

    @Test fun `short viewport falls back to the floor, never the fixed size`() {
        // Z Flip cover geometry (~400dp tall): 0.20 * 400 = 80 -> floor.
        assertEquals(112.dp, scaledHeightFor(400, 0.20f, min = 112.dp, max = 144.dp))
    }

    @Test fun `tall viewport is capped so art cannot eat the page`() {
        // Unfolded inner panel (~960dp tall): 0.20 * 960 = 192 -> cap. That cap is what buys
        // back the ~34dp Home was overflowing by.
        assertEquals(144.dp, scaledHeightFor(960, 0.20f, min = 112.dp, max = 144.dp))
    }

    @Test fun `between the bounds it tracks the viewport`() {
        assertEquals(130.dp, scaledHeightFor(650, 0.20f, min = 112.dp, max = 144.dp))
    }
}
