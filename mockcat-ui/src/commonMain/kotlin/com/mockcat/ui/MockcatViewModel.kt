package com.mockcat.ui

import com.mockcat.api.MockEntry
import com.mockcat.api.MockType
import com.mockcat.api.MockcatStore
import com.mockcat.api.buildHttpUrl
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
    val mockType: MockType,
    val responseCode: String,
    val responseBody: String,
    val delayMs: String,
    val redirectUrl: String,
) {
    companion object {
        fun empty() = MockFormState(
            url = "https://",
            label = "",
            method = "GET",
            isEnabled = true,
            mockType = MockType.STATIC,
            responseCode = "200",
            responseBody = "{}",
            delayMs = "0",
            redirectUrl = "",
        )

        fun from(e: MockEntry) = MockFormState(
            id = e.id,
            url = buildHttpUrl(e.url, e.requiredQueryParams),
            label = e.label,
            method = e.httpMethod,
            isEnabled = e.isEnabled,
            mockType = e.mockType,
            responseCode = e.responseCode?.toString() ?: "",
            responseBody = e.responseBody.orEmpty(),
            delayMs = e.delayMs?.toString() ?: "0",
            redirectUrl = e.redirectUrl.orEmpty(),
        )
    }

    fun toEntry(): MockEntry {
        val (base, q) = splitHttpUrl(url.trim())
        return MockEntry(
            id = id,
            url = base,
            label = label.trim(),
            httpMethod = method.trim().uppercase(),
            isEnabled = isEnabled,
            mockType = mockType,
            responseCode = responseCode.toIntOrNull(),
            responseBody = responseBody.ifBlank { null },
            delayMs = delayMs.toLongOrNull(),
            redirectUrl = redirectUrl.ifBlank { null },
            requiredHeaders = null,
            requiredQueryParams = if (q.isEmpty()) null else q,
        )
    }
}
