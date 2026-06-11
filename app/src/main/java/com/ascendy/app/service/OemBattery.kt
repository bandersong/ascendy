package com.ascendy.app.service

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

/**
 * NH-04: on aggressive-OEM skins (MIUI/HyperOS, EMUI/HarmonyOS, ColorOS, Funtouch, OxygenOS) the
 * standard REQUEST_IGNORE_BATTERY_OPTIMIZATIONS exemption is NOT enough — these vendors freeze
 * background services unless the app is also added to a separate "auto-start" / "protected apps"
 * allowlist that the standard intent never reaches. That freeze stops the foreground-service poll
 * (and can drop alarms), silently weakening enforcement. This routes the user to the right vendor
 * screen so they can whitelist Ascendy.
 *
 * The component names differ per skin AND per version and aren't in our <queries>, so we never call
 * resolveActivity (package-visibility would hide them); we just try to start each candidate and fall
 * back to the next, finally to the generic battery-optimization list. Starting an explicit (exported)
 * component is allowed without visibility.
 */
object OemBattery {

    private val manufacturer: String get() = Build.MANUFACTURER.lowercase()

    private val AGGRESSIVE = setOf(
        "xiaomi", "redmi", "poco", "huawei", "honor",
        "oppo", "realme", "oneplus", "vivo", "iqoo", "meizu",
    )

    /** True on skins known to kill background apps unless separately whitelisted. */
    fun isAggressiveOem(): Boolean = manufacturer in AGGRESSIVE

    /** Human-readable brand for the guidance copy, e.g. "Xiaomi". */
    fun brandLabel(): String =
        Build.MANUFACTURER.ifBlank { "Your phone" }.replaceFirstChar { it.uppercase() }

    private fun comp(pkg: String, cls: String): Intent =
        Intent().setComponent(ComponentName(pkg, cls))

    /** Vendor auto-start / protected-app screens to try, most-likely first, per skin. */
    private fun candidates(): List<Intent> = when (manufacturer) {
        "xiaomi", "redmi", "poco" -> listOf(
            comp("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"),
            comp("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity"),
        )
        "huawei", "honor" -> listOf(
            comp("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"),
            comp("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"),
            comp("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"),
        )
        "oppo", "realme" -> listOf(
            comp("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity"),
            comp("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"),
            comp("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity"),
        )
        "vivo", "iqoo" -> listOf(
            comp("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"),
            comp("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"),
            comp("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"),
        )
        "oneplus" -> listOf(
            comp("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"),
        )
        "meizu" -> listOf(
            comp("com.meizu.safe", "com.meizu.safe.security.SHOW_APPSEC"),
        )
        else -> emptyList()
    }

    /**
     * Open the most specific vendor screen we can. Tries each candidate, falling back to the generic
     * battery-optimization list, then app details — so the button always lands somewhere useful.
     */
    fun openBestSettings(context: Context) {
        for (intent in candidates()) {
            try {
                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                return
            } catch (_: ActivityNotFoundException) {
                // try the next candidate
            } catch (_: SecurityException) {
                // some skins guard these; fall through
            } catch (_: Exception) {
                // be defensive — vendor intents are unreliable
            }
        }
        // Generic fallbacks.
        try {
            context.startActivity(
                Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (_: Exception) {
            try {
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        android.net.Uri.parse("package:" + context.packageName),
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (_: Exception) {}
        }
    }
}
