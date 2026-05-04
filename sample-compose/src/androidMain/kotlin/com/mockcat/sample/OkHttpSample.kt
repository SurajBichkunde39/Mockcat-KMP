package com.mockcat.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mockcat.api.MockEntry
import com.mockcat.api.MockType
import com.mockcat.api.MockcatStore
import com.mockcat.intercept.okhttp.MockcatOkHttpInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

private const val DEMO_URL = "https://example.com/mockcat-okhttp-demo"

@Composable
fun OkHttpSample(
    store: MockcatStore,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val client =
        remember(store) {
            val interceptor = MockcatOkHttpInterceptor(store)
            OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build()
                .also { c -> interceptor.setClient(c) }
        }
    var lastResponse by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(store) {
        store.insertOrUpdate(
            MockEntry(
                url = DEMO_URL,
                label = "OkHttp demo (static)",
                httpMethod = "GET",
                responseCode = 200,
                responseBody = """{"mocked":true,"via":"MockcatOkHttpInterceptor"}""",
                delayMs = 0L,
                isEnabled = true,
                mockType = MockType.STATIC,
            ),
        )
    }
    Card(modifier = modifier.fillMaxWidth().padding(8.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text("OkHttp + MockcatOkHttpInterceptor")
            Button(
                onClick = {
                    scope.launch {
                        val body =
                            withContext(Dispatchers.IO) {
                                client.newCall(Request.Builder().url(DEMO_URL).get().build()).execute()
                                    .use { it.body?.string().orEmpty() }
                            }
                        lastResponse = body
                    }
                },
            ) {
                Text("GET $DEMO_URL")
            }
            lastResponse?.let { Text(it) }
        }
    }
}
