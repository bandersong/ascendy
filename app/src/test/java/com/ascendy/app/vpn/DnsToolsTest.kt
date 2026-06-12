package com.ascendy.app.vpn

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM tests for the hand-rolled DNS/IP packet code. No Android deps — runs in the fast
 * unit-test job. These checksums/parsers are the highest-risk code in the VPN sinkhole because
 * a single off-by-one corrupts every blocked-domain response, so we exercise the edges hard.
 */
class DnsToolsTest {

    /** Build a minimal DNS query message (header + QNAME + QTYPE/QCLASS) for [name]. */
    private fun query(name: String, id: Int = 0x1234): ByteArray {
        val labels = name.split(".").filter { it.isNotEmpty() }
        val body = ArrayList<Byte>()
        fun b(v: Int) = body.add(v.toByte())
        // 12-byte header: id, flags(RD=1), QDCOUNT=1, others 0
        b(id ushr 8); b(id and 0xFF)
        b(0x01); b(0x00)          // RD=1
        b(0x00); b(0x01)          // QDCOUNT=1
        b(0x00); b(0x00)          // ANCOUNT
        b(0x00); b(0x00)          // NSCOUNT
        b(0x00); b(0x00)          // ARCOUNT
        for (label in labels) {
            b(label.length)
            label.forEach { b(it.code) }
        }
        b(0x00)                   // root
        b(0x00); b(0x01)          // QTYPE=A
        b(0x00); b(0x01)          // QCLASS=IN
        return body.toByteArray()
    }

    @Test fun parseQName_basic() {
        assertEquals("example.com", DnsTools.parseQName(query("example.com")))
    }

    @Test fun parseQName_lowercases() {
        assertEquals("example.com", DnsTools.parseQName(query("ExAmPle.COM")))
    }

    @Test fun parseQName_subdomain() {
        assertEquals("a.b.c.example.com", DnsTools.parseQName(query("a.b.c.example.com")))
    }

    @Test fun parseQName_rootQueryIsNull() {
        // Header + single zero length byte = root. Should be null, not "".
        val root = byteArrayOf(0,0,1,0,0,1,0,0,0,0,0,0, 0, 0,1,0,1)
        assertNull(DnsTools.parseQName(root))
    }

    @Test fun parseQName_tooShortIsNull() {
        assertNull(DnsTools.parseQName(ByteArray(5)))
    }

    @Test fun parseQName_compressionPointerBails() {
        // Label length byte with 0xC0 bits set = a pointer, illegal in a question — must bail.
        val q = byteArrayOf(0,0,1,0,0,1,0,0,0,0,0,0, 0xC0.toByte(), 0x0C, 0,1,0,1)
        assertNull(DnsTools.parseQName(q))
    }

    @Test fun parseQName_truncatedLabelIsNull() {
        // Claims a 5-byte label but the buffer ends early.
        val q = byteArrayOf(0,0,1,0,0,1,0,0,0,0,0,0, 0x05, 'a'.code.toByte(), 'b'.code.toByte())
        assertNull(DnsTools.parseQName(q))
    }

    @Test fun nxdomain_setsResponseAndRcode() {
        val q = query("blocked.example")
        val r = DnsTools.nxdomainResponse(q)
        assertEquals("id preserved", q[0], r[0])
        assertEquals("id preserved", q[1], r[1])
        assertTrue("QR bit set", r[2].toInt() and 0x80 != 0)
        assertEquals("RD copied", q[2].toInt() and 0x01, r[2].toInt() and 0x01)
        assertTrue("RA bit set", r[3].toInt() and 0x80 != 0)
        assertEquals("RCODE=NXDOMAIN(3)", 3, r[3].toInt() and 0x0F)
        // ANCOUNT / NSCOUNT / ARCOUNT all zeroed
        assertEquals(0, r[6].toInt()); assertEquals(0, r[7].toInt())
        assertEquals(0, r[8].toInt()); assertEquals(0, r[9].toInt())
        assertEquals(0, r[10].toInt()); assertEquals(0, r[11].toInt())
    }

    @Test fun nxdomain_tooShortReturnedUnchanged() {
        val short = ByteArray(4)
        assertTrue(short.contentEquals(DnsTools.nxdomainResponse(short)))
    }

    @Test fun ipv4Packet_headerChecksumValid() {
        val pkt = DnsTools.buildIpv4UdpPacket(
            srcIp = byteArrayOf(10,0,0,1), dstIp = byteArrayOf(10,0,0,2),
            srcPort = 53, dstPort = 12345, payload = byteArrayOf(1,2,3,4),
        )
        assertEquals("totalLen", 20 + 8 + 4, pkt.size)
        assertEquals("version/IHL", 0x45, pkt[0].toInt() and 0xFF)
        assertEquals("protocol UDP", 17, pkt[9].toInt() and 0xFF)
        // A valid IPv4 header sums (with carry) to 0xFFFF over its 20 bytes incl. checksum field.
        assertEquals(0xFFFF, onesComplementSum(pkt, 0, 20))
    }

    @Test fun ipv6Packet_udpChecksumMandatoryAndValid() {
        val src = ByteArray(16) { (it + 1).toByte() }
        val dst = ByteArray(16) { (16 - it).toByte() }
        val pkt = DnsTools.buildIpv6UdpPacket(src, dst, 53, 9999, byteArrayOf(9,8,7))
        assertEquals("version 6", 0x60, pkt[0].toInt() and 0xF0)
        assertEquals("next header UDP", 17, pkt[6].toInt() and 0xFF)
        // IPv6 forbids a zero UDP checksum.
        assertTrue("checksum non-zero", (pkt[46].toInt() and 0xFF) or (pkt[47].toInt() and 0xFF) != 0)
        // Verify: pseudo-header + UDP segment incl. stored checksum sums to 0xFFFF.
        val udpLen = 8 + 3
        var sum = 0
        fun add(v: Int) { sum += v and 0xFFFF; sum = (sum and 0xFFFF) + (sum ushr 16) }
        for (i in 0 until 16 step 2) add(((src[i].toInt() and 0xFF) shl 8) or (src[i+1].toInt() and 0xFF))
        for (i in 0 until 16 step 2) add(((dst[i].toInt() and 0xFF) shl 8) or (dst[i+1].toInt() and 0xFF))
        add(udpLen); add(17)
        var i = 40
        while (i + 1 < 40 + udpLen) { add(((pkt[i].toInt() and 0xFF) shl 8) or (pkt[i+1].toInt() and 0xFF)); i += 2 }
        if (i < 40 + udpLen) add((pkt[i].toInt() and 0xFF) shl 8)
        assertEquals(0xFFFF, sum and 0xFFFF)
    }

    /** One's-complement 16-bit sum with end-around carry — yields 0xFFFF over a valid header. */
    private fun onesComplementSum(buf: ByteArray, start: Int, len: Int): Int {
        var sum = 0
        var i = start
        val end = start + len
        while (i + 1 < end) { sum += ((buf[i].toInt() and 0xFF) shl 8) or (buf[i+1].toInt() and 0xFF); sum = (sum and 0xFFFF) + (sum ushr 16); i += 2 }
        if (i < end) { sum += (buf[i].toInt() and 0xFF) shl 8; sum = (sum and 0xFFFF) + (sum ushr 16) }
        return sum and 0xFFFF
    }

    // ── NH-09: upstream resolver selection (system DNS first, public fallback last) ──

    private fun addr(s: String): java.net.InetAddress = java.net.InetAddress.getByName(s)
    private val fallbacks = listOf(addr("1.1.1.1"), addr("2606:4700:4700::1111"))
    private val sinkholes = setOf(addr("10.10.10.10"), addr("fd00:1::10"))

    @Test fun upstreamCandidates_systemDnsComesFirst() {
        val out = DnsTools.upstreamCandidates(listOf(addr("192.168.1.1")), fallbacks, sinkholes)
        assertEquals(listOf(addr("192.168.1.1"), addr("1.1.1.1"), addr("2606:4700:4700::1111")), out)
    }

    @Test fun upstreamCandidates_emptySystemList_isFallbackOnly() {
        assertEquals(fallbacks, DnsTools.upstreamCandidates(emptyList(), fallbacks, sinkholes))
    }

    @Test fun upstreamCandidates_neverPicksOwnSinkhole() {
        // If the VPN-exclusion of our package failed, the "system" DNS is our own fake server —
        // forwarding there would loop the query into ourselves forever.
        val out = DnsTools.upstreamCandidates(
            listOf(addr("10.10.10.10"), addr("fd00:1::10")), fallbacks, sinkholes,
        )
        assertEquals(fallbacks, out)
    }

    @Test fun upstreamCandidates_dropsLoopbackAndWildcard() {
        val out = DnsTools.upstreamCandidates(
            listOf(addr("127.0.0.1"), addr("0.0.0.0"), addr("9.9.9.9")), fallbacks, sinkholes,
        )
        assertEquals(addr("9.9.9.9"), out.first())
        assertTrue("no loopback", out.none { it.isLoopbackAddress })
    }

    @Test fun upstreamCandidates_capsAtThreeAndDedups() {
        val out = DnsTools.upstreamCandidates(
            listOf(addr("1.1.1.1"), addr("8.8.8.8"), addr("8.8.4.4"), addr("9.9.9.9")),
            fallbacks, sinkholes,
        )
        assertEquals("cap bounds the worst-case sequential timeout", 3, out.size)
        assertEquals("dedup: 1.1.1.1 appears once", 1, out.count { it == addr("1.1.1.1") })
        // take(2) of system list, then fallbacks fill the remainder
        assertEquals(listOf(addr("1.1.1.1"), addr("8.8.8.8"), addr("2606:4700:4700::1111")), out)
    }
}
