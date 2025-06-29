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
import kotlin.collections.get
import kotlin.text.set

class VideoPlayerViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val songParser = SongParser(application)

    private val _state = MutableStateFlow(VideoPlayerState())
    val state: StateFlow<VideoPlayerState> = _state.asStateFlow()

    private var wasPlayingBeforeBackground: Boolean = false

    init {
        loadSongs()
    }

    private fun loadSongs() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true
                )
            }
            try {
                val songs = songParser.parseSongs().shuffled().toMutableList().apply {
                    if (isNotEmpty()) {
                        this[0] = this[0].copy(youtubeId = "kDNnARSBamU")
                    }
                }
                println("----- Loaded songs: ${songs}")
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
                            error = "No songs found in ids.txt."
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
            is VideoPlayerAction.AppWentToBackground -> {
                wasPlayingBeforeBackground = state.value.isPlaying
            }
            is VideoPlayerAction.AppCameToForeground -> {
                if (wasPlayingBeforeBackground && state.value.currentSong != null) {
                    _state.update {
                        it.copy(
                            currentPlaybackTimeSeconds = 0, // Restart the song
                            isPlaying = true // Ensure it plays
                        )
                    }
                }
                wasPlayingBeforeBackground = false // Reset flag
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
                isPlaying = true // Auto-play previous song
            )
        }
    }

    private fun goToNextSong() {
        _state.update { currentState ->
            val nextIndex = (currentState.currentSongIndex + 1) % currentState.songs.size
            currentState.copy(
                currentSongIndex = nextIndex,
                currentPlaybackTimeSeconds = 0, // Reset time for new song
                totalDurationSeconds = 0, // Reset duration for new song
                isPlaying = true, // Auto-play next song
                error = null
            )
        }
    }
}
