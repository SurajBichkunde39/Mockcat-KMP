package com.mockcat.sample.server

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val fileJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

fun main() {
    val raw =
        Thread.currentThread().contextClassLoader
            .getResourceAsStream("films.json")
            ?.use { it.readAllBytes() }
            ?.decodeToString() ?: error("Missing classpath resource films.json")
    val listSerializer = ListSerializer(Film.serializer())
    val films: List<Film> = fileJson.decodeFromString(listSerializer, raw)
    embeddedServer(
        Netty,
        port = 8080,
    ) { module(films) }
        .start(wait = true)
}

private fun Application.module(films: List<Film>) {
    install(CORS) { anyHost() }
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                prettyPrint = false
            },
        )
    }
    routing {
        get("/") { call.respond(mapOf("status" to "ok", "films" to films.size)) }
        route("/api") {
            get("/movies") { call.respond(MoviesResponse(films = films)) }
            get("/movies/{imdbId}") {
                val id = call.parameters["imdbId"]
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Missing id")
                    return@get
                }
                val film = films.find { it.imdbID == id }
                if (film == null) {
                    call.respond(HttpStatusCode.NotFound, "No film for imdbId=$id")
                } else {
                    call.respond(film)
                }
            }
        }
    }
}

@Serializable
data class Film(
    val Title: String = "",
    val Year: String = "",
    val Rated: String = "",
    val Released: String = "",
    val Runtime: String = "",
    val Genre: String = "",
    val Director: String = "",
    val Actors: String = "",
    val Plot: String = "",
    val Poster: String? = null,
    val imdbID: String,
    val imdbRating: String? = null,
)

@Serializable
data class MoviesResponse(val films: List<Film>)
