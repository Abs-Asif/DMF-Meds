package com.dmf.meds

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
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
    OTC_LIST
}

@Composable
fun parseBoldText(text: String): AnnotatedString {
    return buildAnnotatedString {
        val parts = text.split("**")
        for (i in parts.indices) {
            if (i % 2 == 1) {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(parts[i])
                }
            } else {
                append(parts[i])
            }
        }
    }
}

@Composable
fun MarkdownText(
    text: String,
    isBangla: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight? = null,
    lineHeight: TextUnit = 20.sp
) {
    val family = getAppFontFamily(isBangla)
    Text(
        text = parseBoldText(text),
        fontFamily = family,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color,
        lineHeight = lineHeight,
        modifier = modifier
    )
}

@Composable
fun MarkdownBody(markdown: String, isBangla: Boolean) {
    val lines = markdown.lineSequence()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        lines.forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.isEmpty() -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                trimmed.startsWith("# ") -> {
                    MarkdownText(
                        text = trimmed.substring(2),
                        isBangla = isBangla,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
                        lineHeight = 30.sp
                    )
                }
                trimmed.startsWith("## ") -> {
                    MarkdownText(
                        text = trimmed.substring(3),
                        isBangla = isBangla,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                        lineHeight = 26.sp
                    )
                }
                trimmed.startsWith("### ") -> {
                    MarkdownText(
                        text = trimmed.substring(4),
                        isBangla = isBangla,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
                        lineHeight = 22.sp
                    )
                }
                trimmed.startsWith("* ") || trimmed.startsWith("- ") -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "• ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = getAppFontFamily(isBangla)
                        )
                        MarkdownText(
                            text = trimmed.substring(2),
                            isBangla = isBangla,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                trimmed.startsWith("1. ") || trimmed.startsWith("২. ") || (trimmed.firstOrNull()?.isDigit() == true && trimmed.contains(". ")) -> {
                    val index = trimmed.indexOf(". ")
                    val num = trimmed.substring(0, index + 2)
                    val rest = trimmed.substring(index + 2)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = num,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = getAppFontFamily(isBangla)
                        )
                        MarkdownText(
                            text = rest,
                            isBangla = isBangla,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                else -> {
                    MarkdownText(
                        text = trimmed,
                        isBangla = isBangla,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(
    isBangla: Boolean,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onLanguageToggle: () -> Unit
) {
    var activeArticleType by remember { mutableStateOf<ArticleType?>(null) }
    var activeTitle by remember { mutableStateOf("") }
    var activeUrl by remember { mutableStateOf("") }

    var showSettingsDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

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
            )
        )
    }

    // Settings Popup Dialog
    if (showSettingsDialog) {
        Dialog(onDismissRequest = { showSettingsDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = Trans.settingsTitle(isBangla),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Theme Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Trans.themeLabel(isBangla),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isDarkTheme) Trans.darkModeLabel(isBangla) else Trans.lightModeLabel(isBangla),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = { onThemeToggle() }
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Language Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Trans.languageLabel(isBangla),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isBangla) "বাংলা" else "English",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Switch(
                                checked = isBangla,
                                onCheckedChange = { onLanguageToggle() }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        Button(onClick = { showSettingsDialog = false }) {
                            Text(text = Trans.closeLabel(isBangla))
                        }
                    }
                }
            }
        }
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
                    Text(
                        text = activeTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = getAppFontFamily(isBangla)
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
                    ArticleReaderView(filename = "bmdc_act.md", isBangla = isBangla)
                }
                ArticleType.APPROVED_LIST -> {
                    DrugListArticleView(filename = "allowed.txt", isBangla = isBangla)
                }
                ArticleType.OTC_LIST -> {
                    DrugListArticleView(filename = "OTC.txt", isBangla = isBangla)
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
                Text(
                    text = Trans.infoDesk(isBangla),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                // Settings button
                IconButton(onClick = { showSettingsDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                text = Trans.infoSubtext(isBangla),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
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
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    text = link.titleKey(isBangla),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontFamily = getAppFontFamily(isBangla)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = link.descriptionKey(isBangla),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 14.sp,
                                    fontFamily = getAppFontFamily(isBangla)
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

@Composable
fun ArticleReaderView(filename: String, isBangla: Boolean) {
    val context = LocalContext.current
    var content by remember { mutableStateOf("") }

    LaunchedEffect(filename) {
        try {
            val assetStream = context.assets.open(filename)
            val reader = BufferedReader(InputStreamReader(assetStream))
            val sb = StringBuilder()
            var line: String? = reader.readLine()
            while (line != null) {
                sb.append(line).append("\n")
                line = reader.readLine()
            }
            reader.close()
            assetStream.close()
            content = sb.toString()
        } catch (e: Exception) {
            content = "Error loading article: ${e.message}"
        }
    }

    if (content.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                MarkdownBody(markdown = content, isBangla = isBangla)
            }
        }
    }
}

@Composable
fun DrugListArticleView(filename: String, isBangla: Boolean) {
    val context = LocalContext.current
    var drugsList by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(filename) {
        try {
            val assetStream = context.assets.open(filename)
            val reader = BufferedReader(InputStreamReader(assetStream))
            val list = mutableListOf<String>()
            var line: String? = reader.readLine()
            while (line != null) {
                val trimmed = line.trim()
                if (trimmed.isNotEmpty()) {
                    // Extract name by removing leading digit numbering (e.g. "1. Aspirin" -> "Aspirin")
                    val cleaned = trimmed.replaceFirst(Regex("^\\d+\\.\\s*"), "")
                    list.add(cleaned)
                }
                line = reader.readLine()
            }
            reader.close()
            assetStream.close()
            drugsList = list
        } catch (e: Exception) {
            drugsList = listOf("Error loading list: ${e.message}")
        }
    }

    if (drugsList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(drugsList.size) { index ->
                val drug = drugsList[index]
                val indication = Indications.getBanglaIndication(drug)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
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
                                    text = (index + 1).toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = drug,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isBangla) "নির্দেশনা ও ব্যবহার:" else "Indication & Usage:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = getAppFontFamily(isBangla)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = indication,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = getAppFontFamily(isBangla)
                        )
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
