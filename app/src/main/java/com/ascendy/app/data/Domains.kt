package com.ascendy.app.data

/**
 * Domain canonicalization for the blocklist. Pure string logic, extracted so the normalization
 * rules (what counts as "the same site") can be unit-tested directly — a blocked domain that
 * fails to match the DNS/URL-bar form silently lets a distraction through.
 */
object Domains {

    /**
     * Normalize a user-entered site to its bare registrable host: lowercased, scheme stripped,
     * path/query dropped, port dropped, and a leading "www." removed.
     *
     * e.g. "HTTPS://www.Reddit.com:443/r/all" → "reddit.com".
     */
    fun normalize(raw: String): String {
        var d = raw.trim().lowercase()
        d = d.removePrefix("https://").removePrefix("http://")
        d = d.substringBefore('/')
        d = d.substringBefore(':')
        d = d.removePrefix("www.")
        return d
    }
}
