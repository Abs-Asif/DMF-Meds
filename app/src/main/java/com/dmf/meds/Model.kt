package com.dmf.meds

import com.google.gson.annotations.SerializedName
import java.util.Locale
import kotlin.jvm.Transient
import kotlin.jvm.Volatile

data class Medicine(
    @SerializedName("b") val brand: String,
    @SerializedName("p") val power: String,
    @SerializedName("g") val generic: String,
    @SerializedName("m") val manufacturer: String
) {
    @Transient
    @Volatile
    private var _lowerFullName: String? = null

    val lowerFullName: String
        get() {
            var result = _lowerFullName
            if (result == null) {
                synchronized(this) {
                    result = _lowerFullName
                    if (result == null) {
                        result = "${brand} ${power}".lowercase(Locale.ROOT)
                        _lowerFullName = result
                    }
                }
            }
            return result!!
        }

    @Transient
    @Volatile
    private var _lowerBrand: String? = null

    val lowerBrand: String
        get() {
            var result = _lowerBrand
            if (result == null) {
                synchronized(this) {
                    result = _lowerBrand
                    if (result == null) {
                        result = brand.lowercase(Locale.ROOT)
                        _lowerBrand = result
                    }
                }
            }
            return result!!
        }
}

data class GenericMetadata(
    @SerializedName("is_allowed") val isAllowed: Boolean,
    @SerializedName("is_otc") val isOtc: Boolean,
    @SerializedName("is_antibiotic") val isAntibiotic: Boolean,
    val description: String
)
