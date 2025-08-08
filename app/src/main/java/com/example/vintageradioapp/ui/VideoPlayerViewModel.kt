package com.example.vintageradioapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vintageradioapp.data.SongParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    private val songParser: SongParser,
    private val dispatcher: CoroutineDispatcher
) : ViewModel() {
    private val _state = MutableStateFlow(VideoPlayerState())
    val state: StateFlow<VideoPlayerState> = _state.asStateFlow()

    init {
        loadSongs("70")
    }

    fun checkIfIsTabletAndLandscape(isTabletAndLandscape: Boolean) {
        _state.update { it.copy(isTabletAndLandscape = isTabletAndLandscape) }
    }

    fun loadSongs(decade: String) {
        viewModelScope.launch(dispatcher) {
            _state.update {
                it.copy(
                    isLoading = true
                )
            }
            try {
                val songs = songParser.parseSongs(decade).shuffled()
                if (songs.isNotEmpty()) {
                    _state.update {
                        it.copy(
                            songs = songs,
                            isLoading = false,
                            currentSongIndex = 0, // Start with the first song of the shuffled list
                            currentPlaybackTimeSeconds = 0,
                            totalDurationSeconds = 0, // Will be updated by player
                            error = null
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "No songs found for decade $decade in ids.txt."
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Error loading songs: ${e.message}"
                    )
                }
            }
        }
    }

    fun onAction(action: VideoPlayerAction) {
        when (action) {
            is VideoPlayerAction.PlayPause -> {
                if (state.value.songs.isEmpty()) return // No action if no songs
                // If already playing, pause. If paused, play.
                val newIsPlaying = !state.value.isPlaying
                _state.update { it.copy(isPlaying = newIsPlaying) }
            }
            is VideoPlayerAction.SetPlaying -> {
                if (state.value.songs.isEmpty()) return
                if (state.value.isPlaying != action.playing) {
                    _state.update { it.copy(isPlaying = action.playing) }
                }
            }
            is VideoPlayerAction.NextSong -> {
                if (state.value.songs.isEmpty()) return
                goToNextSong()
            }
            is VideoPlayerAction.PreviousSong -> {
                if (state.value.songs.isEmpty()) return
                goToPreviousSong()
            }
            is VideoPlayerAction.SeekTo -> { // User finished seeking with slider
                if (state.value.songs.isEmpty()) return
                _state.update {
                    it.copy(
                        currentPlaybackTimeSeconds = action.positionSeconds
                    )
                }
            }
            is VideoPlayerAction.UpdatePlaybackTime -> { // Player reported time
                 if (state.value.songs.isEmpty()) return
                _state.update {
                    it.copy(
                        currentPlaybackTimeSeconds = action.timeSeconds
                    )
                }
            }
            is VideoPlayerAction.UpdateTotalDuration -> {
                 if (state.value.songs.isEmpty()) return
                _state.update {
                    it.copy(
                        totalDurationSeconds = action.durationSeconds
                    )
                }
            }
            is VideoPlayerAction.OnError -> {
                _state.update {
                    it.copy(
                        error = action.error,
                        isLoading = false,
                        isPlaying = false
                    )
                }
            }
            is VideoPlayerAction.DismissError -> {
                goToNextSong()
            }
            is VideoPlayerAction.ChangeDecade -> {
                handleChangeDecade(action.decade)
            }
        }
    }

    private fun goToPreviousSong() {
        _state.update { currentState ->
            val prevIndex = (currentState.currentSongIndex - 1 + currentState.songs.size) %
                currentState.songs.size
            currentState.copy(
                currentSongIndex = prevIndex,
                currentPlaybackTimeSeconds = 0, // Reset time for new song
                totalDurationSeconds = 0, // Reset duration for new song
                isPlaying = true, // Auto-play previous song
                isPrevButtonEnabled = true
            )
        }
    }

    private fun handleChangeDecade(decade: String) {
        _state.update { it.copy(currentDecade = decade) }
        loadSongs(decade)
        _state.update { it.copy(isPrevButtonEnabled = false) }
    }

    private fun goToNextSong() {
        _state.update { currentState ->
            val nextIndex = (currentState.currentSongIndex + 1) % currentState.songs.size
            currentState.copy(
                currentSongIndex = nextIndex,
                currentPlaybackTimeSeconds = 0, // Reset time for new song
                totalDurationSeconds = 0, // Reset duration for new song
                isPlaying = true, // Auto-play next song
                error = null,
                isPrevButtonEnabled = true
            )
        }
    }
}
