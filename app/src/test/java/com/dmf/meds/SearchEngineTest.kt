package com.dmf.meds

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileReader

class SearchEngineTest {

    @Test
    fun testSearchSuggestionsBenchmark() = runBlocking {
        var file = File("src/main/assets/medic_data.json")
        if (!file.exists()) {
            file = File("app/src/main/assets/medic_data.json")
        }
        if (!file.exists()) {
            println("medic_data.json not found in test context, skipping benchmark.")
            return@runBlocking
        }

        val gson = Gson()
        val reader = FileReader(file)
        val medicType = object : TypeToken<List<Medicine>>() {}.type
        val medicines: List<Medicine> = gson.fromJson(reader, medicType)
        reader.close()

        val uniqueBrands = medicines.map { it.brand }.distinct().sorted()

        println("Loaded ${medicines.size} medicines with ${uniqueBrands.size} unique brands for benchmarking.")

        // 1. Direct search benchmark (matches exist)
        val startTimeDirect = System.nanoTime()
        val directResult = SearchEngine.getSuggestions("Napa", medicines, uniqueBrands)
        val durationDirectMs = (System.nanoTime() - startTimeDirect) / 1_000_000.0
        println("Direct query 'Napa' took $durationDirectMs ms")
        assertTrue(directResult is SearchResultState.Success)

        // 2. Fuzzy search benchmark (fallback)
        val startTimeFuzzy = System.nanoTime()
        val fuzzyResult = SearchEngine.getSuggestions("Nopaxz", medicines, uniqueBrands)
        val durationFuzzyMs = (System.nanoTime() - startTimeFuzzy) / 1_000_000.0
        println("Fuzzy query 'Nopaxz' took $durationFuzzyMs ms")
        assertTrue(fuzzyResult is SearchResultState.Fallback)
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
