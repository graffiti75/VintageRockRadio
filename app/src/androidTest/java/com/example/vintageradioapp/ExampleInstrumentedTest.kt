package com.example.vintageradioapp

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.junit.Rule

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.vintageradioapp", appContext.packageName)
    }

    // Example Composable UI test
    @Test
    fun appName_isDisplayed() {
        // This test assumes the app name "Vintage Radio App" might be displayed somewhere,
        // or that a Composable with this text exists.
        // For the current app, the main screen loads songs. A better test would be
        // to check for elements of VideoPlayerScreen after data loads or in a specific state.

        // For example, if there's a loading indicator initially:
        // composeTestRule.onNodeWithTag("LoadingIndicator", useUnmergedTree = true).assertExists()
        // Or, after songs load, check for a song title (this requires waiting/idling resources)
        // composeTestRule.waitUntil(timeoutMillis = 5000) {
        //     composeTestRule
        //         .onAllNodesWithText("Queen", substring = true) // Example band name
        //         .fetchSemanticsNodes().isNotEmpty()
        // }
        // composeTestRule.onNodeWithText("Queen", substring = true).assertIsDisplayed()

        // A simple test that just checks if the Activity launches without crashing
        // by verifying a known element (e.g., a button if one was consistently present)
        // For now, this is a placeholder as specific UI element testing depends on the final UI state.
    }
}
