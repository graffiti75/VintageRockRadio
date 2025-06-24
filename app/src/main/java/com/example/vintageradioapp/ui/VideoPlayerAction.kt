package com.example.vintageradioapp.ui // Updated package name

import com.example.vintageradioapp.data.Song // Updated import

sealed interface VideoPlayerAction {
    data object PlayPause : VideoPlayerAction
    data object NextSong : VideoPlayerAction
    data object PreviousSong : VideoPlayerAction
    data class SeekTo(val positionSeconds: Int) : VideoPlayerAction // User initiated seek
    data class PlayerSeekTo(val positionSeconds: Float) : VideoPlayerAction // Command player to seek
    data class UpdatePlaybackTime(val timeSeconds: Int) : VideoPlayerAction // Player reported time
    data class UpdateTotalDuration(val durationSeconds: Int) : VideoPlayerAction
    // data class LoadSongs(val songs: List<Song>) : VideoPlayerAction // Loaded internally now
    data class OnError(val error: String) : VideoPlayerAction
    data object DismissError : VideoPlayerAction
}
