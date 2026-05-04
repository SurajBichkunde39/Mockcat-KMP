package com.mockcat.ui

import com.mockcat.api.MockEntry
import com.mockcat.api.MockType
import com.mockcat.api.MockcatStore
import com.mockcat.api.buildHttpUrl
import com.mockcat.api.http.LoggedHttpBody
import com.mockcat.api.splitHttpUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MockcatViewModel(
    private val store: MockcatStore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(MockcatUiState())
    val state: StateFlow<MockcatUiState> = _state.asStateFlow()

    init {
        scope.launch {
            store.observeAllMocks().collect { list ->
                _state.update { it.copy(entries = list) }
            }
        }
    }

    fun setForm(f: MockFormState) {
        _state.update { it.copy(form = f) }
    }

    fun openEditor(entry: MockEntry? = null) {
        _state.update {
            it.copy(
                form = entry?.let { e -> MockFormState.from(e) } ?: MockFormState.empty(),
            )
        }
    }

    fun closeEditor() {
        _state.update { it.copy(form = null) }
    }

    suspend fun saveForm() {
        val f = _state.value.form ?: return
        store.insertOrUpdate(f.toEntry())
        closeEditor()
    }

    suspend fun delete(entry: MockEntry) {
        if (entry.id != 0L) {
            store.delete(entry)
        }
    }
}

data class MockcatUiState(
    val entries: List<MockEntry> = emptyList(),
    val form: MockFormState? = null,
)

data class MockFormState(
    val id: Long = 0L,
    val url: String,
    val label: String,
    val method: String,
    val isEnabled: Boolean,
    /** Raw text for STATIC / REDIRECT; blank defaults to STATIC in [toEntry]. */
    val mockTypeInput: String,
    val responseCode: String,
    val responseBody: String,
    val delayMs: String,
    val redirectUrl: String,
) {
    fun resolvedMockType(): MockType = mockTypeInput.toMockTypeOrDefault()

    companion object {
        fun empty() = MockFormState(
            id = 0L,
            url = "",
            label = "",
            method = "",
            isEnabled = true,
            mockTypeInput = "",
            responseCode = "",
            responseBody = "",
            delayMs = "",
            redirectUrl = "",
        )

        fun from(e: MockEntry): MockFormState {
            val sr = e.staticResponse
            val responseCodeStr: String
            val responseBodyStr: String
            if (sr != null) {
                responseCodeStr = sr.statusCode.toString()
                responseBodyStr =
                    when (val b = sr.body) {
                        is LoggedHttpBody.None -> ""
                        is LoggedHttpBody.Text -> b.text
                        is LoggedHttpBody.Omitted -> ""
                    }
            } else {
                responseCodeStr = e.responseCode?.toString() ?: ""
                responseBodyStr = e.responseBody.orEmpty()
            }
            return MockFormState(
                id = e.id,
                url = buildHttpUrl(e.url, e.requiredQueryParams),
                label = e.label,
                method = e.httpMethod,
                isEnabled = e.isEnabled,
                mockTypeInput = e.mockType.name,
                responseCode = responseCodeStr,
                responseBody = responseBodyStr,
                delayMs = e.delayMs?.toString() ?: "",
                redirectUrl = e.redirectUrl.orEmpty(),
            )
        }
    }

    fun toEntry(): MockEntry {
        val (base, q) = splitHttpUrl(url.trim())
        return MockEntry(
            id = id,
            url = base,
            label = label.trim(),
            httpMethod = method.trim().uppercase().ifBlank { "GET" },
            isEnabled = isEnabled,
            mockType = resolvedMockType(),
            staticResponse = null,
            responseCode = responseCode.toIntOrNull(),
            responseBody = responseBody.ifBlank { null },
            delayMs = delayMs.toLongOrNull(),
            redirectUrl = redirectUrl.ifBlank { null },
            requiredHeaders = null,
            requiredQueryParams = if (q.isEmpty()) null else q,
        )
    }
}

private fun String.toMockTypeOrDefault(): MockType = try {
    MockType.valueOf(trim().uppercase())
} catch (_: Exception) {
    MockType.STATIC
}
