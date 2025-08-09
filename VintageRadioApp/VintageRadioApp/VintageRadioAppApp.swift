import SwiftUI

@main
struct VintageRadioAppApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            VideoPlayerView()
        }
    }
}
