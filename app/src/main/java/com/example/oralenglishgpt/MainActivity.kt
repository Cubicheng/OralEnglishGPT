package com.example.oralenglishgpt

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.example.oralenglishgpt.ui.ChatScreen

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AIChatTheme {
                Surface {
                    ChatScreen()
                }
            }
        }
    }
}

@Composable
fun AIChatTheme(content: @Composable () -> Unit) {
    androidx.compose.material3.MaterialTheme {
        content()
    }
}