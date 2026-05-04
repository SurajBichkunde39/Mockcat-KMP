package com.mockcat.sample.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class KtorMovieRepository(
    private val client: HttpClient,
    private val baseUrl: String = MovieConfig.BASE_URL,
) : MovieRepository {
    override suspend fun fetchList(): Result<List<FilmDto>> = withContext(Dispatchers.IO) {
        runCatching {
            val url = "$baseUrl/api/movies"
            val response = client.get(url)
            if (!response.status.isSuccess()) {
                error("HTTP ${response.status.value}")
            }
            response.body<MoviesListResponse>().films
        }
    }

    override suspend fun fetchDetail(film: FilmDto): Result<FilmDto> = withContext(Dispatchers.IO) {
        runCatching {
            val url = "$baseUrl/api/movies/${film.imdbID}"
            val response = client.get(url)
            if (!response.status.isSuccess()) {
                error("HTTP ${response.status.value}")
            }
            response.body<FilmDto>()
        }
    }

    /**
     * Call from [com.mockcat.sample.ui.movies.MoviesViewModel] [androidx.lifecycle.ViewModel.onCleared] so the
     * [HttpClient] is not leaked.
     */
    fun close() {
        client.close()
    }
}
