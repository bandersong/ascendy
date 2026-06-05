package com.ascendy.app.blocking

import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pure-JVM tests for the in-memory enforcement predicates ([BlockState.isBlocked] /
 * [BlockState.isDomainBlocked]) — the exact checks the accessibility service and VPN sinkhole run
 * on every foreground app / DNS query. Subdomain matching and allow-list inversion are easy to get
 * subtly wrong, so they're pinned here. BlockState is a singleton; we reset around each test.
 */
class BlockStateTest {

    @Before fun setUp() = BlockState.clear()
    @After fun tearDown() = BlockState.clear()

    @Test fun inactive_blocksNothing() {
        BlockState.set(active = false, blocked = setOf("com.app"))
        assertFalse(BlockState.isBlocked("com.app"))
        assertFalse(BlockState.isDomainBlocked("reddit.com"))
    }

    @Test fun blocklist_matchesListedPackagesOnly() {
        BlockState.set(active = true, blocked = setOf("com.app.bad"))
        assertTrue(BlockState.isBlocked("com.app.bad"))
        assertFalse(BlockState.isBlocked("com.app.good"))
    }

    @Test fun allowList_invertsPackageSemantics() {
        BlockState.set(active = true, blocked = setOf("com.study"), inverted = true)
        assertFalse("allowed app passes", BlockState.isBlocked("com.study"))
        assertTrue("everything else is blocked", BlockState.isBlocked("com.distraction"))
    }

    @Test fun domain_exactAndSubdomainMatch() {
        BlockState.set(active = true, blocked = emptySet(), blockedDomains = setOf("reddit.com"))
        assertTrue("exact host", BlockState.isDomainBlocked("reddit.com"))
        assertTrue("www stripped", BlockState.isDomainBlocked("www.reddit.com"))
        assertTrue("subdomain", BlockState.isDomainBlocked("old.reddit.com"))
    }

    @Test fun domain_rejectsLookalikesAndSuffixTricks() {
        BlockState.set(active = true, blocked = emptySet(), blockedDomains = setOf("reddit.com"))
        assertFalse("prefix lookalike", BlockState.isDomainBlocked("notreddit.com"))
        assertFalse("blocked domain as a prefix of attacker host", BlockState.isDomainBlocked("reddit.com.evil.com"))
    }

    @Test fun domain_allowListInverts() {
        BlockState.set(
            active = true,
            blocked = emptySet(),
            blockedDomains = setOf("khanacademy.org"),
            inverted = true,
        )
        assertFalse("allow-listed site loads", BlockState.isDomainBlocked("khanacademy.org"))
        assertTrue("anything else is sinkholed", BlockState.isDomainBlocked("reddit.com"))
    }

    @Test fun blockedCount_reflectsSet() {
        BlockState.set(active = true, blocked = setOf("a", "b", "c"))
        org.junit.Assert.assertEquals(3, BlockState.blockedCount())
    }
}
