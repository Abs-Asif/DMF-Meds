package com.dmf.meds

import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.util.Locale

@Composable
fun MedicineDetailsView(
    med: Medicine,
    medicines: List<Medicine>,
    genericsMetadata: Map<String, GenericMetadata>,
    isBangla: Boolean,
    onSelectMedicine: (Medicine) -> Unit
) {
    val meta = genericsMetadata[med.generic]

    var showCombinationPopupFor by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Main Details Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Name (Brand + Power)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = med.brand,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = med.power,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Generic Name
                    Text(
                        text = Trans.genericNameLabel(isBangla),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = med.generic,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Generic Description
                    val description = meta?.description ?: Trans.noDescription(isBangla)
                    Text(
                        text = description,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Manufacturer
                    Text(
                        text = Trans.manufacturerLabel(isBangla),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = med.manufacturer,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Remarks Section (1 Mandatory, 2 Optional)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Trans.prescriptionAssessment(isBangla),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // 1. Allowance Status (Mandatory)
                    val isAllowed = meta?.isAllowed == true
                    RemarkRow(
                        title = Trans.allowanceStatus(isBangla),
                        value = if (isAllowed) Trans.allowedYes(isBangla) else Trans.allowedNo(isBangla),
                        subtext = if (isAllowed) Trans.allowedSubtextYes(isBangla) else Trans.allowedSubtextNo(isBangla),
                        isPositive = isAllowed,
                        icon = if (isAllowed) Icons.Default.CheckCircle else Icons.Default.Cancel
                    )

                    // 2. OTC Status (Optional - only show if OTC)
                    val isOtc = meta?.isOtc == true
                    if (isOtc) {
                        Spacer(modifier = Modifier.height(12.dp))
                        RemarkRow(
                            title = Trans.otcStatus(isBangla),
                            value = Trans.allowedYes(isBangla),
                            subtext = Trans.otcSubtext(isBangla),
                            isPositive = true,
                            icon = Icons.Default.Info
                        )
                    }

                    // 3. Antibiotic Status (Optional - only show if antibiotic)
                    val isAntibiotic = meta?.isAntibiotic == true
                    if (isAntibiotic) {
                        Spacer(modifier = Modifier.height(12.dp))
                        RemarkRow(
                            title = Trans.antibioticWarning(isBangla),
                            value = Trans.allowedYes(isBangla),
                            subtext = Trans.antibioticSubtext(isBangla),
                            isPositive = false, // Yellow/Warning style
                            isWarning = true,
                            icon = Icons.Default.Warning
                        )
                    }
                }
            }
        }

        // Expandables Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                // 1. Show alternatives (Same 'g' and 'p')
                ExpandableCard(title = Trans.showAlternatives(isBangla)) {
                    val alternatives = remember(med) {
                        SearchEngine.getAlternatives(med, medicines)
                    }
                    if (alternatives.isEmpty()) {
                        Text(Trans.noAlternatives(isBangla), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            alternatives.take(30).forEach { alt ->
                                CompactMedicineTile(alt)
                            }
                        }
                    }
                }

                // 2. Other Powers (Same 'g')
                ExpandableCard(title = Trans.otherPowers(isBangla)) {
                    val otherPowers = remember(med) {
                        SearchEngine.getOtherPowers(med, medicines)
                    }
                    if (otherPowers.isEmpty()) {
                        Text(Trans.noOtherPowers(isBangla), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            otherPowers.take(30).forEach { alt ->
                                CompactMedicineTile(alt)
                            }
                        }
                    }
                }

                // 3. Other Combination (Clickable generics list)
                ExpandableCard(title = Trans.otherCombinations(isBangla)) {
                    val otherCombos = remember(med) {
                        SearchEngine.getOtherCombinations(med.generic, genericsMetadata.keys)
                    }
                    if (otherCombos.isEmpty()) {
                        Text(Trans.noOtherCombos(isBangla), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            otherCombos.forEach { comboGeneric ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .clickable { showCombinationPopupFor = comboGeneric }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = comboGeneric,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Popup Dialog for Other Combination Clicked Generic
    if (showCombinationPopupFor != null) {
        val selectedGeneric = showCombinationPopupFor!!
        CombinationPopup(
            genericName = selectedGeneric,
            medicines = medicines,
            isBangla = isBangla,
            onClose = { showCombinationPopupFor = null },
            onSelect = { selectedMed ->
                showCombinationPopupFor = null
                onSelectMedicine(selectedMed)
            }
        )
    }
}

@Composable
fun RemarkRow(
    title: String,
    value: String,
    subtext: String,
    isPositive: Boolean,
    isWarning: Boolean = false,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val isDark = MaterialTheme.colorScheme.background == Color(0xFF0F172A)

    val tintColor = when {
        isWarning -> if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706) // Orange
        isPositive -> if (isDark) Color(0xFF4ADE80) else Color(0xFF16A34A) // Green
        else -> if (isDark) Color(0xFFF87171) else Color(0xFFDC2626) // Red
    }

    val containerColor = when {
        isWarning -> if (isDark) Color(0xFF78350F) else Color(0xFFFEF3C7)
        isPositive -> if (isDark) Color(0xFF064E3B) else Color(0xFFDCFCE7)
        else -> if (isDark) Color(0xFF7F1D1D) else Color(0xFFFEE2E2)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tintColor,
            modifier = Modifier
                .size(24.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$title: ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = value,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = tintColor
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun CompactMedicineTile(med: Medicine) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = med.brand,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = med.manufacturer,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = med.power,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ExpandableCard(
    title: String,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (expanded) {
                Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    content()
                }
            }
        }
    }
}

// Full-screen Popup Dialog with search capabilities
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CombinationPopup(
    genericName: String,
    medicines: List<Medicine>,
    isBangla: Boolean,
    onClose: () -> Unit,
    onSelect: (Medicine) -> Unit
) {
    var popupQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    // Filter medicines matching this exact combination generic
    val matchedMedicines = remember(genericName) {
        medicines.filter { it.generic.equals(genericName, ignoreCase = true) }
    }

    // Filter by internal search bar
    val filteredMedicines = remember(popupQuery, matchedMedicines) {
        if (popupQuery.trim().isEmpty()) {
            matchedMedicines
        } else {
            val qLower = popupQuery.lowercase(Locale.ROOT)
            matchedMedicines.filter { med ->
                med.brand.lowercase(Locale.ROOT).contains(qLower) ||
                        med.power.lowercase(Locale.ROOT).contains(qLower)
            }
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false // Custom margins around popup
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // Space margins in all sides
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Header with Title and "X" Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = genericName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar inside popup
                OutlinedTextField(
                    value = popupQuery,
                    onValueChange = { popupQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                    placeholder = {
                        Text(
                            text = Trans.searchBrandPower(isBangla),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (popupQuery.isNotEmpty()) {
                            IconButton(onClick = { popupQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        focusManager.clearFocus()
                    })
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Medicines List
                Box(modifier = Modifier.weight(1f)) {
                    if (filteredMedicines.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(Trans.noMedicinesFound(isBangla), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredMedicines) { med ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                        .clickable { onSelect(med) }
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = med.brand,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = med.manufacturer,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = med.power,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
