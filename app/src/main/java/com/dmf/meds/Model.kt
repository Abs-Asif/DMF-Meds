package com.dmf.meds

import com.google.gson.annotations.SerializedName

import java.util.Locale

data class Medicine(
    @SerializedName("b") val brand: String,
    @SerializedName("p") val power: String,
    @SerializedName("g") val generic: String,
    @SerializedName("m") val manufacturer: String
) {
    // Thread-safe and Gson-safe lazy caching of lowercased strings
    @Transient
    private var _fullNameLower: String? = null

    val fullNameLower: String
        get() {
            var result = _fullNameLower
            if (result == null) {
                result = "$brand $power".lowercase(Locale.ROOT)
                _fullNameLower = result
            }
            return result
        }

    @Transient
    private var _brandLower: String? = null

    val brandLower: String
        get() {
            var result = _brandLower
            if (result == null) {
                result = brand.lowercase(Locale.ROOT)
                _brandLower = result
            }
            return result
        }
}

data class GenericMetadata(
    @SerializedName("is_allowed") val isAllowed: Boolean,
    @SerializedName("is_otc") val isOtc: Boolean,
    @SerializedName("is_antibiotic") val isAntibiotic: Boolean,
    val description: String
)
