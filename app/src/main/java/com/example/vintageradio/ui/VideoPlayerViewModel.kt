package com.example.vintageradio.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vintageradio.data.Song
import com.example.vintageradio.data.SongParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// @HiltViewModel // If using Hilt for dependency injection
class VideoPlayerViewModel(
    application: Application // Or inject SongParser directly if using Hilt
) : ViewModel() {

    private val songParser = SongParser(application) // Manual instantiation

    private val _state = MutableStateFlow(VideoPlayerState())
    val state: StateFlow<VideoPlayerState> = _state.asStateFlow()

    init {
        loadSongs()
    }

    private fun loadSongs() {
        viewModelScope.launch {
            try {
                val songs = songParser.parseSongs()
                if (songs.isNotEmpty()) {
                    _state.update {
                        it.copy(songs = songs, isLoading = false)
                    }
                } else {
                    _state.update {
                        it.copy(isLoading = false, error = "No songs found.")
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
            is VideoPlayerAction.LoadSongs -> { /* This is handled internally now */ }
            is VideoPlayerAction.PlayPause -> {
                _state.update { it.copy(isPlaying = !it.isPlaying) }
                // Actual play/pause logic will be triggered by observing isPlaying in the UI
            }
            is VideoPlayerAction.NextSong -> {
                _state.update { currentState ->
                    val nextIndex = if (currentState.songs.isEmpty()) 0 else (currentState.currentSongIndex + 1) % currentState.songs.size
                    currentState.copy(
                        currentSongIndex = nextIndex,
                        currentPlaybackTimeSeconds = 0,
                        // isPlaying = true // Optionally auto-play next song
                    )
                }
            }
            is VideoPlayerAction.PreviousSong -> {
                _state.update { currentState ->
                    val prevIndex = if (currentState.songs.isEmpty()) 0 else (currentState.currentSongIndex - 1 + currentState.songs.size) % currentState.songs.size
                    currentState.copy(
                        currentSongIndex = prevIndex,
                        currentPlaybackTimeSeconds = 0,
                        // isPlaying = true // Optionally auto-play previous song
                    )
                }
            }
            is VideoPlayerAction.SeekTo -> {
                // The actual seeking will happen in the YouTube player via LaunchedEffect in UI
                // This action updates the state, and the UI observes it.
                // The player itself should be commanded to seek by the UI component.
                _state.update { it.copy(currentPlaybackTimeSeconds = action.positionSeconds) }
            }
            is VideoPlayerAction.UpdatePlaybackTime -> {
                // Prevent overwriting if user is currently seeking
                if (_state.value.currentPlaybackTimeSeconds != action.timeSeconds) {
                     _state.update { it.copy(currentPlaybackTimeSeconds = action.timeSeconds) }
                }
            }
            is VideoPlayerAction.UpdateTotalDuration -> {
                _state.update { it.copy(totalDurationSeconds = action.durationSeconds) }
            }
            is VideoPlayerAction.OnError -> {
                _state.update { it.copy(error = action.error) }
            }
            is VideoPlayerAction.DismissError -> {
                _state.update { it.copy(error = null) }
            }
        }
    }
}
