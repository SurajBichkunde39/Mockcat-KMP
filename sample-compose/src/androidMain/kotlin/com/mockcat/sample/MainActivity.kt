package com.mockcat.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mockcat.sample.ui.movies.MoviesViewModel
import com.mockcat.sample.ui.movies.MoviesViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val factory = remember { MoviesViewModelFactory(application) }
            val viewModel: MoviesViewModel = viewModel(factory = factory)
            SampleApp(viewModel = viewModel)
        }
    }
}
