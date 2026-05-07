package com.mockcat.logger.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mockcat.api.http.LoggedHttpCall

/**
 * HTTP traffic list: method, path summary, and status.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HttpLogListScreen(
    title: String = "HTTP log",
    calls: List<LoggedHttpCall>,
    onBack: (() -> Unit)? = null,
    onCallClick: (Long) -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Text("←", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            items(calls, key = { it.id }) { call ->
                val r = call.response
                val sub =
                    r?.let { "${it.statusCode} · ${it.reasonPhrase.orEmpty()}" } ?: (call.error ?: "…")
                ListItem(
                    headlineContent = {
                        Text(
                            call.request.method + " " + summarizeUrl(call.request.url),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    supportingContent = {
                        Text(
                            sub,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                        .clickable { onCallClick(call.id) },
                )
            }
        }
    }
}

private fun summarizeUrl(url: String): String = try {
    val u = url.substringAfter("://", url)
    u.substringAfter("/", "/")
} catch (_: Exception) {
    url
}
