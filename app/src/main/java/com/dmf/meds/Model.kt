package com.dmf.meds

import com.google.gson.annotations.SerializedName

data class Medicine(
    @SerializedName("b") val brand: String,
    @SerializedName("p") val power: String,
    @SerializedName("g") val generic: String,
    @SerializedName("m") val manufacturer: String
)

data class GenericMetadata(
    @SerializedName("is_allowed") val isAllowed: Boolean,
    @SerializedName("is_otc") val isOtc: Boolean,
    @SerializedName("is_antibiotic") val isAntibiotic: Boolean,
    val description: String
)
