import SwiftUI
import WebKit
import Combine

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
    let viewModel: VideoPlayerViewModel

    func makeUIView(context: Context) -> WKWebView {
        let webConfiguration = WKWebViewConfiguration()
        webConfiguration.allowsInlineMediaPlayback = true

        // Allow videos to be played programmatically
        webConfiguration.mediaTypesRequiringUserActionForPlayback = []

        let userContentController = WKUserContentController()
        userContentController.add(context.coordinator, name: "playbackHandler")
        webConfiguration.userContentController = userContentController

        let webView = WKWebView(frame: .zero, configuration: webConfiguration)
        webView.navigationDelegate = context.coordinator

        // Set a mobile User-Agent to prevent YouTube from loading the desktop player on iPad
        webView.customUserAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1"

        let html = createPlayerHTML()
        webView.loadHTMLString(html, baseURL: URL(string: "https://www.youtube.com"))

        return webView
    }

    private func createPlayerHTML() -> String {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body, html { margin: 0; padding: 0; width: 100%; height: 100%; background-color: black; overflow: hidden; }
                #player { width: 100%; height: 100%; }
            </style>
        </head>
        <body>
            <div id="player"></div>
            <script>
                var tag = document.createElement('script');
                tag.src = "https://www.youtube.com/iframe_api";
                var firstScriptTag = document.getElementsByTagName('script')[0];
                firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

                var player;
                function onYouTubeIframeAPIReady() {
                    player = new YT.Player('player', {
                        playerVars: { 'playsinline': 1, 'controls': 0 },
                        events: {
                            'onReady': onPlayerReady,
                            'onStateChange': onPlayerStateChange,
                            'onError': onPlayerError
                        }
                    });
                }

                function onPlayerReady(event) {
                    window.webkit.messageHandlers.playbackHandler.postMessage({ type: 'ready' });
                }

                function onPlayerStateChange(event) {
                    window.webkit.messageHandlers.playbackHandler.postMessage({ type: 'stateChange', stateCode: event.data });
                    if (event.data == YT.PlayerState.PLAYING) { start_time_updater(); } else { stop_time_updater(); }
                }

                function onPlayerError(event) {
                    window.webkit.messageHandlers.playbackHandler.postMessage({ type: 'error', errorCode: event.data });
                }

                var time_updater = null;
                function start_time_updater() {
                    if (time_updater !== null) { return; }
                    time_updater = setInterval(function() {
                        var currentTime = player.getCurrentTime();
                        var duration = player.getDuration();
                        window.webkit.messageHandlers.playbackHandler.postMessage({
                            type: 'timeUpdate',
                            currentTime: currentTime,
                            duration: duration
                        });
                    }, 1000);
                }

                function stop_time_updater() {
                    if (time_updater !== null) { clearInterval(time_updater); time_updater = null; }
                }

                function loadVideo(videoId) { player.loadVideoById(videoId); }
                function playVideo() { player.playVideo(); }
                function pauseVideo() { player.pauseVideo(); }
                function seekTo(seconds) { player.seekTo(seconds, true); }
            </script>
        </body>
        </html>
        """
    }

    func updateUIView(_ uiView: WKWebView, context: Context) {
        // The view is now command-driven, so updateUIView should be minimal.
        // We might still need to load the initial video if it's not handled by a command.
        if context.coordinator.lastVideoID != videoID {
            uiView.evaluateJavaScript("loadVideo('\(videoID)');", completionHandler: nil)
            context.coordinator.lastVideoID = videoID
        }
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(self, viewModel: viewModel)
    }

    class Coordinator: NSObject, WKNavigationDelegate, WKScriptMessageHandler {
        var parent: YouTubePlayer
        var viewModel: VideoPlayerViewModel
        var lastVideoID: String?
        var lastPlayerState: YTPlayerState? = nil
        private var webView: WKWebView?
        private var cancellables = Set<AnyCancellable>()

        init(_ parent: YouTubePlayer, viewModel: VideoPlayerViewModel) {
            self.parent = parent
            self.viewModel = viewModel
            super.init()

            viewModel.commandPublisher
                .sink { [weak self] command in
                    self?.handle(command)
                }
                .store(in: &cancellables)
        }

        func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
            self.webView = webView
            // When the webview is ready, load the initial video.
            handle(.load(videoID: parent.videoID))
        }

        private func handle(_ command: PlayerCommand) {
            guard let webView = webView else { return }
            switch command {
            case .load(let videoID):
                webView.evaluateJavaScript("loadVideo('\(videoID)');", completionHandler: nil)
                lastVideoID = videoID
            case .play:
                webView.evaluateJavaScript("playVideo();", completionHandler: nil)
            case .pause:
                webView.evaluateJavaScript("pauseVideo();", completionHandler: nil)
            case .seek(let to):
                webView.evaluateJavaScript("seekTo(\(to));", completionHandler: nil)
            }
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
                case "ready":
                    self.viewModel.onAction(.playerReady)
                case "stateChange":
                    if let stateCode = dict["stateCode"] as? Int,
                       let state = YTPlayerState(rawValue: stateCode) {
                        self.lastPlayerState = state
                        // Also update the ViewModel's state based on the player's actual state
                        switch state {
                        case .playing:
                            self.viewModel.onAction(.setPlaying(true))
                        case .paused:
                            self.viewModel.onAction(.setPlaying(false))
                        case .ended:
                            self.viewModel.onAction(.nextSong)
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
