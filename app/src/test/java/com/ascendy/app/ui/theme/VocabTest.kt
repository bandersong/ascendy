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
            val perTheme = themes.mapValues { (_, v) -> specifier.findAll(value(prop, v)).map { it.value }.sorted() }
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

    @Test fun daysShort_hasSevenEntriesPerTheme() {
        for ((name, v) in themes) {
            assertEquals("$name daysShort size", 7, v.daysShort.size)
        }
    }

    @Test fun appTitle_setForEveryTheme() {
        for ((name, v) in themes) assertTrue("$name appTitle blank", v.appTitle.isNotBlank())
    }
}
