package com.example.vintageradio.ui

import com.example.vintageradio.data.Song

data class VideoPlayerState(
    val songs: List<Song> = emptyList(),
    val currentSongIndex: Int = 0,
    val isPlaying: Boolean = false,
    val currentPlaybackTimeSeconds: Int = 0,
    val totalDurationSeconds: Int = 0, // It might be useful to store total duration
    val isLoading: Boolean = true,
    val error: String? = null // To display any error messages
) {
    val currentSong: Song?
        get() = songs.getOrNull(currentSongIndex)
}
