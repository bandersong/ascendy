package com.ascendy.app.qr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM tests for QR payload parsing/minting. The Bitmap rendering path is covered separately
 * under Robolectric ([com.ascendy.app.qr.QrRenderTest]).
 */
class QrToolsParseTest {

    @Test fun parse_validPayload() =
        assertEquals("abc123", QrTools.parseScannedPayload("ascendy:abc123"))

    @Test fun parse_trimsSurroundingWhitespace() =
        assertEquals("abc123", QrTools.parseScannedPayload("  ascendy:abc123\n"))

    @Test fun parse_prefixOnlyIsNull() =
        assertNull(QrTools.parseScannedPayload("ascendy:"))

    @Test fun parse_noPrefixIsNull() =
        assertNull(QrTools.parseScannedPayload("https://example.com"))

    @Test fun parse_prefixIsCaseSensitive() =
        assertNull(QrTools.parseScannedPayload("ASCENDY:abc"))

    @Test fun parse_keepsColonsInsidePayload() =
        assertEquals("a:b:c", QrTools.parseScannedPayload("ascendy:a:b:c"))

    @Test fun newAnchor_payloadParsesBackToId() {
        val (id, payload) = QrTools.newAnchor()
        assertTrue("id non-blank", id.isNotBlank())
        assertTrue("payload carries prefix", payload.startsWith(QrTools.PAYLOAD_PREFIX))
        assertEquals("round-trips through parse", id, QrTools.parseScannedPayload(payload))
    }

    @Test fun newAnchor_idsAreUnique() {
        assertNotEquals(QrTools.newAnchor().first, QrTools.newAnchor().first)
    }
}
