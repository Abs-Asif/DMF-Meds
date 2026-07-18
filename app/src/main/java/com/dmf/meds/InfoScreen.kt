package com.dmf.meds

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

data class InfoLink(
    val title: String,
    val description: String,
    val url: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen() {
    var activeUrl by remember { mutableStateOf<String?>(null) }
    var activeTitle by remember { mutableStateOf("") }

    val links = remember {
        listOf(
            InfoLink(
                title = "BM&DC Check",
                description = "Search MATS / DMF practitioner registration status",
                url = "https://www.bmdc.org.bd/search-mats"
            ),
            InfoLink(
                title = "Drug & Cosmetic Act 2023",
                description = "Official bdlaws portal for Drug and Cosmetic Act",
                url = "http://bdlaws.minlaw.gov.bd/act-details-1447.html?lang=en"
            ),
            InfoLink(
                title = "Approved Drug List",
                description = "Official BM&DC list of medicines for medical assistants",
                url = "https://www.bmdc.org.bd/docs/List-of-Medicine-Medical-Asst.pdf"
            )
        )
    }

    if (activeUrl != null) {
        // Show WebView
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        text = activeTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { activeUrl = null }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
            AppWebView(url = activeUrl!!)
        }
    } else {
        // Show 2-column grid of buttons/cards
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Information Desk",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Quick access to official resources, regulations, and registration registries.",
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
                            activeUrl = link.url
                            activeTitle = link.title
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
                                    text = link.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 20.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = link.description,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 14.sp
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Launch,
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
            update = { webView ->
                // Keep url updated if needed
            }
        )

        if (isLoading) {
            CircularProgressIndicator()
        }
    }
}
