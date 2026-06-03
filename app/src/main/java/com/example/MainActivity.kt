package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val viewModel: QuestionViewModel by viewModels()
  private lateinit var voiceSynthesizer: VoiceSynthesizer

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    voiceSynthesizer = VoiceSynthesizer(this)

    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          MainScreen(
            viewModel = viewModel,
            voiceSynthesizer = voiceSynthesizer,
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }

  override fun onDestroy() {
      super.onDestroy()
      voiceSynthesizer.shutdown()
  }
}
