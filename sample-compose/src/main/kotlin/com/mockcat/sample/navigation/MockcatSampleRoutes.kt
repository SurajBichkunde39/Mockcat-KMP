package com.mockcat.sample.navigation

import com.mockcat.sample.data.ClientKind

/** First destination: user picks OkHttp vs Ktor. */
data object RouteLauncher

/** Movie list / detail for the selected [ClientKind]. */
data class RouteMovies(
    val kind: ClientKind,
)
