import json
import re

# Suffixes/Keywords to classify a generic as an antibiotic
ANTIBIOTIC_KEYWORDS = [
    'cef', 'ceph', 'cillin', 'floxacin', 'mycin', 'penem', 'cycline',
    'phenicol', 'furantoin', 'bactam', 'erythro', 'clarithro', 'azithro',
    'roxithro', 'lincomycin', 'clindamycin', 'metronidazole', 'tinidazole',
    'secnidazole', 'ornidazole'
]

def is_antibiotic_generic(g_name):
    g_lower = g_name.lower()
    # Check if any keyword matches
    for kw in ANTIBIOTIC_KEYWORDS:
        if kw in g_lower:
            return True
    return False

# Base generic descriptions mapping
BASE_DESCRIPTIONS = {
    'paracetamol': 'A widely used pain reliever (analgesic) and fever reducer (antipyretic).',
    'acetaminophen': 'A widely used pain reliever (analgesic) and fever reducer (antipyretic).',
    'aspirin': 'A nonsteroidal anti-inflammatory drug (NSAID) used to reduce pain, fever, and inflammation, and as an antiplatelet to prevent blood clots.',
    'ibuprofen': 'A nonsteroidal anti-inflammatory drug (NSAID) used to relieve pain, reduce inflammation, and lower fever.',
    'diclofenac': 'A nonsteroidal anti-inflammatory drug (NSAID) used to relieve pain and reduce inflammation in joint and muscle conditions.',
    'aceclofenac': 'A nonsteroidal anti-inflammatory drug (NSAID) used to relieve pain and inflammation in arthritis.',
    'naproxen': 'A nonsteroidal anti-inflammatory drug (NSAID) used to relieve pain, swelling, stiffness, and fever.',
    'ketorolac': 'A potent NSAID used for the short-term management of moderate to severe acute pain.',
    'esomeprazole': 'A proton pump inhibitor used to decrease stomach acid production, treating acid reflux and heartburn.',
    'pantoprazole': 'A proton pump inhibitor used to decrease stomach acid production, treating GERD and peptic ulcers.',
    'lansoprazole': 'A proton pump inhibitor used to decrease stomach acid production.',
    'rabeprazole': 'A proton pump inhibitor used to reduce stomach acid.',
    'omeprazole': 'A proton pump inhibitor used to reduce stomach acid, treating GERD, gastritis, and ulcers.',
    'famotidine': 'An H2 blocker used to treat and prevent ulcers, and treat GERD.',
    'ranitidine': 'An H2-receptor antagonist used to decrease stomach acid production, treating heartburn and acid reflux.',
    'metronidazole': 'An antimicrobial medication used to treat bacterial and protozoal infections.',
    'domperidone': 'A dopamine antagonist used as an antiemetic and prokinetic to treat nausea, vomiting, and bloating.',
    'ondansetron': 'A medication used to prevent nausea and vomiting caused by cancer chemotherapy, radiation therapy, or surgery.',
    'mebeverine': 'An antispasmodic medication used to treat symptoms of irritable bowel syndrome (IBS).',
    'hyoscine': 'An antispasmodic drug used to relieve abdominal cramps, pain, and spasms.',
    'salbutamol': 'A bronchodilator used to quickly relieve shortness of breath and wheezing in asthma and COPD.',
    'montelukast': 'A leukotriene receptor antagonist used for the maintenance treatment of asthma and to relieve seasonal allergies.',
    'folic acid': 'A B-vitamin (B9) essential for DNA synthesis, red blood cell production, and fetal development.',
    'calcium': 'An essential mineral used to prevent or treat low blood calcium levels, or as an antacid.',
    'calcium carbonate': 'An essential mineral used to prevent or treat low blood calcium levels, or as an antacid.',
    'cholecalciferol': 'A vitamin essential for calcium absorption, bone health, and immune support.',
    'vitamin d3': 'A vitamin essential for calcium absorption, bone health, and immune support.',
    'ascorbic acid': 'An essential antioxidant vitamin (Vitamin C) used to support immune function, collagen synthesis, and iron absorption.',
    'vitamin c': 'An essential antioxidant vitamin (Vitamin C) used to support immune function, collagen synthesis, and iron absorption.',
    'zinc': 'An essential mineral vital for immune function, wound healing, protein synthesis, and growth.',
    'ferrous': 'An essential mineral used to prevent and treat iron deficiency anemia.',
    'iron': 'An essential mineral used to prevent and treat iron deficiency anemia.',
    'multivitamin': 'A comprehensive supplement containing essential vitamins and minerals to support overall health.',
    'vitamin b complex': 'A supplement containing a combination of B vitamins, essential for energy metabolism and neurological health.',
    'vitamin a': 'An essential vitamin vital for vision, immune function, and skin health.',
    'retinol': 'An essential vitamin vital for vision, immune function, and skin health.',
    'atorvastatin': 'A statin medication used to lower bad cholesterol (LDL) and triglycerides.',
    'rosuvastatin': 'A statin medication used to lower bad cholesterol (LDL) and prevent cardiovascular events.',
    'amlodipine': 'A calcium channel blocker used to treat high blood pressure and chest pain (angina).',
    'losartan': 'An angiotensin II receptor blocker used to lower high blood pressure and protect kidneys in diabetic patients.',
    'metformin': 'An oral antidiabetic medication used to control blood sugar levels in type 2 diabetes.',
    'gliclazide': 'A sulfonylurea antidiabetic drug used to lower blood glucose levels in type 2 diabetes.',
    'glimepiride': 'A sulfonylurea medication used to treat type 2 diabetes.',
    'ciprofloxacin': 'A fluoroquinolone antibiotic used to treat serious bacterial infections.',
    'azithromycin': 'A macrolide antibiotic used to treat bacterial infections like respiratory, ear, and skin infections.',
    'cefixime': 'An oral third-generation cephalosporin antibiotic used to treat various bacterial infections.',
    'flucloxacillin': 'A penicillinase-resistant penicillin antibiotic used to treat skin, soft tissue, and bone infections.',
    'clindamycin': 'A lincosamide antibiotic used to treat serious skin, bone, and soft tissue bacterial infections.',
    'miconazole': 'An antifungal medication used to treat skin, mouth, and vaginal yeast infections.',
    'ketoconazole': 'An antifungal agent used to treat serious fungal infections, dandruff, and skin conditions.',
    'clotrimazole': 'An antifungal medication used to treat fungal infections of the skin like athlete\'s foot.',
    'betamethasone': 'A potent corticosteroid used to reduce inflammation, itching, and redness in skin conditions.',
    'hydrocortisone': 'A mild corticosteroid used to reduce skin inflammation, swelling, and itching.',
    'levocetirizine': 'An antihistamine used to relieve allergy symptoms such as watery eyes, runny nose, and itching.',
    'cetirizine': 'An antihistamine used to treat allergy symptoms and hives.',
    'fexofenadine': 'A non-drowsy antihistamine used to relieve hay fever and allergy symptoms.',
    'desloratadine': 'A non-drowsy antihistamine used to treat allergy symptoms.',
    'rupatadine': 'An antihistamine used to treat allergic rhinitis and hives.',
    'levofloxacin': 'A fluoroquinolone antibiotic used to treat severe bacterial infections.',
    'moxifloxacin': 'A fluoroquinolone antibiotic used to treat respiratory tract and abdominal infections.',
    'mebendazole': 'An anthelmintic agent used to treat parasitic worm infections.',
    'albendazole': 'An anthelmintic agent used to treat parasitic worm infections.',
    'dextromethorphan': 'A cough suppressant used to temporarily relieve coughs caused by minor throat irritation.',
    'chlorpheniramine': 'An antihistamine used to treat runny nose, sneezing, and itchy throat or eyes.',
    'promethazine': 'An antihistamine and antiemetic used to prevent nausea and motion sickness, and treat allergies.',
    'xylometazoline': 'A nasal decongestant used to clear stuffy nose and sinus congestion.',
    'diazepam': 'A benzodiazepine used to treat anxiety, muscle spasms, alcohol withdrawal, and seizures.',
    'phenobarbitone': 'A barbiturate anticonvulsant used to control seizures and treat insomnia.',
    'phenobarbital': 'A barbiturate anticonvulsant used to control seizures and treat insomnia.',
    'misoprostol': 'A synthetic prostaglandin used to prevent stomach ulcers and for obstetric indications.',
    'oxytocin': 'A hormone used to induce labor, control bleeding after childbirth, and support lactation.',
    'hydrochlorothiazide': 'A diuretic (water pill) used to treat high blood pressure and fluid retention (edema).',
    'lidocaine': 'A local anesthetic used to numb tissue in a specific area.',
    'lignocaine': 'A local anesthetic used to numb tissue in a specific area.',
    'chloramphenicol': 'An antibiotic used to treat serious eye, ear, or systemic bacterial infections.',
    'chlorhexidine': 'An antiseptic and disinfectant used to reduce bacteria on the skin and in the mouth.',
    'chloroxylenol': 'An antiseptic and disinfectant used for skin disinfection and surgical cleaning.',
    'povidone iodine': 'A broad-spectrum antiseptic used for skin disinfection before and after surgery.',
    'silver sulfadiazine': 'A topical antibacterial agent used to prevent and treat infections in second- and third-degree burns.',
    'silver sulphadiazine': 'A topical antibacterial agent used to prevent and treat infections in second- and third-degree burns.',
    'sulphamethoxazole + trimethoprim': 'A combination antibiotic (Co-trimoxazole) used to treat urinary tract infections, bronchitis, and middle ear infections.',
    'sulfamethoxazole + trimethoprim': 'A combination antibiotic (Co-trimoxazole) used to treat urinary tract infections, bronchitis, and middle ear infections.',
    'gliclazide + metformin': 'A combination oral antidiabetic medication used to improve blood sugar control in type 2 diabetes.',
    'vildagliptin + metformin': 'A combination antidiabetic medication used to manage blood sugar levels in type 2 diabetes.',
    'amlodipine + olmesartan': 'A combination of a calcium channel blocker and an angiotensin receptor blocker used to treat high blood pressure.',
    'flupentixol + melitracen': 'A combination of an antipsychotic and a tricyclic antidepressant used to treat mild to moderate anxiety and depression.',
    'desogestrel + ethinylestradiol': 'A low-dose combined oral contraceptive pill used to prevent pregnancy.',
    'levonorgestrel + ethinylestradiol': 'A combined oral contraceptive pill used to prevent pregnancy.',
    'benzoic acid + salicylic acid': 'An antifungal and keratolytic ointment used to treat skin fungal infections (Whitfield\'s Ointment).',
    'salicylic acid + benzoic acid': 'An antifungal and keratolytic ointment used to treat skin fungal infections (Whitfield\'s Ointment).',
    'aluminium hydroxide + magnesium hydroxide': 'An antacid combination used to relieve indigestion, heartburn, and sour stomach.',
    'aluminium hydroxide + magnesium hydroxide + simethicone': 'An antacid and antigas combination used to treat heartburn, acid indigestion, and gas bloating.',
    'sodium chloride + dextrose': 'An intravenous fluid used to provide water, electrolytes, and calories for hydration and nutrition.',
}

CLASS_SUFFIXES = [
    ('prazole', 'Proton Pump Inhibitor (PPI)', 'A proton pump inhibitor used to reduce stomach acid, treating GERD, heartburn, and ulcers.'),
    ('tidine', 'H2 Receptor Blocker', 'An H2 receptor antagonist used to decrease stomach acid production, treating acid reflux.'),
    ('olol', 'Beta-Blocker', 'A beta-blocker used to lower blood pressure, reduce heart rate, and treat angina.'),
    ('pril', 'ACE Inhibitor', 'An ACE inhibitor used to lower blood pressure and treat heart failure.'),
    ('sartan', 'Angiotensin Receptor Blocker (ARB)', 'An angiotensin II receptor blocker used to lower high blood pressure and protect kidney function.'),
    ('statin', 'HMG-CoA Reductase Inhibitor', 'A statin used to lower cholesterol levels and reduce risk of cardiovascular disease.'),
    ('dipine', 'Calcium Channel Blocker', 'A calcium channel blocker used to treat high blood pressure and chest pain.'),
    ('floxacin', 'Fluoroquinolone Antibiotic', 'A fluoroquinolone antibiotic used to treat various bacterial infections, including urinary tract and respiratory infections.'),
    ('cillin', 'Penicillin Antibiotic', 'A penicillin antibiotic used to treat a wide range of bacterial infections.'),
    ('cef', 'Cephalosporin Antibiotic', 'A cephalosporin antibiotic used to treat various bacterial infections.'),
    ('ceph', 'Cephalosporin Antibiotic', 'A cephalosporin antibiotic used to treat various bacterial infections.'),
    ('thromycin', 'Macrolide Antibiotic', 'A macrolide antibiotic used to treat bacterial infections such as respiratory and skin infections.'),
    ('cycline', 'Tetracycline Antibiotic', 'A tetracycline antibiotic used to treat bacterial infections, acne, and tick-borne diseases.'),
    ('penem', 'Carbapenem Antibiotic', 'A broad-spectrum carbapenem antibiotic used to treat severe or multidrug-resistant bacterial infections.'),
    ('mycin', 'Antibiotic', 'An antibiotic used to treat serious bacterial infections.'),
    ('onazole', 'Antifungal', 'An antifungal agent used to treat skin, vaginal, or systemic fungal infections.'),
    ('vir', 'Antiviral', 'An antiviral agent used to treat viral infections.'),
    ('afil', 'PDE5 Inhibitor', 'A PDE5 inhibitor used to treat erectile dysfunction and pulmonary arterial hypertension.'),
    ('tinib', 'Tyrosine Kinase Inhibitor', 'A tyrosine kinase inhibitor used as a targeted cancer therapy.'),
    ('mab', 'Monoclonal Antibody', 'A monoclonal antibody used for targeted therapy of cancer or inflammatory disorders.'),
    ('asone', 'Corticosteroid', 'A corticosteroid anti-inflammatory agent used to reduce swelling and allergic reactions.'),
    ('olone', 'Corticosteroid', 'A corticosteroid anti-inflammatory agent used to reduce swelling and allergic reactions.'),
    ('gliflozin', 'SGLT2 Inhibitor', 'An SGLT2 inhibitor used to improve blood sugar control in type 2 diabetes.'),
    ('gliptin', 'DPP-4 Inhibitor', 'A DPP-4 inhibitor used to treat type 2 diabetes by increasing insulin release.'),
    ('bendazole', 'Anthelmintic', 'An anthelmintic agent used to treat parasitic worm infections.'),
    ('caine', 'Local Anesthetic', 'A local anesthetic used to numb a specific area of the body.'),
]

def get_single_description(name):
    name_clean = name.lower().strip()

    # Try exact match in BASE_DESCRIPTIONS
    for k, desc in BASE_DESCRIPTIONS.items():
        if k in name_clean:
            return desc

    # Try CLASS_SUFFIXES matching
    for suffix, cl, desc in CLASS_SUFFIXES:
        if name_clean.endswith(suffix) or f'{suffix} ' in name_clean:
            return desc

    # Keyword-based classifications for remaining generics to complete missing/default data
    if any(x in name_clean for x in ['vitamin', 'mineral', 'nutrient', 'nutrients', 'folate', 'calcium', 'zinc', 'iron', 'supplement', 'multivitamin', 'nutritional']):
        return "A comprehensive nutritional supplement containing essential vitamins and minerals to support overall health and fill dietary gaps."

    if any(x in name_clean for x in ['herb', 'extract', 'arista', 'asava', 'rasa', 'bhasma', 'lauha', 'ghrita', 'taila', 'hayat', 'abhayarista', 'vasica', 'glycyrrhiza', 'centella']):
        return "A traditional herbal/ayurvedic formulation used for holistic health management, natural healing, and symptom relief."

    if any(x in name_clean for x in ['estradiol', 'estrogen', 'progesterone', 'testosterone', 'hormone', 'levonorgestrel', 'desogestrel']):
        return "A hormone replacement or endocrine agent used for therapeutic hormone balance and related clinical indications."

    if any(x in name_clean for x in ['vaccine', 'immunoglobulin', 'tetanus', 'bcg', 'toxoid']):
        return "An immunizing agent used to induce active immunity and protect against specific infectious diseases."

    if any(x in name_clean for x in ['adapalene', 'benzoyl peroxide', 'tretinoin', 'isotretinoin', 'clindamycin', 'acne', 'salicylic']):
        return "A dermatological preparation used for the topical treatment of acne and other skin conditions."

    if any(x in name_clean for x in ['alendronic', 'bisphosphonate', 'ibandronate', 'risedronate', 'zoledronic']):
        return "A bone-resorption inhibitor used for the treatment and prevention of osteoporosis and bone disorders."

    if any(x in name_clean for x in ['alfuzosin', 'tamsulosin', 'silodosin', 'dutasteride', 'finasteride']):
        return "A therapeutic urological agent used to improve urinary flow and treat symptoms of benign prostatic hyperplasia (BPH)."

    if any(x in name_clean for x in ['acarbose', 'miglitol', 'voglibose', 'metformin', 'gliclazide', 'glimepiride', 'vildagliptin', 'sitagliptin', 'empagliflozin', 'dapagliflozin', 'pioglitazone']):
        return "An oral antidiabetic medication used to improve glycemic control in patients with type 2 diabetes."

    if any(x in name_clean for x in ['acetylcysteine', 'carbocisteine', 'bromhexine', 'ambroxol', 'guaifenesin']):
        return "A muscle relaxant or expectorant agent used to reduce the viscosity of mucus and assist in clearing the respiratory tract."

    if 'charcoal' in name_clean:
        return "An adsorbent agent used in the emergency management of oral poisonings, drug overdoses, and abdominal gas."

    if any(x in name_clean for x in ['adrenaline', 'epinephrine', 'norepinephrine', 'dopamine', 'dobutamine']):
        return "A potent sympathomimetic amine used as an emergency vasopressor and cardiac stimulant during critical care."

    if any(x in name_clean for x in ['allantoin', 'urea', 'glycolic', 'moisturizer', 'panthenol']):
        return "A keratolytic skin-conditioning agent used to soothe, hydrate, and promote healing of dry or rough skin."

    if any(x in name_clean for x in ['eye drop', 'eye ointment', 'lubricant eye', 'tear', 'hypromellose', 'carboxymethylcellulose']):
        return "An ophthalmic lubricant or artificial tear preparation used to relieve dry, irritated, or burning eyes."

    return f"A pharmaceutical agent used for therapeutic management of relevant clinical conditions."

def generate_description_for_generic(g_name):
    g_lower = g_name.lower().strip()
    if g_lower in BASE_DESCRIPTIONS:
        return BASE_DESCRIPTIONS[g_lower]

    # Split by delimiters
    parts = re.split(r'\s*,\s*|\s*\+\s*|\s*&\s*|\s+and\s+|\s+with\s+|\s+plus\s+|\s*/\s*', g_name, flags=re.IGNORECASE)
    if len(parts) <= 1:
        return get_single_description(g_name)

    descs = []
    for p in parts:
        p_clean = p.strip()
        if p_clean:
            d = get_single_description(p_clean)
            descs.append(f"{p_clean} ({d.rstrip('.')})")

    return "A combination therapy containing: " + "; ".join(descs) + "."

def is_vitamin_or_mineral_constituent(g_name_lower):
    p = g_name_lower.strip()

    # Check simple B vitamin shorthands or numbers, e.g. "b1", "b2", "b6", "b12", "d3", "k2", etc.
    if re.match(r'^(vitamin\s+)?([abcedk]\d*(-\d+)?)$', p):
        return True

    # Common vitamin/mineral names/keywords
    keywords = [
        'vitamin', 'thiamine', 'pyridoxine', 'cyanocobalamin', 'riboflavin', 'riboflavine',
        'calcium', 'zinc', 'iron', 'ferrous', 'ferric', 'folic', 'folate', 'multivitamin',
        'multimineral', 'cholecalciferol', 'calcitriol', 'ascorbic', 'tocopherol', 'tocopheryl',
        'menaquinone', 'phytomenadione', 'cod liver oil', 'magnesium', 'manganese', 'copper',
        'selenium', 'chromium', 'molybdenum', 'potassium', 'carbonate', 'phosphate', 'orotate',
        'gluconate', 'fumarate', 'succinate', 'pantothenate', 'biotin', 'nicotinamide', 'niacin',
        'coenzyme q10', 'glutathione', 'l-carnitine', 'l-arginine', 'amino acid', 'amino acids',
        'nutrient', 'nutrients', 'supplement', 'antioxidant', 'prebiotic', 'probiotic', 'symbiotic',
        'algae', 'coral', 'eggshell', 'elemental', 'citrate', 'lactate', 'pregnancy', 'mineral',
        'minerals', 'b1', 'b2', 'b6', 'b12', 'd3', 'k2', 'b-complex'
    ]
    for kw in keywords:
        if kw in p:
            return True
    return False

def match_allowed_rule(g_name_lower):
    # If it is a vitamin or mineral, it is allowed by default
    if is_vitamin_or_mineral_constituent(g_name_lower):
        return True

    # Standard single drugs mapping
    if 'condom' in g_name_lower:
        return True
    if g_name_lower == 'aspirin' or g_name_lower == 'acetylsalicylic acid':
        return True
    if g_name_lower == 'paracetamol' or g_name_lower == 'acetaminophen':
        return True
    if 'diclofenac' in g_name_lower:
        return True
    if g_name_lower == 'ibuprofen':
        return True
    if 'methyl salicylate' in g_name_lower or 'methylsalicylate' in g_name_lower:
        return True
    if g_name_lower == 'albendazole':
        return True
    if 'amoxicillin' in g_name_lower:
        return True
    if 'ampicillin' in g_name_lower:
        return True
    if 'chloroquine' in g_name_lower:
        return True
    if 'cloxacillin' in g_name_lower:
        return True
    if 'co-trimoxazole' in g_name_lower or ('sulphamethoxazole' in g_name_lower and 'trimethoprim' in g_name_lower) or ('sulfamethoxazole' in g_name_lower and 'trimethoprim' in g_name_lower):
        return True
    if 'doxycycline' in g_name_lower:
        return True
    if 'griseofulvin' in g_name_lower:
        return True
    if 'phenoxymethylpenicillin' in g_name_lower or 'penicillin v' in g_name_lower:
        return True
    if 'procaine penicillin' in g_name_lower or 'procaine benzylpenicillin' in g_name_lower or 'benzylpenicillin' in g_name_lower:
        return True
    if 'pyrantel' in g_name_lower:
        return True
    if 'quinine' in g_name_lower:
        return True
    if ('pyrimethamine' in g_name_lower and 'sulfadoxine' in g_name_lower) or ('pyrimethamine' in g_name_lower and 'sulphadoxine' in g_name_lower):
        return True
    if 'tetracycline' in g_name_lower or 'oxytetracycline' in g_name_lower:
        return True
    if 'glyceryl trinitrate' in g_name_lower or 'nitroglycerin' in g_name_lower or 'nitroglycerine' in g_name_lower:
        return True
    if 'methyldopa' in g_name_lower:
        return True
    if 'propranolol' in g_name_lower:
        return True
    if 'antacid' in g_name_lower or ('aluminium hydroxide' in g_name_lower and 'magnesium hydroxide' in g_name_lower) or ('aluminum hydroxide' in g_name_lower and 'magnesium hydroxide' in g_name_lower):
        return True
    if g_name_lower == 'glycerin' or g_name_lower == 'glycerol' or 'glycerin (suppository)' in g_name_lower:
        return True
    if 'hyoscine' in g_name_lower:
        return True
    if 'magnesium hydroxide' in g_name_lower or 'milk of magnesia' in g_name_lower:
        return True
    if 'omeprazole' in g_name_lower:
        return True
    if 'oral rehydration' in g_name_lower or g_name_lower == 'ors' or 'rehydration salt' in g_name_lower or g_name_lower == 'ors sachet':
        return True
    if 'ranitidine' in g_name_lower:
        return True
    if 'potassium permanganate' in g_name_lower or 'potassium permengnate' in g_name_lower:
        return True
    if 'atropine' in g_name_lower:
        return True
    if 'benzoic acid' in g_name_lower and 'salicylic acid' in g_name_lower:
        return True
    if 'benzyl benzoate' in g_name_lower:
        return True
    if 'chlorhexidine' in g_name_lower:
        return True
    if 'chloroxylenol' in g_name_lower or 'chloroxylenal' in g_name_lower or 'chlorisylenal' in g_name_lower:
        return True
    if 'gentian violet' in g_name_lower:
        return True
    if 'neomycin' in g_name_lower or 'gentamicin' in g_name_lower or 'gentamycin' in g_name_lower or 'bacitracin' in g_name_lower or 'bactrocin' in g_name_lower:
        return True
    if 'permethrin' in g_name_lower:
        return True
    if 'povidone iodine' in g_name_lower or 'povidone-iodine' in g_name_lower:
        return True
    if 'silver sulfadiazine' in g_name_lower or 'silver sulphadiazine' in g_name_lower:
        return True
    if 'calcium' in g_name_lower:
        return True
    if 'ferrous' in g_name_lower or 'fumarate' in g_name_lower or 'gluconate' in g_name_lower or 'carbonyl iron' in g_name_lower or 'iron polymaltose' in g_name_lower:
        return True
    if 'folic acid' in g_name_lower or 'folate' in g_name_lower:
        return True
    if 'multivitamin' in g_name_lower:
        return True
    if 'riboflavin' in g_name_lower or 'riboflavine' in g_name_lower or 'vitamin b2' in g_name_lower:
        return True
    if 'vitamin a' in g_name_lower or 'retinol' in g_name_lower:
        return True
    if 'vitamin b complex' in g_name_lower or 'vitamin b1' in g_name_lower or 'thiamine' in g_name_lower or 'pyridoxine' in g_name_lower or 'cyanocobalamin' in g_name_lower:
        return True
    if 'vitamin c' in g_name_lower or 'ascorbic acid' in g_name_lower:
        return True
    if 'zinc' in g_name_lower:
        return True
    if 'chloramphenicol' in g_name_lower:
        return True
    if 'chlorpheniramine' in g_name_lower:
        return True
    if 'dextromethorphan' in g_name_lower or 'dextromethorphen' in g_name_lower:
        return True
    if 'promethazine' in g_name_lower:
        return True
    if 'salbutamol' in g_name_lower:
        return True
    if 'xylometazoline' in g_name_lower:
        return True
    if 'contraceptive' in g_name_lower or 'desogestrel' in g_name_lower or 'levonorgestrel' in g_name_lower or 'ethinylestradiol' in g_name_lower or 'ethinyl estradiol' in g_name_lower:
        return True
    if 'cholera fluid' in g_name_lower or 'cholera saline' in g_name_lower:
        return True
    if 'dextrose' in g_name_lower:
        return True
    if 'sodium chloride' in g_name_lower:
        return True
    if 'water for injection' in g_name_lower:
        return True
    if 'diazepam' in g_name_lower:
        return True
    if 'phenobarbitone' in g_name_lower or 'phenobarbital' in g_name_lower:
        return True
    if 'ergometrine' in g_name_lower or 'methylergometrine' in g_name_lower:
        return True
    if 'misoprostol' in g_name_lower:
        return True
    if 'oxytocin' in g_name_lower:
        return True
    if 'hydrochlorothiazide' in g_name_lower:
        return True
    if 'lidocaine' in g_name_lower or 'lignocaine' in g_name_lower:
        return True
    if 'vaccine' in g_name_lower or 'tetanus' in g_name_lower or 'bcg' in g_name_lower or 'measles' in g_name_lower or 'polio' in g_name_lower or 'dpt' in g_name_lower or 'influenza' in g_name_lower or 'hepatitis b' in g_name_lower or 'toxoid' in g_name_lower:
        return True
    if 'mebendazole' in g_name_lower:
        return True
    if 'mouthwash' in g_name_lower:
        return True
    if 'sunscreen' in g_name_lower:
        return True
    return False

def match_otc_rule(g_name_lower):
    # If it is a vitamin or mineral, it is OTC by default
    if is_vitamin_or_mineral_constituent(g_name_lower):
        return True

    if 'condom' in g_name_lower:
        return True
    if g_name_lower == 'albendazole':
        return True
    if 'antacid' in g_name_lower or ('aluminium hydroxide' in g_name_lower and 'magnesium hydroxide' in g_name_lower) or ('aluminum hydroxide' in g_name_lower and 'magnesium hydroxide' in g_name_lower):
        return True
    if 'vitamin c' in g_name_lower or 'ascorbic acid' in g_name_lower:
        return True
    if 'benzyl benzoate' in g_name_lower:
        return True
    if 'calcium' in g_name_lower:
        return True
    if 'chloramphenicol' in g_name_lower: # Eye/Ear drop/ointment
        return True
    if 'chlorhexidine' in g_name_lower:
        return True
    if 'chloroxylenol' in g_name_lower or 'chloroxylenal' in g_name_lower or 'chlorisylenal' in g_name_lower:
        return True
    if 'chlorpheniramine' in g_name_lower:
        return True
    if 'diclofenac' in g_name_lower: # Gel is OTC
        return True
    if 'dextromethorphan' in g_name_lower or 'dextromethorphen' in g_name_lower:
        return True
    if 'ferrous' in g_name_lower or 'fumarate' in g_name_lower or 'gluconate' in g_name_lower or 'carbonyl iron' in g_name_lower or 'iron polymaltose' in g_name_lower:
        return True
    if 'gentian violet' in g_name_lower:
        return True
    if g_name_lower == 'glycerin' or g_name_lower == 'glycerol' or 'glycerin (suppository)' in g_name_lower:
        return True
    if 'contraceptive' in g_name_lower or 'desogestrel' in g_name_lower or 'levonorgestrel' in g_name_lower or 'ethinylestradiol' in g_name_lower or 'ethinyl estradiol' in g_name_lower:
        return True
    if 'mebendazole' in g_name_lower:
        return True
    if 'methyl salicylate' in g_name_lower or 'methylsalicylate' in g_name_lower:
        return True
    if 'magnesium hydroxide' in g_name_lower or 'milk of magnesia' in g_name_lower:
        return True
    if 'mouthwash' in g_name_lower:
        return True
    if 'multivitamin' in g_name_lower:
        return True
    if 'neomycin' in g_name_lower or 'gentamicin' in g_name_lower or 'gentamycin' in g_name_lower or 'bacitracin' in g_name_lower or 'bactrocin' in g_name_lower:
        return True
    if 'omeprazole' in g_name_lower:
        return True
    if 'oral rehydration' in g_name_lower or g_name_lower == 'ors' or 'rehydration salt' in g_name_lower or g_name_lower == 'ors sachet':
        return True
    if 'paracetamol' in g_name_lower or 'acetaminophen' in g_name_lower:
        return True
    if 'permethrin' in g_name_lower:
        return True
    if 'potassium permanganate' in g_name_lower or 'potassium permengnate' in g_name_lower:
        return True
    if 'povidone iodine' in g_name_lower or 'povidone-iodine' in g_name_lower:
        return True
    if 'promethazine' in g_name_lower:
        return True
    if 'ranitidine' in g_name_lower:
        return True
    if 'riboflavin' in g_name_lower or 'riboflavine' in g_name_lower or 'vitamin b2' in g_name_lower:
        return True
    if 'salbutamol' in g_name_lower:
        return True
    if 'benzoic acid' in g_name_lower and 'salicylic acid' in g_name_lower:
        return True
    if 'silver sulfadiazine' in g_name_lower or 'silver sulphadiazine' in g_name_lower:
        return True
    if 'sunscreen' in g_name_lower:
        return True
    if 'vitamin a' in g_name_lower or 'retinol' in g_name_lower:
        return True
    if 'vitamin b complex' in g_name_lower or 'vitamin b1' in g_name_lower or 'thiamine' in g_name_lower or 'pyridoxine' in g_name_lower or 'cyanocobalamin' in g_name_lower:
        return True
    if 'xylometazoline' in g_name_lower:
        return True
    return False

def main():
    # Load raw data
    with open('medic-data.json', 'r') as f:
        medic_data = json.load(f)

    # Process unique generics
    unique_g = sorted(list(set(x['g'] for x in medic_data)))
    print(f"Total Unique Generics: {len(unique_g)}")

    processed_generics = {}
    for g in unique_g:
        g_lower = g.lower().strip()

        # Guard: if condom is in generic, bypass splitting
        if 'condom' in g_lower:
            parts = [g_lower]
        else:
            # Split by '+', '&', '/', or word 'and', 'with', 'plus', or ','
            parts = [p.strip().lower() for p in re.split(r'\s*,\s*|\s*\+\s*|\s*&\s*|\s+and\s+|\s+with\s+|\s+plus\s+|\s*/\s*', g, flags=re.IGNORECASE) if p.strip()]

        if len(parts) > 1:
            # Combined drug logic: allowed ONLY if ALL individual constituent generics are approved
            all_parts_allowed = True
            all_parts_otc = True
            for p in parts:
                p_allowed = match_allowed_rule(p) or match_otc_rule(p)
                p_otc = match_otc_rule(p)

                # Special broad rule matching for individual constituent checks (like vitamins/minerals)
                if not p_allowed:
                    all_parts_allowed = False
                if not p_otc:
                    all_parts_otc = False

            is_allowed = all_parts_allowed
            is_otc = all_parts_otc
        else:
            # Single drug logic
            is_allowed = match_allowed_rule(g_lower) or match_otc_rule(g_lower)
            is_otc = match_otc_rule(g_lower)

        is_anti = is_antibiotic_generic(g)
        desc = generate_description_for_generic(g)

        processed_generics[g] = {
            'is_allowed': is_allowed,
            'is_otc': is_otc,
            'is_antibiotic': is_anti,
            'description': desc
        }

    # Let's count totals
    allowed_count = sum(1 for x in processed_generics.values() if x['is_allowed'])
    otc_count = sum(1 for x in processed_generics.values() if x['is_otc'])
    anti_count = sum(1 for x in processed_generics.values() if x['is_antibiotic'])

    print(f"Processed: Allowed={allowed_count}, OTC={otc_count}, Antibiotic={anti_count}")

    # Save the processed mapping inside assets
    with open('app/src/main/assets/generics_processed.json', 'w') as f:
        json.dump(processed_generics, f, indent=4)
    # Also save at root for reference/caching
    with open('generics_processed.json', 'w') as f:
        json.dump(processed_generics, f, indent=4)

    print("Saved generics_processed.json successfully.")

if __name__ == '__main__':
    main()
