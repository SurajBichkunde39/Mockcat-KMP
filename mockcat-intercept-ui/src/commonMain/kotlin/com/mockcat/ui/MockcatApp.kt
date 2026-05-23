package com.mockcat.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
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
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (form == null) {
                val total = ui.entries.size
                val active = ui.entries.count { it.isEnabled }
                TopAppBar(
                    title = {
                        Column {
                            Text("Mockcat")
                            if (total > 0) {
                                Text(
                                    "$total mock${if (total == 1) "" else "s"} · $active active",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    },
                )
            } else {
                TopAppBar(
                    title = {
                        Text(if (form.id == 0L) "Add mock" else "Edit mock")
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.closeEditor() }) {
                            Text("←", style = MaterialTheme.typography.titleLarge)
                        }
                    },
                )
            }
        },
        floatingActionButton = {
            if (form == null) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.openEditor(null) },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Add mock") },
                )
            }
        },
    ) { padding ->
        if (form == null) {
            Column(
                modifier = Modifier.padding(padding).fillMaxSize(),
            ) {
                if (ui.entries.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 32.dp),
                        ) {
                            Icon(
                                Icons.Outlined.Inbox,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.outline,
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No mocks yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Tap \"Add mock\" to create your first mock rule",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        items(ui.entries, key = { it.id }) { m: MockEntry ->
                            var showDeleteDialog by remember { mutableStateOf(false) }
                            val dismissState = rememberSwipeToDismissBoxState()
                            LaunchedEffect(dismissState.currentValue) {
                                if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                                    showDeleteDialog = true
                                }
                            }
                            if (showDeleteDialog) {
                                AlertDialog(
                                    onDismissRequest = {
                                        showDeleteDialog = false
                                        scope.launch { dismissState.reset() }
                                    },
                                    title = { Text("Delete mock?") },
                                    text = { Text("\"${m.url}\" will be permanently deleted.") },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                showDeleteDialog = false
                                                scope.launch { viewModel.delete(m) }
                                            },
                                        ) {
                                            Text("Delete", color = MaterialTheme.colorScheme.error)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(
                                            onClick = {
                                                showDeleteDialog = false
                                                scope.launch { dismissState.reset() }
                                            },
                                        ) {
                                            Text("Cancel")
                                        }
                                    },
                                )
                            }
                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = false,
                                backgroundContent = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.error)
                                            .padding(end = 20.dp),
                                        contentAlignment = Alignment.CenterEnd,
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete mock",
                                            tint = MaterialTheme.colorScheme.onError,
                                        )
                                    }
                                },
                            ) {
                                Column {
                                    ListItem(
                                        leadingContent = {
                                            Switch(
                                                checked = m.isEnabled,
                                                onCheckedChange = { viewModel.toggleEnabled(m) },
                                            )
                                        },
                                        headlineContent = {
                                            Text(
                                                urlPath(m.url),
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontFamily = FontFamily.Monospace,
                                            )
                                        },
                                        supportingContent = {
                                            Column {
                                                urlHost(m.url)?.let {
                                                    Text(
                                                        it,
                                                        style = MaterialTheme.typography.bodySmall,
                                                    )
                                                }
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                ) {
                                                    MethodChip(m.httpMethod)
                                                    MockTypeChip(m.mockType)
                                                }
                                                if (m.label.isNotBlank()) {
                                                    Text(
                                                        m.label,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.outline,
                                                    )
                                                }
                                            }
                                        },
                                        trailingContent = {
                                            IconButton(onClick = { viewModel.openEditor(m) }) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = "Edit mock",
                                                )
                                            }
                                        },
                                    )
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }
        } else {
            FormContent(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize(),
                form = form,
                onUpdate = { viewModel.setForm(it) },
                onSave = { scope.launch { viewModel.saveForm() } },
                onCancel = { viewModel.closeEditor() },
            )
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

@Composable
private fun MethodChip(method: String) {
    val color = when (method.uppercase()) {
        "GET" -> Color(0xFF1976D2)
        "POST" -> Color(0xFF388E3C)
        "PUT" -> Color(0xFFF57C00)
        "PATCH" -> Color(0xFF7B1FA2)
        "DELETE" -> Color(0xFFD32F2F)
        else -> Color(0xFF546E7A)
    }
    Chip(label = method.uppercase(), color = color)
}

@Composable
private fun MockTypeChip(type: MockType) {
    val color = when (type) {
        MockType.STATIC -> Color(0xFF00796B)
        MockType.REDIRECT -> Color(0xFFE65100)
    }
    Chip(label = type.name, color = color)
}

@Composable
private fun Chip(label: String, color: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(color, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = Color.White,
        )
    }
}

private fun urlPath(url: String): String = try {
    url.substringAfter("://", url).substringAfter("/", "/").let { "/$it" }
} catch (_: Exception) {
    url
}

private fun urlHost(url: String): String? = try {
    url.substringAfter("://", "").substringBefore("/").takeIf { it.isNotBlank() }
} catch (_: Exception) {
    null
}
