# 🩺 DMF Meds (Drug reference app for MATS/DMF practitioners)

DMF Meds is a highly performant, beautifully crafted, native Android application built with **Kotlin**, **Jetpack Compose (Material 3)**, and **Gradle**. It acts as a comprehensive, offline-first clinical decision support system and drug reference registry specifically tailored for **Medical Assistant Training School (MATS)** and **Diploma in Medical Faculty (DMF)** practitioners in Bangladesh.

The application enables practitioners to instantly search a massive database of **over 25,000 commercial brand-name medicines** in Bangladesh to verify whether they are legally authorized to prescribe them under current Bangladesh Medical & Dental Council (BM&DC) regulations, while also providing clinical guidelines, drug interactions, alternative recommendations, and medical ethics fatwas.

---

## 🚀 Key Features

### 1. Offline Search & Levenshtein Fallback Matching
* **Sub-Millisecond Search:** Instantaneous matching of brand names, generics, and strengths from a local, cached SQLite-alternative JSON database of 25,700+ commercial drugs.
* **Fuzzy Spelling Correction:** If a user mistypes a brand name (e.g., "Napa" typed as "Nopa" or "Skelo" instead of "Seclo"), the search engine utilizes an optimized **Levenshtein Distance** algorithm (edit distance $\le 3$) to suggest matching alternative brands.

### 2. Strict Prescription Assessment Logic
* **Individual Ingredient Checks:** The app parses individual ingredients in combination drugs. Under strict clinical rules, a combined drug (e.g., *Drug A + Drug B*) is marked as **Allowed** only if **all** of its constituent generics are approved on their own. If even one generic is unapproved, the entire medicine is marked as **Not Allowed**.
* **Direct Status Presentation:** No confusing ratings or stats values. The screen directly tells the user if a drug is `Allowed to Prescribe`, `OTC Medicine (Allowed by Default)`, or `Antibiotic Medicine (Exercise Caution)`.
* **Seamless Interface:** Multiple assessment results are consolidated into a single contiguous box with organic rounded borders (top rounded, flat middle, bottom rounded for multiple rows), avoiding cluttered nested borders.

### 3. Native BM&DC Act 2023 Page
* **Zero-Lag Native Rendering:** The raw `bmdc_act.md` file (1,000+ lines) is parsed asynchronously on a background thread (`Dispatchers.IO`) once upon screen entry.
* **Interactive Navigation:** Sections are displayed as expandable card components with high-performance horizontal chapter tabs allowing instant scrolling/jumping and full-text search capability inside the Act.

### 4. Custom Bangla Typography (Kalpurush)
* **Dynamic Sizing & Font Selection:** The customized `@Composable fun DMFText(...)` utility dynamically inspects strings for Bangla characters (unicode range `\u0980` to `\u09FF`).
* **Enhanced Legibility:** If Bangla is detected, the app automatically overrides the font family with **Kalpurush** (`kalpurush.ttf`) and applies a **1.25x scaling multiplier** to both the font size and line height, resolving the default small-font rendering of Bangla scripts.

### 5. Premium Drifting Blurred-Bubble Canvas
* **Alive & Responsive Background:** Replaces static looping gradients with a custom, hardware-accelerated Canvas animation.
* **Fluid 2D Physics:** Uses independent, non-synchronous infinite transitions to float three colorful, soft glowing bubbles (Sky Blue, Violet, Rose) with smooth radial gradients. The bubbles drift across the screen randomly, forming a premium, organic backdrop.

### 6. Authentic Islamic Fatwas
* **Ethics & Compliance:** Features four highly detailed fatwas compiled and translated from **islamqa.info/bn** regarding:
  1. *Doctor's Ethics & Secret Keeping* (চিকিৎসকের নীতি ও নৈতিকতা)
  2. *Patient's Rights & Malpractice Liability* (রোগীর অধিকার ও ক্ষতিপূরণ)
  3. *Obeying Government Laws & Registration* (সরকারি আইন ও নিবন্ধন)
  4. *Prescribing Outside Authorized Scope* (চিকিৎসকের আইনি দায়বদ্ধতা)
* **Compact, Clickable Cards:** Displayed in a list that slides open into a dedicated full-text page utilizing proper Kalpurush formatting.

### 7. Forced Dark Mode & English Interface
* **Forced Dark Aesthetic:** The interface runs exclusively in Dark Mode (`#0F172A` deep slate background) with beautiful sky blue primary accents.
* **Clean & Distraction-Free:** Settings button, configurations, and language toggles have been completely removed from the UI. The interface labels are hardcoded in English, while retaining gorgeous native Bangla formatting for actual medical texts and fatwas.

---

## 🛠️ Project Architecture & Database Preprocessing

### Folder Layout
```
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── assets/              # Local Offline Database files
│   │   │   │   ├── allowed.txt      # Raw list of approved generics
│   │   │   │   ├── OTC.txt          # Raw list of Over-The-Counter drugs
│   │   │   │   ├── bmdc_act.md      # Official Bangla legislation text
│   │   │   │   ├── fatwas.json      # Structured medical fatwas
│   │   │   │   ├── medic_data.json  # 25,700+ commercial brand listings
│   │   │   │   └── generics_processed.json # Processed generic database mapping
│   │   │   ├── java/com/dmf/meds/
│   │   │   │   ├── MainActivity.kt   # Main Entrypoint, Navigation & BackHandler
│   │   │   │   ├── InfoScreen.kt     # Info Desk layout, BM&DC Check
│   │   │   │   ├── DrugActScreen.kt  # Compiled native BM&DC Act screen
│   │   │   │   ├── FatawasScreen.kt  # Islamic Fatwa reader
│   │   │   │   ├── MedicineScreen.kt # Live Canvas background, Search Bar & Fallback
│   │   │   │   ├── MedicineDetailsView.kt # Combined Assessment results & alternatives
│   │   │   │   ├── Language.kt       # Dynamic Bangla font wrapper, translations
│   │   │   │   ├── SearchEngine.kt   # Levenshtein Search & Alternative lookups
│   │   │   │   └── Model.kt          # Gson Serialized Data Models
│   │   │   └── res/
│   │   │       └── font/kalpurush.ttf # Custom font asset
│   │       └── AndroidManifest.xml
│   │   └── test/                    # JUnit Search & Levenshtein Unit Tests
├── preprocess.py                    # Python script to rebuild generics database
├── release.keystore                 # Signing keystore for release builds
└── build.gradle.kts                 # Multiplatform Kotlin Gradle Configs
```

### Build & Database Generation Pipeline
The app's database is pre-processed using a local Python script `preprocess.py`:
1. It reads `medic_data.json`, extracting all unique generic ingredients (1,960+ unique molecular combinations).
2. It reads the official BM&DC `allowed.txt` and `OTC.txt`.
3. It maps each ingredient. If it is a combined generic, it splits the ingredients by delimiters (`+`, `&`, `and`, `with`, `plus`, `/`) and verifies if **every single part** is listed in `allowed.txt` or `OTC.txt`.
4. It auto-generates localized descriptions for single and combined therapies.
5. It runs a regex check against known prefixes/suffixes (e.g., `cef`, `cillin`, `floxacin`, `mycin`) to tag **antibiotics** and flag safety warnings.
6. It exports `generics_processed.json` which is packaged into assets and loaded into memory instantly when the app is opened.

To run the preprocessing script:
```bash
python3 preprocess.py
```

---

## 📦 Compilation & Release

### Requirements
* **JDK:** Version 21
* **Gradle:** Version 8.8
* **Android SDK:** Compile SDK Level 34

### Compilation Commands

To run all unit tests for Levenshtein fallbacks, search suggestions, and combo-assessment lookups:
```bash
gradle :app:testDebugUnitTest
```

To compile a fully signed, highly optimized release APK (`release.keystore` is stored in the repository root):
```bash
gradle :app:assembleRelease
```
The compiled APK will be generated at:
`app/build/outputs/apk/release/app-release.apk`

---

## 📜 Regulatory Reference & Compliance
The application strictly incorporates regulations and classifications outlined by the **Bangladesh Medical & Dental Council (BM&DC)** under the **Drug and Cosmetic Act 2023** (ঔষধ ও কসমেটিকস্ আইন ২০২৩), ensuring MATS and DMF clinicians practice safely, legally, and ethically.
