package com.ascendy.app.qr

import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** QR bitmap rendering needs android.graphics.Bitmap, so it runs under Robolectric. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class QrRenderTest {

    @Test fun render_producesRequestedSquareSize() {
        val bmp = QrTools.render("ascendy:render-test-payload", sizePx = 256)
        assertEquals(256, bmp.width)
        assertEquals(256, bmp.height)
    }

    @Test fun render_isStrictlyBlackAndWhite() {
        val bmp = QrTools.render("ascendy:bw", sizePx = 128)
        // Sample a few pixels; a 1-bit QR must only ever emit pure black or white.
        for (xy in listOf(0 to 0, 64 to 64, 127 to 127)) {
            val p = bmp.getPixel(xy.first, xy.second)
            assertTrue("pixel ${xy} not monochrome", p == Color.BLACK || p == Color.WHITE)
        }
    }
}
