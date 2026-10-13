package com.mockcat.sample.navigation

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.mockcat.sample.ui.ClientLauncherScreen
import com.mockcat.sample.ui.movies.MoviesScreen
import com.mockcat.sample.ui.movies.MoviesViewModel
import com.mockcat.sample.ui.movies.MoviesViewModelFactory
import com.mockcat.sample.ui.theme.SampleTheme

/**
 * [Navigation 3](https://developer.android.com/guide/navigation/navigation-3) graph: back stack
 * is `[RouteLauncher]`, then after choosing a client `[RouteLauncher, RouteMovies(...)]` so
 * system back on the list returns to the client picker. In-movie detail still uses
 * [MoviesViewModel] in-memory state; the movies screen’s predictive-back handler keeps system back
 * on detail from popping the graph. [rememberViewModelStoreNavEntryDecorator] scopes
 * [MoviesViewModel] to [RouteMovies].
 */
@Composable
fun MockcatNavHost() {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val application = context.applicationContext as Application
    SampleTheme {
        val backStack = remember { mutableStateListOf<Any>(RouteLauncher) }
        NavDisplay(
            backStack = backStack,
            onBack = {
                when (val last = backStack.lastOrNull()) {
                    is RouteLauncher -> activity.finish()
                    is RouteMovies -> backStack.removeLast()
                    null -> activity.finish()
                    else -> backStack.removeLastOrNull()
                }
            },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = { key ->
                when (key) {
                    is RouteLauncher -> NavEntry(key) {
                        ClientLauncherScreen(
                            onSelect = { kind ->
                                backStack.add(RouteMovies(kind))
                            },
                        )
                    }
                    is RouteMovies -> NavEntry(key) {
                        val viewModel: MoviesViewModel = viewModel(
                            factory = remember(key.kind) {
                                MoviesViewModelFactory(application, key.kind)
                            },
                        )
                        MoviesScreen(
                            viewModel = viewModel,
                            clientKind = key.kind,
                            onChangeClient = { backStack.removeLast() },
                        )
                    }
                    else -> error("Unknown route: $key")
                }
            },
        )
    }
}
