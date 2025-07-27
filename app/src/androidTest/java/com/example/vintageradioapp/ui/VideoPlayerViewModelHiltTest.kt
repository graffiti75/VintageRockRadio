package com.example.vintageradioapp.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vintageradioapp.data.Song
import com.example.vintageradioapp.data.SongParser
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class VideoPlayerViewModelHiltTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var songParser: SongParser

    private lateinit var viewModel: VideoPlayerViewModel
    private val scheduler = TestCoroutineScheduler()
    private val dispatcher = StandardTestDispatcher(scheduler)

    @Before
    fun setUp() {
        hiltRule.inject()
        viewModel = VideoPlayerViewModel(songParser, dispatcher)
    }

    @Test
    fun loadSongs_updatesStateWithSongs() = runTest(scheduler) {
        val songs = listOf(Song(decade = "70", year = "1975", band = "Queen", songTitle = "Bohemian Rhapsody", youtubeId = "fJ9rUzIMcZQ"))
        coEvery { songParser.parseSongs("70") } returns songs

        viewModel.loadSongs("70")

        Assert.assertEquals(false, viewModel.state.value.isLoading)
        Assert.assertEquals(songs, viewModel.state.value.songs)
        Assert.assertEquals(songs[0], viewModel.state.value.currentSong)
    }
}
