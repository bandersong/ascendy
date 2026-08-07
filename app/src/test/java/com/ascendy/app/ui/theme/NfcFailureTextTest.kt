package com.ascendy.app.ui.theme

import com.ascendy.app.nfc.NfcFailure
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Every NFC failure must reach the user as a distinct, plain-language sentence in every theme.
 * The old behaviour was to swallow all of them, so the guard here is that the mapping stays total
 * (a new NfcFailure member fails the `when` at compile time) and stays 1:1 — a copy/paste that
 * points two reasons at the same string puts the user back to guessing.
 */
class NfcFailureTextTest {

    private val themes = mapOf("Kawaii" to KawaiiVocab, "Tough" to ToughVocab, "Neutral" to NeutralVocab)

    @Test fun everyReason_hasNonBlankTextInEveryTheme() {
        for ((themeName, v) in themes) {
            for (reason in NfcFailure.entries) {
                assertTrue(
                    "$themeName has no copy for $reason",
                    v.nfcFailureText(reason).isNotBlank()
                )
            }
        }
    }

    @Test fun reasons_mapToDistinctText() {
        for ((themeName, v) in themes) {
            val texts = NfcFailure.entries.map { v.nfcFailureText(it) }
            assertEquals(
                "$themeName reuses the same sentence for two different failures",
                NfcFailure.entries.size,
                texts.toSet().size
            )
        }
    }
}
