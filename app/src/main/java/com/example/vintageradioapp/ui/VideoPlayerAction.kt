package com.example.vintageradioapp.ui

sealed interface VideoPlayerAction {
    data object PlayPause : VideoPlayerAction
    data object NextSong : VideoPlayerAction
    data object PreviousSong : VideoPlayerAction
    data class SeekTo(val positionSeconds: Int) : VideoPlayerAction // User initiated seek finished
    data class UpdatePlaybackTime(val timeSeconds: Int) : VideoPlayerAction // Player reported time
    data class UpdateTotalDuration(val durationSeconds: Int) : VideoPlayerAction
    data class OnError(val error: String) : VideoPlayerAction
    data object DismissError : VideoPlayerAction
    data class SetPlaying(val playing: Boolean) : VideoPlayerAction // For onStateChange synchronization
    data object AppWentToBackground : VideoPlayerAction
    data object AppCameToForeground : VideoPlayerAction
    data object RestartTriggerConsumed : VideoPlayerAction
}
