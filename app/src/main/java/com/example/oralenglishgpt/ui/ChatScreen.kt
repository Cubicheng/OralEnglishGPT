package com.example.oralenglishgpt.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oralenglishgpt.database.AppDatabase
import com.example.oralenglishgpt.utils.NetworkUtils
import com.example.oralenglishgpt.viewModel.ChatViewModel
import com.example.oralenglishgpt.viewModel.ChatViewModelFactory
import com.example.oralenglishgpt.viewModel.stt.STTViewModel
import com.example.oralenglishgpt.viewModel.tts.TTSViewModel
import com.example.oralenglishgpt.viewModel.tts.TTSViewModelFactory
import kotlinx.coroutines.launch

@SuppressLint("UnrememberedMutableState")
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
    val snackbarHostState = remember { SnackbarHostState() }

    sttViewModel.onSendMessage = { recognizedText ->
        scope.launch {
            if (recognizedText.isNotBlank()) {
                chatViewModel.sendMessage(recognizedText)
            }
        }
    }

    LaunchedEffect(ttsViewModel) {
        chatViewModel.ttsViewModel = ttsViewModel
    }

    LaunchedEffect(Unit) {
        NetworkUtils.showNetworkErrorSnackbar(context, snackbarHostState)
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
                ChatTopBar(
                    scope = scope,
                    drawerState = drawerState,
                    autoPlay = chatViewModel.autoPlay.value,
                    onAutoPlayChange = { isChecked ->
                        chatViewModel.toggleAutoPlay()
                    }
                )
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        reverseLayout = true
                    ) {
                        if (chatViewModel.isGeneratingResponse.value) {
                            item {
                                GeneratingResponseIndicator()
                            }
                        }

                        itemsIndexed(chatViewModel.messages.reversed()) { index, message ->
                            val isPlaying by derivedStateOf {
                                ttsViewModel.currentPlayingIndex == index && ttsViewModel.isPlaying
                            }

                            MessageBubble(
                                text = message.content,
                                isUser = message.role == "user",
                                ttsViewModel = ttsViewModel,
                                isPlaying = isPlaying,
                                onPlayToggle = {
                                    if (isPlaying) {
                                        ttsViewModel.stop()
                                    } else {
                                        ttsViewModel.playText(message.content, index)
                                    }
                                },
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
                        },
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        )
    }
}
