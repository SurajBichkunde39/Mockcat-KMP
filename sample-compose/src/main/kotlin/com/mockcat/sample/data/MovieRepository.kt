package com.mockcat.sample.data

/**
 * Movies list/detail for the in-repo `sample-server` (`:sample-server:run`); OkHttp or Ktor HTTP stack.
 */
interface MovieRepository {
    suspend fun fetchList(): Result<List<FilmDto>>

    suspend fun fetchDetail(film: FilmDto): Result<FilmDto>
}
