package com.example.oralenglishgpt.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oralenglishgpt.database.AppDatabase
import com.example.oralenglishgpt.viewModel.ChatViewModel
import com.example.oralenglishgpt.viewModel.ChatViewModelFactory
import com.example.oralenglishgpt.viewModel.tts.TTSViewModel
import com.example.oralenglishgpt.viewModel.tts.TTSViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {
    val context = LocalContext.current
    val viewModel: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(context, AppDatabase.getDatabase(LocalContext.current))
    )
    val ttsViewModel: TTSViewModel = viewModel(
        factory = TTSViewModelFactory(context)
    )
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ConversationHistoryDrawer(
                conversations = viewModel.conversations,
                onConversationSelected = { id ->
                    scope.launch {
                        viewModel.loadConversation(id)
                        drawerState.close()
                    }
                },
                onNewConversation = {
                    scope.launch {
                        viewModel.newConversation()
                        drawerState.close()
                    }
                },
                selectedConversationId = viewModel.currentConversationId,
                viewModel = viewModel
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("OralEnglishGPT") },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } }
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "菜单")
                        }
                    }
                )
            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // 消息列表
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        reverseLayout = true
                    ) {
                        items(viewModel.messages.reversed()) { message ->
                            MessageBubble(
                                text = message.content,
                                isUser = message.role == "user",
                                ttsViewModel = ttsViewModel
                            )
                        }
                    }

                    // 输入框
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Input text...") }
                        )

                        Spacer(Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    scope.launch { // 在协程中调用挂起函数
                                        viewModel.sendMessage(inputText)
                                        inputText = ""
                                    }
                                }
                            }
                        ) {
                            Text("Send")
                        }
                    }
                }
            }
        )
    }
}
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