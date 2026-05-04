import MockcatLoggerUI
import SwiftUI

/// `installHttpLogReaderForIos()` and `createHttpLogListViewController()` come from the
/// `MockcatLoggerUI` framework (build with Gradle, see README in this folder).
struct ContentView: View {
    @State private var showHttpLog = false

    var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 12) {
                Text(
                    "Build MockcatLoggerUI, embed in Xcode, then open the HTTP log. " +
                        "The list is empty until traffic is written to the shared log store."
                )
                .font(.body)
                Button("Open HTTP log") {
                    showHttpLog = true
                }
            }
            .padding()
            .navigationTitle("Mockcat (SwiftUI)")
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
}

/// Wraps the Compose `UIViewController` for SwiftUI.
private struct HttpLogListViewControllerHost: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        HttpLogListViewControllerKt.createHttpLogListViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
