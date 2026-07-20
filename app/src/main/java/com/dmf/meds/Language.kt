package com.dmf.meds

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

val KalpurushFontFamily = FontFamily(
    Font(R.font.kalpurush, FontWeight.Normal)
)

fun String.containsBangla(): Boolean {
    for (char in this) {
        if (char in '\u0980'..'\u09FF') return true
    }
    return false
}

@Composable
fun DMFText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    lineHeight: TextUnit = TextUnit.Unspecified
) {
    val containsBn = text.containsBangla()
    val family = if (containsBn) KalpurushFontFamily else FontFamily.Default

    // Scale up font size and line height if the text contains Bangla
    val finalFontSize = if (containsBn && fontSize != TextUnit.Unspecified) {
        (fontSize.value * 1.25f).sp
    } else {
        fontSize
    }

    val finalLineHeight = if (containsBn && lineHeight != TextUnit.Unspecified) {
        (lineHeight.value * 1.25f).sp
    } else if (containsBn && fontSize != TextUnit.Unspecified) {
        (fontSize.value * 1.25f * 1.35f).sp
    } else {
        lineHeight
    }

    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = finalFontSize,
        fontWeight = fontWeight,
        fontFamily = family,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines,
        lineHeight = finalLineHeight
    )
}

@Composable
fun getAppFontFamily(isBangla: Boolean): FontFamily {
    return KalpurushFontFamily
}

@Composable
fun getTypography(isBangla: Boolean): Typography {
    val family = KalpurushFontFamily
    return Typography(
        displayLarge = TextStyle(fontFamily = family),
        displayMedium = TextStyle(fontFamily = family),
        displaySmall = TextStyle(fontFamily = family),
        headlineLarge = TextStyle(fontFamily = family),
        headlineMedium = TextStyle(fontFamily = family),
        headlineSmall = TextStyle(fontFamily = family),
        titleLarge = TextStyle(fontFamily = family),
        titleMedium = TextStyle(fontFamily = family),
        titleSmall = TextStyle(fontFamily = family),
        bodyLarge = TextStyle(fontFamily = family),
        bodyMedium = TextStyle(fontFamily = family),
        bodySmall = TextStyle(fontFamily = family),
        labelLarge = TextStyle(fontFamily = family),
        labelMedium = TextStyle(fontFamily = family),
        labelSmall = TextStyle(fontFamily = family)
    )
}

object Trans {
    // Tab labels
    fun medicines(isBangla: Boolean) = if (isBangla) "ঔষধসমূহ" else "Medicines"
    fun information(isBangla: Boolean) = if (isBangla) "তথ্য ডেস্ক" else "Information"
    fun fatawas(isBangla: Boolean) = if (isBangla) "ফতোয়া" else "Fatawas"

    // Search Page
    fun searchMedicines(isBangla: Boolean) = if (isBangla) "ঔষধ অনুসন্ধান" else "Search Medicines"
    fun enterBrandName(isBangla: Boolean) = if (isBangla) "ব্র্যান্ডের নাম, পাওয়ার লিখুন..." else "Enter brand name, power..."
    fun noMedicinesFound(isBangla: Boolean) = if (isBangla) "কোনো ঔষধ পাওয়া যায়নি।" else "No medicines found."
    fun searchHelp(isBangla: Boolean) = if (isBangla) {
        "প্রেসক্রিপশন সম্মতি যাচাই করতে এবং বিস্তারিত দেখতে একটি ঔষধের ব্র্যান্ডের নাম বা পাওয়ার লিখুন।"
    } else {
        "Enter a medicine brand name or power to verify compliance and view info."
    }

    // Spelling Fallback
    fun spellingFallbackTitle(isBangla: Boolean) = if (isBangla) "বানান / উচ্চারণ সংশোধন" else "Spelling / Pronunciation Fallback"
    fun spellingFallbackText(isBangla: Boolean) = if (isBangla) {
        "আপনি হয়তো ঔষধের নামটি ভুল টাইপ করেছেন। হয়তো আপনি নিচের কোনো একটি খুঁজছেন:"
    } else {
        "You might have typed the medicine name incorrectly. Perhaps you were searching for one of the following:"
    }

    // Medicine Details Page
    fun genericNameLabel(isBangla: Boolean) = if (isBangla) "জেনেরিক নাম" else "Generic Name"
    fun noDescription(isBangla: Boolean) = if (isBangla) "কোনো বিবরণ উপলব্ধ নেই।" else "No description available."
    fun manufacturerLabel(isBangla: Boolean) = if (isBangla) "প্রস্তুতকারক" else "Manufacturer"
    fun prescriptionAssessment(isBangla: Boolean) = if (isBangla) "প্রেসক্রিপশন মূল্যায়ন" else "Prescription Assessment"
    fun allowanceStatus(isBangla: Boolean) = if (isBangla) "অনুমতি স্থিতি" else "Allowance Status"
    fun allowedYes(isBangla: Boolean) = if (isBangla) "হ্যাঁ" else "YES"
    fun allowedNo(isBangla: Boolean) = if (isBangla) "না" else "NO"
    fun allowedSubtextYes(isBangla: Boolean) = if (isBangla) {
        "ডিএমএফ / ম্যাটস চিকিৎসকদের এই ঔষধটি প্রেসক্রিপশন করার অনুমতি রয়েছে।"
    } else {
        "DMF / MATS practitioners are allowed to prescribe this medicine."
    }
    fun allowedSubtextNo(isBangla: Boolean) = if (isBangla) {
        "প্রেসক্রিপশন করার অনুমতি নেই। এই ঔষধটিতে এমন জেনেরিক রয়েছে যা অনুমোদিত তালিকায় নেই।"
    } else {
        "NOT allowed to prescribe. This medicine contains generics not listed in the approved prescription lists."
    }
    fun otcStatus(isBangla: Boolean) = if (isBangla) "ওটিসি স্থিতি" else "OTC Status"
    fun otcSubtext(isBangla: Boolean) = if (isBangla) {
        "এটি একটি ওভার-দ্য-কাউন্টার (ওটিসি) ঔষধ। ডিএমএফ চিকিৎসকদের এটি প্রেসক্রাইব করার অনুমতি রয়েছে।"
    } else {
        "This is an Over-The-Counter (OTC) medicine. DMF practitioners are permitted to prescribe it by default."
    }
    fun antibioticWarning(isBangla: Boolean) = if (isBangla) "অ্যান্টিবায়োটিক সতর্কতা" else "Antibiotic Warning"
    fun antibioticSubtext(isBangla: Boolean) = if (isBangla) {
        "সতর্কতা: এটি একটি অ্যান্টিবায়োটিক। সর্বোচ্চ সতর্কতা অবলম্বন করুন এবং অ্যান্টিবায়োটিক ব্যবহারের নির্দেশিকা অনুসরণ করুন।"
    } else {
        "Warning: This is an Antibiotic. Exercise maximum caution and follow antibiotic stewardship guidelines."
    }

    fun showAlternatives(isBangla: Boolean) = if (isBangla) "বিকল্প ঔষধসমূহ" else "Show Alternatives"
    fun noAlternatives(isBangla: Boolean) = if (isBangla) "একই পাওয়ারের কোনো বিকল্প ঔষধ পাওয়া যায়নি।" else "No alternatives found with the same power."
    fun otherPowers(isBangla: Boolean) = if (isBangla) "অন্যান্য পাওয়ার" else "Other Powers"
    fun noOtherPowers(isBangla: Boolean) = if (isBangla) "অন্যান্য পাওয়ার উপলব্ধ নেই।" else "No other powers available."
    fun otherCombinations(isBangla: Boolean) = if (isBangla) "অন্যান্য কম্বিনেশন" else "Other Combinations"
    fun noOtherCombos(isBangla: Boolean) = if (isBangla) "এই ড্রাগ ধারণকারী অন্য কোনো কম্বিনেশন জেনেরিক নেই।" else "No other combination generics containing this drug."
    fun searchBrandPower(isBangla: Boolean) = if (isBangla) "ব্র্যান্ডের নাম বা পাওয়ার খুঁজুন..." else "Search brand name or power..."
    fun closeLabel(isBangla: Boolean) = if (isBangla) "বন্ধ করুন" else "Close"

    // Information Screen
    fun infoDesk(isBangla: Boolean) = if (isBangla) "তথ্য ডেস্ক" else "Information Desk"
    fun infoSubtext(isBangla: Boolean) = if (isBangla) {
        "অফিসিয়াল রিসোর্স, প্রবিধান এবং নিবন্ধন রেজিস্ট্রিগুলোতে দ্রুত অ্যাক্সেস।"
    } else {
        "Quick access to official resources, regulations, and registration registries."
    }
    fun bmdcCheckTitle(isBangla: Boolean) = if (isBangla) "বিএমডিসি যাচাই" else "BM&DC Check"
    fun bmdcCheckDesc(isBangla: Boolean) = if (isBangla) {
        "ম্যাটস / ডিএমএফ চিকিৎসকদের নিবন্ধন অবস্থা অনুসন্ধান করুন"
    } else {
        "Search MATS / DMF practitioner registration status"
    }
    fun drugActTitle(isBangla: Boolean) = if (isBangla) "ঔষধ ও কসমেটিকস আইন ২০২৩" else "Drug & Cosmetic Act 2023"
    fun drugActDesc(isBangla: Boolean) = if (isBangla) {
        "ঔষধ ও কসমেটিকস আইনের ধারা ও নিবন্ধসমূহ"
    } else {
        "Drug and Cosmetic Act 2023"
    }
    fun approvedListTitle(isBangla: Boolean) = if (isBangla) "অনুমোদিত ঔষধের তালিকা" else "Approved Drug List"
    fun approvedListDesc(isBangla: Boolean) = if (isBangla) {
        "মেডিকেল অ্যাসিস্ট্যান্টদের জন্য বিএমডিসির অনুমোদিত ঔষধের তালিকা"
    } else {
        "Official BM&DC list of medicines for medical assistants"
    }
    fun otcListTitle(isBangla: Boolean) = if (isBangla) "ওটিসি ঔষধের তালিকা" else "OTC Drug List"
    fun otcListDesc(isBangla: Boolean) = if (isBangla) {
        "বিএমডিসি অনুমোদিত ওভার-দ্য-কাউন্টার ঔষধের তালিকা"
    } else {
        "BM&DC approved Over-The-Counter drugs list"
    }

    // Exit confirmation
    fun exitTitle(isBangla: Boolean) = if (isBangla) "প্রস্থান নিশ্চিত করুন" else "Confirm Exit"
    fun exitText(isBangla: Boolean) = if (isBangla) {
        "আপনি কি নিশ্চিত যে আপনি অ্যাপটি থেকে প্রস্থান করতে চান?"
    } else {
        "Are you sure you want to exit the app?"
    }
    fun yesLabel(isBangla: Boolean) = if (isBangla) "হ্যাঁ" else "Yes"
    fun noLabel(isBangla: Boolean) = if (isBangla) "না" else "No"

    // Settings
    fun settingsTitle(isBangla: Boolean) = if (isBangla) "সেটিংস" else "Settings"
    fun themeLabel(isBangla: Boolean) = if (isBangla) "অ্যাপ থিম" else "Theme"
    fun languageLabel(isBangla: Boolean) = if (isBangla) "অ্যাপের ভাষা" else "App Language"
    fun lightModeLabel(isBangla: Boolean) = if (isBangla) "দিনের মোড" else "Light Mode"
    fun darkModeLabel(isBangla: Boolean) = if (isBangla) "রাতের মোড" else "Dark Mode"
}
