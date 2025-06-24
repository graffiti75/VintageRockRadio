package com.example.vintageradio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
// import androidx.lifecycle.viewmodel.compose.viewModel // Alternative for ViewModel instantiation
import com.example.vintageradio.ui.VideoPlayerScreen
import com.example.vintageradio.ui.VideoPlayerViewModel
import com.example.vintageradio.ui.theme.VintageRadioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VintageRadioTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Instantiate ViewModel here or use Hilt/viewModel()
                    val viewModel = VideoPlayerViewModel(application = application)
                    VideoPlayerScreen(viewModel = viewModel)
                }
            }
        }
    }
}
