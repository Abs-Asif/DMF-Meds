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

@Composable
fun MovingBlurredBubblesBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "bubbles")

    // Bubble 1 animations (Sky Blue)
    val x1 by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "x1"
    )
    val y1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "y1"
    )

    // Bubble 2 animations (Violet/Indigo)
    val x2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(22000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "x2"
    )
    val y2 by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "y2"
    )

    // Bubble 3 animations (Vibrant Pink/Rose)
    val x3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "x3"
    )
    val y3 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "y3"
    )

    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Forced Dark Theme base background color
    ) {
        val width = size.width
        val height = size.height

        // Draw Bubble 1 (Sky Blue)
        val center1 = Offset(x1 * width, y1 * height)
        val radius1 = 280.dp.toPx()
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x3060A5FA), Color.Transparent),
                center = center1,
                radius = radius1
            ),
            center = center1,
            radius = radius1
        )

        // Draw Bubble 2 (Violet)
        val center2 = Offset(x2 * width, y2 * height)
        val radius2 = 320.dp.toPx()
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x248B5CF6), Color.Transparent),
                center = center2,
                radius = radius2
            ),
            center = center2,
            radius = radius2
        )

        // Draw Bubble 3 (Rose/Pink)
        val center3 = Offset(x3 * width, y3 * height)
        val radius3 = 240.dp.toPx()
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x1CFC49A1), Color.Transparent),
                center = center3,
                radius = radius3
            ),
            center = center3,
            radius = radius3
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineScreen(
    medicines: List<Medicine>,
    uniqueBrands: List<String>,
    genericsMetadata: Map<String, GenericMetadata>,
    genericDetails: Map<String, GenericDetail>,
    isBangla: Boolean,
    isSearchFocused: Boolean,
    onSearchFocusedChange: (Boolean) -> Unit,
    queryState: MutableState<String>,
    selectedMedicineState: MutableState<Medicine?>,
    onOpenInteractionChecker: () -> Unit
) {
    var query by queryState
    var selectedMedicine by selectedMedicineState
    var searchResultState by remember { mutableStateOf<SearchResultState>(SearchResultState.Success(emptyList())) }

    val focusManager = LocalFocusManager.current

    // Dynamically calculate allowed medicines count
    val allowedCount = remember(medicines, genericsMetadata) {
        medicines.count { genericsMetadata[it.generic]?.isAllowed == true }
    }

    // List of demo medicines for roll up placeholder animation
    val demoMedicines = remember {
        listOf(
            "Napa 500mg", "Seclo 20mg", "Fenadin 120mg", "Alatrol 10mg", "Xylomet 0.1%",
            "Aspirin 75mg", "Omeprazole 20mg", "Paracetamol", "Zox 500mg", "Sergel 20mg"
        )
    }

    var currentRollUpIndex by remember { mutableIntStateOf(0) }

    // Coroutine-based text roll-up animation in empty state
    LaunchedEffect(query, isSearchFocused) {
        if (query.isEmpty()) {
            while (true) {
                kotlinx.coroutines.delay(2500)
                currentRollUpIndex = (currentRollUpIndex + 1) % demoMedicines.size
            }
        }
    }

    // Trigger suggestion search when query changes
    LaunchedEffect(query) {
        if (query.trim().isEmpty()) {
            searchResultState = SearchResultState.Success(emptyList())
        } else {
            searchResultState = SearchEngine.getSuggestions(query, medicines, uniqueBrands)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Clicking on any blank spot in the background clears search focus
                focusManager.clearFocus()
                onSearchFocusedChange(false)
            }
    ) {
        // High quality premium live blurred bubble background
        MovingBlurredBubblesBackground()

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
                    DMFText(
                        text = Trans.searchMedicines(isBangla),
                        fontSize = 24.sp,
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
                    textStyle = LocalTextStyle.current.copy(fontSize = 18.sp, fontWeight = FontWeight.Medium),
                    placeholder = {
                        if (query.isEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                DMFText(
                                    text = "Try searching ",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                AnimatedContent(
                                    targetState = demoMedicines[currentRollUpIndex],
                                    transitionSpec = {
                                        // Slide in from top, slide out to bottom (moving top to bottom)
                                        (slideInVertically { height -> -height } + fadeIn(animationSpec = tween(300)))
                                            .togetherWith(slideOutVertically { height -> height } + fadeOut(animationSpec = tween(300)))
                                    },
                                    label = "rollupAnimation"
                                ) { medName ->
                                    DMFText(
                                        text = medName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        } else {
                            DMFText(
                                text = "Enter brand name, power...",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
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

            Spacer(modifier = Modifier.height(12.dp))

            // Interaction Checker Button (Always visible on Search Page, beautifully styled)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onOpenInteractionChecker,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CompareArrows,
                        contentDescription = "Interaction Checker",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    DMFText(
                        text = "Interaction Checker",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
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
                        genericDetailsMap = genericDetails,
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
                                    DMFText(
                                        text = Trans.noMedicinesFound(isBangla),
                                        fontSize = 14.sp,
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
                                        containerColor = Color(0xFF78350F) // dark orange warning
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
                                            tint = Color(0xFFFBBF24),
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                        Column {
                                            DMFText(
                                                text = Trans.spellingFallbackTitle(isBangla),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color(0xFFFCD34D)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            DMFText(
                                                text = Trans.spellingFallbackText(isBangla),
                                                fontSize = 11.sp,
                                                color = Color(0xFFFCD34D).copy(alpha = 0.9f)
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
                    // Search bar is empty, show nice helper & stats boxes
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp, start = 16.dp, end = 16.dp)
                        ) {
                            // Stats Cards (Row with 2 beautiful stats boxes)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Stats 1: Total Medicines
                                Card(
                                    modifier = Modifier.weight(1f).height(110.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Icon(
                                            imageVector = Icons.Default.MedicalServices,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        DMFText(
                                            text = medicines.size.toString(),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        DMFText(
                                            text = "Total Medicines",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                // Stats 2: Allowed Medicines
                                Card(
                                    modifier = Modifier.weight(1f).height(110.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        DMFText(
                                            text = allowedCount.toString(),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF10B981)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        DMFText(
                                            text = "Allowed to Prescribe",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            // Citation & Updated Info
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Update,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    DMFText(
                                        text = "Database Updated: July 2026",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    DMFText(
                                        text = "Made by Abdullah Bari",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
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
                DMFText(
                    text = med.brand,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                DMFText(
                    text = med.power,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            DMFText(
                text = med.generic,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            DMFText(
                text = med.manufacturer,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
