package com.example.vintageradioapp.ui

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun VideoPlayerScreen(viewModel: VideoPlayerViewModel) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    // Remembering the YouTubePlayerView instance
    val youTubePlayerView = remember { YouTubePlayerView(context) }
    var playerRef by remember { mutableStateOf<YouTubePlayer?>(null) } // Hold the player instance

    // Observe lifecycle to correctly pause/play the player
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, playerRef) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    if (state.isPlaying) playerRef?.pause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    // Only resume if it was playing before pause and the app intends it to play
                    if (state.isPlaying) playerRef?.play()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    // youTubePlayerView.release() // Handled in onDispose of AndroidView's DisposableEffect
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    // Main player setup and control logic
    DisposableEffect(state.currentSong?.youtubeId) {
        val listener = object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                playerRef = youTubePlayer
                state.currentSong?.let { song ->
                    youTubePlayer.loadVideo(song.youtubeId, state.currentPlaybackTimeSeconds.toFloat())
                    viewModel.onAction(VideoPlayerAction.UpdateTotalDuration(0))
                }
            }

            override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                viewModel.onAction(VideoPlayerAction.UpdateTotalDuration(duration.toInt()))
            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                viewModel.onAction(VideoPlayerAction.UpdatePlaybackTime(second.toInt()))
            }

            override fun onVideoEnded(youTubePlayer: YouTubePlayer) {
                viewModel.onAction(VideoPlayerAction.NextSong)
            }

            override fun onError(youTubePlayer: YouTubePlayer, error: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerError) {
                viewModel.onAction(VideoPlayerAction.OnError("Youtube Player Error: ${error.name}"))
            }

            override fun onStateChange(youTubePlayer: YouTubePlayer, playerState: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState) {
                val currentViewModelIsPlaying = state.isPlaying
                when (playerState) {
                    com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.PLAYING -> {
                        if (!currentViewModelIsPlaying) viewModel.onAction(VideoPlayerAction.PlayPause)
                    }
                    com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.PAUSED -> {
                        if (currentViewModelIsPlaying) viewModel.onAction(VideoPlayerAction.PlayPause)
                    }
                    else -> {}
                }
            }
        }
        youTubePlayerView.enableAutomaticInitialization = false
        youTubePlayerView.initialize(listener, IFramePlayerOptions.Builder().controls(0).autoplay(if(state.songs.isEmpty()) 0 else 1).build())


        onDispose {
            playerRef = null // Clear the reference
            youTubePlayerView.release()
        }
    }

    // Effect for Play/Pause commands from ViewModel
    LaunchedEffect(state.isPlaying, playerRef) {
        playerRef?.let { player ->
            if (state.isPlaying) {
                player.play()
            } else {
                player.pause()
            }
        }
    }

    // Effect for Seeking command from ViewModel (e.g. after slider drag)
    // This is one way to handle it; direct call onValueChangeFinished is also good.
    // This might be redundant if onValueChangeFinished directly calls player.seekTo
    LaunchedEffect(state.currentPlaybackTimeSeconds, playerRef) {
        // This effect is tricky because currentPlaybackTimeSeconds also updates FROM the player.
        // We only want to seek if this change was user-initiated (e.g. from slider).
        // A dedicated "command" for seeking (e.g., a SharedFlow) from ViewModel might be cleaner
        // or ensuring the slider's onValueChangeFinished is the sole source of truth for commanding a seek.
        // For now, assuming the slider onValueChangeFinished is the primary way to command a seek.
    }


    if (state.isLoading && state.songs.isEmpty()) { // Show loading only if songs aren't loaded yet
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    if (state.error != null) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { viewModel.onAction(VideoPlayerAction.DismissError) }) {
                    Text("Dismiss")
                }
            }
        }
        return
    }

    val currentSong = state.currentSong

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Left Column: Video Player and Song Info
        Column(
            modifier = Modifier
                .weight(0.65f) // Adjusted weight
                .fillMaxHeight()
                .padding(end = 8.dp)
        ) {
            AndroidView(
                factory = { youTubePlayerView },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 9f) // Maintain aspect ratio
                    .background(Color.Black)
            )

            Spacer(modifier = Modifier.height(16.dp))

            currentSong?.let { song ->
                Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Text(song.band, style = MaterialTheme.typography.displayMedium)
                    Text(song.songTitle, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Album Year: ${song.year} (${song.decade}s)", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("ID: ${song.youtubeId}", style = MaterialTheme.typography.bodySmall)
                }
            } ?: Box(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                 Text("No song selected or available.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
            }
        }

        // Right Column: Controls
        Column(
            modifier = Modifier
                .weight(0.35f) // Adjusted weight
                .fillMaxHeight()
                .padding(start = 8.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Play/Pause Button
            Button(
                onClick = { viewModel.onAction(VideoPlayerAction.PlayPause) },
                enabled = currentSong != null,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text(if (state.isPlaying) "Pause" else "Play", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(modifier = Modifier.height(16.dp))

            var sliderPosition by remember(state.currentPlaybackTimeSeconds) { mutableStateOf(state.currentPlaybackTimeSeconds.toFloat()) }

            Slider(
                value = sliderPosition,
                onValueChange = { newValue -> sliderPosition = newValue },
                onValueChangeFinished = {
                    playerRef?.seekTo(sliderPosition) // Command player to seek
                    viewModel.onAction(VideoPlayerAction.SeekTo(sliderPosition.toInt())) // Inform VM of user action
                },
                valueRange = 0f..(state.totalDurationSeconds.toFloat().takeIf { it > 0 } ?: 100f),
                modifier = Modifier.fillMaxWidth(),
                enabled = currentSong != null,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f)
                )
            )
            Text(
                text = formatTime(sliderPosition.toInt()) + " / " + formatTime(state.totalDurationSeconds),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { viewModel.onAction(VideoPlayerAction.PreviousSong) },
                    enabled = state.songs.size > 1,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Prev", style = MaterialTheme.typography.labelLarge)
                }
                Button(
                    onClick = { viewModel.onAction(VideoPlayerAction.NextSong) },
                    enabled = state.songs.size > 1,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Next", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%d:%02d".format(minutes, remainingSeconds)
}

// Preview will be added in the next step as per the plan.
// Adding it now for completeness of this file's recreation.
@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240,orientation=landscape")
@Composable
fun VideoPlayerScreenPreview() {
    // Mock ViewModel or State for Preview
    // For simplicity, we'll use a ViewModel with mock data if Application context is an issue for preview
    // Or, pass a mock VideoPlayerState directly if the ViewModel is hard to instantiate in preview
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

    // A simplified ViewModel for preview that doesn't rely on Android Application context
    class PreviewVideoPlayerViewModel : VideoPlayerViewModel(Application()) { // This might still be tricky
        override val state: StateFlow<VideoPlayerState> = MutableStateFlow(previewState)
        override fun onAction(action: VideoPlayerAction) {} // No-op for preview
    }


    VintageRadioAppTheme {
        // Due to Application context in ViewModel, direct preview might be hard.
        // A common pattern is to have a "dumb" Composable that takes state directly.
        // For now, let's try with a simplified state or a fake ViewModel if possible.

        // This is a simplified approach for preview.
        // Ideally, VideoPlayerScreen would take VideoPlayerState and lambda for actions.
        Surface {
             VideoPlayerScreenContent(state = previewState, onAction = {})
        }
    }
}

// Extracted content for better previewability, takes state and actions directly
@Composable
fun VideoPlayerScreenContent(state: VideoPlayerState, onAction: (VideoPlayerAction) -> Unit) {
    val currentSong = state.currentSong
    // This preview won't have a working YouTube player, just the UI layout.
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Left Column
        Column(modifier = Modifier.weight(0.65f).fillMaxHeight().padding(end = 8.dp)) {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(16/9f).background(Color.Black), contentAlignment = Alignment.Center) {
                Text("YouTube Player Preview", color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
            currentSong?.let { song ->
                Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Text(song.band, style = MaterialTheme.typography.displayMedium)
                    Text(song.songTitle, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Album Year: ${song.year} (${song.decade}s)", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("ID: ${song.youtubeId}", style = MaterialTheme.typography.bodySmall)
                }
            } ?: Box(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                 Text("No song selected.", style = MaterialTheme.typography.bodyLarge)
            }
        }
        // Right Column
        Column(
            modifier = Modifier.weight(0.35f).fillMaxHeight().padding(start = 8.dp)
                           .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)).padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(onClick = { onAction(VideoPlayerAction.PlayPause) }, enabled = currentSong != null, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                Text(if (state.isPlaying) "Pause" else "Play", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Slider(
                value = state.currentPlaybackTimeSeconds.toFloat(),
                onValueChange = { /*onAction(VideoPlayerAction.SeekTo(it.toInt()))*/ }, // Preview doesn't need action
                onValueChangeFinished = {onAction(VideoPlayerAction.SeekTo(state.currentPlaybackTimeSeconds))},
                valueRange = 0f..(state.totalDurationSeconds.toFloat().takeIf { it > 0 } ?: 100f),
                modifier = Modifier.fillMaxWidth(),
                enabled = currentSong != null,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f)
                )
            )
            Text(
                text = formatTime(state.currentPlaybackTimeSeconds) + " / " + formatTime(state.totalDurationSeconds),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { onAction(VideoPlayerAction.PreviousSong) }, enabled = state.songs.size > 1, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                    Text("Prev", style = MaterialTheme.typography.labelLarge)
                }
                Button(onClick = { onAction(VideoPlayerAction.NextSong) }, enabled = state.songs.size > 1, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                    Text("Next", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
