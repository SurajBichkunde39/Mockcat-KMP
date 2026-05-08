package com.mockcat.logger.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.mockcat.api.http.LoggedHttpCall
import com.mockcat.logger.HttpLogReaderRegistry
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Host-agnostic list that reads from [HttpLogReaderRegistry] (installed by the logging layer).
 * Manages in-place navigation to the detail screen.
 *
 * @param onShareCurl Optional callback invoked with a ready-to-use curl command string when the
 *   user taps "Share as curl" on a detail screen. The platform layer is responsible for presenting
 *   the OS share sheet; pass null to hide the share option entirely.
 */
@Composable
fun HttpLogListContentFromRegistry(onShareCurl: ((String) -> Unit)? = null) {
    val reader = HttpLogReaderRegistry.currentOrNull()
    var calls by remember { mutableStateOf<List<LoggedHttpCall>>(emptyList()) }
    var selectedCallId by remember { mutableStateOf<Long?>(null) }
    val scope = rememberCoroutineScope()

    if (reader == null) {
        MaterialTheme(colorScheme = lightColorScheme()) {
            Text(
                "HTTP log is not available yet. On Android, build an OkHttpClient with " +
                    "com.mockcat.logger.okhttp.MockcatLogging (the reader is registered when the " +
                    "interceptor is created). On iOS, call getHttpLogStoreForIos() during app startup.",
            )
        }
        return
    }

    LaunchedEffect(reader) {
        reader.observeLogs().collect { calls = it }
    }

    PlatformBackHandler(enabled = selectedCallId != null) {
        selectedCallId = null
    }

    MaterialTheme(colorScheme = lightColorScheme()) {
        val selected = selectedCallId
        if (selected != null) {
            HttpLogDetailScreen(
                callId = selected,
                reader = reader,
                onBack = { selectedCallId = null },
                onShareCurl = onShareCurl,
            )
        } else {
            HttpLogListScreen(
                calls = calls,
                onClear = { scope.launch { reader.clear() } },
                onCallClick = { selectedCallId = it },
            )
        }
    }
}
