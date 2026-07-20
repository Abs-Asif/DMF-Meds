package com.dmf.meds

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

data class ActSection(
    val number: String,
    val title: String,
    val body: String,
    val chapterTitle: String
)

data class ActChapter(
    val title: String,
    val sections: List<ActSection>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrugActScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var chapters by remember { mutableStateOf<List<ActChapter>>(emptyList()) }
    var allSections by remember { mutableStateOf<List<ActSection>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedChapterIndex by remember { mutableStateOf(-1) }

    val listState = rememberLazyListState()

    // Load and parse the Act asynchronously
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val assetStream = context.assets.open("bmdc_act.md")
                val reader = BufferedReader(InputStreamReader(assetStream))
                val rawLines = reader.readLines()
                reader.close()
                assetStream.close()

                val parsedChapters = mutableListOf<ActChapter>()
                var currentChapterTitle = "প্রারম্ভিক"
                var currentSections = mutableListOf<ActSection>()

                var lastSectionNum = ""
                var lastSectionTitle = ""
                val lastSectionBody = StringBuilder()

                fun flushLastSection() {
                    if (lastSectionNum.isNotEmpty()) {
                        currentSections.add(
                            ActSection(
                                number = lastSectionNum,
                                title = lastSectionTitle,
                                body = lastSectionBody.toString().trim(),
                                chapterTitle = currentChapterTitle
                            )
                        )
                        lastSectionNum = ""
                        lastSectionTitle = ""
                        lastSectionBody.clear()
                    }
                }

                fun flushLastChapter() {
                    flushLastSection()
                    if (currentSections.isNotEmpty() || currentChapterTitle != "প্রারম্ভিক") {
                        parsedChapters.add(
                            ActChapter(
                                title = currentChapterTitle,
                                sections = currentSections.toList()
                            )
                        )
                        currentSections = mutableListOf()
                    }
                }

                for (line in rawLines) {
                    val trimmed = line.trim()
                    if (trimmed.isEmpty()) continue

                    // Check for Chapter headings (e.g., "প্রথম অধ্যায়", "দ্বিতীয় অধ্যায়" or starting with "# " / "## ")
                    if (trimmed.contains("অধ্যায়") && !trimmed.contains("ধারা")) {
                        flushLastChapter()
                        currentChapterTitle = trimmed.replace("#", "").trim()
                    } else if (trimmed.matches(Regex("^\\d+।.*")) || trimmed.matches(Regex("^[১২৩৪৫৬৭৮৯০]+।.*"))) {
                        // Section detected: starts with number followed by '।'
                        flushLastSection()
                        val parts = trimmed.split("।", limit = 2)
                        lastSectionNum = parts[0].trim() + "।"
                        lastSectionTitle = if (parts.size > 1) parts[1].trim() else ""
                    } else {
                        // Append to section body
                        if (lastSectionNum.isNotEmpty()) {
                            lastSectionBody.append(trimmed).append("\n")
                        } else {
                            // General intro text or preamble
                            if (trimmed.startsWith("# ") || trimmed.startsWith("## ")) {
                                currentChapterTitle = trimmed.replace("#", "").trim()
                            } else {
                                lastSectionBody.append(trimmed).append("\n")
                            }
                        }
                    }
                }
                flushLastChapter()

                chapters = parsedChapters
                allSections = parsedChapters.flatMap { it.sections }
                isLoading = false
            } catch (e: Exception) {
                e.printStackTrace()
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // Filter sections based on search query or chapter selection
            val filteredSections = remember(searchQuery, selectedChapterIndex, chapters, allSections) {
                var list = if (selectedChapterIndex >= 0 && selectedChapterIndex < chapters.size) {
                    chapters[selectedChapterIndex].sections
                } else {
                    allSections
                }

                if (searchQuery.trim().isNotEmpty()) {
                    val query = searchQuery.lowercase().trim()
                    list = list.filter { sec ->
                        sec.number.lowercase().contains(query) ||
                                sec.title.lowercase().contains(query) ||
                                sec.body.lowercase().contains(query)
                    }
                }
                list
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                placeholder = {
                    Text(
                        text = "ধারার নম্বর বা শব্দ দিয়ে খুঁজুন...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )

            // Horizontal Scrollable Chapter Tabs
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedChapterIndex == -1,
                        onClick = { selectedChapterIndex = -1 },
                        label = { DMFText("সব ধারা", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }
                itemsIndexed(chapters) { index, chapter ->
                    FilterChip(
                        selected = selectedChapterIndex == index,
                        onClick = { selectedChapterIndex = index },
                        label = { DMFText(chapter.title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main sections list
            if (filteredSections.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    DMFText("কোনো ধারা পাওয়া যায়নি।", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(filteredSections) { index, section ->
                        var isExpanded by remember(section) { mutableStateOf(false) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isExpanded = !isExpanded },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        DMFText(
                                            text = section.chapterTitle,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        DMFText(
                                            text = "${section.number} ${section.title}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (isExpanded) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    DMFText(
                                        text = section.body,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
