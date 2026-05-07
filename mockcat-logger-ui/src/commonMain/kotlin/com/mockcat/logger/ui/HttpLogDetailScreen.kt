package com.mockcat.logger.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mockcat.api.http.HttpHeaderField
import com.mockcat.api.http.HttpRequestSnapshot
import com.mockcat.api.http.HttpResponseSnapshot
import com.mockcat.api.http.LoggedHttpBody
import com.mockcat.api.http.LoggedHttpCall
import com.mockcat.logger.HttpLogReader
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HttpLogDetailScreen(
    callId: Long,
    reader: HttpLogReader,
    onBack: () -> Unit,
) {
    var call by remember { mutableStateOf<LoggedHttpCall?>(null) }
    LaunchedEffect(callId) {
        call = reader.getById(callId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        call?.let { "${it.request.method} /${extractPath(it.request.url)}" }
                            ?: "HTTP Log",
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                },
            )
        },
    ) { padding ->
        val currentCall = call
        if (currentCall == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            DetailContent(call = currentCall, modifier = Modifier.padding(padding))
        }
    }
}

@Composable
private fun DetailContent(call: LoggedHttpCall, modifier: Modifier = Modifier) {
    val tabs = listOf("Overview", "Request", "Response")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(title) },
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (page) {
                0 -> OverviewTab(call)
                1 -> RequestTab(call.request)
                else -> ResponseTab(call.response, call.error)
            }
        }
    }
}

@Composable
private fun OverviewTab(call: LoggedHttpCall) {
    val status = when {
        call.response != null -> "Complete"
        call.error != null -> "Failed"
        else -> "Requesting"
    }
    val responseSummary = call.response?.let { r ->
        "${r.statusCode}${r.reasonPhrase?.let { " $it" } ?: ""}"
    } ?: call.error ?: "—"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DetailRow("URL", call.request.url)
        DetailRow("Method", call.request.method)
        DetailRow("Protocol", call.response?.protocol ?: call.request.protocol ?: "—")
        DetailRow("Status", status)
        DetailRow("Response", responseSummary)
        DetailRow("Request time", formatTimestampMs(call.requestTimestampMs))
        DetailRow("Response time", formatTimestampMs(call.responseTimestampMs))
        DetailRow("Duration", call.durationMs?.let { "$it ms" } ?: "—")
        DetailRow("Request size", formatByteCount(bodyByteCount(call.request.body)))
        DetailRow("Response size", formatByteCount(bodyByteCount(call.response?.body)))
    }
}

@Composable
private fun RequestTab(request: HttpRequestSnapshot) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SectionHeader("Headers")
        HeadersList(request.headers)
        Spacer(Modifier.height(8.dp))
        SectionHeader("Body")
        BodyContent(request.body)
    }
}

@Composable
private fun ResponseTab(response: HttpResponseSnapshot?, error: String?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when {
            response != null -> {
                SectionHeader("Headers")
                HeadersList(response.headers)
                Spacer(Modifier.height(8.dp))
                SectionHeader("Body")
                BodyContent(response.body)
            }
            error != null -> {
                SectionHeader("Error")
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            else -> Text("Awaiting response…", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )
    HorizontalDivider()
}

@Composable
private fun HeadersList(headers: List<HttpHeaderField>) {
    if (headers.isEmpty()) {
        Text("No headers", style = MaterialTheme.typography.bodySmall)
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            headers.forEach { header ->
                Row {
                    Text(
                        text = "${header.name}: ",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = header.value,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun BodyContent(body: LoggedHttpBody) {
    when (body) {
        is LoggedHttpBody.None -> Text("No body", style = MaterialTheme.typography.bodySmall)
        is LoggedHttpBody.Omitted -> Text(
            "Body not captured (${body.reason})",
            style = MaterialTheme.typography.bodySmall,
        )
        is LoggedHttpBody.Text -> {
            SelectionContainer {
                Text(
                    text = formatBodyText(body.text, body.contentType),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(120.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
    }
}

private fun extractPath(url: String): String = try {
    val afterScheme = url.substringAfter("://", url)
    afterScheme.substringAfter("/", "")
} catch (_: Exception) {
    url
}

private fun formatBodyText(text: String, contentType: String?): String {
    if (contentType?.contains("json", ignoreCase = true) != true) return text
    return prettyPrintJson(text)
}

private fun prettyPrintJson(raw: String): String = buildString {
    var indent = 0
    var inString = false
    var i = 0
    while (i < raw.length) {
        val c = raw[i]
        when {
            c == '"' && (i == 0 || raw[i - 1] != '\\') -> {
                inString = !inString
                append(c)
            }
            inString -> append(c)
            c == '{' || c == '[' -> {
                append(c)
                append('\n')
                indent++
                repeat(indent * 2) { append(' ') }
            }
            c == '}' || c == ']' -> {
                append('\n')
                indent--
                repeat(indent * 2) { append(' ') }
                append(c)
            }
            c == ',' -> {
                append(c)
                append('\n')
                repeat(indent * 2) { append(' ') }
            }
            c == ':' -> append(": ")
            c == ' ' || c == '\n' || c == '\r' || c == '\t' -> Unit
            else -> append(c)
        }
        i++
    }
}

private fun formatTimestampMs(ms: Long?): String {
    if (ms == null) return "—"
    val totalSeconds = ms / 1000
    val millis = ms % 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = (totalSeconds / 3600) % 24
    return "${hours.toString().padStart(2, '0')}:" +
        "${minutes.toString().padStart(2, '0')}:" +
        "${seconds.toString().padStart(2, '0')}." +
        millis.toString().padStart(3, '0')
}

private fun formatByteCount(bytes: Long?): String {
    if (bytes == null) return "—"
    return when {
        bytes < 1024L -> "$bytes B"
        bytes < 1024L * 1024L -> "${bytes / 1024} KB"
        else -> "${bytes / (1024L * 1024L)} MB"
    }
}

private fun bodyByteCount(body: LoggedHttpBody?): Long? = when (body) {
    is LoggedHttpBody.Text -> body.byteLength ?: body.text.length.toLong()
    else -> null
}
