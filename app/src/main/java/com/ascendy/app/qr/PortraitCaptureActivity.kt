package com.ascendy.app.qr

import com.journeyapps.barcodescanner.CaptureActivity

/**
 * Subclass of ZXing's CaptureActivity that's declared portrait in our manifest. ZXing's bundled
 * one is locked to landscape — we override here so the scanner matches the rest of the app.
 */
class PortraitCaptureActivity : CaptureActivity()
