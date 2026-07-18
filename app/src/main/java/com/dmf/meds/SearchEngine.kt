package com.dmf.meds

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

object SearchEngine {

    // Helper to calculate Levenshtein Distance
    fun getLevenshteinDistance(s: String, t: String): Int {
        val m = s.length
        val n = t.length
        if (m == 0) return n
        if (n == 0) return m

        var prev = IntArray(n + 1) { it }
        var curr = IntArray(n + 1)

        for (i in 1..m) {
            curr[0] = i
            val sChar = s[i - 1].lowercaseChar()
            for (j in 1..n) {
                val cost = if (sChar == t[j - 1].lowercaseChar()) 0 else 1
                curr[j] = minOf(
                    prev[j] + 1,       // deletion
                    curr[j - 1] + 1,   // insertion
                    prev[j - 1] + cost // substitution
                )
            }
            // Swap prev and curr
            val temp = prev
            prev = curr
            curr = temp
        }
        return prev[n]
    }

    // High performance suggestion matching
    suspend fun getSuggestions(
        query: String,
        medicines: List<Medicine>,
        uniqueBrands: List<String>
    ): SearchResultState = withContext(Dispatchers.Default) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            return@withContext SearchResultState.Success(emptyList())
        }

        val queryLower = trimmedQuery.lowercase(Locale.ROOT)

        // 1. Try to find direct matches (prefix / substring)
        val matches = medicines.filter { med ->
            val fullName = "${med.brand} ${med.power}".lowercase(Locale.ROOT)
            fullName.contains(queryLower)
        }

        if (matches.isNotEmpty()) {
            // Sort adaptive: prefix matches first, then substring matches
            val sortedMatches = matches.sortedWith(compareBy<Medicine> { med ->
                val brandLower = med.brand.lowercase(Locale.ROOT)
                // If brand name starts with query, highest priority (0)
                if (brandLower.startsWith(queryLower)) 0
                // If full name starts with query, next priority (1)
                else if ("${med.brand} ${med.power}".lowercase(Locale.ROOT).startsWith(queryLower)) 1
                // Otherwise substring match (2)
                else 2
            }.thenBy { it.brand }.thenBy { it.power })

            return@withContext SearchResultState.Success(sortedMatches.take(150))
        }

        // 2. If no matches found, find fuzzy suggestions based on unique brand names
        // Let's filter unique brands that are close to the query
        val fuzzyResults = uniqueBrands.map { brand ->
            val dist = getLevenshteinDistance(trimmedQuery, brand)
            brand to dist
        }
        .filter { it.second <= 3 } // Edit distance of 3 or less
        .sortedBy { it.second }
        .take(10)
        .map { it.first }

        if (fuzzyResults.isNotEmpty()) {
            // Find representative medicines for these fuzzy brand names to show as suggestions
            val fallbackMeds = mutableListOf<Medicine>()
            val addedBrands = mutableSetOf<String>()
            for (brand in fuzzyResults) {
                val reps = medicines.filter { it.brand.equals(brand, ignoreCase = true) }
                for (rep in reps) {
                    val key = "${rep.brand} ${rep.power}"
                    if (!addedBrands.contains(key)) {
                        fallbackMeds.add(rep)
                        addedBrands.add(key)
                    }
                }
            }
            return@withContext SearchResultState.Fallback(fallbackMeds.take(50))
        }

        return@withContext SearchResultState.Success(emptyList())
    }

    // Alternatives: same 'g' (generic) and 'p' (power) as selected medicine
    fun getAlternatives(selected: Medicine, medicines: List<Medicine>): List<Medicine> {
        return medicines.filter {
            it.generic.equals(selected.generic, ignoreCase = true) &&
                    it.power.equals(selected.power, ignoreCase = true) &&
                    !it.brand.equals(selected.brand, ignoreCase = true)
        }.distinctBy { it.brand }
    }

    // Other Powers: same 'g' (generic) but excluding the exact selected medicine (complete list of other medicines with same generic)
    fun getOtherPowers(selected: Medicine, medicines: List<Medicine>): List<Medicine> {
        return medicines.filter {
            it.generic.equals(selected.generic, ignoreCase = true) &&
                    !(it.brand.equals(selected.brand, ignoreCase = true) && it.power.equals(selected.power, ignoreCase = true))
        }.distinctBy { "${it.brand} ${it.power}" }
    }

    // Other Combinations: other generic names in the database where the searched medicine's generic name is present
    fun getOtherCombinations(selectedGeneric: String, allGenerics: Set<String>): List<String> {
        val selLower = selectedGeneric.lowercase(Locale.ROOT)
        return allGenerics.filter { gen ->
            val genLower = gen.lowercase(Locale.ROOT)
            genLower.contains(selLower) && !genLower.equals(selLower, ignoreCase = true)
        }.sorted()
    }
}

sealed interface SearchResultState {
    data class Success(val medicines: List<Medicine>) : SearchResultState
    data class Fallback(val medicines: List<Medicine>) : SearchResultState
}
