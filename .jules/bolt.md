## 2025-01-24 - [Levenshtein Pre-Filtering and Sequential String Allocation Avoidance]
**Learning:**
1. Calculating Levenshtein Distance is computationally expensive ($O(M \times N)$). When matching against large arrays of unique names, we can mathematically guarantee that any candidate with a length difference of more than 3 cannot have a Levenshtein distance $\le 3$. Pre-filtering candidates using `Math.abs(brand.length - queryLength) <= 3` reduces the Levenshtein overhead by over 95%.
2. Performing substring, trim, and lowercase operations on every line of a 191,000+ line CSV inside a tight loop creates massive GC pressure and CPU throttling on mobile devices. Creating a flat $O(1)$ lookup set of relevant active ingredients allows us to immediately skip parsing and allocation for non-relevant lines, transforming the sequential search runtime from seconds to milliseconds.

**Action:**
- Always pre-filter lists by length before running Levenshtein comparisons.
- Always use $O(1)$ set/hash lookups to early-exit from parsing loops when scanning large text datasets sequentially.

## 2025-01-25 - [Double-Checked Volatile Lazy Cache to Bypass Gson Unsafe Deserialization]
**Learning:**
1. Doing string formatting and lowercase transformations within a tight, interactive filter loop on 20,000+ items triggers massive GC pressure (~40,000 object allocations per keystroke), leading to UI jank and lags.
2. In Kotlin projects using Gson for deserializing models without zero-argument constructors, Gson utilizes JVM `Unsafe` allocation, completely bypassing normal property initializers inside the class body. This means any field initialized in the class body can result in `NullPointerException`s if referenced.
3. Using thread-safe, double-checked locked volatile lazy properties ensures zero GC pressure and avoids Unsafe-deserialization-related crashes, as the fields naturally default to null and initialize safely upon first lookup.

**Action:**
- Cache expensive, frequently queried fields on model classes using transient volatile backing fields with synchronized check-and-load getters to support Gson unsafe deserialization.
