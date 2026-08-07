package com.ascendy.app.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import com.ascendy.app.R

/**
 * The single way this app opens a system settings screen.
 *
 * A settings screen is not guaranteed to exist. AOSP forks, Android Go builds, tablets and OEM
 * skins drop or rename them (ACTION_USAGE_ACCESS_SETTINGS and ACTION_MANAGE_OVERLAY_PERMISSION are
 * the usual missing ones), and the vendor auto-start screens in [OemBattery] are undocumented
 * components that only exist on some firmwares. A bare
 * `startActivity(Intent(Settings.ACTION_...))` throws ActivityNotFoundException on those devices
 * and takes the whole app down on a permission tap.
 *
 * So: try each candidate in order, fall back to this app's own entry in the system app list (which
 * always exists), and only if even that is gone tell the user. A settings tap never crashes.
 */
object SettingsLauncher {

    private const val TAG = "SettingsLauncher"

    /** Open the first of [intents] the device actually has, else the app's own details page. */
    fun open(context: Context, vararg intents: Intent) {
        for (intent in intents) if (start(context, intent)) return
        val details = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:" + context.packageName),
        )
        if (start(context, details)) return
        Toast.makeText(context, R.string.settings_screen_unavailable, Toast.LENGTH_LONG).show()
    }

    /**
     * ActivityNotFoundException is the documented miss. SecurityException covers vendor components
     * that exist but aren't exported to us. Anything else an OEM settings app throws on launch is
     * swallowed too — a broken settings screen must degrade to the next candidate, never be fatal.
     */
    private fun start(context: Context, intent: Intent): Boolean = try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (e: Exception) {
        android.util.Log.w(TAG, "no activity for ${intent.action ?: intent.component}: ${e.message}")
        false
    }
}
