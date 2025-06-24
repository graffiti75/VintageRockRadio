package com.example.vintageradioapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vintageradioapp.data.SongParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VideoPlayerViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val songParser = SongParser(application)

    private val _state = MutableStateFlow(VideoPlayerState())
    val state: StateFlow<VideoPlayerState> = _state.asStateFlow()

    init {
        loadSongs()
    }

    private fun loadSongs() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val songs = songParser.parseSongs()
                if (songs.isNotEmpty()) {
                    _state.update {
                        it.copy(
                            songs = songs,
                            isLoading = false,
                            currentSongIndex = 0,
                            currentPlaybackTimeSeconds = 0,
                            totalDurationSeconds = 0, // Will be updated by player
                            error = null
                        )
                    }
                } else {
                    _state.update {
                        it.copy(isLoading = false, error = "No songs found in ids.txt.")
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false, error = "Error loading songs: ${e.message}")
                }
            }
        }
    }

    fun onAction(action: VideoPlayerAction) {
        when (action) {
            is VideoPlayerAction.PlayPause -> {
                if (state.value.songs.isEmpty()) return // No action if no songs
                _state.update { it.copy(isPlaying = !it.isPlaying) }
            }
            is VideoPlayerAction.NextSong -> {
                if (state.value.songs.isEmpty()) return
                _state.update { currentState ->
                    val nextIndex = (currentState.currentSongIndex + 1) % currentState.songs.size
                    currentState.copy(
                        currentSongIndex = nextIndex,
                        currentPlaybackTimeSeconds = 0, // Reset time for new song
                        totalDurationSeconds = 0, // Reset duration for new song
                        isPlaying = true // Auto-play next song
                    )
                }
            }
            is VideoPlayerAction.PreviousSong -> {
                if (state.value.songs.isEmpty()) return
                _state.update { currentState ->
                    val prevIndex = (currentState.currentSongIndex - 1 + currentState.songs.size) % currentState.songs.size
                    currentState.copy(
                        currentSongIndex = prevIndex,
                        currentPlaybackTimeSeconds = 0, // Reset time for new song
                        totalDurationSeconds = 0, // Reset duration for new song
                        isPlaying = true // Auto-play previous song
                    )
                }
            }
            is VideoPlayerAction.SeekTo -> { // User finished seeking with slider
                if (state.value.songs.isEmpty()) return
                _state.update { it.copy(currentPlaybackTimeSeconds = action.positionSeconds) }
                // The player itself is commanded to seek from the UI (Slider's onValueChangeFinished)
            }
            is VideoPlayerAction.UpdatePlaybackTime -> { // Player reported time
                 if (state.value.songs.isEmpty()) return
                _state.update { it.copy(currentPlaybackTimeSeconds = action.timeSeconds) }
            }
            is VideoPlayerAction.UpdateTotalDuration -> {
                 if (state.value.songs.isEmpty()) return
                _state.update { it.copy(totalDurationSeconds = action.durationSeconds) }
            }
            is VideoPlayerAction.OnError -> {
                _state.update { it.copy(error = action.error, isLoading = false, isPlaying = false) }
            }
            is VideoPlayerAction.DismissError -> {
                _state.update { it.copy(error = null) }
                // Optionally reload songs or go to a known state if error was critical
                if (state.value.songs.isEmpty()) loadSongs()
            }
        }
    }
}
