package com.mockcat.sample.ui.movies

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mockcat.logger.ui.MockcatLoggerUi
import com.mockcat.sample.data.FilmDto
import com.mockcat.sample.data.MovieConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesScreen(
    viewModel: MoviesViewModel,
) {
    val s = viewModel.uiState.collectAsStateWithLifecycle().value
    val context = LocalContext.current

    Scaffold(
        topBar = {
            if (s.isDetail) {
                CenterAlignedTopAppBar(
                    title = { Text("Movie", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.onBack() }) {
                            Text("←", style = MaterialTheme.typography.titleLarge)
                        }
                    },
                )
            } else {
                LargeTopAppBar(
                    title = {
                        Column {
                            Text("Mockcat sample")
                            Text(
                                "MockcatHttpLoggingInterceptor + store + Chucker",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = {
                                context.startActivity(
                                    MockcatLoggerUi.getHttpLogListScreen(context),
                                )
                            },
                        ) {
                            Text("HTTP log")
                        }
                    },
                )
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (s.isLoading || s.isLoadingDetail) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            if (s.errorMessage != null) {
                ErrorBanner(
                    message = s.errorMessage,
                    onDismiss = { viewModel.onDismissError() },
                )
            }
            if (s.isDetail) {
                s.selectedFilm?.let { film ->
                    FilmDetail(
                        listItem = film,
                        detail = s.detail,
                        loading = s.isLoadingDetail,
                    )
                }
            } else {
                ListSection(
                    state = s,
                    onLoad = { viewModel.loadMovieList() },
                    onFilmClick = { viewModel.openDetail(it) },
                )
            }
        }
    }
}

@Composable
private fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text(message, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onDismiss) { Text("Dismiss") }
        }
    }
}

@Composable
private fun ListSection(
    state: MoviesUiState,
    onLoad: () -> Unit,
    onFilmClick: (FilmDto) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            "Server: ${MovieConfig.BASE_URL}  ·  `./gradlew :sample-server:run` (see AGENT.md)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        FilledTonalButton(
            onClick = onLoad,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Load movie list") }
        Spacer(Modifier.height(8.dp))
        Text(
            "Add mocks via Mockcat UI (when you wire mockcat-ui) or import; the interceptor uses the shared store.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            when {
                state.isLoading && state.list.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.isListEmpty -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("No data yet", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Start the Ktor server, then load. Tap a row for details (GET /api/movies/{imdbId}).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.list, key = { it.imdbID }) { film ->
                            FilmListRow(
                                film = film,
                                onClick = { onFilmClick(film) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilmListRow(
    film: FilmDto,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
        ) {
            Text(
                film.Title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (film.Year.isNotBlank() || !film.imdbRating.isNullOrBlank()) {
                Text(
                    "${film.Year}  ·  ★ ${film.imdbRating ?: "—"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FilmDetail(
    listItem: FilmDto,
    detail: FilmDto?,
    loading: Boolean,
) {
    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    val f = detail ?: listItem
    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp),
    ) {
        Text(f.Title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            "Year: ${f.Year}  ·  Rating: ${f.imdbRating ?: "—"}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        if (f.Plot.isNotBlank()) {
            Text(f.Plot, style = MaterialTheme.typography.bodyLarge)
        } else {
            Text("No plot in response.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
