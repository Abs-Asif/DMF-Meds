package com.dmf.meds

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchEngineTest {

    @Test
    fun testGetSuggestions() = runBlocking {
        val medicines = listOf(
            Medicine("Napa", "500 mg", "Paracetamol", "Beximco"),
            Medicine("Seclo", "20 mg", "Omeprazole", "SMC")
        )
        val uniqueBrands = listOf("Napa", "Seclo")

        val result = SearchEngine.getSuggestions("nap", medicines, uniqueBrands)
        assertTrue(result is SearchResultState.Success)
        val meds = (result as SearchResultState.Success).medicines
        assertEquals(1, meds.size)
        assertEquals("Napa", meds[0].brand)
    }

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
            Medicine("Napa", "500 mg", "Paracetamol", "Beximco", packSizeInfo = "(6 x 10: ৳ 720.00)"), // unit price = 12.00
            Medicine("Napa", "120 mg/5 ml", "Paracetamol", "Beximco"), // different power
            Medicine("Ace", "500 mg", "Paracetamol", "Square", packSizeInfo = "(10 x 10: ৳ 150.00)"), // same power, same generic (alternative) - unit price = 1.50 (cheaper)
            Medicine("Fast", "500 mg", "Paracetamol", "Acme", packSizeInfo = "(10 x 10: ৳ 800.00)"), // same power, same generic (alternative) - unit price = 8.00 (more expensive)
            Medicine("Seclo", "20 mg", "Omeprazole", "SMC") // different generic
        )

        val alts = SearchEngine.getAlternatives(selected, list)
        assertEquals(2, alts.size)
        // Check order is cheap to expensive (Ace = 1.50, Fast = 8.00)
        assertEquals("Ace", alts[0].brand)
        assertEquals("Fast", alts[1].brand)
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

    @Test
    fun testParsePriceCalculation() {
        val res1 = parsePriceCalculation("(6 x 10: ৳ 720.00)")
        assertEquals(1, res1.size)
        assertEquals("6 x 10", res1[0].packText)
        assertEquals(60, res1[0].quantity)
        assertEquals(720.00, res1[0].totalPrice, 0.001)
        assertEquals(12.00, res1[0].unitPrice, 0.001)

        val res2 = parsePriceCalculation("(12's pack: ৳ 420.00)")
        assertEquals(1, res2.size)
        assertEquals("12's pack", res2[0].packText)
        assertEquals(12, res2[0].quantity)
        assertEquals(420.00, res2[0].totalPrice, 0.001)
        assertEquals(35.00, res2[0].unitPrice, 0.001)

        val res3 = parsePriceCalculation("(5's pack: ৳ 4,475.00),(5's pack: ৳ 6,750.00)")
        assertEquals(2, res3.size)
        assertEquals(5, res3[0].quantity)
        assertEquals(4475.00, res3[0].totalPrice, 0.001)
        assertEquals(895.00, res3[0].unitPrice, 0.001)
        assertEquals(5, res3[1].quantity)
        assertEquals(6750.00, res3[1].totalPrice, 0.001)
        assertEquals(1350.00, res3[1].unitPrice, 0.001)
    }

    @Test
    fun testGetBanglaIndication() {
        // Test an OTC drug (Oral Rehydration Salt)
        val indicationORS = Indications.getBanglaIndication("Oral Rehydration Salt")
        assertTrue(indicationORS.contains("ডায়রিয়া"))

        // Test a WHO essential drug (Abiraterone)
        val indicationAbiraterone = Indications.getBanglaIndication("Abiraterone")
        assertTrue(indicationAbiraterone.contains("ক্যান্সার"))

        // Test fallback
        val fallback = Indications.getBanglaIndication("Unknown Drug X")
        assertTrue(fallback.contains("শারীরিক উপসর্গ"))
    }
}
