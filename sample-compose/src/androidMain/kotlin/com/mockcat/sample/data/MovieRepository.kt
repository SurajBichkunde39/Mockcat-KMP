package com.mockcat.sample.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * All HTTP and JSON for the movies API — [MoviesViewModel] uses this; UI does not.
 */
class MovieRepository(
    private val client: OkHttpClient,
    private val baseUrl: String = MovieConfig.BASE_URL,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /** List from Ktor. If a static mock in the store matches, [MockcatOkHttpInterceptor] returns it. */
    suspend fun fetchList(): Result<List<FilmDto>> = withContext(Dispatchers.IO) {
        val url = "$baseUrl/api/movies"
        runCatching {
            client.newCall(Request.Builder().url(url).get().build()).execute().use { resp ->
                if (!resp.isSuccessful) {
                    error("HTTP ${resp.code}")
                }
                val body = resp.body?.string().orEmpty()
                json.decodeFromString(MoviesListResponse.serializer(), body).films
            }
        }
    }

    suspend fun fetchDetail(film: FilmDto): Result<FilmDto> = withContext(Dispatchers.IO) {
        val url = "$baseUrl/api/movies/${film.imdbID}"
        runCatching {
            client.newCall(Request.Builder().url(url).get().build()).execute().use { resp ->
                if (!resp.isSuccessful) {
                    error("HTTP ${resp.code}")
                }
                val body = resp.body?.string().orEmpty()
                if (body.isBlank()) {
                    error("Empty response")
                }
                json.decodeFromString(FilmDto.serializer(), body)
            }
        }
    }
}
