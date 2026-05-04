package com.mockcat.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockcatApp(
    store: MockcatStore,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val viewModel = remember { MockcatViewModel(store) }
    var ui by remember { mutableStateOf(viewModel.state.value) }
    LaunchedEffect(viewModel) {
        viewModel.state.collect { ui = it }
    }
    MaterialTheme {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = { TopAppBar(title = { Text("Mockcat") }) },
        ) { padding ->
            Column(
                modifier = Modifier.padding(padding).padding(16.dp),
            ) {
                if (ui.form == null) {
                    Button(
                        onClick = { viewModel.openEditor(null) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Add mock")
                    }
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ui.entries) { m: MockEntry ->
                            ListItem(
                                headlineContent = { Text(m.url) },
                                supportingContent = { Text("${m.httpMethod} ${m.mockType}") },
                                trailingContent = {
                                    Button(onClick = { viewModel.openEditor(m) }) { Text("Edit") }
                                },
                            )
                        }
                    }
                } else {
                    FormContent(
                        form = ui.form!!,
                        onUpdate = { viewModel.setForm(it) },
                        onSave = { scope.launch { viewModel.saveForm() } },
                        onCancel = { viewModel.closeEditor() },
                    )
                }
            }
        }
    }
}

@Composable
private fun FormContent(
    form: MockFormState,
    onUpdate: (MockFormState) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = form.url,
            onValueChange = { onUpdate(form.copy(url = it)) },
            label = { Text("URL") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = form.method,
            onValueChange = { onUpdate(form.copy(method = it)) },
            label = { Text("Method") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = form.label,
            onValueChange = { onUpdate(form.copy(label = it)) },
            label = { Text("Label") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = form.mockType.name,
            onValueChange = { t -> onUpdate(form.copy(mockType = t.toEnumOrDefault())) },
            label = { Text("Type (STATIC/REDIRECT)") },
            modifier = Modifier.fillMaxWidth(),
        )
        if (form.mockType == MockType.STATIC) {
            OutlinedTextField(
                value = form.responseCode,
                onValueChange = { onUpdate(form.copy(responseCode = it)) },
                label = { Text("Response code") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = form.responseBody,
                onValueChange = { onUpdate(form.copy(responseBody = it)) },
                label = { Text("Response body") },
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            OutlinedTextField(
                value = form.redirectUrl,
                onValueChange = { onUpdate(form.copy(redirectUrl = it)) },
                label = { Text("Redirect URL") },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Save") }
        Button(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Cancel") }
    }
}

private fun String.toEnumOrDefault(): MockType = try {
    MockType.valueOf(this.trim().uppercase())
} catch (_: Exception) {
    MockType.STATIC
}
