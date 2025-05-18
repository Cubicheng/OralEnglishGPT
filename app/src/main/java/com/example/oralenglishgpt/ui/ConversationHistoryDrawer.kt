package com.example.oralenglishgpt.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.oralenglishgpt.api.Conversation
import com.example.oralenglishgpt.viewModel.ChatViewModel
import androidx.compose.runtime.getValue

@Composable
fun ConversationHistoryDrawer(
    viewModel: ChatViewModel,
    conversations: List<Conversation>,
    onConversationSelected: (String) -> Unit,
    selectedConversationId: String?,
    onNewConversation: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 添加对话框状态监听
    val showDeleteDialog by viewModel.showDeleteDialog

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            title = { Text("Confirm") },
            text = { Text("Sure to delete?") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmDelete() }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.cancelDelete() }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 左侧：对话信息
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
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

                        // 右侧：删除按钮

                        IconButton(
                            onClick = { viewModel.showDeleteConfirmation(conversation.id) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}