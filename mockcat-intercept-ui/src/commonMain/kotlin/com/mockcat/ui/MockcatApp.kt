package com.mockcat.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
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
    val form = ui.form
    BackHandler(enabled = form != null) {
        viewModel.closeEditor()
    }
    MaterialTheme {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                if (form == null) {
                    TopAppBar(title = { Text("Mockcat") })
                } else {
                    TopAppBar(
                        title = {
                            Text(
                                if (form.id == 0L) {
                                    "Add mock"
                                } else {
                                    "Edit mock"
                                },
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { viewModel.closeEditor() },
                            ) {
                                Text(
                                    "←",
                                    style = MaterialTheme.typography.titleLarge,
                                )
                            }
                        },
                    )
                }
            },
        ) { padding ->
            Column(
                modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            ) {
                if (form == null) {
                    Button(
                        onClick = { viewModel.openEditor(null) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Add mock")
                    }
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
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
                        modifier = Modifier.fillMaxSize(),
                        form = form,
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
    modifier: Modifier = Modifier,
    form: MockFormState,
    onUpdate: (MockFormState) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    val scroll = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = form.url,
            onValueChange = { onUpdate(form.copy(url = it)) },
            label = { Text("URL") },
            placeholder = { Text("https://10.0.2.2:8080/api/movies") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = form.method,
            onValueChange = { onUpdate(form.copy(method = it)) },
            label = { Text("HTTP method") },
            placeholder = { Text("GET") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = form.label,
            onValueChange = { onUpdate(form.copy(label = it)) },
            label = { Text("Label (optional)") },
            placeholder = { Text("e.g. Movies list") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = form.mockTypeInput,
            onValueChange = { onUpdate(form.copy(mockTypeInput = it)) },
            label = { Text("Mock type") },
            placeholder = { Text("STATIC or REDIRECT") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        if (form.resolvedMockType() == MockType.STATIC) {
            OutlinedTextField(
                value = form.responseCode,
                onValueChange = { onUpdate(form.copy(responseCode = it)) },
                label = { Text("Response code") },
                placeholder = { Text("200") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = form.responseBody,
                onValueChange = { onUpdate(form.copy(responseBody = it)) },
                label = { Text("Response body") },
                placeholder = { Text("{\"items\":[]}") },
                minLines = 5,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            OutlinedTextField(
                value = form.redirectUrl,
                onValueChange = { onUpdate(form.copy(redirectUrl = it)) },
                label = { Text("Redirect URL") },
                placeholder = { Text("https://…") },
                singleLine = false,
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        OutlinedTextField(
            value = form.delayMs,
            onValueChange = { onUpdate(form.copy(delayMs = it)) },
            label = { Text("Delay (ms, optional)") },
            placeholder = { Text("0") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
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
