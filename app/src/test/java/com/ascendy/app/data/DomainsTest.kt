package com.ascendy.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

/** Pure-JVM tests for blocklist domain canonicalization. */
class DomainsTest {

    @Test fun stripsSchemePortPathAndWww() =
        assertEquals("reddit.com", Domains.normalize("https://www.Reddit.com:443/r/all"))

    @Test fun lowercasesAndTrims() =
        assertEquals("news.ycombinator.com", Domains.normalize("  HTTP://news.YCombinator.com/  "))

    @Test fun bareDomainUnchanged() =
        assertEquals("reddit.com", Domains.normalize("Reddit.com"))

    @Test fun stripsLeadingWwwOnly() =
        assertEquals("example.com", Domains.normalize("www.example.com"))

    @Test fun keepsInnerWww() =
        assertEquals("sub.www.example.com", Domains.normalize("sub.www.example.com"))

    @Test fun dropsPathWithoutScheme() =
        assertEquals("example.com", Domains.normalize("example.com/path?q=1"))

    @Test fun handlesHostWithPort() =
        assertEquals("localhost", Domains.normalize("http://localhost:8080"))
}
