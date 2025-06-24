package com.example.vintageradioapp.ui

import com.example.vintageradioapp.data.Song

data class VideoPlayerState(
    val songs: List<Song> = emptyList(),
    val currentSongIndex: Int = 0,
    val isPlaying: Boolean = false,
    val currentPlaybackTimeSeconds: Int = 0,
    val totalDurationSeconds: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val currentSong: Song?
        get() = songs.getOrNull(currentSongIndex)
}
