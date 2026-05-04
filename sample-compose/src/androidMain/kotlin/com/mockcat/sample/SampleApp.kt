package com.mockcat.sample

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.mockcat.sample.ui.movies.MoviesScreen
import com.mockcat.sample.ui.movies.MoviesViewModel
import com.mockcat.sample.ui.theme.SampleTheme

/**
 * Root of the sample: [SampleTheme] + [MoviesScreen]. The activity wires a [MoviesViewModel] whose
 * [okhttp3.OkHttpClient] is built with [com.mockcat.sample.data.OkHttpClientFactory] (interceptor
 * + Chucker only; no [com.mockcat.api.MockcatStore] in feature code).
 */
@Composable
fun SampleApp(viewModel: MoviesViewModel) {
    SampleTheme {
        Surface {
            MoviesScreen(viewModel = viewModel)
        }
    }
}
