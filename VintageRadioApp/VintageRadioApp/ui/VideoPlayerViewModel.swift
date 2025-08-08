import Foundation
import Combine

class VideoPlayerViewModel: ObservableObject {
    @Published private(set) var state = VideoPlayerState()

    private let songParser = SongParser()

    init() {
        loadSongs(decade: state.currentDecade)
    }

    func onAction(_ action: VideoPlayerAction) {
        switch action {
        case .playPause:
            if state.songs.isEmpty { return }
            state.isPlaying.toggle()
        case .setPlaying(let playing):
            if state.songs.isEmpty { return }
            if state.isPlaying != playing {
                state.isPlaying = playing
            }
        case .nextSong:
            if state.songs.isEmpty { return }
            goToNextSong()
        case .previousSong:
            if state.songs.isEmpty { return }
            goToPreviousSong()
        case .seekTo(let positionSeconds):
            if state.songs.isEmpty { return }
            state.currentPlaybackTimeSeconds = positionSeconds
        case .updatePlaybackTime(let timeSeconds):
            if state.songs.isEmpty { return }
            state.currentPlaybackTimeSeconds = timeSeconds
        case .updateTotalDuration(let durationSeconds):
            if state.songs.isEmpty { return }
            state.totalDurationSeconds = durationSeconds
        case .onError(let error):
            state.error = error
            state.isLoading = false
            state.isPlaying = false
        case .dismissError:
            goToNextSong()
        case .changeDecade(let decade):
            handleChangeDecade(decade: decade)
        case .checkIfIsTabletAndLandscape(let isTabletAndLandscape):
            state.isTabletAndLandscape = isTabletAndLandscape
        }
    }

    private func loadSongs(decade: String) {
        state.isLoading = true
        let songs = songParser.parseSongs(decade: decade).shuffled()
        if songs.isNotEmpty {
            state.songs = songs
            state.isLoading = false
            state.currentSongIndex = 0
            state.currentPlaybackTimeSeconds = 0
            state.totalDurationSeconds = 0
            state.error = nil
        } else {
            state.isLoading = false
            state.error = "No songs found for decade \(decade) in ids.txt."
        }
    }

    private func goToNextSong() {
        let nextIndex = (state.currentSongIndex + 1) % state.songs.count
        state.currentSongIndex = nextIndex
        state.currentPlaybackTimeSeconds = 0
        state.totalDurationSeconds = 0
        state.isPlaying = true
        state.error = nil
        state.isPrevButtonEnabled = true
    }

    private func goToPreviousSong() {
        let prevIndex = (state.currentSongIndex - 1 + state.songs.count) % state.songs.count
        state.currentSongIndex = prevIndex
        state.currentPlaybackTimeSeconds = 0
        state.totalDurationSeconds = 0
        state.isPlaying = true
        state.isPrevButtonEnabled = true
    }

    private func handleChangeDecade(decade: String) {
        state.currentDecade = decade
        loadSongs(decade: decade)
        state.isPrevButtonEnabled = false
    }
}
