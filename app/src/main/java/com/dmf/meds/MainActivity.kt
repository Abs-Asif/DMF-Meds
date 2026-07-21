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
        var isLoading by remember { mutableStateOf(true) }

        // Load database asynchronously
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                try {
                    val gson = Gson()

                    // 1. Load medicines
                    val medicStream = assets.open("medic_data.json")
                    val reader = InputStreamReader(medicStream)
                    val medicType = object : TypeToken<List<Medicine>>() {}.type
                    val loadedMedicines: List<Medicine> = gson.fromJson(reader, medicType)
                    reader.close()
                    medicStream.close()

                    // Extract unique brands for Levenshtein fallback matching
                    val brands = loadedMedicines.map { it.brand }.distinct().sorted()

                    // 2. Load generics metadata
                    val genericStream = assets.open("generics_processed.json")
                    val genericReader = InputStreamReader(genericStream)
                    val genericType = object : TypeToken<Map<String, GenericMetadata>>() {}.type
                    val loadedGenerics: Map<String, GenericMetadata> = gson.fromJson(genericReader, genericType)
                    genericReader.close()
                    genericStream.close()

                    // Update state
                    medicines = loadedMedicines
                    uniqueBrands = brands
                    genericsMetadata = loadedGenerics
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isLoading = false
                }
            }
        }

        if (isLoading || medicines == null || genericsMetadata == null || uniqueBrands == null) {
            // Live premium blurred drifting bubble background on splash screen!
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                MovingBlurredBubblesBackground()

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Logo Box with subtle elevation shadow/glow effect
                    Surface(
                        modifier = Modifier.size(96.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = "DMF Meds",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "DMF Meds",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "MATS / DMF Clinical Companion",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Loading Clinical Registry...",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                }
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
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            tonalElevation = 8.dp,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val tabs = listOf(
                                    Triple(0, Icons.Default.Info, Trans.information(isBangla)),
                                    Triple(1, Icons.Default.MedicalServices, Trans.medicines(isBangla)),
                                    Triple(2, Icons.Default.Book, Trans.fatawas(isBangla))
                                )

                                tabs.forEach { (index, icon, label) ->
                                    val isSelected = selectedTab == index

                                    Box(
                                        modifier = Modifier
                                            .clickable(
                                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                                indication = null
                                            ) { selectedTab = index }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = if (isSelected) {
                                                Modifier
                                                    .background(
                                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                                        shape = RoundedCornerShape(16.dp)
                                                    )
                                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                                            } else {
                                                Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                            }
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = label,
                                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                                modifier = Modifier.size(if (isSelected) 28.dp else 22.dp)
                                            )
                                            if (isSelected) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = label,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
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
                            onBack = { isInteractionCheckerOpen = false }
                        )
                    } else {
                        when (selectedTab) {
                            0 -> InfoScreen()
                            1 -> MedicineScreen(
                                medicines = medicines!!,
                                uniqueBrands = uniqueBrands!!,
                                genericsMetadata = genericsMetadata!!,
                                isBangla = isBangla,
                                isSearchFocused = isSearchActive,
                                onSearchFocusedChange = { isSearchActive = it },
                                queryState = queryState,
                                selectedMedicineState = selectedMedicineState,
                                onOpenInteractionChecker = { isInteractionCheckerOpen = true }
                            )
                            2 -> FatawasScreen()
                        }
                    }
                }
            }
        }
    }
}
