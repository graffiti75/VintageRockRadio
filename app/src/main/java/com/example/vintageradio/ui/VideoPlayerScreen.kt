package com.example.vintageradio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vintageradio.ui.theme.VintageBrown
import com.example.vintageradio.ui.theme.VintageCream
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun VideoPlayerScreen(viewModel: VideoPlayerViewModel) {
    val state by viewModel.state.collectAsState()

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (state.error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error: ${state.error}", color = Color.Red)
            Button(onClick = { viewModel.onAction(VideoPlayerAction.DismissError) }) {
                Text("Dismiss")
            }
        }
        return
    }

    val currentSong = state.currentSong

    // Main layout - Landscape
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(VintageBrown) // Vintage radio body color
            .padding(16.dp)
    ) {
        // Left Column: Video Player and Song Info
        Column(
            modifier = Modifier
                .weight(0.7f) // Takes 70% of the width
                .fillMaxHeight()
                .padding(end = 8.dp)
        ) {
            // Video Player - Top-Left
            val context = LocalContext.current
            val youTubePlayerView = remember { YouTubePlayerView(context) }
            var player: YouTubePlayer? = null // To control the player

            // Effects to control player state based on ViewModel state
            LaunchedEffect(state.currentSong?.youtubeId, state.isPlaying) {
                player?.let { ytPlayer ->
                    state.currentSong?.let { song ->
                        // Load video when song changes or playback needs to start
                        // The listener will handle autoPlay if isPlaying is true initially
                        ytPlayer.loadVideo(song.youtubeId, state.currentPlaybackTimeSeconds.toFloat())
                    }
                }
            }

            LaunchedEffect(state.isPlaying) {
                player?.let { ytPlayer ->
                     if (state.isPlaying) {
                        ytPlayer.play()
                    } else {
                        ytPlayer.pause()
                    }
                }
            }

            AndroidView(
                factory = {
                    youTubePlayerView.apply {
                        enableAutomaticInitialization = false
                        initialize(object : AbstractYouTubePlayerListener() {
                            override fun onReady(youTubePlayer: YouTubePlayer) {
                                player = youTubePlayer
                                state.currentSong?.let { song ->
                                    youTubePlayer.loadVideo(song.youtubeId, state.currentPlaybackTimeSeconds.toFloat())
                                    viewModel.onAction(VideoPlayerAction.UpdateTotalDuration(0)) // Will be updated by player
                                }
                            }

                            override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                                viewModel.onAction(VideoPlayerAction.UpdateTotalDuration(duration.toInt()))
                            }

                            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                                viewModel.onAction(VideoPlayerAction.UpdatePlaybackTime(second.toInt()))
                            }

                            override fun onVideoEnded(youTubePlayer: YouTubePlayer) {
                                viewModel.onAction(VideoPlayerAction.NextSong) // Auto-play next song
                            }

                            override fun onError(youTubePlayer: YouTubePlayer, error: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerError) {
                                viewModel.onAction(VideoPlayerAction.OnError("Youtube Player Error: ${error.name}"))
                            }
                             override fun onStateChange(youTubePlayer: YouTubePlayer, playerState: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState) {
                                when (playerState) {
                                    com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.PLAYING -> {
                                        if (!state.isPlaying) viewModel.onAction(VideoPlayerAction.PlayPause) // Sync state
                                    }
                                    com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.PAUSED -> {
                                        if (state.isPlaying) viewModel.onAction(VideoPlayerAction.PlayPause) // Sync state
                                    }
                                    // Handle other states if necessary
                                    else -> {}
                                }
                            }
                        }, IFramePlayerOptions.Builder().controls(0).build()) // Disable default controls
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp) // Adjusted height
                    .background(Color.Black)
            )

            // Lifecycle management for the YouTubePlayerView
            DisposableEffect(Unit) {
                onDispose {
                    youTubePlayerView.release()
                    player = null
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Song Information
            currentSong?.let { song ->
                Column(modifier = Modifier.fillMaxWidth().background(VintageCream.copy(alpha = 0.1f)).padding(8.dp)) {
                    Text("Decade: ${song.decade}", fontSize = 16.sp, color = VintageCream)
                    Text("Year: ${song.year}", fontSize = 16.sp, color = VintageCream)
                    Text("Band: ${song.band}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = VintageCream)
                    Text("Song: ${song.songTitle}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = VintageCream)
                    Text("YouTube ID: ${song.youtubeId}", fontSize = 12.sp, color = VintageCream.copy(alpha = 0.7f))
                }
            }
        }

        // Right Column: Controls
        Column(
            modifier = Modifier
                .weight(0.3f) // Takes 30% of the width
                .fillMaxHeight()
                .padding(start = 8.dp)
                .background(VintageBrown.copy(alpha=0.5f)), // Slightly different shade for control panel
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly // Distribute controls
        ) {
            Text("Controls", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = VintageCream)

            // Play/Pause Button
            Button(onClick = { viewModel.onAction(VideoPlayerAction.PlayPause) }) {
                Text(if (state.isPlaying) "Pause" else "Play")
            }

            // Slider
            var sliderPosition by remember(state.currentPlaybackTimeSeconds) { mutableStateOf(state.currentPlaybackTimeSeconds.toFloat()) }

            Slider(
                value = sliderPosition,
                onValueChange = { newValue -> sliderPosition = newValue },
                onValueChangeFinished = {
                    player?.seekTo(sliderPosition)
                    viewModel.onAction(VideoPlayerAction.SeekTo(sliderPosition.toInt()))
                },
                valueRange = 0f..(state.totalDurationSeconds.toFloat().takeIf { it > 0 } ?: 100f),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
            Text(
                text = "${state.currentPlaybackTimeSeconds / 60}:${(state.currentPlaybackTimeSeconds % 60).toString().padStart(2, '0')} / ${state.totalDurationSeconds / 60}:${(state.totalDurationSeconds % 60).toString().padStart(2, '0')}",
                color = VintageCream
            )


            // Previous/Next Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(onClick = { viewModel.onAction(VideoPlayerAction.PreviousSong) }, enabled = state.songs.isNotEmpty()) {
                    Text("Prev")
                }
                Button(onClick = { viewModel.onAction(VideoPlayerAction.NextSong) }, enabled = state.songs.isNotEmpty()) {
                    Text("Next")
                }
            }
        }
    }
}
