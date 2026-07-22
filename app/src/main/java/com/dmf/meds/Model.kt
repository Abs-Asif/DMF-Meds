package com.dmf.meds

import com.google.gson.annotations.SerializedName
import java.util.Locale

data class Medicine(
    @SerializedName("b") val brand: String,
    @SerializedName("p") val power: String,
    @SerializedName("g") val generic: String,
    @SerializedName("m") val manufacturer: String
) {
    @Transient
    @Volatile
    private var _lowerBrand: String? = null

    @Transient
    @Volatile
    private var _lowerFullName: String? = null

    // Optimization: Cache lowercased values lazily to prevent redundant string creations during search,
    // safe under Gson Unsafe deserialization which bypasses initializer block.
    val lowerBrand: String
        get() = _lowerBrand ?: synchronized(this) {
            _lowerBrand ?: brand.lowercase(Locale.ROOT).also { _lowerBrand = it }
        }

    val lowerFullName: String
        get() = _lowerFullName ?: synchronized(this) {
            _lowerFullName ?: "${brand} ${power}".lowercase(Locale.ROOT).also { _lowerFullName = it }
        }
}

data class GenericMetadata(
    @SerializedName("is_allowed") val isAllowed: Boolean,
    @SerializedName("is_otc") val isOtc: Boolean,
    @SerializedName("is_antibiotic") val isAntibiotic: Boolean,
    val description: String
)
