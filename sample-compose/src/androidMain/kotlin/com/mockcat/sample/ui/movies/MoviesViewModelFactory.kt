package com.mockcat.sample.ui.movies

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mockcat.sample.data.MovieRepository
import com.mockcat.sample.data.OkHttpClientFactory

class MoviesViewModelFactory(
    private val application: Application,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass != MoviesViewModel::class.java) {
            error("Unknown ViewModel: ${modelClass.name}")
        }
        val client = OkHttpClientFactory.create(application.applicationContext)
        return MoviesViewModel(MovieRepository(client)) as T
    }
}
