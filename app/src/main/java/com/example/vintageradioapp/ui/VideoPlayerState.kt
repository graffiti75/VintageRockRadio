package com.example.vintageradioapp.ui

import com.example.vintageradioapp.data.Song

data class VideoPlayerState(
    val songs: List<Song> = List(1) { Song() },
    val currentSongIndex: Int = 0,
    val isPlaying: Boolean = false,
    val currentPlaybackTimeSeconds: Int = 0,
    val totalDurationSeconds: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
    val triggerRestart: Boolean = false
) {
    val currentSong: Song?
        get() = songs.getOrNull(currentSongIndex)
}
