package com.dmf.meds

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
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

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainAppContainer()
            }
        }
    }

    @Composable
    fun MainAppContainer() {
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
            // High-quality startup/loading screen
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = "DMF Meds",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "DMF Meds",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Loading Medicine Database...",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    CircularProgressIndicator()
                }
            }
        } else {
            // Main Bottom Navigation Scaffold
            var selectedTab by remember { mutableIntStateOf(0) }

            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = Color.White,
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            label = { Text("Medicines", fontWeight = FontWeight.SemiBold) },
                            icon = {
                                Icon(
                                    Icons.Default.MedicalServices,
                                    contentDescription = "Medicines"
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = Color.Gray,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedTextColor = Color.Gray
                            )
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            label = { Text("Information", fontWeight = FontWeight.SemiBold) },
                            icon = { Icon(Icons.Default.Info, contentDescription = "Information") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = Color.Gray,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedTextColor = Color.Gray
                            )
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            label = { Text("Fatawas", fontWeight = FontWeight.SemiBold) },
                            icon = { Icon(Icons.Default.Book, contentDescription = "Fatawas") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = Color.Gray,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedTextColor = Color.Gray
                            )
                        )
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    when (selectedTab) {
                        0 -> MedicineScreen(
                            medicines = medicines!!,
                            uniqueBrands = uniqueBrands!!,
                            genericsMetadata = genericsMetadata!!
                        )
                        1 -> InfoScreen()
                        2 -> FatawasScreen()
                    }
                }
            }
        }
    }
}
