package com.ascendy.app.service

/**
 * Extracts the bare host from whatever a browser shows in its URL bar — which may be a full URL,
 * a bare host, or a typed search string. Pulled out of [BlockingAccessibilityService] so the
 * authority-parsing rules (the things that decide whether a page gets blocked) are unit-testable.
 *
 * Hardened against two bypass shapes the old inline version missed: an explicit port
 * (`site.com:8080`) and userinfo (`evil.com@site.com`), both of which must resolve to the real
 * registrable host so the block still fires.
 */
object UrlHost {

    /** Returns the lowercased, www-stripped host, or null if [raw] isn't host-like (e.g. a search). */
    fun fromUrlBar(raw: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null
        val schemeless = trimmed.removePrefix("https://").removePrefix("http://")
        // Authority = everything up to the first path/query/fragment/space.
        var authority = schemeless
            .substringBefore('/')
            .substringBefore('?')
            .substringBefore('#')
            .substringBefore(' ')
        // Drop userinfo (user:pass@host) — the real host is after the last '@'.
        if ('@' in authority) authority = authority.substringAfterLast('@')
        // Drop an explicit port.
        authority = authority.substringBefore(':')
        // No dot ⇒ it's a search query or a bare hostname like "localhost", not a blockable domain.
        if ('.' !in authority) return null
        return authority.removePrefix("www.").lowercase().ifBlank { null }
    }
}
