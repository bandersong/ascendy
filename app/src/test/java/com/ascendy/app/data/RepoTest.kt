package com.ascendy.app.data

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.runner.RunWith
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Repository behavior over real Room (Robolectric): default-list invariant + write-time normalization. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RepoTest {

    private lateinit var repo: AscendyRepo

    @Before fun setUp() {
        repo = AscendyRepo(ApplicationProvider.getApplicationContext())
    }

    @Test fun ensureDefaultList_isIdempotent() = runTest {
        val a = repo.ensureDefaultList()
        val b = repo.ensureDefaultList()
        assertEquals("same default row reused", a.id, b.id)
        assertTrue("flagged default", a.isDefault)
    }

    @Test fun addDomain_normalizesOnWrite() = runTest {
        val id = repo.upsertList(Blocklist(name = "sites"))
        repo.addDomain(id, "HTTPS://www.Reddit.com/r/all")
        assertTrue("stored canonical host", repo.domains(id).contains("reddit.com"))
    }

    @Test fun addAndRemoveDomain_roundTrips() = runTest {
        val id = repo.upsertList(Blocklist(name = "sites2"))
        repo.addDomain(id, "example.com")
        assertTrue(repo.domains(id).contains("example.com"))
        repo.removeDomain(id, "example.com")
        assertTrue(repo.domains(id).isEmpty())
    }

    @Test fun addPackage_roundTrips() = runTest {
        val id = repo.upsertList(Blocklist(name = "apps"))
        repo.addPackage(id, "com.foo.bar")
        assertTrue(repo.packages(id).contains("com.foo.bar"))
    }
}
