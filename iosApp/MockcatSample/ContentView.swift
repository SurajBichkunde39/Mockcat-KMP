import SwiftUI

/// 1) Build KMP frameworks from the repo (see [AGENT.md]).
/// 2) In Xcode, add the produced `.framework` or `.xcframework` for `MockcatApi`,
///    `MockcatPersistence`, and `MockcatInterceptUrlsession` and embed.
/// 3) Implement a `URLProtocol` subclass that calls
///    `RunMockcatUrlSessionResolve` with a `getMockcatStoreForIos()` store, or build
///    [HttpRequestMetadata] in Swift and use the shared store API.
struct ContentView: View {
    @State private var url: String = "https://example.com"
    @State private var result: String = "Build frameworks and wire Mockcat; see AGENT.md."

    var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 12) {
                TextField("URL", text: $url)
                    .textFieldStyle(.roundedBorder)
                Button("Go (URLSession)") { /* wire URLSessionConfiguration.protocolClasses */ }
                Text(result)
            }
            .padding()
            .navigationTitle("Mockcat (SwiftUI)")
        }
    }
}
