package com.example.oralenglishgpt.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.oralenglishgpt.api.Conversation

@Composable
fun ConversationHistoryDrawer(
    conversations: List<Conversation>,
    onConversationSelected: (String) -> Unit,
    selectedConversationId: String?,
    onNewConversation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .width(280.dp)
            .padding(16.dp)
    ) {
        // 新建对话按钮
        Button(
            onClick = onNewConversation,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("New Conversation")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 历史对话列表
        Text("History", style = MaterialTheme.typography.titleMedium)

        LazyColumn {
            items(conversations) { conversation ->
                val isSelected = conversation.id == selectedConversationId
                Card(
                    onClick = { onConversationSelected(conversation.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = MaterialTheme.shapes.medium
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = conversation.title,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "${conversation.messages.size} 条消息",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}