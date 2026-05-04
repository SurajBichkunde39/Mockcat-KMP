import MockcatLoggerUI
import SwiftUI

struct ContentView: View {
    @State private var baseUrlText = "http://localhost:8080"
    @State private var isLoading = false
    @State private var errorText: String?
    @State private var films: [FilmSummary] = []
    @State private var showHttpLog = false

    var body: some View {
        NavigationStack {
            VStack(spacing: 12) {
                VStack(alignment: .leading, spacing: 6) {
                    Text("Sample server base URL")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                    TextField("http://localhost:8080", text: $baseUrlText)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled()
                        .keyboardType(.URL)
                        .textFieldStyle(.roundedBorder)
                }
                .padding(.horizontal)
                .padding(.top, 8)

                if let errorText {
                    Text(errorText)
                        .foregroundStyle(.red)
                        .padding(.horizontal)
                }

                if films.isEmpty {
                    Spacer()
                    Text("No movies yet.")
                        .foregroundStyle(.secondary)
                    Button(isLoading ? "Fetching…" : "Fetch movies") {
                        Task { await fetchMovies() }
                    }
                    .disabled(isLoading)
                    .buttonStyle(.borderedProminent)
                    Spacer()
                } else {
                    List(films) { film in
                        NavigationLink(value: film.imdbID) {
                            VStack(alignment: .leading, spacing: 2) {
                                Text(film.title)
                                Text(film.year)
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                            }
                        }
                    }
                    .overlay {
                        if isLoading {
                            ProgressView()
                        }
                    }
                }
            }
            .navigationTitle("Movies (URLSession)")
            .navigationDestination(for: String.self) { imdbID in
                MovieDetailScreen(
                    baseUrlText: $baseUrlText,
                    imdbID: imdbID
                )
            }
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button(isLoading ? "Fetching…" : "Fetch") {
                        Task { await fetchMovies() }
                    }
                    .disabled(isLoading)
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button("HTTP log") {
                        // Be defensive: the Compose VC requires the registry to be installed.
                        // If SwiftUI lifecycle changes and `onAppear` hasn't fired yet, install here too.
                        InstallHttpLogReaderForIosKt.installHttpLogReaderForIos()
                        showHttpLog = true
                    }
                }
            }
        }
        .onAppear(perform: {
            // Top-level Kotlin file facades are exposed as *Kt types (see framework Headers/MockcatLoggerUI.h).
            InstallHttpLogReaderForIosKt.installHttpLogReaderForIos()
        })
        .fullScreenCover(isPresented: $showHttpLog) {
            NavigationStack {
                HttpLogListViewControllerHost()
                    .ignoresSafeArea()
                    .toolbar {
                        ToolbarItem(placement: .cancellationAction) {
                            Button("Close") {
                                showHttpLog = false
                            }
                        }
                    }
            }
        }
    }

    private func fetchMovies() async {
        errorText = nil
        isLoading = true
        defer { isLoading = false }
        do {
            let base = try ApiBaseURL(baseUrlText)
            let response: MoviesResponse = try await URLSessionApi.fetchJSON(from: base.url.appending(path: "/api/movies"))
            films = response.films.map { FilmSummary(imdbID: $0.imdbID, title: $0.Title, year: $0.Year) }
        } catch {
            errorText = error.localizedDescription
        }
    }
}

/// Wraps the Compose `UIViewController` for SwiftUI.
private struct HttpLogListViewControllerHost: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        HttpLogListViewControllerKt.createHttpLogListViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
