package com.dmf.meds

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineScreen(
    medicines: List<Medicine>,
    uniqueBrands: List<String>,
    genericsMetadata: Map<String, GenericMetadata>
) {
    var query by remember { mutableStateOf("") }
    var selectedMedicine by remember { mutableStateOf<Medicine?>(null) }
    var isSearchFocused by remember { mutableStateOf(false) }
    var searchResultState by remember { mutableStateOf<SearchResultState>(SearchResultState.Success(emptyList())) }

    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Trigger suggestion search when query changes
    LaunchedEffect(query) {
        if (query.trim().isEmpty()) {
            searchResultState = SearchResultState.Success(emptyList())
        } else {
            searchResultState = SearchEngine.getSuggestions(query, medicines, uniqueBrands)
        }
    }

    // Moving pastel gradient background
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val animOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    val movingGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFF1F5F9), // Soft slate/white
            Color(0xFFEFF6FF), // Soft blue/white
            Color(0xFFFDF2F8), // Soft pink/white
            Color(0xFFFFFFFF)
        ),
        start = Offset(animOffset, animOffset),
        end = Offset(animOffset + 800f, animOffset + 1200f)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(movingGradient)
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
                        text = "Search Medicines",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }

            // Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
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
                        .onFocusChanged { isSearchFocused = it.isFocused }
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(28.dp)),
                    placeholder = { Text("Enter brand name, power...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = {
                                query = ""
                                selectedMedicine = null
                                focusManager.clearFocus()
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent
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
            ) {
                if (selectedMedicine != null) {
                    // Show Medicine details
                    MedicineDetailsView(
                        med = selectedMedicine!!,
                        medicines = medicines,
                        genericsMetadata = genericsMetadata,
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
                                        text = "No medicines found.",
                                        color = Color.Gray,
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
                                        containerColor = Color(0xFFFEF3C7) // soft orange/yellow warning
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
                                            tint = Color(0xFFD97706),
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                        Column {
                                            Text(
                                                text = "Spelling / Pronunciation Fallback",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF92400E)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "You might have typed the medicine name incorrectly. Perhaps you were searching for one of the following:",
                                                fontSize = 12.sp,
                                                color = Color(0xFFB45309)
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
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Enter a medicine brand name or power to verify prescription compliance and view detailed info.",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    color = Color(0xFF1E293B)
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
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = med.manufacturer,
                fontSize = 11.sp,
                color = Color.LightGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
