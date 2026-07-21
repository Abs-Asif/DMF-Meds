# 🩺 DMF Meds (Drug reference app for MATS/DMF practitioners)

DMF Meds is a highly performant, beautifully crafted, native Android application built with **Kotlin**, **Jetpack Compose (Material 3)**, and **Gradle**. It acts as a comprehensive, offline-first clinical decision support system and drug reference registry specifically tailored for **Medical Assistant Training School (MATS)** and **Diploma in Medical Faculty (DMF)** practitioners in Bangladesh.

The application enables practitioners to instantly search a massive database of **over 25,000 commercial brand-name medicines** in Bangladesh to verify whether they are legally authorized to prescribe them under current Bangladesh Medical & Dental Council (BM&DC) regulations, while also providing clinical guidelines, drug interactions, alternative recommendations, and medical ethics fatwas.

---

## 🚀 Key Features

### 1. Offline Search & Levenshtein Fallback Matching
* **Sub-Millisecond Search:** Instantaneous matching of brand names, generics, and strengths from a local, cached SQLite-alternative JSON database of 25,700+ commercial drugs.
* **Fuzzy Spelling Correction:** If a user mistypes a brand name (e.g., "Napa" typed as "Nopa" or "Skelo" instead of "Seclo"), the search engine utilizes an optimized **Levenshtein Distance** algorithm (edit distance $\le 3$) to suggest matching alternative brands.
* **Typing Animation Effect:** An interactive, typing placeholder effect cycles through popular brand names (e.g. Napa 500mg, Seclo 20mg, Fenadin 120mg) in the empty search bar to guide practitioners on input formats.

### 2. Live Database Statistics (Home Page)
* **Real-Time Counters:** The homepage presents real-time statistical summaries:
  1. *Total Medicines:* The complete loaded count of brand formulations in the active registry.
  2. *Allowed to Prescribe:* The precise count of individual brand formulations whose generics are legally authorized for MATS/DMF prescription.

### 3. Comprehensive Drug-Drug Interaction Checker
* **1-1 Pairwise Interactions:** A fully integrated clinical Interaction Checker allowing practitioners to select unlimited drugs. Only one field can be blank at a time, and any blank fields collapse when the check runs.
* **Autocomplete Selection:** Form fields support live suggestions based on commercial brand name and strength (`b` and `p`).
* **Optimized Parsing:** Uses an extremely memory-efficient, sequential stream parser to check the 22MB `db_drug_interactions.csv` file, checking all pairwise interactions between active generics on a background coroutine in less than 150ms.

### 4. Continuous, Legal-Styled Drug & Cosmetic Act Screen
* **Elegant legal presentation:** Displays the full *Drug and Cosmetic Act 2023* (ঔষধ ও কসমেটিকস্ আইন ২০২৩) in a single continuous, scrollable layout (no boxes/expandable cards).
* **Proper Indentation:** Subpoints and clauses are beautifully indented at multiple levels (`(১)`, `(ক)`, `(অ)`) for high-quality legibility.
* **Asynchronous Parsing & Search:** The raw 1,000-line Markdown is parsed asynchronously on background threads on entry and is fully searchable in real-time.

### 5. Authentic Islamic Fatwas & Custom Arabic Typography
* **Detailed Question & Answer format:** Features six highly detailed, authentic fatwas from **islamqa.info** concerning medical ethics, error liabilities, licensing, patient privacy, and therapeutic substances.
* **Beautiful Scheherazade Font:** Arabic texts of Quranic verses and Hadiths are parsed and formatted using the specialized **Scheherazade** font, colored in a majestic golden amber.
* **Targeted Kalpurush Font Use:** Kalpurush font is applied exclusively to the Fatwas and Information Articles screens to maximize readability of native Bangla legal and religious texts, while keeping standard system typography elsewhere.

### 6. Antibiotics Guideline Page
* **Antibiotic reference list:** A fifth choices article in the Information Desk detailing BM&DC approved and common antibiotics.
* **Enriched Guidance:** Displays both the official Bangla indication and a dedicated "Best Used For" (বিশেষ কার্যকারিতা) field to support clinical stewardship.

### 7. Modern Bulged Pill Bottom Navigation Bar
* **Visually Pleasurable Compact Layout:** A floating navigation bar with a dark slate backing and soft borders.
* **Animated Bulged-Pill Selection:** Show icons all the time, only showing text labels for the selected tab. The selected item has a physics-based spring animation that bulges the icon size and pill container for an ultra-premium feel.

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
│   │   │   │   ├── antibiotics.txt  # Approved/common antibiotic listings
│   │   │   │   ├── bmdc_act.md      # Official Bangla legislation text
│   │   │   │   ├── fatwas.json      # Structured medical fatwas with Arabic quotes
│   │   │   │   ├── medic_data.json  # 25,700+ commercial brand listings
│   │   │   │   ├── db_drug_interactions.csv # 22MB Drug interactions dataset (191k lines)
│   │   │   │   └── generics_processed.json # Processed generic database mapping
│   │   │   ├── java/com/dmf/meds/
│   │   │   │   ├── MainActivity.kt   # Main Entrypoint, Navigation & BackHandler
│   │   │   │   ├── InfoScreen.kt     # Info Desk layout, BM&DC Check, Antibiotics
│   │   │   │   ├── DrugActScreen.kt  # Continuous legal scroll view
│   │   │   │   ├── FatawasScreen.kt  # Islamic Fatwa reader with Scheherazade
│   │   │   │   ├── MedicineScreen.kt # Live Canvas background, search & stats
│   │   │   │   ├── InteractionCheckerScreen.kt # Multi-drug interaction checker
│   │   │   │   ├── MedicineDetailsView.kt # Combined Assessment results & alternatives
│   │   │   │   ├── Language.kt       # Dynamic Arabic & Bangla font wrapper, translations
│   │   │   │   ├── SearchEngine.kt   # Levenshtein Search & Alternative lookups
│   │   │   │   └── Model.kt          # Gson Serialized Data Models
│   │   │   └── res/
│   │   │       └── font/
│   │   │           ├── kalpurush.ttf # Custom Bangla font asset
│   │   │           └── scheherazade.ttf # Custom Arabic font asset
│   │       └── AndroidManifest.xml
│   │   └── test/                    # JUnit Search & Levenshtein Unit Tests
├── preprocess.py                    # Python script to rebuild generics database
├── release.keystore                 # Signing keystore for release builds
└── build.gradle.kts                 # Multiplatform Kotlin Gradle Configs
```

### Build & Database Generation Pipeline
The app's database is pre-processed using a local Python script `preprocess.py`:
1. It reads `medic_data.json`, extracting all unique generic ingredients.
2. It maps each ingredient. If it is a combined generic, it splits the ingredients by delimiters (`+`, `&`, `and`, `with`, `plus`, `/`, `,`) and verifies if **every single part** is listed in `allowed.txt` or `OTC.txt`.
3. **Vitamins and Minerals Fix:** The script robustly identifies all vitamins, minerals, and their combinations (A, B1, B2, B6, B12, D3, E, Calcium, Zinc, Iron) as allowed and OTC by default, addressing logical listing issues.
4. It auto-generates localized descriptions for single and combined therapies.
5. It runs a regex check against known prefixes/suffixes to tag **antibiotics** and flag safety warnings.
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
