package com.ascendy.app.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure-JVM tests for the release-tag parsing + update-available comparison. */
class UpdaterVersionTest {

    @Test fun parse_vPrefixedTag() = assertEquals(47, Updater.parseLatestVersionCode("v47"))
    @Test fun parse_plainNumber() = assertEquals(47, Updater.parseLatestVersionCode("47"))
    @Test fun parse_zero() = assertEquals(0, Updater.parseLatestVersionCode("v0"))
    @Test fun parse_trailingWhitespace() = assertEquals(48, Updater.parseLatestVersionCode("v48 "))
    @Test fun parse_nonNumericIsNull() = assertNull(Updater.parseLatestVersionCode("vX"))
    @Test fun parse_emptyIsNull() = assertNull(Updater.parseLatestVersionCode(""))
    @Test fun parse_bareVIsNull() = assertNull(Updater.parseLatestVersionCode("v"))

    @Test fun available_whenStrictlyNewer() = assertTrue(Updater.isUpdateAvailable(48, 47))
    @Test fun notAvailable_whenEqual() = assertFalse(Updater.isUpdateAvailable(47, 47))
    @Test fun notAvailable_whenOlder() = assertFalse(Updater.isUpdateAvailable(46, 47))
}
