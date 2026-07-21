package com.dmf.meds

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

sealed class ActElement {
    data class Title(val text: String) : ActElement()
    data class Subtitle(val text: String) : ActElement()
    data class Date(val text: String) : ActElement()
    data class ChapterHeader(val text: String) : ActElement()
    data class SectionHeader(val text: String) : ActElement()
    data class SectionContent(val text: String) : ActElement()
    data class Subsection(val text: String, val indentLevel: Int) : ActElement()
    data class Paragraph(val text: String) : ActElement()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrugActScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var elements by remember { mutableStateOf<List<ActElement>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    val listState = rememberLazyListState()

    // Parse and cache the legal act asynchronously
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val assetStream = context.assets.open("bmdc_act.md")
                val reader = BufferedReader(InputStreamReader(assetStream))
                val rawLines = reader.readLines().map { it.trim() }.filter { it.isNotEmpty() }
                reader.close()
                assetStream.close()

                val parsedList = mutableListOf<ActElement>()

                for (i in rawLines.indices) {
                    val line = rawLines[i]

                    when {
                        i == 0 -> {
                            parsedList.add(ActElement.Title(line))
                        }
                        line.startsWith("(") && line.endsWith(")") -> {
                            parsedList.add(ActElement.Subtitle(line))
                        }
                        line.startsWith("[") && line.endsWith("]") -> {
                            parsedList.add(ActElement.Date(line))
                        }
                        line.contains("অধ্যায়") -> {
                            parsedList.add(ActElement.ChapterHeader(line))
                        }
                        line.matches(Regex("^[১২৩৪৫৬৭৮৯০]+।.*")) -> {
                            // Section content line, e.g. "১। (১) এই আইন..."
                            parsedList.add(ActElement.SectionContent(line))
                        }
                        line.startsWith("(") && (line.contains(")") || line.contains("।")) -> {
                            // Subsection e.g. "(১) ...", "(ক) ...", "(অ) ..."
                            val indent = if (line.startsWith("(অ") || line.startsWith("(আ") || line.startsWith("(ই")) 2 else 1
                            parsedList.add(ActElement.Subsection(line, indent))
                        }
                        else -> {
                            // Check if next line is a section number. If so, this line is a section header!
                            val nextLine = rawLines.getOrNull(i + 1) ?: ""
                            if (nextLine.matches(Regex("^[১২৩৪৫৬৭৮৯০]+।.*"))) {
                                parsedList.add(ActElement.SectionHeader(line))
                            } else {
                                parsedList.add(ActElement.Paragraph(line))
                            }
                        }
                    }
                }

                elements = parsedList
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
            // Real-time search filter over sections/paragraphs
            val filteredElements = remember(searchQuery, elements) {
                if (searchQuery.trim().isEmpty()) {
                    elements
                } else {
                    val query = searchQuery.lowercase().trim()
                    elements.filter { el ->
                        when (el) {
                            is ActElement.Title -> el.text.lowercase().contains(query)
                            is ActElement.Subtitle -> el.text.lowercase().contains(query)
                            is ActElement.ChapterHeader -> el.text.lowercase().contains(query)
                            is ActElement.SectionHeader -> el.text.lowercase().contains(query)
                            is ActElement.SectionContent -> el.text.lowercase().contains(query)
                            is ActElement.Subsection -> el.text.textLowercaseContains(query)
                            is ActElement.Paragraph -> el.text.lowercase().contains(query)
                            else -> false
                        }
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
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
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            // Main scrollable content
            if (filteredElements.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    DMFText("কোনো ধারা পাওয়া যায়নি।", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    items(filteredElements) { element ->
                        when (element) {
                            is ActElement.Title -> {
                                DMFText(
                                    text = element.text,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 6.dp),
                                    forceKalpurush = true
                                )
                            }
                            is ActElement.Subtitle -> {
                                DMFText(
                                    text = element.text,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 6.dp),
                                    forceKalpurush = true
                                )
                            }
                            is ActElement.Date -> {
                                DMFText(
                                    text = element.text,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    forceKalpurush = true
                                )
                            }
                            is ActElement.ChapterHeader -> {
                                Spacer(modifier = Modifier.height(24.dp))
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    DMFText(
                                        text = element.text,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center,
                                        forceKalpurush = true
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    HorizontalDivider(
                                        modifier = Modifier.width(60.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                        thickness = 2.dp
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            is ActElement.SectionHeader -> {
                                Spacer(modifier = Modifier.height(12.dp))
                                DMFText(
                                    text = element.text,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.fillMaxWidth(),
                                    forceKalpurush = true
                                )
                            }
                            is ActElement.SectionContent -> {
                                DMFText(
                                    text = element.text,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.fillMaxWidth(),
                                    forceKalpurush = true
                                )
                            }
                            is ActElement.Subsection -> {
                                val leadingPadding = if (element.indentLevel == 2) 28.dp else 14.dp
                                DMFText(
                                    text = element.text,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = leadingPadding, top = 2.dp, bottom = 2.dp),
                                    forceKalpurush = true
                                )
                            }
                            is ActElement.Paragraph -> {
                                DMFText(
                                    text = element.text,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Normal,
                                    lineHeight = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth(),
                                    forceKalpurush = true
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

// Extension function to prevent smart casting issues in kotlin
private fun String.textLowercaseContains(query: String): Boolean {
    return this.lowercase().contains(query)
}
