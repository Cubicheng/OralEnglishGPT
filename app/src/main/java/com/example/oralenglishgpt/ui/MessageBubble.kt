package com.example.oralenglishgpt.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.oralenglishgpt.R
import com.example.oralenglishgpt.viewModel.tts.TTSViewModel

@Composable
fun MessageBubble(
    text: String,
    isUser: Boolean,
    ttsViewModel: TTSViewModel,
    isPlaying: Boolean,
    onPlayToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f), // 限制卡片宽度（留白更美观）
            colors = CardDefaults.cardColors(
                containerColor = if (isUser)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(12.dp),
                color = if (isUser)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface
            )
        }

        if (!isUser) {
            IconButton(
                onClick = onPlayToggle
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isPlaying)
                            R.drawable.pause_circle
                        else
                            R.drawable.sound_filling
                    ),
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = if (ttsViewModel.isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}