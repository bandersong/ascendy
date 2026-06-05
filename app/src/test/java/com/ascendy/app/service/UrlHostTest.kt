package com.ascendy.app.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Pure-JVM tests for URL-bar host extraction, incl. the port/userinfo bypass regressions. */
class UrlHostTest {

    @Test fun fullUrl() = assertEquals("reddit.com", UrlHost.fromUrlBar("https://reddit.com/r/all"))
    @Test fun bareHost() = assertEquals("reddit.com", UrlHost.fromUrlBar("reddit.com"))
    @Test fun stripsWwwAndCase() = assertEquals("reddit.com", UrlHost.fromUrlBar("www.Reddit.com"))
    @Test fun keepsSubdomain() =
        assertEquals("news.ycombinator.com", UrlHost.fromUrlBar("https://news.ycombinator.com/item?id=1"))

    // ── regression: these used to leak through the blocker ──
    @Test fun stripsExplicitPort() = assertEquals("reddit.com", UrlHost.fromUrlBar("reddit.com:8080"))
    @Test fun stripsPortWithSchemeAndPath() =
        assertEquals("example.com", UrlHost.fromUrlBar("https://example.com:443/path?q=1#frag"))
    @Test fun stripsUserinfo() = assertEquals("reddit.com", UrlHost.fromUrlBar("https://evil.com@reddit.com/"))
    @Test fun stripsUserinfoAndPort() =
        assertEquals("reddit.com", UrlHost.fromUrlBar("http://user:pass@reddit.com:8443/x"))

    // ── search strings / non-hosts must NOT be treated as a blockable host ──
    @Test fun searchWithoutDotIsNull() = assertNull(UrlHost.fromUrlBar("how to quit reddit"))
    @Test fun firstTokenWins_whenItLooksLikeAHost() =
        assertEquals("reddit.com", UrlHost.fromUrlBar("reddit.com vs twitter"))
    @Test fun bareLocalhostIsNull() = assertNull(UrlHost.fromUrlBar("http://localhost"))
    @Test fun emptyIsNull() = assertNull(UrlHost.fromUrlBar("   "))
}
