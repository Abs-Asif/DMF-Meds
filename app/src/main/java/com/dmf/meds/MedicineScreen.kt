package com.dmf.meds

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineScreen(
    medicines: List<Medicine>,
    uniqueBrands: List<String>,
    genericsMetadata: Map<String, GenericMetadata>,
    isBangla: Boolean,
    isSearchFocused: Boolean,
    onSearchFocusedChange: (Boolean) -> Unit,
    queryState: MutableState<String>,
    selectedMedicineState: MutableState<Medicine?>
) {
    var query by queryState
    var selectedMedicine by selectedMedicineState
    var searchResultState by remember { mutableStateOf<SearchResultState>(SearchResultState.Success(emptyList())) }

    val focusManager = LocalFocusManager.current

    // Trigger suggestion search when query changes
    LaunchedEffect(query) {
        if (query.trim().isEmpty()) {
            searchResultState = SearchResultState.Success(emptyList())
        } else {
            searchResultState = SearchEngine.getSuggestions(query, medicines, uniqueBrands)
        }
    }

    // Moving soft/pastel gradient background matching light/dark modes
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val animOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    val backgroundColors = if (MaterialTheme.colorScheme.background == Color(0xFF0F172A)) {
        // Dark Mode pastel gradient
        listOf(
            Color(0xFF0F172A),
            Color(0xFF1E1E38),
            Color(0xFF111827),
            Color(0xFF0F172A)
        )
    } else {
        // Light Mode pastel gradient
        listOf(
            Color(0xFFFFFFFF),
            Color(0xFFF1F5F9), // Soft slate/white
            Color(0xFFEFF6FF), // Soft blue/white
            Color(0xFFFDF2F8), // Soft pink/white
            Color(0xFFFFFFFF)
        )
    }

    val movingGradient = Brush.linearGradient(
        colors = backgroundColors,
        start = Offset(animOffset, animOffset),
        end = Offset(animOffset + 800f, animOffset + 1200f)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(movingGradient)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Clicking on any blank spot in the background clears search focus
                focusManager.clearFocus()
                onSearchFocusedChange(false)
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Animated layout from center to top
            val isAtTop = isSearchFocused || query.isNotEmpty() || selectedMedicine != null
            val topPadding by animateDpAsState(
                targetValue = if (isAtTop) 16.dp else 180.dp,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
                label = "padding"
            )

            Spacer(modifier = Modifier.height(topPadding))

            // Search header shown in middle
            AnimatedVisibility(
                visible = !isAtTop,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = Trans.searchMedicines(isBangla),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }

            // Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {}, // Consume clicks on this bar
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        if (selectedMedicine != null && !it.equals("${selectedMedicine?.brand} ${selectedMedicine?.power}", ignoreCase = true)) {
                            selectedMedicine = null
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { onSearchFocusedChange(it.isFocused) }
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(28.dp)),
                    placeholder = {
                        Text(
                            text = Trans.enterBrandName(isBangla),
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            modifier = Modifier.padding(start = 12.dp) // Symmetric edge margin
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    query = ""
                                    selectedMedicine = null
                                    focusManager.clearFocus()
                                },
                                modifier = Modifier.padding(end = 8.dp) // Symmetric edge margin
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body Area: Selected Medicine details OR Suggestions
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // Clicking on empty space of suggestions container clears search focus
                        focusManager.clearFocus()
                        onSearchFocusedChange(false)
                    }
            ) {
                if (selectedMedicine != null) {
                    // Show Medicine details
                    MedicineDetailsView(
                        med = selectedMedicine!!,
                        medicines = medicines,
                        genericsMetadata = genericsMetadata,
                        isBangla = isBangla,
                        onSelectMedicine = { newMed ->
                            selectedMedicine = newMed
                            query = "${newMed.brand} ${newMed.power}"
                            focusManager.clearFocus()
                        }
                    )
                } else if (query.trim().isNotEmpty()) {
                    // Show suggestions based on state
                    when (val state = searchResultState) {
                        is SearchResultState.Success -> {
                            if (state.medicines.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    Text(
                                        text = Trans.noMedicinesFound(isBangla),
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                        modifier = Modifier.padding(top = 32.dp)
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(state.medicines) { med ->
                                        SuggestionTile(med = med) {
                                            selectedMedicine = med
                                            query = "${med.brand} ${med.power}"
                                            focusManager.clearFocus()
                                        }
                                    }
                                }
                            }
                        }
                        is SearchResultState.Fallback -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Notice/Warning Card
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (MaterialTheme.colorScheme.background == Color(0xFF0F172A)) {
                                            Color(0xFF78350F) // dark orange warning
                                        } else {
                                            Color(0xFFFEF3C7) // soft orange/yellow warning
                                        }
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Warning",
                                            tint = if (MaterialTheme.colorScheme.background == Color(0xFF0F172A)) {
                                                Color(0xFFFBBF24)
                                            } else {
                                                Color(0xFFD97706)
                                            },
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                        Column {
                                            Text(
                                                text = Trans.spellingFallbackTitle(isBangla),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = if (MaterialTheme.colorScheme.background == Color(0xFF0F172A)) {
                                                    Color(0xFFFCD34D)
                                                } else {
                                                    Color(0xFF92400E)
                                                }
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = Trans.spellingFallbackText(isBangla),
                                                fontSize = 12.sp,
                                                color = if (MaterialTheme.colorScheme.background == Color(0xFF0F172A)) {
                                                    Color(0xFFFCD34D).copy(alpha = 0.9f)
                                                } else {
                                                    Color(0xFFB45309)
                                                }
                                            )
                                        }
                                    }
                                }

                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(state.medicines) { med ->
                                        SuggestionTile(med = med) {
                                            selectedMedicine = med
                                            query = "${med.brand} ${med.power}"
                                            focusManager.clearFocus()
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Search bar is empty, show nice helper
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 40.dp)
                        ) {
                            // REMOVED THE MEDICINE BAG ICON AS PER REQUIREMENT 3
                            Text(
                                text = Trans.searchHelp(isBangla),
                                fontSize = 12.sp, // REDUCED FONT SIZE AS PER REQUIREMENT 3
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SuggestionTile(med: Medicine, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = med.brand,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = med.power,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = med.generic,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = med.manufacturer,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
