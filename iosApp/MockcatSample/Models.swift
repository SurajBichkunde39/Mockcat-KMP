import Foundation

struct MoviesResponse: Decodable {
    let films: [Film]
}

struct Film: Decodable {
    let Title: String
    let Year: String
    let Rated: String?
    let Released: String?
    let Runtime: String?
    let Genre: String?
    let Director: String?
    let Actors: String?
    let Plot: String?
    let Poster: String?
    let imdbID: String
    let imdbRating: String?
}

struct FilmSummary: Identifiable, Hashable {
    let imdbID: String
    let title: String
    let year: String

    var id: String { imdbID }
}

