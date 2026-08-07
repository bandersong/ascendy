package com.ascendy.app.qr

import android.os.Bundle
import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.journeyapps.barcodescanner.CaptureActivity

/**
 * Subclass of ZXing's CaptureActivity that's declared portrait in our manifest. ZXing's bundled
 * one is locked to landscape — we override here so the scanner matches the rest of the app.
 *
 * It also fixes this screen for Android 16. zxing-android-embedded 4.3.0 predates edge-to-edge and
 * its theme still inherits android:Theme.Holo.NoActionBar.Fullscreen, but Android 16's forced
 * edge-to-edge (compat change DISABLE_OPT_OUT_EDGE_TO_EDGE, confirmed ENABLED for this app on a
 * real SM-F766U1) applies per-app — third-party activities included. The library's layout has no
 * inset handling at all, so its chrome (the status/prompt text pinned to the bottom of
 * zxing_barcode_scanner.xml) ends up underneath the navigation bar and the preview runs under the
 * display cutout.
 *
 * Padding the content root by the system bars restores exactly the pre-Android-16 geometry: the
 * camera preview and the viewfinder box are laid out inside the bars, like on Android 15 and below.
 * The framing rectangle and decoding are both derived from the preview view's own size, so nothing
 * about scanning changes. On older devices where the Holo fullscreen theme actually hides the
 * status bar, that inset is zero and this is a no-op.
 */
class PortraitCaptureActivity : CaptureActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root: View = findViewById(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, windowInsets ->
            val bars: Insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            // Nothing below us reads insets (the library never asked for any), so stop here.
            WindowInsetsCompat.CONSUMED
        }
    }
}
