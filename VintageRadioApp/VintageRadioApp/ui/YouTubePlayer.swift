import SwiftUI
import WebKit

enum YTPlayerState: Int {
    case unstarted = -1
    case ended = 0
    case playing = 1
    case paused = 2
    case buffering = 3
    case cued = 5
}

struct YouTubePlayer: UIViewRepresentable {
    let videoID: String
    let isPlaying: Bool
    let seekTo: Double
    let viewModel: VideoPlayerViewModel

    func makeUIView(context: Context) -> WKWebView {
        let webConfiguration = WKWebViewConfiguration()
        webConfiguration.allowsInlineMediaPlayback = true
        let userContentController = WKUserContentController()
        userContentController.add(context.coordinator, name: "playbackHandler")
        webConfiguration.userContentController = userContentController

        let webView = WKWebView(frame: .zero, configuration: webConfiguration)
        webView.navigationDelegate = context.coordinator

        do {
            if let url = Bundle.main.url(forResource: "youtube_player", withExtension: "html") {
                let htmlString = try String(contentsOf: url)
                webView.loadHTMLString(htmlString, baseURL: URL(string: "https://www.youtube.com"))
            }
        } catch {
            print("Error loading youtube_player.html: \(error)")
        }

        return webView
    }

    func updateUIView(_ uiView: WKWebView, context: Context) {
        if context.coordinator.lastVideoID != videoID {
            uiView.evaluateJavaScript("loadVideo('\(videoID)');", completionHandler: nil)
            context.coordinator.lastVideoID = videoID
        }

        // Only send play/pause commands if the player's state is different from the desired state
        if isPlaying && context.coordinator.lastPlayerState != .playing {
            uiView.evaluateJavaScript("playVideo();", completionHandler: nil)
        } else if !isPlaying && context.coordinator.lastPlayerState != .paused {
            uiView.evaluateJavaScript("pauseVideo();", completionHandler: nil)
        }

        if abs(seekTo - context.coordinator.lastSeekTo) > 1 {
            uiView.evaluateJavaScript("seekTo(\(seekTo));", completionHandler: nil)
            context.coordinator.lastSeekTo = seekTo
        }
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(self, viewModel: viewModel)
    }

    class Coordinator: NSObject, WKNavigationDelegate, WKScriptMessageHandler {
        var parent: YouTubePlayer
        var viewModel: VideoPlayerViewModel
        var lastVideoID: String?
        var lastSeekTo: Double = 0
        var lastPlayerState: YTPlayerState? = nil

        init(_ parent: YouTubePlayer, viewModel: VideoPlayerViewModel) {
            self.parent = parent
            self.viewModel = viewModel
        }

        func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
            guard message.name == "playbackHandler",
                  let dict = message.body as? [String: Any],
                  let type = dict["type"] as? String else { return }

            DispatchQueue.main.async {
                switch type {
                case "timeUpdate":
                    if let currentTime = dict["currentTime"] as? Double,
                       let duration = dict["duration"] as? Double {
                        self.viewModel.onAction(.updatePlaybackTime(currentTime))
                        self.viewModel.onAction(.updateTotalDuration(duration))
                    }
                case "error":
                    if let errorCode = dict["errorCode"] as? Int {
                        self.viewModel.onAction(.onPlayerError(errorCode))
                    }
                case "stateChange":
                    if let stateCode = dict["stateCode"] as? Int,
                       let state = YTPlayerState(rawValue: stateCode) {
                        self.lastPlayerState = state
                        // Also update the ViewModel's state based on the player's actual state
                        switch state {
                        case .playing:
                            self.viewModel.onAction(.setPlaying(true))
                        case .paused, .ended:
                            self.viewModel.onAction(.setPlaying(false))
                        default:
                            break // Do nothing for buffering, cued, etc.
                        }
                    }
                default:
                    break
                }
            }
        }
    }
}
