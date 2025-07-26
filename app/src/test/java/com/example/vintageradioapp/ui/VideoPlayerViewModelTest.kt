package com.example.vintageradioapp.ui

import com.example.vintageradioapp.data.Song
import com.example.vintageradioapp.data.SongParser
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class VideoPlayerViewModelTest {

    private lateinit var viewModel: VideoPlayerViewModel
    private val testDispatcher = StandardTestDispatcher()
    private val songParser: SongParser = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = VideoPlayerViewModel(songParser, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() {
        assertEquals(VideoPlayerState(isLoading = true), viewModel.state.value)
    }

    @Test
    fun `loadSongs updates state with songs`() = runTest {
        val songs = listOf(Song(decade = "70", year = "1975", band = "Queen", songTitle = "Bohemian Rhapsody", youtubeId = "fJ9rUzIMcZQ"))
        coEvery { songParser.parseSongs() } returns songs

        viewModel.loadSongs()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, viewModel.state.value.isLoading)
        assertEquals(songs, viewModel.state.value.songs)
        assertEquals(songs[0], viewModel.state.value.currentSong)
    }

    @Test
    fun `nextSong updates currentSong`() = runTest {
        val songs = listOf(
            Song(decade = "70", year = "1975", band = "Queen", songTitle = "Bohemian Rhapsody", youtubeId = "fJ9rUzIMcZQ"),
            Song(decade = "70", year = "1971", band = "Led Zeppelin", songTitle = "Stairway to Heaven", youtubeId = "iXQUu5Dti4g")
        )
        coEvery { songParser.parseSongs() } returns songs
        viewModel.loadSongs()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(VideoPlayerAction.NextSong)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(songs[1], viewModel.state.value.currentSong)
    }

    @Test
    fun `previousSong updates currentSong`() = runTest {
        val songs = listOf(
            Song(decade = "70", year = "1975", band = "Queen", songTitle = "Bohemian Rhapsody", youtubeId = "fJ9rUzIMcZQ"),
            Song(decade = "70", year = "1971", band = "Led Zeppelin", songTitle = "Stairway to Heaven", youtubeId = "iXQUu5Dti4g")
        )
        coEvery { songParser.parseSongs() } returns songs
        viewModel.loadSongs()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(VideoPlayerAction.NextSong)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(VideoPlayerAction.PreviousSong)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(songs[0], viewModel.state.value.currentSong)
    }
}
