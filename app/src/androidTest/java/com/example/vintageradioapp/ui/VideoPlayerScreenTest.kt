package com.example.vintageradioapp.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vintageradioapp.data.Song
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VideoPlayerScreenTest {

	@get:Rule
	val composeTestRule = createComposeRule()

	@Test
	fun decadeButtons_areDisplayed() {
		val decades = listOf("50", "60", "70", "80", "90", "2000")
		composeTestRule.setContent {
			DecadeButtons(onAction = {})
		}

		decades.forEach { decade ->
			composeTestRule.onNodeWithText(decade).assertIsDisplayed()
		}
	}

	@Test
	fun songInformation_isDisplayed() {
		val song = Song(
			decade = "70",
			year = "1975",
			band = "Queen",
			songTitle = "Bohemian Rhapsody",
			youtubeId = "fJ9rUzIMcZQ"
		)
		composeTestRule.setContent {
			VideoPlayerScreenContent(
				state = VideoPlayerState(songs = listOf(song), currentSongIndex = 0),
				onAction = {}
			)
		}

		composeTestRule.onNodeWithText("Queen").assertIsDisplayed()
		composeTestRule.onNodeWithText("Bohemian Rhapsody").assertIsDisplayed()
		composeTestRule.onNodeWithText("Year: 1975 (70s)").assertIsDisplayed()
	}
}
