package com.mockcat.sample.ui.movies

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mockcat.sample.data.ClientKind
import com.mockcat.sample.data.KtorClientFactory
import com.mockcat.sample.data.KtorMovieRepository
import com.mockcat.sample.data.OkHttpClientFactory
import com.mockcat.sample.data.OkHttpMovieRepository

class MoviesViewModelFactory(
    private val application: Application,
    private val kind: ClientKind,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass != MoviesViewModel::class.java) {
            error("Unknown ViewModel: ${modelClass.name}")
        }
        val repository = when (kind) {
            ClientKind.OkHttp -> {
                val client = OkHttpClientFactory.create(
                    appContext = application.applicationContext,
                )
                OkHttpMovieRepository(client)
            }
            ClientKind.Ktor -> {
                KtorMovieRepository(
                    KtorClientFactory.create(application.applicationContext),
                )
            }
        }
        return MoviesViewModel(repository) as T
    }
}
