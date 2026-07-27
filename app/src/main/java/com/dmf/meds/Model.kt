package com.dmf.meds

import com.google.gson.annotations.SerializedName
import java.util.Locale
import kotlin.jvm.Transient
import kotlin.jvm.Volatile

data class Medicine(
    @SerializedName("brand_name") val brand: String,
    @SerializedName("strength") val power: String,
    @SerializedName("generic") val generic: String,
    @SerializedName("manufacturer") val manufacturer: String
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

data class GenericDetail(
    @SerializedName("generic_name") val genericName: String,
    @SerializedName("drug_class") val drugClass: String?,
    val indication: String?,
    @SerializedName("indication_description") val indicationDescription: String?,
    @SerializedName("therapeutic_class_description") val therapeuticClassDescription: String?,
    @SerializedName("pharmacology_description") val pharmacologyDescription: String?,
    @SerializedName("dosage_description") val dosageDescription: String?,
    @SerializedName("administration_description") val administrationDescription: String?,
    @SerializedName("interaction_description") val interactionDescription: String?,
    @SerializedName("contraindications_description") val contraindicationsDescription: String?,
    @SerializedName("side_effects_description") val sideEffectsDescription: String?,
    @SerializedName("pregnancy_and_lactation_description") val pregnancyAndLactationDescription: String?,
    @SerializedName("precautions_description") val precautionsDescription: String?,
    @SerializedName("pediatric_usage_description") val pediatricUsageDescription: String?,
    @SerializedName("overdose_effects_description") val overdoseEffectsDescription: String?,
    @SerializedName("duration_of_treatment_description") val durationOfTreatmentDescription: String?,
    @SerializedName("reconstitution_description") val reconstitutionDescription: String?,
    @SerializedName("storage_conditions_description") val storageConditionsDescription: String?
)
