import SwiftUI

struct MovieDetailScreen: View {
    @Binding var baseUrlText: String
    let imdbID: String

    @State private var isLoading = false
    @State private var errorText: String?
    @State private var film: Film?

    var body: some View {
        Group {
            if isLoading {
                ProgressView()
            } else if let errorText {
                VStack(spacing: 12) {
                    Text(errorText)
                        .foregroundStyle(.red)
                    Button("Retry") {
                        Task { await fetchDetail() }
                    }
                    .buttonStyle(.borderedProminent)
                }
                .padding()
            } else if let film {
                List {
                    Section {
                        VStack(alignment: .leading, spacing: 6) {
                            Text(film.Title)
                                .font(.title2)
                                .fontWeight(.semibold)
                            Text("\(film.Year) • \(film.imdbID)")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                        .padding(.vertical, 4)
                    }

                    sectionRow("Rating", film.imdbRating)
                    sectionRow("Runtime", film.Runtime)
                    sectionRow("Genre", film.Genre)
                    sectionRow("Rated", film.Rated)
                    sectionRow("Released", film.Released)
                    sectionRow("Director", film.Director)
                    sectionRow("Actors", film.Actors)

                    if let plot = film.Plot, !plot.isEmpty {
                        Section("Plot") {
                            Text(plot)
                        }
                    }
                }
            } else {
                Text("No data.")
                    .foregroundStyle(.secondary)
            }
        }
        .navigationTitle("Movie")
        .navigationBarTitleDisplayMode(.inline)
        .task { await fetchDetail() }
    }

    @ViewBuilder
    private func sectionRow(_ title: String, _ value: String?) -> some View {
        if let value, !value.isEmpty {
            Section(title) { Text(value) }
        }
    }

    private func fetchDetail() async {
        errorText = nil
        isLoading = true
        defer { isLoading = false }
        do {
            let base = try ApiBaseURL(baseUrlText)
            let url = base.url.appending(path: "/api/movies/\(imdbID)")
            film = try await URLSessionApi.fetchJSON(from: url)
        } catch {
            errorText = error.localizedDescription
        }
    }
}

