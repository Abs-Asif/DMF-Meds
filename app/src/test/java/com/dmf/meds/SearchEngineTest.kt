package com.dmf.meds

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchEngineTest {

    @Test
    fun testGetLevenshteinDistance() {
        assertEquals(0, SearchEngine.getLevenshteinDistance("Napa", "Napa"))
        assertEquals(1, SearchEngine.getLevenshteinDistance("Seklo", "Seclo"))
        assertEquals(3, SearchEngine.getLevenshteinDistance("Napa", "Napoli"))
        assertEquals(8, SearchEngine.getLevenshteinDistance("Ace", "Paracetamol"))
    }

    @Test
    fun testGetAlternatives() {
        val selected = Medicine("Napa", "500 mg", "Paracetamol", "Beximco")
        val list = listOf(
            Medicine("Napa", "500 mg", "Paracetamol", "Beximco"),
            Medicine("Napa", "120 mg/5 ml", "Paracetamol", "Beximco"), // different power
            Medicine("Ace", "500 mg", "Paracetamol", "Square"), // same power, same generic (alternative)
            Medicine("Fast", "500 mg", "Paracetamol", "Acme"), // same power, same generic (alternative)
            Medicine("Seclo", "20 mg", "Omeprazole", "SMC") // different generic
        )

        val alts = SearchEngine.getAlternatives(selected, list)
        assertEquals(2, alts.size)
        assertTrue(alts.any { it.brand == "Ace" })
        assertTrue(alts.any { it.brand == "Fast" })
    }

    @Test
    fun testGetOtherPowers() {
        val selected = Medicine("Napa", "500 mg", "Paracetamol", "Beximco")
        val list = listOf(
            Medicine("Napa", "500 mg", "Paracetamol", "Beximco"),
            Medicine("Napa", "120 mg/5 ml", "Paracetamol", "Beximco"), // same generic (different power)
            Medicine("Ace", "500 mg", "Paracetamol", "Square"), // same generic
            Medicine("Fast", "250 mg", "Paracetamol", "Acme"), // same generic
            Medicine("Seclo", "20 mg", "Omeprazole", "SMC") // different generic
        )

        val otherPowers = SearchEngine.getOtherPowers(selected, list)
        assertEquals(3, otherPowers.size)
        assertTrue(otherPowers.any { it.brand == "Napa" && it.power == "120 mg/5 ml" })
        assertTrue(otherPowers.any { it.brand == "Ace" && it.power == "500 mg" })
        assertTrue(otherPowers.any { it.brand == "Fast" && it.power == "250 mg" })
    }

    @Test
    fun testGetOtherCombinations() {
        val allGenerics = setOf(
            "Paracetamol",
            "Paracetamol + Caffeine",
            "Paracetamol + Tramadol Hydrochloride",
            "Omeprazole",
            "Sulphamethoxazole + Trimethoprim"
        )

        val combos = SearchEngine.getOtherCombinations("Paracetamol", allGenerics)
        assertEquals(2, combos.size)
        assertTrue(combos.contains("Paracetamol + Caffeine"))
        assertTrue(combos.contains("Paracetamol + Tramadol Hydrochloride"))
        assertTrue(!combos.contains("Paracetamol")) // Excluded exact match
    }
}
