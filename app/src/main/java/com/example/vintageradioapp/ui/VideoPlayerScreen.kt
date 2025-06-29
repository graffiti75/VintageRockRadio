package com.example.vintageradioapp.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
	val state by viewModel.state.collectAsStateWithLifecycle()
	VideoPlayerScreenContent(
		state = state,
		onAction = viewModel::onAction
	)
}

@Composable
fun VideoPlayerScreenContent(
	state: VideoPlayerState,
	onAction: (VideoPlayerAction) -> Unit
) {
	val context = LocalContext.current
	val youTubePlayerView = remember { YouTubePlayerView(context) }
	val youtubePlayerState = remember { mutableStateOf<YouTubePlayer?>(null) }
	val youtubePlayer = youtubePlayerState.value
	val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

	YoutubeListenerDisposableEffect(
		state = state,
		onAction = onAction,
		lifecycleOwner = lifecycleOwner,
		youTubePlayerView = youTubePlayerView,
		youtubePlayerState = youtubePlayerState
	)

	// Effect to load/cue videos when the current song changes or playback time is reset
	LaunchedEffect(
		key1 = youtubePlayer,
		key2 = state.currentSong,
		key3 = state.currentPlaybackTimeSeconds // React to playback time changes for restart
	) {
		youtubePlayer?.let { player ->
			state.currentSong?.let { song ->
				// This effect is only responsible for ensuring the correct video is loaded/cued
				// at the correct time. It does not call play() or pause().
				// The other LaunchedEffect (keyed on state.isPlaying) will handle playback.
				player.loadVideo(song.youtubeId, state.currentPlaybackTimeSeconds.toFloat())
			}
		}
	}

	// Effect to handle play/pause state based on ViewModel's isPlaying state
	LaunchedEffect(key1 = youtubePlayer, key2 = state.isPlaying) {
		youtubePlayer?.let { player ->
			if (state.currentSong != null) { // Only attempt to play/pause if a song is available
				if (state.isPlaying) {
					player.play()
				} else {
					player.pause()
				}
			}
		}
	}

	VideoPlayerContentUI(
		state = state,
		onAction = onAction,
		youTubePlayerView = youTubePlayerView,
		youtubePlayer = youtubePlayer
	)
}

@Composable
fun YoutubeListenerDisposableEffect(
	state: VideoPlayerState,
	onAction: (VideoPlayerAction) -> Unit,
	lifecycleOwner: LifecycleOwner,
	youTubePlayerView: YouTubePlayerView,
	youtubePlayerState: MutableState<YouTubePlayer?>
) {
	// Use rememberUpdatedState to ensure the listener always has the latest isPlaying state
	// without causing the DisposableEffect to re-run unnecessarily.
	val currentIsPlayingState = rememberUpdatedState(state.isPlaying)

	DisposableEffect(lifecycleOwner) {
		val youtubePlayerListener = object : AbstractYouTubePlayerListener() {
			override fun onReady(youTubePlayer: YouTubePlayer) {
				youtubePlayerState.value = youTubePlayer
			}

			override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
				onAction(VideoPlayerAction.UpdateTotalDuration(duration.toInt()))
			}

			override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
				onAction(VideoPlayerAction.UpdatePlaybackTime(second.toInt()))
			}

			override fun onError(
				youTubePlayer: YouTubePlayer,
				error: PlayerConstants.PlayerError
			) {
				onAction(VideoPlayerAction.OnError("Youtube Player Error: ${error.name}"))
			}

			override fun onStateChange(
				youTubePlayer: YouTubePlayer,
				playerState: PlayerConstants.PlayerState
			) {
				val isPlayingInViewModel = currentIsPlayingState.value
				when (playerState) {
					PlayerConstants.PlayerState.PLAYING -> {
						if (!isPlayingInViewModel) {
							onAction(VideoPlayerAction.SetPlaying(true))
						}
					}
					PlayerConstants.PlayerState.PAUSED -> {
						val isLikelyNewSong = state.currentPlaybackTimeSeconds < 2 // Heuristic for new song
						if (isPlayingInViewModel) {
							if (isLikelyNewSong) {
								// If a new song was likely just loaded and meant to play,
								// but player emits PAUSED, this might be a transient state
								// before our explicit play() command takes full effect.
								// Aggressively setting ViewModel's isPlaying to false here
								// would fight the intention to play.
								// The LaunchedEffect for state.isPlaying (being true) should
								// ensure player.play() is called. We let that effect win.
								// No action here, to avoid counteracting the play command.
							} else {
								// Player paused, and it's not immediately after a new song load
								// intended for playback. Sync ViewModel to paused.
								onAction(VideoPlayerAction.SetPlaying(false))
							}
						}
					}
					PlayerConstants.PlayerState.ENDED -> {
						// When video ends, go to the next song
						onAction(VideoPlayerAction.NextSong)
					}
					// VIDEO_CUED is a common state after loadVideo with autoplay(0).
					// UNSTARTED is the initial state.
					// BUFFERING, UNKNOWN, VIDEO_DATA_LOADED are other states.
					// We generally don't need to fight these states unless they
					// contradict our ViewModel's isPlaying expectation for PLAYING/PAUSED.
					else -> {}
				}
			}
		}

		youTubePlayerView.enableAutomaticInitialization = false
		// Ensure autoplay(0) is critical here. We control play/pause via LaunchedEffects.
		val options = IFramePlayerOptions.Builder().controls(0).autoplay(0).build()
		youTubePlayerView.initialize(youtubePlayerListener, options)

		val lifecycleObserver = LifecycleEventObserver { _, event ->
			when (event) {
				Lifecycle.Event.ON_RESUME -> {
					onAction(VideoPlayerAction.AppCameToForeground)
				}
				Lifecycle.Event.ON_PAUSE -> {
					youtubePlayerState.value?.pause() // Still directly pause the player
					onAction(VideoPlayerAction.AppWentToBackground)
				}
				else -> {}
			}
		}
		lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
		onDispose {
			youtubePlayerState.value = null
			youTubePlayerView.release()
			lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
		}
	}
}

@Composable
private fun VideoPlayerContentUI(
	state: VideoPlayerState,
	onAction: (VideoPlayerAction) -> Unit,
	youTubePlayerView: YouTubePlayerView,
	youtubePlayer: YouTubePlayer?
) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
	) {
		if (state.isLoading && state.songs.isEmpty()) {
			CircularProgressIndicator(
				modifier = Modifier.align(Alignment.Center),
				color = MaterialTheme.colorScheme.primary
			)
		} else if (state.error != null) {
			onAction(VideoPlayerAction.DismissError)
		} else if (state.songs.isEmpty() && !state.isLoading) {
			RetryState()
		} else {
			val currentSong = state.currentSong
			Row(
				modifier = Modifier
					.fillMaxSize()
					.padding(16.dp)
			) {
				YoutubePlayerContent(
					youTubePlayerView = youTubePlayerView,
					currentSong = currentSong
				)
				MusicControls(
					modifier = Modifier
						.weight(0.35f)
						.fillMaxHeight()
						.padding(start = 8.dp),
					state = state,
					onAction = onAction,
					currentSong = currentSong,
					youtubePlayer = youtubePlayer
				)
			}
		}
	}
}

@Composable
private fun RetryState() {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text(
			text = "No songs loaded.",
			color = MaterialTheme.colorScheme.onBackground,
			style = MaterialTheme.typography.bodyLarge
		)
		Spacer(modifier = Modifier.height(8.dp))
		Button(
			onClick = {}
		) {
			Text(
				text = "Retry Load",
				style = MaterialTheme.typography.labelLarge
			)
		}
	}
}

/**
 * Left Column contains the Youtube Video Player and the Song Info.
 */
@Composable
private fun RowScope.YoutubePlayerContent(
	youTubePlayerView: YouTubePlayerView,
	currentSong: Song? = null
) {
	Column(
		modifier = Modifier
			.weight(0.65f) // This Column takes 65% of the width
			.fillMaxHeight() // It will fill the available height
			.padding(end = 8.dp)
	) {
		// Video Player takes a weighted portion of the vertical space
		AndroidView(
			factory = { youTubePlayerView },
			modifier = Modifier
				.fillMaxWidth()
				.aspectRatio(16 / 9f) // Maintain aspect ratio
				.weight(0.7f) // Takes 70% of the vertical space in this Column
				.background(Color.Black)
		)
		Spacer(modifier = Modifier.height(8.dp))

		// Song details take the remaining weighted portion
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.weight(0.3f) // Takes 30% of the vertical space in this Column
				.padding(horizontal = 8.dp)
		) {
			currentSong?.let { song ->
				Text(song.band, style = MaterialTheme.typography.displayMedium)
				Text(song.songTitle, style = MaterialTheme.typography.titleLarge)
				Spacer(modifier = Modifier.height(4.dp))
				Text(
					"Year: ${song.year} (${song.decade}s)",
					style = MaterialTheme.typography.bodyMedium
				)
				Spacer(modifier = Modifier.height(4.dp))
				Text(
					"ID: ${song.youtubeId}",
					style = MaterialTheme.typography.bodySmall
				)
			} ?: Box( // Placeholder if no song, fills the details section
				modifier = Modifier
					.fillMaxSize() // Fill the 30% allocated space
					.padding(8.dp)
			) {
				Text(
					"No song selected.",
					style = MaterialTheme.typography.bodyLarge,
					modifier = Modifier.align(Alignment.Center)
				)
			}
		}
	}
}

@Composable
private fun RowScope.MusicControls(
	modifier: Modifier = Modifier,
	state: VideoPlayerState,
	onAction: (VideoPlayerAction) -> Unit,
	currentSong: Song?,
	youtubePlayer: YouTubePlayer?
) {
	Column(
		modifier = modifier
			.background(
				MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
			)
			.padding(16.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		var sliderPosition by remember(state.currentPlaybackTimeSeconds) {
			mutableFloatStateOf(
				state.currentPlaybackTimeSeconds.toFloat()
			)
		}

		Button(
			onClick = {
				onAction(VideoPlayerAction.PlayPause)
		  	},
			enabled = currentSong != null,
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.primary
			)
		) {
			Text(
				text = if (state.isPlaying) "Pause" else "Play",
				style = MaterialTheme.typography.labelLarge
			)
		}
		Spacer(modifier = Modifier.height(24.dp))
		Slider(
			value = sliderPosition,
			onValueChange = { newValue -> sliderPosition = newValue },
			onValueChangeFinished = {
				youtubePlayer?.seekTo(sliderPosition)
				onAction(VideoPlayerAction.SeekTo(sliderPosition.toInt()))
			},
			valueRange = 0f..(
				state.totalDurationSeconds.toFloat().takeIf { it > 0f } ?: 100f
			),
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
		NextPreviousButtons(
			state = state,
			onAction = onAction
		)
	}
}

@Composable
private fun NextPreviousButtons(
	state: VideoPlayerState,
	onAction: (VideoPlayerAction) -> Unit
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceEvenly
	) {
		Button(
			onClick = {
				onAction(VideoPlayerAction.PreviousSong)
		  	},
			enabled = state.songs.size > 1,
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.secondary
			)
		) {
			Text(
				text = "Prev",
				style = MaterialTheme.typography.labelLarge.copy(
					color = MaterialTheme.colorScheme.onSecondary
				)
			)
		}
		Button(
			onClick = {
				onAction(VideoPlayerAction.NextSong)
		  	},
			enabled = state.songs.size > 1,
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.secondary
			)
		) {
			Text(
				text = "Next",
				style = MaterialTheme.typography.labelLarge.copy(
					color = MaterialTheme.colorScheme.onSecondary
				)
			)
		}
	}
}

fun formatTime(totalSeconds: Int): String {
	val minutes = totalSeconds / 60
	val seconds = totalSeconds % 60
	return "%d:%02d".format(minutes, seconds)
}

@Preview(
	showBackground = true,
	device = "spec:width=1280dp,height=800dp,dpi=240,orientation=landscape"
)
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

@Preview(
	showBackground = true,
	device = "spec:width=1280dp,height=800dp,dpi=240,orientation=landscape",
	name = "Loading State"
)
@Composable
fun VideoPlayerScreenLoadingPreview() {
	val previewState = VideoPlayerState(isLoading = true)
	VintageRadioAppTheme {
		Surface {
			VideoPlayerScreenContent(state = previewState, onAction = {})
		}
	}
}

@Preview(
	showBackground = true,
	device = "spec:width=1280dp,height=800dp,dpi=240,orientation=landscape",
	name = "Error State"
)
@Composable
fun VideoPlayerScreenErrorPreview() {
	val previewState = VideoPlayerState(
		isLoading = false,
		error = "Failed to load songs. Please check connection."
	)
	VintageRadioAppTheme {
		Surface {
			VideoPlayerScreenContent(state = previewState, onAction = {})
		}
	}
}

@Preview(
	showBackground = true,
	device = "spec:width=1280dp,height=800dp,dpi=240,orientation=landscape",
	name = "No Songs State"
)
@Composable
fun VideoPlayerScreenNoSongsPreview() {
	val previewState = VideoPlayerState(isLoading = false, songs = emptyList())
	VintageRadioAppTheme {
		Surface {
			VideoPlayerScreenContent(state = previewState, onAction = {})
		}
	}
}

@Preview(
	showBackground = true,
	device = "spec:width=1280dp,height=800dp,dpi=240,orientation=landscape",
)
@Composable
fun YoutubePlayerContentPreview() {
	val context = LocalContext.current
	val youTubePlayerView = remember { YouTubePlayerView(context) }
	VintageRadioAppTheme {
		Surface {
			Row(
				modifier = Modifier
					.fillMaxSize()
					.padding(16.dp)
			) {
				YoutubePlayerContent(
					youTubePlayerView = youTubePlayerView,
					currentSong = Song()
				)
			}
		}
	}
}

@Preview(
	showBackground = true,
	device = "spec:width=1280dp,height=800dp,dpi=240,orientation=landscape",
)
@Composable
fun MusicControlsPreview() {
	VintageRadioAppTheme {
		Surface {
			Row(
				modifier = Modifier
					.fillMaxSize()
					.padding(16.dp)
			) {
				MusicControls(
					modifier = Modifier
						.weight(0.35f)
						.fillMaxHeight()
						.padding(start = 8.dp),
					state = VideoPlayerState(),
					onAction = {},
					currentSong = Song(),
					youtubePlayer = null
				)
			}
		}
	}
}
