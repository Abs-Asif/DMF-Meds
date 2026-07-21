# Bolt's Journal

## 2026-07-21 - [Fuzzy & Direct Search Optimizations on Large Local Datasets]
**Learning:**
1. Real-time search suggestions over 25,000+ local objects on every keystroke suffer heavily from string allocation and lowercase conversion overhead. Lazily caching lowercased strings inside data class properties (using transient fields to prevent Gson/deserialization side-effects) removes this overhead completely.
2. Running the Levenshtein Distance algorithm across 16,000+ unique brand names on a mobile device is highly resource-intensive and blocks the main thread. Since we only want results with an edit distance of 3 or less, we can filter out candidates where the length difference is greater than 3. This reduces candidate brands by ~80-90% before doing any heavy computing.
3. Lowercasing strings once upfront rather than calling `.lowercaseChar()` in the inner loop of the Levenshtein nested loop dramatically reduces character manipulation overhead.

**Action:**
- In offline mobile datasets with real-time fuzzy matching, always pre-filter candidate lists using simple O(1) checks (like string length bounds) before executing heavy distance algorithms.
- Cache pre-processed search terms lazily inside model entities when loading large datasets.
