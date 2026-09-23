package com.dmf.meds

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF60A5FA), // bright/soft sky blue for dark theme
    onPrimary = Color(0xFF0F172A),
    background = Color(0xFF0F172A), // very dark slate
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF1E293B), // card/surface background in dark
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFEFF6FF)
)

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2563EB), // standard primary blue
    onPrimary = Color.White,
    background = Color(0xFFF8FAFC), // soft off-white slate
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E40AF)
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Force Dark Theme and English UI globally
            val isDarkTheme = true
            val isBangla = false

            val colorScheme = DarkColorScheme
            val typography = getTypography(isBangla)

            MaterialTheme(
                colorScheme = colorScheme,
                typography = typography
            ) {
                MainAppContainer()
            }
        }
    }

    @Composable
    fun MainAppContainer() {
        val isDarkTheme = true
        val isBangla = false

        var medicines by remember { mutableStateOf<List<Medicine>?>(null) }
        var uniqueBrands by remember { mutableStateOf<List<String>?>(null) }
        var genericsMetadata by remember { mutableStateOf<Map<String, GenericMetadata>?>(null) }
        var genericDetails by remember { mutableStateOf<Map<String, GenericDetail>?>(null) }
        var isLoading by remember { mutableStateOf(true) }

        // Update states
        val context = androidx.compose.ui.platform.LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        var lastUpdatedDate by remember { mutableStateOf(DatabaseManager.getLastUpdatedDate(context)) }
        var isUpdating by remember { mutableStateOf(false) }
        var updateProgress by remember { java.lang.Float.valueOf(0f); mutableFloatStateOf(0f) }
        var updateError by remember { mutableStateOf<String?>(null) }
        var showSaturdayPrompt by remember { mutableStateOf(false) }

        fun triggerDatabaseUpdate() {
            if (isUpdating) return
            isUpdating = true
            updateProgress = 0f
            updateError = null
            coroutineScope.launch(Dispatchers.IO) {
                val result = DatabaseManager.downloadLatestDatabase(context) { progress ->
                    coroutineScope.launch(Dispatchers.Main) {
                        updateProgress = progress
                    }
                }
                withContext(Dispatchers.Main) {
                    isUpdating = false
                    result.fold(
                        onSuccess = { updatedMeds ->
                            medicines = updatedMeds
                            uniqueBrands = updatedMeds.map { it.brand }.distinct().sorted()
                            lastUpdatedDate = DatabaseManager.getLastUpdatedDate(context)
                        },
                        onFailure = { error ->
                            updateError = error.localizedMessage ?: "Failed to update database"
                        }
                    )
                }
            }
        }

        // Load database asynchronously
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                try {
                    val gson = Gson()

                    // 1. Load medicines using DatabaseManager (from list.json or asset fallback)
                    val loadedMedicines: List<Medicine> = DatabaseManager.loadMedicines(context)

                    // Extract unique brands for Levenshtein fallback matching
                    val brands = loadedMedicines.map { it.brand }.distinct().sorted()

                    // 2. Load generics metadata
                    val genericStream = assets.open("generics_processed.json")
                    val genericReader = InputStreamReader(genericStream)
                    val genericType = object : TypeToken<Map<String, GenericMetadata>>() {}.type
                    val loadedGenerics: Map<String, GenericMetadata> = gson.fromJson(genericReader, genericType)
                    genericReader.close()
                    genericStream.close()

                    // 3. Load detailed generic information
                    val genericDetailStream = assets.open("generic_data.json")
                    val genericDetailReader = InputStreamReader(genericDetailStream)
                    val genericDetailType = object : TypeToken<List<GenericDetail>>() {}.type
                    val loadedDetails: List<GenericDetail> = gson.fromJson(genericDetailReader, genericDetailType)
                    genericDetailReader.close()
                    genericDetailStream.close()
                    val detailsMap = loadedDetails.associateBy { it.genericName.lowercase(java.util.Locale.ROOT) }

                    // Update state
                    medicines = loadedMedicines
                    uniqueBrands = brands
                    genericsMetadata = loadedGenerics
                    genericDetails = detailsMap

                    // Check Saturday automatic update prompt
                    if (DatabaseManager.shouldPromptSaturdayUpdate(context)) {
                        withContext(Dispatchers.Main) {
                            showSaturdayPrompt = true
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isLoading = false
                }
            }
        }

        // Saturday Update Dialog Prompt
        if (showSaturdayPrompt) {
            AlertDialog(
                onDismissRequest = {
                    showSaturdayPrompt = false
                    DatabaseManager.markSaturdayPrompted(context)
                },
                title = {
                    Text(
                        text = "Weekly Database Update",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Text(
                        text = "It's Saturday! A new database update is available. Would you like to update the medicine list now?",
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showSaturdayPrompt = false
                            triggerDatabaseUpdate()
                        }
                    ) {
                        Text("Update Now", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showSaturdayPrompt = false
                            DatabaseManager.markSaturdayPrompted(context)
                        }
                    ) {
                        Text("Later")
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                textContentColor = MaterialTheme.colorScheme.onSurface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        }

        if (isLoading || medicines == null || genericsMetadata == null || uniqueBrands == null || genericDetails == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_launcher),
                    contentDescription = "DMF Meds",
                    modifier = Modifier.size(128.dp)
                )
            }
        } else {
            // Selected bottom navigation tab
            // tab 0 = Information, tab 1 = Medicines (middle), tab 2 = Fatawas
            var selectedTab by remember { mutableIntStateOf(1) }

            // Search Active states hoisted to clear from system back button
            var isSearchActive by remember { mutableStateOf(false) }
            var queryState = remember { mutableStateOf("") }
            var selectedMedicineState = remember { mutableStateOf<Medicine?>(null) }

            // Interaction Checker Open State
            var isInteractionCheckerOpen by remember { mutableStateOf(false) }

            // Back Exit Confirmation Dialog State
            var showExitDialog by remember { mutableStateOf(false) }

            // Dynamic BackHandler
            BackHandler(enabled = true) {
                if (isInteractionCheckerOpen) {
                    isInteractionCheckerOpen = false
                } else if (selectedTab == 1 && (isSearchActive || queryState.value.isNotEmpty() || selectedMedicineState.value != null)) {
                    // Reset Medicine search state
                    isSearchActive = false
                    queryState.value = ""
                    selectedMedicineState.value = null
                } else if (selectedTab == 0 || selectedTab == 2) {
                    // Go back to home/search page
                    selectedTab = 1
                } else {
                    showExitDialog = true
                }
            }

            // Exit Confirmation Dialog
            if (showExitDialog) {
                AlertDialog(
                    onDismissRequest = { showExitDialog = false },
                    title = {
                        Text(
                            text = Trans.exitTitle(isBangla),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    },
                    text = {
                        Text(
                            text = Trans.exitText(isBangla),
                            fontSize = 15.sp
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showExitDialog = false
                                finish() // Terminate activity / Exit app
                            }
                        ) {
                            Text(
                                text = Trans.yesLabel(isBangla),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showExitDialog = false }) {
                            Text(text = Trans.noLabel(isBangla))
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    textContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            }

            Scaffold(
                bottomBar = {
                    if (!isInteractionCheckerOpen) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            val tabs = listOf(
                                Triple(0, Icons.Default.Info, Trans.information(isBangla)),
                                Triple(1, Icons.Default.MedicalServices, Trans.medicines(isBangla)),
                                Triple(2, Icons.Default.Book, Trans.fatawas(isBangla))
                            )

                            tabs.forEach { (index, icon, label) ->
                                val isSelected = selectedTab == index
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { selectedTab = index },
                                    icon = {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .padding(if (isInteractionCheckerOpen) PaddingValues(0.dp) else innerPadding)
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    if (isInteractionCheckerOpen) {
                        InteractionCheckerScreen(
                            medicines = medicines!!,
                            prefilledMedicine = selectedMedicineState.value,
                            onBack = { isInteractionCheckerOpen = false }
                        )
                    } else {
                        when (selectedTab) {
                            0 -> InfoScreen()
                            1 -> MedicineScreen(
                                medicines = medicines!!,
                                uniqueBrands = uniqueBrands!!,
                                genericsMetadata = genericsMetadata!!,
                                genericDetails = genericDetails!!,
                                isBangla = isBangla,
                                isSearchFocused = isSearchActive,
                                onSearchFocusedChange = { isSearchActive = it },
                                queryState = queryState,
                                selectedMedicineState = selectedMedicineState,
                                onOpenInteractionChecker = { isInteractionCheckerOpen = true },
                                lastUpdatedDate = lastUpdatedDate,
                                isUpdating = isUpdating,
                                updateProgress = updateProgress,
                                updateError = updateError,
                                onTriggerUpdate = { triggerDatabaseUpdate() }
                            )
                            2 -> FatawasScreen()
                        }
                    }
                }
            }
        }
    }
}
