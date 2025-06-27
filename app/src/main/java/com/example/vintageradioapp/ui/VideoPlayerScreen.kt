package com.example.vintageradioapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.vintageradioapp.data.Song
import com.example.vintageradioapp.ui.theme.VintageRadioAppTheme
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun LockScreenOrientation(orientation: Int) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context as? Activity ?: return@DisposableEffect onDispose {}
        val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = orientation
        onDispose {
            // Restore original orientation when composable leaves the composition
            activity.requestedOrientation = originalOrientation
        }
    }
}

@Composable
fun VideoPlayerScreen(viewModel: VideoPlayerViewModel) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
    val state by viewModel.state.collectAsState()
    VideoPlayerScreenContent(state = state, onAction = viewModel::onAction)
}

@Composable
fun VideoPlayerScreenContent(state: VideoPlayerState, onAction: (VideoPlayerAction) -> Unit) {
    val context = LocalContext.current
    val youTubePlayerView = remember { YouTubePlayerView(context) }
    var playerRef by remember { mutableStateOf<YouTubePlayer?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current

    // Player Lifecycle Management tied to Composable lifecycle & app lifecycle
    DisposableEffect(key1 = lifecycleOwner, key2 = state.currentSong?.youtubeId) {
        val youtubePlayerListener = object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                playerRef = youTubePlayer
                state.currentSong?.let { song ->
                    // When player is ready, load or cue based on current isPlaying state.
                    // This ensures that if a song is selected while paused, it cues; if playing, it loads and plays.
                    if (state.isPlaying) {
                        youTubePlayer.loadVideo(song.youtubeId, state.currentPlaybackTimeSeconds.toFloat())
                    } else {
                        youTubePlayer.cueVideo(song.youtubeId, state.currentPlaybackTimeSeconds.toFloat())
                    }
                }
            }

            override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                onAction(VideoPlayerAction.UpdateTotalDuration(duration.toInt()))
            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                onAction(VideoPlayerAction.UpdatePlaybackTime(second.toInt()))
            }

            override fun onError(youTubePlayer: YouTubePlayer, error: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerError) {
                onAction(VideoPlayerAction.OnError("YT Player Error: ${error.name}"))
            }

            override fun onStateChange(youTubePlayer: YouTubePlayer, playerState: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState) {
                val currentViewModelIsPlaying = state.isPlaying
                when (playerState) {
                    PlayerConstants.PlayerState.PLAYING -> {
                        if (!currentViewModelIsPlaying) onAction(VideoPlayerAction.PlayPause)
                    }
                    PlayerConstants.PlayerState.PAUSED -> {
                        if (currentViewModelIsPlaying) onAction(VideoPlayerAction.PlayPause)
                    }
                    PlayerConstants.PlayerState.ENDED -> {
                        onAction(VideoPlayerAction.NextSong)
                    }
                    else -> {}
                }
            }
        }

        youTubePlayerView.enableAutomaticInitialization = false
        // Controls=0: Disable native controls. Autoplay=0: Don't autoplay unless logic dictates.
        val options = IFramePlayerOptions.Builder().controls(0).autoplay(0).build()
        youTubePlayerView.initialize(youtubePlayerListener, options)


        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> playerRef?.let { if (state.isPlaying) it.play() }
                Lifecycle.Event.ON_PAUSE -> playerRef?.pause() // Always pause when app is paused
                // ON_DESTROY is handled by onDispose
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            playerRef = null
            youTubePlayerView.release() // Release player resources
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }

    // Effect to handle play/pause commands from ViewModel and song changes.
    // This uses loadVideo when isPlaying becomes true, which is more assertive for starting playback,
    // especially after a seek or when a new song is loaded while in play mode.
    // It's keyed by isPlaying, playerRef (which changes on new song), and currentSong's ID.
    LaunchedEffect(state.isPlaying, playerRef, state.currentSong?.youtubeId) {
        playerRef?.let { player ->
            state.currentSong?.let { song ->
                if (state.isPlaying) {
                    // When isPlaying becomes true, or song changes while isPlaying is true,
                    // load the video from the current playback time.
                    // This ensures that after a seek and then play, or new song, playback starts correctly.
                    player.loadVideo(song.youtubeId, state.currentPlaybackTimeSeconds.toFloat())
                } else {
                    // If isPlaying is false, ensure the player is paused.
                    player.pause()
                }
            }
        }
    }

    // Effect to handle seek commands (e.g. when currentPlaybackTimeSeconds changes due to slider)
    // This is tricky. The primary source of truth for player seek should be user interaction.
    // The ViewModel state.currentPlaybackTimeSeconds is updated by both user and player.
    // A more robust way might be a dedicated command flow or ensuring only onValueChangeFinished triggers this.
    // For now, let's assume the player instance (playerRef) is the authority for seeks.

    // --- UI ---
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (state.isLoading && state.songs.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
        } else if (state.error != null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { onAction(VideoPlayerAction.DismissError) }) {
                    Text("Dismiss", style = MaterialTheme.typography.labelLarge)
                }
            }
        } else if (state.songs.isEmpty() && !state.isLoading) {
             Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No songs loaded.", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { /* Trigger reload or provide guidance */ }) { // Placeholder for retry/reload
                    Text("Retry Load", style = MaterialTheme.typography.labelLarge)
                }
            }
        } else {
            val currentSong = state.currentSong
            Row(
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                // Left Column: Video Player and Song Info
                Column(
                    modifier = Modifier.weight(0.65f).fillMaxHeight().padding(end = 8.dp)
                ) {
                    AndroidView(
                        factory = { youTubePlayerView },
                        modifier = Modifier.fillMaxWidth().aspectRatio(16 / 9f).background(Color.Black)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    currentSong?.let { song ->
                        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                            Text(song.band, style = MaterialTheme.typography.displayMedium)
                            Text(song.songTitle, style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Year: ${song.year} (${song.decade}s)", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("ID: ${song.youtubeId}", style = MaterialTheme.typography.bodySmall)
                        }
                    } ?: Box(modifier = Modifier.fillMaxWidth().padding(8.dp).heightIn(min = 100.dp)) { // Ensure some space if no song
                        Text(
                            "No song selected.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                // Right Column: Controls
                Column(
                    modifier = Modifier.weight(0.35f).fillMaxHeight().padding(start = 8.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)) // Softer background
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { onAction(VideoPlayerAction.PlayPause) },
                        enabled = currentSong != null,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(if (state.isPlaying) "Pause" else "Play", style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    var sliderPosition by remember(state.currentPlaybackTimeSeconds) { mutableFloatStateOf(state.currentPlaybackTimeSeconds.toFloat()) }

                    Slider(
                        value = sliderPosition,
                        onValueChange = { newValue -> sliderPosition = newValue },
                        onValueChangeFinished = {
                            playerRef?.seekTo(sliderPosition)
                            onAction(VideoPlayerAction.SeekTo(sliderPosition.toInt()))
                        },
                        valueRange = 0f..(state.totalDurationSeconds.toFloat().takeIf { it > 0f } ?: 100f),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = currentSong != null && state.totalDurationSeconds > 0,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                    )
                    Text(
                        text = "${formatTime(sliderPosition.toInt())} / ${formatTime(state.totalDurationSeconds)}",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { onAction(VideoPlayerAction.PreviousSong) },
                            enabled = state.songs.size > 1,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) { Text("Prev", style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onSecondary)) }
                        Button(
                            onClick = { onAction(VideoPlayerAction.NextSong) },
                            enabled = state.songs.size > 1,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) { Text("Next", style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onSecondary)) }
                    }
                }
            }
        }
    }
}

fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240,orientation=landscape")
@Composable
fun VideoPlayerScreenPreview() {
    val previewSongs = listOf(
        Song("70", "1975", "Queen", "Bohemian Rhapsody", "fJ9rUzIMcZQ"),
        Song("70", "1971", "Led Zeppelin", "Stairway to Heaven", "iXQUu5Dti4g")
    )
    val previewState = VideoPlayerState(
        songs = previewSongs,
        currentSongIndex = 0,
        isPlaying = false,
        currentPlaybackTimeSeconds = 30,
        totalDurationSeconds = 240,
        isLoading = false
    )
    VintageRadioAppTheme {
        Surface {
            VideoPlayerScreenContent(state = previewState, onAction = {})
        }
    }
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240,orientation=landscape", name = "Loading State")
@Composable
fun VideoPlayerScreenLoadingPreview() {
    val previewState = VideoPlayerState(isLoading = true)
    VintageRadioAppTheme {
        Surface {
            VideoPlayerScreenContent(state = previewState, onAction = {})
        }
    }
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240,orientation=landscape", name = "Error State")
@Composable
fun VideoPlayerScreenErrorPreview() {
    val previewState = VideoPlayerState(isLoading = false, error = "Failed to load songs. Please check connection.")
    VintageRadioAppTheme {
        Surface {
            VideoPlayerScreenContent(state = previewState, onAction = {})
        }
    }
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240,orientation=landscape", name = "No Songs State")
@Composable
fun VideoPlayerScreenNoSongsPreview() {
    val previewState = VideoPlayerState(isLoading = false, songs = emptyList())
    VintageRadioAppTheme {
        Surface {
            VideoPlayerScreenContent(state = previewState, onAction = {})
        }
    }
}
