package com.example.vintageradioapp.ui // Updated package name

import android.app.Application
import androidx.lifecycle.AndroidViewModel // Using AndroidViewModel for Application context
import androidx.lifecycle.viewModelScope
// import com.example.vintageradioapp.data.Song // Not directly used here
import com.example.vintageradioapp.data.SongParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// @HiltViewModel // If using Hilt for dependency injection
class VideoPlayerViewModel(
    application: Application // constructor(application: Application) for AndroidViewModel
) : AndroidViewModel(application) {

    private val songParser = SongParser(application)

    private val _state = MutableStateFlow(VideoPlayerState())
    val state: StateFlow<VideoPlayerState> = _state.asStateFlow()

    // To command the player (e.g., seek) from the ViewModel if necessary, though direct control from UI is often cleaner
    // private val _playerCommand = MutableSharedFlow<PlayerCommand>()
    // val playerCommand = _playerCommand.asSharedFlow()

    init {
        loadSongs()
    }

    private fun loadSongs() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val songs = songParser.parseSongs() // This is now a suspend function
                if (songs.isNotEmpty()) {
                    _state.update {
                        it.copy(songs = songs, isLoading = false, currentSongIndex = 0, currentPlaybackTimeSeconds = 0)
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
            is VideoPlayerAction.PlayPause -> {
                _state.update { it.copy(isPlaying = !it.isPlaying) }
            }
            is VideoPlayerAction.NextSong -> {
                _state.update { currentState ->
                    val nextIndex = if (currentState.songs.isEmpty()) 0 else (currentState.currentSongIndex + 1) % currentState.songs.size
                    currentState.copy(
                        currentSongIndex = nextIndex,
                        currentPlaybackTimeSeconds = 0,
                        isPlaying = if (currentState.songs.isNotEmpty()) true else false // Auto-play next if songs available
                    )
                }
            }
            is VideoPlayerAction.PreviousSong -> {
                _state.update { currentState ->
                    val prevIndex = if (currentState.songs.isEmpty()) 0 else (currentState.currentSongIndex - 1 + currentState.songs.size) % currentState.songs.size
                    currentState.copy(
                        currentSongIndex = prevIndex,
                        currentPlaybackTimeSeconds = 0,
                        isPlaying = if (currentState.songs.isNotEmpty()) true else false // Auto-play previous if songs available
                    )
                }
            }
            is VideoPlayerAction.SeekTo -> { // User dragged slider
                _state.update { it.copy(currentPlaybackTimeSeconds = action.positionSeconds) }
                // Optionally, if player needs to be commanded from VM:
                // viewModelScope.launch { _playerCommand.emit(PlayerCommand.Seek(action.positionSeconds.toFloat())) }
            }
             is VideoPlayerAction.PlayerSeekTo -> {
                // This is a command for the player, usually triggered from UI after user finishes seek gesture
                // The ViewModel doesn't need to do much here other than potentially logging
                // The actual seek is handled by the UI observing this or a dedicated command flow
            }
            is VideoPlayerAction.UpdatePlaybackTime -> { // Player reported time
                // Only update if not actively seeking, to avoid slider jumpiness
                // This check might be better handled in the UI if slider has its own state
                _state.update { it.copy(currentPlaybackTimeSeconds = action.timeSeconds) }
            }
            is VideoPlayerAction.UpdateTotalDuration -> {
                _state.update { it.copy(totalDurationSeconds = action.durationSeconds) }
            }
            is VideoPlayerAction.OnError -> {
                _state.update { it.copy(error = action.error, isLoading = false) }
            }
            is VideoPlayerAction.DismissError -> {
                _state.update { it.copy(error = null) }
            }
        }
    }
}

// sealed interface PlayerCommand {
//     data class Seek(val positionSeconds: Float) : PlayerCommand
// }
