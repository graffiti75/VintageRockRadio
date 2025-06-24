package com.example.vintageradio.ui

sealed interface VideoPlayerAction {
    data object PlayPause : VideoPlayerAction
    data object NextSong : VideoPlayerAction
    data object PreviousSong : VideoPlayerAction
    data class SeekTo(val positionSeconds: Int) : VideoPlayerAction
    data class UpdatePlaybackTime(val timeSeconds: Int) : VideoPlayerAction
    data class UpdateTotalDuration(val durationSeconds: Int) : VideoPlayerAction
    data class LoadSongs(val songs: List<com.example.vintageradio.data.Song>) : VideoPlayerAction
    data class OnError(val error: String) : VideoPlayerAction
    data object DismissError : VideoPlayerAction
}
