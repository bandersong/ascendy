package com.ascendy.app.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM cover for the per-brand auto-start routing. The regression that motivated it: Samsung
 * was missing from the table entirely, so on the biggest Android OEM in the world the "stop the OS
 * killing Ascendy" button fell straight through to the generic battery screen.
 */
class OemBatteryTest {

    @Test fun samsungHasCandidates() =
        assertTrue(OemBattery.candidatesFor("samsung").isNotEmpty())

    /** Device Care (One UI 1+) is where the Sleeping / Never-sleeping app lists live. */
    @Test fun samsungTriesDeviceCareFirst() =
        assertEquals("com.samsung.android.lool", OemBattery.candidatesFor("samsung").first().first)

    @Test fun xiaomiStillRoutesToMiuiAutostart() =
        assertEquals(
            "com.miui.permcenter.autostart.AutoStartManagementActivity",
            OemBattery.candidatesFor("xiaomi").first().second,
        )

    /** Stock/Pixel/unknown brands have no vendor screen — SettingsLauncher's generics take over. */
    @Test fun unknownBrandHasNoCandidates() =
        assertTrue(OemBattery.candidatesFor("google").isEmpty())

    /** Lookup is on the lowercased manufacturer; a capitalised key would silently match nothing. */
    @Test fun tableKeysAreLowercase() =
        assertTrue(OemBattery.candidatesFor("SAMSUNG").isEmpty())

    /** Every entry must be a real package/class pair — a blank half can't be started. */
    @Test fun everyCandidateIsFullyQualified() {
        val brands = listOf(
            "samsung", "xiaomi", "redmi", "poco", "huawei", "honor",
            "oppo", "realme", "oneplus", "vivo", "iqoo", "meizu",
        )
        for (brand in brands) {
            val candidates = OemBattery.candidatesFor(brand)
            assertTrue("$brand has no candidates", candidates.isNotEmpty())
            for ((pkg, cls) in candidates) {
                assertTrue("$brand: bad package '$pkg'", pkg.contains('.'))
                assertTrue("$brand: bad class '$cls'", cls.contains('.'))
            }
        }
    }
}
