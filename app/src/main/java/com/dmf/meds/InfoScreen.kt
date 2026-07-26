package com.dmf.meds

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import java.io.BufferedReader
import java.io.InputStreamReader

data class InfoLink(
    val titleKey: (Boolean) -> String,
    val descriptionKey: (Boolean) -> String,
    val isArticle: Boolean,
    val articleType: ArticleType,
    val url: String = ""
)

enum class ArticleType {
    WEBVIEW,
    BMDC_ACT,
    APPROVED_LIST,
    OTC_LIST,
    ANTIBIOTIC_LIST,
    WHO_ESSENTIAL_LIST
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen() {
    val isBangla = false // Force English UI controls
    var activeArticleType by remember { mutableStateOf<ArticleType?>(null) }
    var activeTitle by remember { mutableStateOf("") }
    var activeUrl by remember { mutableStateOf("") }

    // Navigation fix: handle back presses inside InfoScreen when an article is open
    BackHandler(enabled = activeArticleType != null) {
        activeArticleType = null
    }

    val links = remember {
        listOf(
            InfoLink(
                titleKey = { Trans.bmdcCheckTitle(it) },
                descriptionKey = { Trans.bmdcCheckDesc(it) },
                isArticle = false,
                articleType = ArticleType.WEBVIEW,
                url = "https://www.bmdc.org.bd/search-mats"
            ),
            InfoLink(
                titleKey = { Trans.drugActTitle(it) },
                descriptionKey = { Trans.drugActDesc(it) },
                isArticle = true,
                articleType = ArticleType.BMDC_ACT
            ),
            InfoLink(
                titleKey = { Trans.approvedListTitle(it) },
                descriptionKey = { Trans.approvedListDesc(it) },
                isArticle = true,
                articleType = ArticleType.APPROVED_LIST
            ),
            InfoLink(
                titleKey = { Trans.otcListTitle(it) },
                descriptionKey = { Trans.otcListDesc(it) },
                isArticle = true,
                articleType = ArticleType.OTC_LIST
            ),
            InfoLink(
                titleKey = { if (it) "অ্যান্টিবায়োটিক নির্দেশিকা" else "Antibiotic Guidelines" },
                descriptionKey = { if (it) "বিএমডিসি অনুমোদিত এবং বহুল ব্যবহৃত অ্যান্টিবায়োটিকসমূহের তালিকা" else "BM&DC approved and common antibiotics reference guide" },
                isArticle = true,
                articleType = ArticleType.ANTIBIOTIC_LIST
            ),
            InfoLink(
                titleKey = { if (it) "ডব্লিউএইচও অত্যাবশ্যকীয় ঔষধ" else "WHO Essential Medicines" },
                descriptionKey = { if (it) "বিশ্ব স্বাস্থ্য সংস্থা (WHO) অনুমোদিত অত্যাবশ্যকীয় ঔষধের বৈশ্বিক তালিকা" else "World Health Organization list of essential medicines" },
                isArticle = true,
                articleType = ArticleType.WHO_ESSENTIAL_LIST
            )
        )
    }

    if (activeArticleType != null) {
        // Show active article view
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            TopAppBar(
                title = {
                    DMFText(
                        text = activeTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { activeArticleType = null }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )

            when (activeArticleType) {
                ArticleType.WEBVIEW -> {
                    AppWebView(url = activeUrl)
                }
                ArticleType.BMDC_ACT -> {
                    // Fully compiled native page
                    DrugActScreen(onBack = { activeArticleType = null })
                }
                ArticleType.APPROVED_LIST -> {
                    DrugListArticleView(filename = "allowed.txt", isAntibioticList = false)
                }
                ArticleType.OTC_LIST -> {
                    DrugListArticleView(filename = "OTC.txt", isAntibioticList = false)
                }
                ArticleType.ANTIBIOTIC_LIST -> {
                    DrugListArticleView(filename = "antibiotics.txt", isAntibioticList = true)
                }
                ArticleType.WHO_ESSENTIAL_LIST -> {
                    DrugListArticleView(filename = "who_essential_medicines.txt", isAntibioticList = false)
                }
                else -> {}
            }
        }
    } else {
        // Show main 2-column grid of buttons/cards
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DMFText(
                    text = Trans.infoDesk(isBangla),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            DMFText(
                text = Trans.infoSubtext(isBangla),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(links.size) { index ->
                    val link = links[index]
                    ElevatedCard(
                        onClick = {
                            activeTitle = link.titleKey(isBangla)
                            activeArticleType = link.articleType
                            activeUrl = link.url
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                DMFText(
                                    text = link.titleKey(isBangla),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                DMFText(
                                    text = link.descriptionKey(isBangla),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 12.sp
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Launch,
                                    contentDescription = "Open",
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

data class DrugItem(
    val originalIndex: Int,
    val name: String,
    val lowerName: String,
    val indication: String,
    val lowerIndication: String,
    val bestUsedFor: String,
    val lowerBestUsedFor: String
)

@Composable
fun DrugListArticleView(filename: String, isAntibioticList: Boolean = false) {
    val context = LocalContext.current
    var drugsList by remember { mutableStateOf<List<DrugItem>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(filename) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val assetStream = context.assets.open(filename)
                val reader = BufferedReader(InputStreamReader(assetStream))
                val list = mutableListOf<DrugItem>()
                var line: String? = reader.readLine()
                var index = 1
                while (line != null) {
                    val trimmed = line.trim()
                    if (trimmed.isNotEmpty()) {
                        // Extract name by removing leading digit numbering (e.g. "1. Aspirin" -> "Aspirin")
                        val cleaned = trimmed.replaceFirst(Regex("^\\d+\\.\\s*"), "")
                        val indication = Indications.getBanglaIndication(cleaned)
                        val bestUsedFor = if (isAntibioticList) Indications.getBestUsedFor(cleaned) else ""
                        list.add(
                            DrugItem(
                                originalIndex = index,
                                name = cleaned,
                                lowerName = cleaned.lowercase(java.util.Locale.ROOT),
                                indication = indication,
                                lowerIndication = indication.lowercase(java.util.Locale.ROOT),
                                bestUsedFor = bestUsedFor,
                                lowerBestUsedFor = bestUsedFor.lowercase(java.util.Locale.ROOT)
                            )
                        )
                        index++
                    }
                    line = reader.readLine()
                }
                reader.close()
                assetStream.close()
                drugsList = list
            } catch (e: Exception) {
                drugsList = listOf(
                    DrugItem(
                        originalIndex = 1,
                        name = "Error loading list",
                        lowerName = "error loading list",
                        indication = e.message ?: "",
                        lowerIndication = (e.message ?: "").lowercase(java.util.Locale.ROOT),
                        bestUsedFor = "",
                        lowerBestUsedFor = ""
                    )
                )
            }
        }
    }

    val filteredDrugs = remember(searchQuery, drugsList) {
        val q = searchQuery.trim().lowercase(java.util.Locale.ROOT)
        if (q.isEmpty()) {
            drugsList
        } else {
            drugsList.filter { drug ->
                drug.lowerName.contains(q) ||
                drug.lowerIndication.contains(q) ||
                (isAntibioticList && drug.lowerBestUsedFor.contains(q))
            }
        }
    }

    if (drugsList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                placeholder = {
                    Text(
                        text = "ঔষধের নাম বা রোগের উপসর্গ দিয়ে খুঁজুন...",
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

            if (filteredDrugs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    DMFText("কোনো ঔষধ পাওয়া যায়নি।", color = MaterialTheme.colorScheme.onSurfaceVariant, forceKalpurush = true)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    items(
                        items = filteredDrugs,
                        key = { it.originalIndex }
                    ) { drug ->
                        val originalIndex = drug.originalIndex
                        val indication = drug.indication

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(6.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = originalIndex.toString(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    DMFText(
                                        text = drug.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        forceKalpurush = true
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                DMFText(
                                    text = indication,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    forceKalpurush = true
                                )

                                if (isAntibioticList) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    DMFText(
                                        text = "বিশেষ কার্যকারিতা (Best used for):",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        forceKalpurush = true
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    DMFText(
                                        text = drug.bestUsedFor,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        forceKalpurush = true
                                    )
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
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AppWebView(url: String) {
    var isLoading by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        supportZoom()
                        builtInZoomControls = true
                        displayZoomControls = false
                    }
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            request?.url?.let { view?.loadUrl(it.toString()) }
                            return true
                        }
                    }
                    loadUrl(url)
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { _ -> }
        )

        if (isLoading) {
            CircularProgressIndicator()
        }
    }
}
