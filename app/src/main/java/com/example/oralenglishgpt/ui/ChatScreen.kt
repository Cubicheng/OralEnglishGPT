package com.example.oralenglishgpt.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oralenglishgpt.database.AppDatabase
import com.example.oralenglishgpt.viewModel.ChatViewModel
import com.example.oralenglishgpt.viewModel.ChatViewModelFactory
import com.example.oralenglishgpt.viewModel.stt.STTViewModel
import com.example.oralenglishgpt.viewModel.tts.TTSViewModel
import com.example.oralenglishgpt.viewModel.tts.TTSViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {
    val context = LocalContext.current
    val sttViewModel: STTViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(context, AppDatabase.getDatabase(LocalContext.current))
    )
    val ttsViewModel: TTSViewModel = viewModel(
        factory = TTSViewModelFactory(context)
    )
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // 设置语音识别后的发送逻辑
    sttViewModel.onSendMessage = { recognizedText ->
        scope.launch {
            if (recognizedText.isNotBlank()) {
                chatViewModel.sendMessage(recognizedText)
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ConversationHistoryDrawer(
                conversations = chatViewModel.conversations,
                onConversationSelected = { id ->
                    scope.launch {
                        chatViewModel.loadConversation(id)
                        drawerState.close()
                    }
                },
                onNewConversation = {
                    scope.launch {
                        chatViewModel.newConversation()
                        drawerState.close()
                    }
                },
                selectedConversationId = chatViewModel.currentConversationId,
                viewModel = chatViewModel
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("OralEnglishGPT") },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch {
                                drawerState.open()
                            } }
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
                        items(chatViewModel.messages.reversed()) { message ->
                            MessageBubble(
                                text = message.content,
                                isUser = message.role == "user",
                                ttsViewModel = ttsViewModel
                            )
                        }
                    }

                    InputArea(
                        sttViewModel = sttViewModel,
                        ttsViewModel = ttsViewModel,
                        onSendMessage = { text ->
                            scope.launch {
                                chatViewModel.sendMessage(text)
                            }
                        }
                    )
                }
            }
        )
    }
}
