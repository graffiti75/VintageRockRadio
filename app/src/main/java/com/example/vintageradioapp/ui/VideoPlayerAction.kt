package com.example.vintageradioapp.ui

// import com.example.vintageradioapp.data.Song // Not needed here

sealed interface VideoPlayerAction {
    data object PlayPause : VideoPlayerAction
    data object NextSong : VideoPlayerAction
    data object PreviousSong : VideoPlayerAction
    data class SeekTo(val positionSeconds: Int) : VideoPlayerAction // User initiated seek finished
    // data class PlayerSeekCommand(val positionSeconds: Float) : VideoPlayerAction // Command player to seek, if needed via VM
    data class UpdatePlaybackTime(val timeSeconds: Int) : VideoPlayerAction // Player reported time
    data class UpdateTotalDuration(val durationSeconds: Int) : VideoPlayerAction
    data class OnError(val error: String) : VideoPlayerAction
    data object DismissError : VideoPlayerAction
}
