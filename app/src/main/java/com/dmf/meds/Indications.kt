package com.dmf.meds

import java.util.Locale

object Indications {

    /**
     * Gets a beautifully enriched Bangla description (Indication) for a generic/drug name based on keyword rules.
     */
    fun getBanglaIndication(drugName: String): String {
        val nameLower = drugName.lowercase(Locale.ROOT)

        return when {
            // 1. Analgesics / Antipyretics
            containsAny(nameLower, "paracetamol", "acetaminophen", "aspirin", "diclofenac", "ibuprofen", "salicylate", "naproxen") -> {
                "সাধারণ জ্বর, তীব্র মাথা ব্যাথা, শরীর ব্যাথা, হাড় ও জয়েন্টের ব্যাথা এবং বাতের প্রদাহ উপশমে ব্যবহৃত অত্যন্ত কার্যকরী ব্যাথানাশক।"
            }
            // 2. Antacids / PPI / Gastric
            containsAny(nameLower, "antacid", "omeprazole", "ranitidine", "aluminium", "magnesium hydroxide", "esomeprazole", "pantoprazole", "lansoprazole", "famotidine") -> {
                "অতিরিক্ত অম্লতা (এসিডিটি), বুক জ্বালাপোড়া, গ্যাস্ট্রাইটিস, বুক চিবানো এবং পেপটিক আলসার উপশম ও চিকিৎসায় নির্দেশিত।"
            }
            // 3. Antibiotics
            containsAny(nameLower, "amoxicillin", "ampicillin", "cloxacillin", "co-trimoxazole", "doxycycline", "phenoxymethylpenicillin", "penicillin", "tetracycline", "oxytetracycline", "flucloxacillin", "cef", "mycin", "ciprofloxacin", "chloramphenicol", "gentamicin", "gentamycin", "neomycin", "bacitracin") -> {
                "ব্যাকটেরিয়াজনিত সংক্রমণ (যেমন: ফুসফুস ও শ্বাসতন্ত্র, ত্বক, কান-গলা, মূত্রনালী) চিকিৎসায় নির্দেশিত অত্যন্ত কার্যকরী অ্যান্টিবায়োটিক।"
            }
            // 4. Anthelmintic
            containsAny(nameLower, "albendazole", "mebendazole", "pyrantel") -> {
                "পাকস্থলী ও অন্ত্রের বিভিন্ন ক্ষতিকর গোলকৃমি, ফিতাকৃমি বা সুতাকৃমি ধ্বংস ও কৃমিজনিত রোগ নিরাময়ে ব্যবহৃত অত্যন্ত নিরাপদ কৃমিনাশক।"
            }
            // 5. Vitamins & Minerals
            containsAny(nameLower, "calcium", "ferrous", "fumarate", "gluconate", "iron", "folic acid", "multivitamin", "riboflavin", "vitamin", "ascorbic acid", "zinc") -> {
                "শরীরের রোগ প্রতিরোধ ক্ষমতা বৃদ্ধি, পুষ্টির ঘাটতি পূরণ, রক্তস্বল্পতা বা অ্যানিমিয়া দূরীকরণ এবং হাড় ও সামগ্রিক স্বাস্থ্য সুরক্ষায় ব্যবহৃত সম্পূরক।"
            }
            // 6. Antiseptics & Disinfectants
            containsAny(nameLower, "benzyl benzoate", "chlorhexidine", "chloroxylenol", "gentian violet", "povidone iodine", "povidone-iodine", "permethrin", "potassium permanganate", "silver sulfadiazine", "silver sulphadiazine") -> {
                "ত্বকের কাটাছেঁড়া, খোসপাঁচড়া, পোড়া অংশ এবং যেকোনো ক্ষতের জীবাণু ধ্বংস ও বাহ্যিক সংক্রমণ প্রতিরোধে ব্যবহৃত জীবাণুনাশক।"
            }
            // 7. Antihistamines & Allergy
            containsAny(nameLower, "chlorpheniramine", "promethazine", "dextromethorphan", "cetirizine", "levocetirizine", "fexofenadine") -> {
                "অ্যালার্জিজনিত সর্দি, হাঁচি, নাক চুলকানো, চোখ দিয়ে পানি পড়া এবং তীব্র শুকনো কাশি উপশমে নির্দেশিত অ্যালার্জি প্রতিরোধক।"
            }
            // 8. Nasal Decongestants
            containsAny(nameLower, "xylometazoline") -> {
                "অতিরিক্ত ঠাণ্ডা বা সর্দিজনিত কারণে নাক বন্ধ হয়ে যাওয়া দ্রুত দূর করে শ্বাস-প্রশ্বাস স্বাভাবিক করতে ব্যবহৃত নাকের ড্রপ।"
            }
            // 9. Antihypertensives / Cardiovascular
            containsAny(nameLower, "glyceryl trinitrate", "nitroglycerin", "methyldopa", "propranolol", "amlodipine", "losartan", "atenolol") -> {
                "উচ্চ রক্তচাপ নিয়ন্ত্রণ, হৃদস্পন্দন স্বাভাবিক রাখা এবং বুকে ব্যাথা (অ্যাঞ্জাইনা) উপশমে ব্যবহৃত অত্যন্ত সংবেদনশীল হৃদরোগের ঔষধ।"
            }
            // 10. Contraceptives
            containsAny(nameLower, "contraceptive", "desogestrel", "levonorgestrel") -> {
                "অবাঞ্ছিত গর্ভাবস্থা রোধে এবং পরিকল্পিত পরিবার গঠনে ব্যবহৃত বিএমডিসি অনুমোদিত অত্যন্ত নিরাপদ ও কার্যকারী জন্মনিয়ন্ত্রণকারী ঔষধ।"
            }
            // 11. Local Anesthetics
            containsAny(nameLower, "lidocaine", "lignocaine") -> {
                "ছোটখাটো অস্ত্রোপচার, ফোড়া কাটা বা সেলাই করার সময় নির্দিষ্ট স্থান সাময়িকভাবে ব্যথামুক্ত বা অবশ করতে ব্যবহৃত লোকাল অবশকারী।"
            }
            // 12. Gastrointestinal/Antispasmodics
            containsAny(nameLower, "hyoscine", "mebeverine", "domperidone", "ondansetron") -> {
                "পেট কামড়ানো, তলপেটের তীব্র ব্যথা, বমি বমি ভাব বা বমি হওয়া দূর করতে নির্দেশিত পাকস্থলীর ঔষধ।"
            }
            // Fallback default Bangla indication
            else -> {
                "বিএমডিসি নির্দেশিকা অনুযায়ী নির্দিষ্ট শারীরিক উপসর্গ উপশমে চিকিৎসায় ব্যবহৃত ঔষধ।"
            }
        }
    }

    /**
     * Gets a detailed "best used for" description in Bangla for antibiotics.
     */
    fun getBestUsedFor(drugName: String): String {
        val nameLower = drugName.lowercase(Locale.ROOT)

        return when {
            nameLower.contains("amoxicillin") -> "তীব্র টনসিলাইটিস, ওটিটিস মিডিয়া, সাইনুসাইটিস, ব্রঙ্কাইটিস, নিউমোনিয়া এবং মূত্রনালীর ইনফেকশন।"
            nameLower.contains("ampicillin") -> "শ্বাসনালীর ইনফেকশন, পরিপাকতন্ত্রের ইনফেকশন এবং মূত্রনালীর তীব্র ব্যাকটেরিয়াজনিত সংক্রমণ।"
            nameLower.contains("cloxacillin") -> "ত্বকের ইনফেকশন, ফোড়া, ক্ষত এবং ব্যাকটেরিয়াজনিত হাড় ও জয়েন্টের ইনফেকশন।"
            nameLower.contains("trimethoprim") || nameLower.contains("co-trimoxazole") -> "ক্রনিক ব্রঙ্কাইটিস, মূত্রনালীর ইনফেকশন, কানের ইনফেকশন এবং শিগেলা বা ব্যাকটেরিয়াজনিত ডায়রিয়া।"
            nameLower.contains("doxycycline") -> "ব্রণ বা একনে, টাইফাস জ্বর, কলেরা, ক্ল্যামাইডিয়াল ইনফেকশন এবং দীর্ঘমেয়াদী শ্বাসনালীর প্রদাহ।"
            nameLower.contains("phenoxymethylpenicillin") -> "স্ট্রেপ্টোকক্কাল টনসিলাইটিস, ফ্যারিনজাইটিস এবং বাতজ্বর বা রিউম্যাটিক ফিভার প্রতিরোধে।"
            nameLower.contains("procaine") -> "তীব্র ব্যাকটেরিয়াজনিত সিফিলিস, ডিপথেরিয়া এবং নরম কলার জটিল সংক্রমণ।"
            nameLower.contains("tetracycline") -> "তীব্র ব্রণ, চোখের ব্যাকটেরিয়াজনিত কনজাংটিভাইটিস এবং অন্ত্রের বিভিন্ন সংক্রমণ।"
            nameLower.contains("oxytetracycline") -> "ত্বকের সংক্রমণ, চোখের ঘা এবং পশুপাখির সংস্পর্শ থেকে ছড়ানো বিশেষ ব্যাকটেরিয়াজনিত রোগ।"
            nameLower.contains("flucloxacillin") -> "নরম কলার সংক্রমণ, সেলুলাইটিস, কাটাছেঁড়ার ইনফেকশন এবং অস্ত্রোপচার পরবর্তী ইনফেকশন প্রতিরোধে।"
            nameLower.contains("erythromycin") -> "পেনিসিলিন এলার্জি রোগীদের ক্ষেত্রে শ্বাসনালীর সংক্রমণ, হুপিং কাশি এবং ডিপথেরিয়ার চিকিৎসায়।"
            nameLower.contains("azithromycin") -> "তীব্র টনসিলাইটিস, সাইনুসাইটিস, ব্রঙ্কাইটিস, নিউমোনিয়া, টাইফয়েড এবং মূত্রনালীর সংক্রমণ।"
            nameLower.contains("ciprofloxacin") -> "টাইফয়েড জ্বর, তীব্র মূত্রনালীর ইনফেকশন, হাড় ও জয়েন্টের সংক্রমণ এবং ব্যাকটেরিয়াজনিত জটিল ডায়রিয়া।"
            nameLower.contains("cefixime") -> "তীব্র মূত্রনালীর ইনফেকশন, নিউমোনিয়া, ব্রঙ্কাইটিস, কানের মধ্যবর্তী অংশের ইনফেকশন এবং টাইফয়েড জ্বর।"
            nameLower.contains("metronidazole") -> "অ্যামিবায়োসিস, জিয়ার্ডিয়াসিস, অন্ত্রের ব্যাকটেরিয়াজনিত ইনফেকশন এবং দাঁত ও মাড়ির তীব্র ইনফেকশন।"
            else -> "ব্যাকটেরিয়াজনিত বিভিন্ন জটিল ও সাধারণ ব্যাকটেরিয়াল ইনফেকশন উপশম ও চিকিৎসায়।"
        }
    }

    private fun containsAny(source: String, vararg keywords: String): Boolean {
        for (kw in keywords) {
            if (source.contains(kw)) return true
        }
        return false
    }
}
