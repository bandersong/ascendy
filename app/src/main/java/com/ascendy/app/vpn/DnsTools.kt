package com.ascendy.app.vpn

/**
 * Minimal helpers for DNS message parsing + IPv4/UDP/DNS packet construction.
 * Hand-rolled to avoid pulling in a heavy DNS library.
 */
object DnsTools {

    /** Parse the QNAME (lowercased, no trailing dot) out of a DNS query message. */
    fun parseQName(dns: ByteArray): String? {
        if (dns.size < 13) return null
        var i = 12   // skip 12-byte header
        val sb = StringBuilder()
        while (i < dns.size) {
            val len = dns[i].toInt() and 0xFF
            if (len == 0) break
            if (len and 0xC0 != 0) return null      // pointer in a query — bail
            i++
            if (i + len > dns.size) return null
            if (sb.isNotEmpty()) sb.append('.')
            for (k in 0 until len) {
                val c = dns[i + k].toInt() and 0xFF
                sb.append(c.toChar())
            }
            i += len
        }
        return sb.toString().lowercase().ifEmpty { null }
    }

    /** Build an NXDOMAIN response for an inbound DNS query (echoes the question section). */
    fun nxdomainResponse(query: ByteArray): ByteArray {
        if (query.size < 12) return query
        val out = query.copyOf()
        // QR=1, RCODE=3 (NXDOMAIN). Keep AA=0, TC=0, RD copied from query, RA=1.
        val rdBit = query[2].toInt() and 0x01
        out[2] = (0x80 or rdBit).toByte()          // QR=1, OPCODE=0, AA=0, TC=0, RD copied
        out[3] = (0x80 or 0x03).toByte()           // RA=1, RCODE=NXDOMAIN
        // ANCOUNT, NSCOUNT, ARCOUNT all 0
        out[6] = 0; out[7] = 0
        out[8] = 0; out[9] = 0
        out[10] = 0; out[11] = 0
        return out
    }

    /** Wrap a UDP payload in IPv4+UDP headers with computed checksums. */
    fun buildIpv4UdpPacket(
        srcIp: ByteArray, dstIp: ByteArray,
        srcPort: Int, dstPort: Int,
        payload: ByteArray,
    ): ByteArray {
        val udpLen = 8 + payload.size
        val totalLen = 20 + udpLen
        val packet = ByteArray(totalLen)

        // ── IPv4 header ──
        packet[0] = 0x45              // version 4, IHL 5
        packet[1] = 0                 // DSCP/ECN
        packet[2] = (totalLen ushr 8 and 0xFF).toByte()
        packet[3] = (totalLen and 0xFF).toByte()
        packet[4] = 0; packet[5] = 0  // identification
        packet[6] = 0x40              // don't-fragment flag, frag offset 0
        packet[7] = 0
        packet[8] = 64                // TTL
        packet[9] = 17                // protocol UDP
        // checksum placeholder at [10..11]
        System.arraycopy(srcIp, 0, packet, 12, 4)
        System.arraycopy(dstIp, 0, packet, 16, 4)
        val ipChk = ipChecksum(packet, 0, 20)
        packet[10] = (ipChk ushr 8 and 0xFF).toByte()
        packet[11] = (ipChk and 0xFF).toByte()

        // ── UDP header ──
        packet[20] = (srcPort ushr 8 and 0xFF).toByte()
        packet[21] = (srcPort and 0xFF).toByte()
        packet[22] = (dstPort ushr 8 and 0xFF).toByte()
        packet[23] = (dstPort and 0xFF).toByte()
        packet[24] = (udpLen ushr 8 and 0xFF).toByte()
        packet[25] = (udpLen and 0xFF).toByte()
        // checksum at [26..27] — we leave it 0 (IPv4 allows this for UDP)

        System.arraycopy(payload, 0, packet, 28, payload.size)
        return packet
    }

    private fun ipChecksum(buf: ByteArray, start: Int, len: Int): Int {
        var sum = 0
        var i = start
        val end = start + len
        while (i + 1 < end) {
            val word = ((buf[i].toInt() and 0xFF) shl 8) or (buf[i + 1].toInt() and 0xFF)
            sum += word
            if (sum and 0x10000 != 0) sum = (sum and 0xFFFF) + 1
            i += 2
        }
        if (i < end) {
            val word = (buf[i].toInt() and 0xFF) shl 8
            sum += word
            if (sum and 0x10000 != 0) sum = (sum and 0xFFFF) + 1
        }
        return (sum.inv() and 0xFFFF)
    }
}
