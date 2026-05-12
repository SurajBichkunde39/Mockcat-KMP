import SwiftUI

extension Notification.Name {
    static let mockcatOpenLogger = Notification.Name("MockcatOpenLogger")
}

@main
struct MockcatSampleApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
