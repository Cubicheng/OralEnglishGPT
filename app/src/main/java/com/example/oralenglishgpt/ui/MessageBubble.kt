package com.example.oralenglishgpt.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.oralenglishgpt.viewModel.tts.TTSViewModel

@Composable
fun MessageBubble(
    text: String,
    isUser: Boolean,
    ttsViewModel: TTSViewModel // 新增参数
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

        // 播放按钮（仅AI消息显示，居左对齐）
        if (!isUser) {
            IconButton(
                onClick = { ttsViewModel.speak(text) },
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(36.dp) // 稍小的按钮
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "朗读",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp) // 更精致的图标大小
                )
            }
        }
    }
}