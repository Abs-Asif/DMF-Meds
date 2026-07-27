package com.dmf.meds

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

data class InteractionMatch(
    val drug1Name: String,
    val drug1Generic: String,
    val drug2Name: String,
    val drug2Generic: String,
    val description: String
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun InteractionCheckerScreen(
    medicines: List<Medicine>,
    prefilledMedicine: Medicine? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    // Selected medicines list
    var selectedMeds by remember {
        mutableStateOf(
            if (prefilledMedicine != null) listOf(prefilledMedicine) else emptyList()
        )
    }

    // Single unified search query state
    var query by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }

    var interactionResults by remember { mutableStateOf<List<InteractionMatch>?>(null) }
    var isChecking by remember { mutableStateOf(false) }

    // Autocomplete list filtered on the fly
    val suggestions = remember(query, medicines, selectedMeds) {
        val q = query.trim().lowercase(Locale.ROOT)
        if (q.isEmpty()) {
            emptyList()
        } else {
            medicines.asSequence()
                .filter { med ->
                    // Exclude already selected ones
                    !selectedMeds.any { it.brand == med.brand && it.power == med.power } &&
                    (med.brand.lowercase(Locale.ROOT).contains(q) || med.power.lowercase(Locale.ROOT).contains(q))
                }
                .take(15)
                .toList()
        }
    }

    // Automatic analysis triggers whenever selectedMeds changes
    LaunchedEffect(selectedMeds) {
        if (selectedMeds.size < 2) {
            interactionResults = null
            return@LaunchedEffect
        }

        isChecking = true
        interactionResults = null

        scope.launch(Dispatchers.IO) {
            val matches = mutableListOf<InteractionMatch>()
            try {
                val assetStream = context.assets.open("db_drug_interactions.csv")
                val reader = BufferedReader(InputStreamReader(assetStream))

                val medicinePairings = mutableListOf<Pair<Medicine, List<String>>>()
                for (med in selectedMeds) {
                    val parts = med.generic.split(Regex("\\s*\\+\\s*|\\s*&\\s*|\\s+and\\s+|\\s+with\\s+|\\s+plus\\s+|\\s*/\\s*", RegexOption.IGNORE_CASE))
                        .map { it.trim().lowercase(Locale.ROOT) }
                        .filter { it.isNotEmpty() }
                    medicinePairings.add(Pair(med, parts))
                }

                val activeIngredients = medicinePairings.flatMap { it.second }.toSet()

                var line = reader.readLine()
                if (line != null && line.startsWith("Drug 1")) {
                    line = reader.readLine() // skip header
                }

                while (line != null) {
                    val firstComma = line.indexOf(',')
                    if (firstComma != -1) {
                        val d1 = line.substring(0, firstComma).trim().lowercase(Locale.ROOT)
                        if (activeIngredients.contains(d1)) {
                            val secondComma = line.indexOf(',', firstComma + 1)
                            if (secondComma != -1) {
                                val d2 = line.substring(firstComma + 1, secondComma).trim().lowercase(Locale.ROOT)
                                if (activeIngredients.contains(d2)) {
                                    val desc = line.substring(secondComma + 1).trim().removeSurrounding("\"")

                                    for (i in 0 until medicinePairings.size) {
                                        for (j in i + 1 until medicinePairings.size) {
                                            val medA = medicinePairings[i].first
                                            val ingredientsA = medicinePairings[i].second

                                            val medB = medicinePairings[j].first
                                            val ingredientsB = medicinePairings[j].second

                                            val match1 = ingredientsA.contains(d1) && ingredientsB.contains(d2)
                                            val match2 = ingredientsA.contains(d2) && ingredientsB.contains(d1)

                                            if (match1 || match2) {
                                                val alreadyMatched = matches.any {
                                                    (it.drug1Name == medA.brand && it.drug2Name == medB.brand) ||
                                                    (it.drug1Name == medA.brand && it.drug2Name == medB.brand)
                                                }
                                                if (!alreadyMatched) {
                                                    matches.add(
                                                        InteractionMatch(
                                                            drug1Name = medA.brand,
                                                            drug1Generic = medA.generic,
                                                            drug2Name = medB.brand,
                                                            drug2Generic = medB.generic,
                                                            description = desc
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    line = reader.readLine()
                }
                reader.close()
                assetStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            withContext(Dispatchers.Main) {
                isChecking = false
                interactionResults = matches
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Simple and Minimal Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                DMFText(
                    text = "Interaction Checker",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Clean Unified Autocomplete Input Field
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { isFocused = it.isFocused }
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        placeholder = {
                            DMFText(
                                text = "Search & add drugs to analyze...",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )

                    // Autocomplete popup directly below input field
                    if (isFocused && suggestions.isNotEmpty()) {
                        Popup(
                            alignment = Alignment.BottomCenter,
                            onDismissRequest = {}
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                                    .heightIn(max = 200.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            ) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(suggestions) { med ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedMeds = selectedMeds + med
                                                    query = ""
                                                    focusManager.clearFocus()
                                                }
                                                .padding(horizontal = 16.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                DMFText(
                                                    text = med.brand,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                DMFText(
                                                    text = med.generic,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            DMFText(
                                                text = med.power,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Selected Medicine Chips Flow (Modern Flow Layout)
            if (selectedMeds.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DMFText(
                            text = "Selected Drugs (${selectedMeds.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            selectedMeds.forEach { med ->
                                InputChip(
                                    selected = true,
                                    onClick = {
                                        selectedMeds = selectedMeds.filter { it != med }
                                    },
                                    label = {
                                        DMFText(
                                            text = "${med.brand} ${med.power}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = InputChipDefaults.inputChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        selectedTrailingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Status loading bar or fallback message
            item {
                if (selectedMeds.size < 2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CompareArrows,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            DMFText(
                                text = "Add at least 2 drugs to analyze drug interactions.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else if (isChecking) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            DMFText(
                                text = "Performing Clinical Analysis...",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Results Listing
            val results = interactionResults
            if (results != null) {
                item {
                    val isSafe = results.isEmpty()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSafe) Color(0xFF064E3B).copy(alpha = 0.2f)
                                            else Color(0xFF7F1D1D).copy(alpha = 0.2f)
                        ),
                        border = BorderStroke(
                            width = 1.5.dp,
                            color = if (isSafe) Color(0xFF10B981) else Color(0xFFEF4444)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSafe) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isSafe) Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                DMFText(
                                    text = if (isSafe) "No Interaction Risks Detected" else "Potential Risks Detected",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSafe) Color(0xFF34D399) else Color(0xFFF87171)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                DMFText(
                                    text = if (isSafe) "All selected drugs are mutually compatible for general use."
                                           else "Found ${results.size} clinical interaction(s). Review details below.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                items(results) { match ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF7F1D1D), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    DMFText(
                                        text = "ALERT",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 9.sp,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                DMFText(
                                    text = "${match.drug1Name} ↔ ${match.drug2Name}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFFF87171)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            DMFText(
                                text = "Generics comparison: ${match.drug1Generic} vs ${match.drug2Generic}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(12.dp))
                            DMFText(
                                text = match.description,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
