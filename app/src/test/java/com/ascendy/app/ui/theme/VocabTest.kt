package com.ascendy.app.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.reflect.full.memberProperties

/**
 * Guards the three-theme copy registry. The recurring hazard (per the project's own workflow note)
 * is editing one theme's strings and forgetting the other two — especially printf-style format
 * fields, where a dropped "%d"/"%s" crashes String.format at runtime. We assert every `*Fmt` field
 * carries the SAME format specifiers across Kawaii / Tough / Neutral, plus structural invariants.
 */
class VocabTest {

    private val themes = mapOf("Kawaii" to KawaiiVocab, "Tough" to ToughVocab, "Neutral" to NeutralVocab)
    private val specifier = Regex("%(?:[a-zA-Z]|%)")

    private fun stringProps() = Vocab::class.memberProperties
        .filter { it.returnType.classifier == String::class }

    @Suppress("UNCHECKED_CAST")
    private fun value(prop: kotlin.reflect.KProperty1<*, *>, instance: Vocab): String =
        (prop as kotlin.reflect.KProperty1<Vocab, String>).get(instance)

    @Test fun formatFields_haveIdenticalSpecifiersAcrossThemes() {
        val fmtProps = stringProps().filter { it.name.endsWith("Fmt") }
        assertTrue("expected several *Fmt fields", fmtProps.size >= 5)
        for (prop in fmtProps) {
            val perTheme = themes.mapValues { (_, v) -> specifier.findAll(value(prop, v)).map { it.value }.toList().sorted() }
            val reference = perTheme.getValue("Neutral")
            assertTrue("Neutral.${prop.name} should contain a format specifier", reference.isNotEmpty())
            for ((name, specs) in perTheme) {
                assertEquals("${prop.name}: $name specifiers differ from Neutral", reference, specs)
            }
        }
    }

    @Test fun formatFields_areNeverBlank() {
        for (prop in stringProps().filter { it.name.endsWith("Fmt") }) {
            for ((name, v) in themes) {
                assertTrue("${prop.name} blank in $name", value(prop, v).isNotBlank())
            }
        }
    }

    /**
     * Vocab is one constructor away from the JVM's hard 255-argument-slot limit, and blowing it
     * fails at CLASS LOAD, not compile — the app builds green and then dies with ClassFormatError
     * the first time anything reads a string. Catch it here, in words, instead of there.
     */
    @Test fun fieldCount_staysUnderJvmArgumentLimit() {
        val fields = Vocab::class.constructors.first().parameters.size
        assertTrue(
            "Vocab has $fields constructor fields; the JVM allows 254 plus the receiver. " +
                "Group related strings into a nested class instead of adding another flat field.",
            fields <= 254,
        )
    }

    @Test fun daysShort_hasSevenEntriesPerTheme() {
        for ((name, v) in themes) {
            assertEquals("$name daysShort size", 7, v.daysShort.size)
        }
    }

    @Test fun appTitle_setForEveryTheme() {
        for ((name, v) in themes) assertTrue("$name appTitle blank", v.appTitle.isNotBlank())
    }

    /**
     * NH-13: make lockstep enforceable, not just convention. EVERY String field must be non-blank in
     * EVERY theme — so a new field added to one theme but left empty (or forgotten) in another fails
     * CI here instead of shipping a blank label. The only documented exception is the `*Emoji` fields,
     * which the minimalist Neutral theme intentionally leaves empty.
     */
    /**
     * The nested [ChartVocab] holder exists to buy argument slots back (see its KDoc), which means
     * its strings are invisible to the sweeps above. Same lockstep rules, applied by hand.
     */
    @Test fun chartVocab_isCompleteAndLockstepAcrossThemes() {
        val reference = specifier.findAll(NeutralVocab.chart.summaryFmt).map { it.value }.toList().sorted()
        assertEquals("summaryFmt should take 3 args", listOf("%s", "%s", "%s"), reference)
        for ((name, v) in themes) {
            assertTrue("$name chart.summaryFmt blank", v.chart.summaryFmt.isNotBlank())
            assertTrue("$name chart.dayFmt blank", v.chart.dayFmt.isNotBlank())
            assertTrue("$name chart.empty blank", v.chart.empty.isNotBlank())
            assertEquals(
                "$name chart.summaryFmt specifiers differ from Neutral",
                reference,
                specifier.findAll(v.chart.summaryFmt).map { it.value }.toList().sorted(),
            )
            assertEquals(
                "$name chart.dayFmt should take 2 args",
                listOf("%s", "%s"),
                specifier.findAll(v.chart.dayFmt).map { it.value }.toList().sorted(),
            )
            // The real hazard is a positional mismatch that only crashes at runtime.
            v.chart.summaryFmt.format(v.chart.dayFmt.format("Mon", "45m"), "5h 0m", "42m")
        }
    }

    /**
     * Vocab's constructor is a few fields from the JVM's hard 255-argument-slot ceiling. Past it,
     * everything compiles and then dies at class load with `ClassFormatError: Too many arguments
     * in method signature` — twice now, in this campaign. `data class` also emits `copy$default`,
     * whose signature is (instance + every field + one int bitmask per 32 fields + a marker), so
     * that synthetic method hits the wall well before the constructor does. Fail here, in words,
     * instead of on-device in hieroglyphics: group new strings into a nested holder like
     * [ChartVocab] rather than adding top-level fields.
     */
    @Test fun constructorArguments_stayUnderJvmLimit() {
        val n = Vocab::class.java.constructors.first().parameterCount
        val copyDefaultSlots = 1 + n + ((n + 31) / 32) + 1
        assertTrue(
            "Vocab.copy\$default would need $copyDefaultSlots argument slots (JVM max 255) with " +
                "$n constructor fields — nest new strings in a holder instead",
            copyDefaultSlots <= 255,
        )
    }

    @Test fun everyStringField_isNonBlankInEveryTheme_exceptEmoji() {
        val emojiAllowlist: (String) -> Boolean = { it.endsWith("Emoji") }
        for (prop in stringProps()) {
            if (emojiAllowlist(prop.name)) continue
            for ((name, v) in themes) {
                assertTrue(
                    "${prop.name} is blank in $name — every theme must define it (lockstep)",
                    value(prop, v).isNotBlank(),
                )
            }
        }
    }
}
