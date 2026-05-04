package com.mockcat.logger.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mockcat.api.http.LoggedHttpCall
import com.mockcat.logger.HttpLogReaderRegistry
import kotlinx.coroutines.flow.collect

/**
 * Host-agnostic list that reads from [HttpLogReaderRegistry] (installed by the logging layer).
 */
@Composable
fun HttpLogListContentFromRegistry() {
    val reader = HttpLogReaderRegistry.currentOrNull()
    var calls by remember { mutableStateOf<List<LoggedHttpCall>>(emptyList()) }

    if (reader == null) {
        MaterialTheme(colorScheme = lightColorScheme()) {
            Text(
                "HTTP log is not available yet. On Android, build an OkHttpClient with " +
                    "com.mockcat.android.okhttp.MockcatLogging (the reader is registered when the " +
                    "interceptor is created). On iOS, call getHttpLogStoreForIos() during app startup.",
            )
        }
        return
    }

    LaunchedEffect(reader) {
        reader.observeLogs().collect { calls = it }
    }

    MaterialTheme(colorScheme = lightColorScheme()) {
        HttpLogListScreen(calls = calls)
    }
}
