package com.example.vintageradioapp // Updated package name

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider // Required for non-Hilt ViewModel
import com.example.vintageradioapp.ui.VideoPlayerScreen
import com.example.vintageradioapp.ui.VideoPlayerViewModel
import com.example.vintageradioapp.ui.theme.VintageRadioAppTheme // Updated theme name

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Manually create ViewModelFactory if not using Hilt
        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(VideoPlayerViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return VideoPlayerViewModel(application) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
        val viewModel = ViewModelProvider(this, viewModelFactory).get(VideoPlayerViewModel::class.java)

        setContent {
            VintageRadioAppTheme { // Use the correct theme name
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VideoPlayerScreen(viewModel = viewModel)
                }
            }
        }
    }
}
