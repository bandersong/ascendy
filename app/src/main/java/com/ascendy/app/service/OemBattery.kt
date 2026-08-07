package com.ascendy.app.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

/**
 * NH-04: on aggressive-OEM skins (One UI, MIUI/HyperOS, EMUI/HarmonyOS, ColorOS, Funtouch,
 * OxygenOS) the standard REQUEST_IGNORE_BATTERY_OPTIMIZATIONS exemption is NOT enough — these
 * vendors freeze background services unless the app is also added to a separate "auto-start" /
 * "protected apps" / "never sleeping apps" allowlist that the standard intent never reaches. That
 * freeze stops the foreground-service poll (and can drop alarms), silently weakening enforcement.
 * This routes the user to the right vendor screen so they can whitelist Ascendy.
 *
 * The component names differ per skin AND per version and aren't in our <queries>, so we never call
 * resolveActivity (package-visibility would hide them); we just try to start each candidate and
 * fall back to the next, finally to the generic battery-optimization list (see [SettingsLauncher]).
 * Starting an explicit (exported) component is allowed without visibility.
 */
object OemBattery {

    private val manufacturer: String get() = Build.MANUFACTURER.lowercase()

    private val AGGRESSIVE = setOf(
        // Samsung ships more Android phones than anyone and One UI is one of the worst offenders:
        // "Put unused apps to sleep" is ON by default and Device Care will park a focus blocker
        // mid-session. It was missing here, so candidatesFor() returned nothing and the whole
        // guidance card was hidden on the single biggest OEM in the world.
        "samsung",
        "xiaomi", "redmi", "poco", "huawei", "honor",
        "oppo", "realme", "oneplus", "vivo", "iqoo", "meizu",
    )

    /** True on skins known to kill background apps unless separately whitelisted. */
    fun isAggressiveOem(): Boolean = manufacturer in AGGRESSIVE

    /** Human-readable brand for the guidance copy, e.g. "Xiaomi". */
    fun brandLabel(): String =
        Build.MANUFACTURER.ifBlank { "Your phone" }.replaceFirstChar { it.uppercase() }

    /**
     * Vendor auto-start / protected-app / never-sleeping-app screens to try, most-likely first, per
     * skin. Plain package/class pairs rather than Intents so the per-brand routing is unit-testable
     * without a device; [openBestSettings] turns them into explicit intents.
     */
    internal fun candidatesFor(brand: String): List<Pair<String, String>> = when (brand) {
        // One UI's app-sleep controls live in Device Care ("Battery > Background usage limits",
        // which owns the Sleeping / Deep sleeping / Never sleeping app lists). The hosting package
        // moved from com.samsung.android.sm (Smart Manager, Android 7–8) to com.samsung.android.lool
        // (Device Care, One UI 1+), and the activity moved between two class paths across One UI
        // versions — try all four, then the generic fallbacks. The China ROM (sm_cn) keeps a
        // separate auto-run list.
        "samsung" -> listOf(
            "com.samsung.android.lool" to "com.samsung.android.sm.ui.battery.BatteryActivity",
            "com.samsung.android.lool" to "com.samsung.android.sm.battery.ui.BatteryActivity",
            "com.samsung.android.sm" to "com.samsung.android.sm.ui.battery.BatteryActivity",
            "com.samsung.android.sm" to "com.samsung.android.sm.ui.ram.AutoRunActivity",
            "com.samsung.android.sm_cn" to "com.samsung.android.sm.ui.ram.AutoRunActivity",
        )
        "xiaomi", "redmi", "poco" -> listOf(
            "com.miui.securitycenter" to "com.miui.permcenter.autostart.AutoStartManagementActivity",
            "com.miui.securitycenter" to "com.miui.permcenter.permissions.PermissionsEditorActivity",
        )
        "huawei", "honor" -> listOf(
            "com.huawei.systemmanager" to "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity",
            "com.huawei.systemmanager" to "com.huawei.systemmanager.optimize.process.ProtectActivity",
            "com.huawei.systemmanager" to "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity",
        )
        "oppo", "realme" -> listOf(
            "com.coloros.safecenter" to "com.coloros.safecenter.startupapp.StartupAppListActivity",
            "com.coloros.safecenter" to "com.coloros.safecenter.permission.startup.StartupAppListActivity",
            "com.oppo.safe" to "com.oppo.safe.permission.startup.StartupAppListActivity",
        )
        "vivo", "iqoo" -> listOf(
            "com.vivo.permissionmanager" to "com.vivo.permissionmanager.activity.BgStartUpManagerActivity",
            "com.iqoo.secure" to "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager",
            "com.iqoo.secure" to "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity",
        )
        "oneplus" -> listOf(
            "com.oneplus.security" to "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity",
        )
        "meizu" -> listOf(
            "com.meizu.safe" to "com.meizu.safe.security.SHOW_APPSEC",
        )
        else -> emptyList()
    }

    /**
     * Open the most specific vendor screen we can. Each candidate is tried in turn and the generic
     * battery-optimization list closes the chain; SettingsLauncher adds the app-details page after
     * that — so the button always lands somewhere useful, on every firmware, and never throws.
     */
    fun openBestSettings(context: Context) {
        val vendor = candidatesFor(manufacturer)
            .map { (pkg, cls) -> Intent().setComponent(ComponentName(pkg, cls)) }
        SettingsLauncher.open(
            context,
            *vendor.toTypedArray(),
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS),
        )
    }
}
