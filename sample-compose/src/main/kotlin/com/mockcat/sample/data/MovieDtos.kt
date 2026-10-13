package com.mockcat.sample.data

import kotlinx.serialization.Serializable

@Serializable
data class FilmDto(
    val Title: String = "",
    val Year: String = "",
    val imdbID: String,
    val Plot: String = "",
    val Poster: String? = null,
    val imdbRating: String? = null,
)

@Serializable
data class MoviesListResponse(
    val films: List<FilmDto>,
)
