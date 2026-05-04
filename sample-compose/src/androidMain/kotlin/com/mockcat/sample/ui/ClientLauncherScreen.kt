package com.mockcat.sample.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mockcat.sample.data.ClientKind

@Composable
fun ClientLauncherScreen(
    onSelect: (ClientKind) -> Unit,
) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Mockcat sample",
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                "Choose how HTTP is implemented for the movies API.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FilledTonalButton(
                onClick = { onSelect(ClientKind.OkHttp) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("OkHttp integration (Mockcat + Chucker + HTTP log)")
            }
            FilledTonalButton(
                onClick = { onSelect(ClientKind.Ktor) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Ktor client (HTTP only, no Mockcat in this build)")
            }
        }
    }
}
