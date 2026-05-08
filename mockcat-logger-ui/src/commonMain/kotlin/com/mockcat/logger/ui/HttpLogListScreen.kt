package com.mockcat.logger.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mockcat.api.http.LoggedHttpBody
import com.mockcat.api.http.LoggedHttpCall

/**
 * HTTP traffic list: status badge, method + path, host, duration, and response size.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HttpLogListScreen(
    title: String = "HTTP log",
    calls: List<LoggedHttpCall>,
    onBack: (() -> Unit)? = null,
    onClear: (() -> Unit)? = null,
    onCallClick: (Long) -> Unit = {},
) {
    val showClearDialog = remember { mutableStateOf(false) }

    if (showClearDialog.value) {
        AlertDialog(
            onDismissRequest = { showClearDialog.value = false },
            title = { Text("Clear all logs?") },
            text = { Text("This will permanently delete all ${calls.size} recorded HTTP calls.") },
            confirmButton = {
                TextButton(onClick = {
                    showClearDialog.value = false
                    onClear?.invoke()
                }) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog.value = false }) {
                    Text("Cancel")
                }
            },
        )
    }

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
                actions = {
                    if (onClear != null && calls.isNotEmpty()) {
                        IconButton(onClick = { showClearDialog.value = true }) {
                            Text("🗑", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            items(calls, key = { it.id }) { call ->
                ListItem(
                    leadingContent = { StatusBadge(call) },
                    headlineContent = {
                        Text(
                            call.request.method + " " + summarizeUrl(call.request.url),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    supportingContent = {
                        Text(
                            extractHost(call.request.url),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                    trailingContent = {
                        val duration = call.durationMs?.let { "$it ms" }
                        val size = responseByteCount(call)?.let { formatByteCount(it) }
                        if (duration != null || size != null) {
                            Column(horizontalAlignment = Alignment.End) {
                                duration?.let {
                                    Text(it, style = MaterialTheme.typography.labelSmall)
                                }
                                size?.let {
                                    Text(it, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    },
                    modifier = Modifier.clickable { onCallClick(call.id) },
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun StatusBadge(call: LoggedHttpCall) {
    val statusCode = call.response?.statusCode
    val badgeText: String
    val badgeColor: Color

    when {
        statusCode != null -> {
            badgeText = statusCode.toString()
            badgeColor = when (statusCode / 100) {
                2 -> Color(0xFF4CAF50)
                3 -> Color(0xFFFF9800)
                else -> MaterialTheme.colorScheme.error
            }
        }
        call.error != null -> {
            badgeText = "ERR"
            badgeColor = MaterialTheme.colorScheme.error
        }
        else -> {
            badgeText = "···"
            badgeColor = MaterialTheme.colorScheme.outline
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .width(52.dp)
            .background(badgeColor, RoundedCornerShape(4.dp))
            .padding(vertical = 4.dp, horizontal = 2.dp),
    ) {
        Text(
            text = badgeText,
            style = MaterialTheme.typography.labelMedium,
            fontFamily = FontFamily.Monospace,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}

private fun extractHost(url: String): String = url.substringAfter("://", url).substringBefore("/").substringBefore("?")

private fun responseByteCount(call: LoggedHttpCall): Long? = (call.response?.body as? LoggedHttpBody.Text)
    ?.let { it.byteLength ?: it.text.length.toLong() }

private fun formatByteCount(bytes: Long): String = when {
    bytes < 1024L -> "$bytes B"
    bytes < 1024L * 1024L -> "${bytes / 1024} KB"
    else -> "${bytes / (1024L * 1024L)} MB"
}

private fun summarizeUrl(url: String): String = try {
    val u = url.substringAfter("://", url)
    u.substringAfter("/", "/")
} catch (_: Exception) {
    url
}
