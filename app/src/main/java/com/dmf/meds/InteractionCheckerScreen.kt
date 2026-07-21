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
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

data class SelectedDrugSlot(
    val id: Int,
    var query: String = "",
    var selectedMedicine: Medicine? = null
)

data class InteractionMatch(
    val drug1Name: String,
    val drug1Generic: String,
    val drug2Name: String,
    val drug2Generic: String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractionCheckerScreen(
    medicines: List<Medicine>,
    prefilledMedicine: Medicine? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    // Initialize with prefilled medicine in Slot 1 if available
    var slots by remember {
        mutableStateOf(
            listOf(
                SelectedDrugSlot(
                    id = 1,
                    query = prefilledMedicine?.let { "${it.brand} ${it.power}" } ?: "",
                    selectedMedicine = prefilledMedicine
                ),
                SelectedDrugSlot(id = 2)
            )
        )
    }

    var nextId by remember { mutableStateOf(3) }
    var interactionResults by remember { mutableStateOf<List<InteractionMatch>?>(null) }
    var isChecking by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Redesigned modern, clean Top Bar
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

            // Beautiful Instruction Alert Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Information",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        DMFText(
                            text = "Add drugs to the list, select suggestions to bind, and run 'Check Interactions' to perform clinically verified 1-1 safety checks.",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Section Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DMFText(
                        text = "Clinical Query List",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    DMFText(
                        text = "${slots.size} slot(s)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            // Dynamic list of input rows
            items(slots, key = { it.id }) { slot ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        width = 1.5.dp,
                        color = if (slot.selectedMedicine != null) Color(0xFF10B981).copy(alpha = 0.4f)
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DMFText(
                                text = "Drug ${slots.indexOf(slot) + 1}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (slot.selectedMedicine != null) Color(0xFF10B981)
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            if (slot.selectedMedicine != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    DMFText(
                                        text = "Bound",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        DrugInputSlotRow(
                            slot = slot,
                            medicines = medicines,
                            onQueryChange = { q ->
                                slots = slots.map {
                                    if (it.id == slot.id) it.copy(query = q) else it
                                }
                            },
                            onSelect = { med ->
                                slots = slots.map {
                                    if (it.id == slot.id) it.copy(query = "${med.brand} ${med.power}", selectedMedicine = med) else it
                                }
                                focusManager.clearFocus()
                            },
                            onClear = {
                                slots = slots.map {
                                    if (it.id == slot.id) it.copy(query = "", selectedMedicine = null) else it
                                }
                            },
                            onDelete = {
                                if (slots.size > 2) {
                                    slots = slots.filter { it.id != slot.id }
                                } else {
                                    slots = slots.map {
                                        if (it.id == slot.id) it.copy(query = "", selectedMedicine = null) else it
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // Add Drug Button (+ icon)
            item {
                val hasBlank = slots.any { it.selectedMedicine == null && it.query.trim().isEmpty() }
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            if (!hasBlank) {
                                slots = slots + SelectedDrugSlot(id = nextId)
                                nextId++
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        enabled = !hasBlank,
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Drug",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        DMFText(
                            text = "Add Another Drug",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Action Button: Check Interactions
            item {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        val activeSlots = slots.filter { it.selectedMedicine != null || it.query.trim().isNotEmpty() }
                        slots = activeSlots.ifEmpty {
                            listOf(SelectedDrugSlot(id = 1), SelectedDrugSlot(id = 2))
                        }

                        val selectedMeds = slots.mapNotNull { it.selectedMedicine }
                        if (selectedMeds.size < 2) {
                            interactionResults = emptyList()
                            return@Button
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

                                var line = reader.readLine()
                                if (line != null && line.startsWith("Drug 1")) {
                                    line = reader.readLine() // skip header
                                }

                                while (line != null) {
                                    val firstComma = line.indexOf(',')
                                    if (firstComma != -1) {
                                        val secondComma = line.indexOf(',', firstComma + 1)
                                        if (secondComma != -1) {
                                            val d1 = line.substring(0, firstComma).trim().lowercase(Locale.ROOT)
                                            val d2 = line.substring(firstComma + 1, secondComma).trim().lowercase(Locale.ROOT)
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
                                                            (it.drug1Name == medB.brand && it.drug2Name == medA.brand)
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
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CompareArrows,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            DMFText(
                                text = "Run Interaction Analysis",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Results Section
            item {
                val results = interactionResults
                if (results != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header Status Alert
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

                        // Listing of detected interactions
                        results.forEach { match ->
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
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrugInputSlotRow(
    slot: SelectedDrugSlot,
    medicines: List<Medicine>,
    onQueryChange: (String) -> Unit,
    onSelect: (Medicine) -> Unit,
    onClear: () -> Unit,
    onDelete: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    // Sequence-based real-time filtering
    val suggestions = remember(slot.query, medicines) {
        val q = slot.query.trim().lowercase(Locale.ROOT)
        if (q.isEmpty() || slot.selectedMedicine != null) {
            emptyList()
        } else {
            medicines.asSequence()
                .filter { it.brand.lowercase(Locale.ROOT).contains(q) || it.power.lowercase(Locale.ROOT).contains(q) }
                .take(15)
                .toList()
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = slot.query,
            onValueChange = {
                onQueryChange(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused }
                .border(
                    width = 1.dp,
                    color = if (slot.selectedMedicine != null) Color(0xFF10B981).copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                ),
            placeholder = {
                DMFText(
                    text = "Search drug brand, power...",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.MedicalServices,
                    contentDescription = null,
                    tint = if (slot.selectedMedicine != null) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (slot.query.isNotEmpty()) {
                        IconButton(onClick = onClear) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
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

        // Suggestion Dropdown/Overlay using Popup
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
                                    .clickable { onSelect(med) }
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
