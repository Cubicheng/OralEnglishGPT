package com.example.oralenglishgpt.viewModel

import android.util.Log
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oralenglishgpt.api.ApiClient
import com.example.oralenglishgpt.api.ChatRequest
import com.example.oralenglishgpt.api.Conversation
import com.example.oralenglishgpt.api.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import androidx.compose.runtime.State

class ChatViewModel(private val apiKey: String) : ViewModel() {
    // 当前对话消息
    private val _messages = mutableStateListOf<Message>()
    val messages: List<Message> = _messages

    // 历史对话列表
    private val _conversations = mutableStateListOf<Conversation>()
    val conversations: List<Conversation> get() = _conversations

    // 当前对话ID
    private val _currentConversationId = mutableStateOf(generateId())
    var currentConversationId: String
        get() = _currentConversationId.value
        set(value) { _currentConversationId.value = value }

    private val api = ApiClient.instance

    // 添加对话框状态
    private val _showDeleteDialog = mutableStateOf(false)
    val showDeleteDialog: State<Boolean> = _showDeleteDialog

    private val _conversationToDelete = mutableStateOf<String?>(null)

    // 打开删除确认对话框
    fun showDeleteConfirmation(conversationId: String) {
        _conversationToDelete.value = conversationId
        _showDeleteDialog.value = true
    }

    // 取消删除
    fun cancelDelete() {
        _showDeleteDialog.value = false
        _conversationToDelete.value = null
    }

    // 确认删除
    fun confirmDelete() {
        _conversationToDelete.value?.let { conversationId ->
            deleteConversation(conversationId)
        }
        _showDeleteDialog.value = false
        _conversationToDelete.value = null
    }

    // 创建新对话
    fun newConversation() {
        if (messages.isNotEmpty()) {
            saveCurrentConversation()
        }

        // 重置当前对话
        currentConversationId = generateId()
        _messages.clear()
    }

    // 保存当前对话到历史记录
    private fun saveCurrentConversation() {
        if (_messages.isEmpty()) {
            Log.d("ChatVM", "当前对话无消息，跳过保存")
            return
        }

        val existingIndex = _conversations.indexOfFirst { it.id == currentConversationId }

        val title = generateConversationTitle(_messages)

        val currentConversation = Conversation(
            id = currentConversationId,
            title = title,
            messages = _messages.toList(),
            lastModified = System.currentTimeMillis()
        )

        if (existingIndex >= 0) {
            _conversations[existingIndex] = currentConversation
            Log.d("ChatVM", "更新历史对话: ${currentConversation.title}")
        } else {
            _conversations.add(currentConversation)
            Log.d("ChatVM", "新增历史对话: ${currentConversation.title}")
        }

        _conversations.sortBy { it.lastModified }
    }

    private fun generateConversationTitle(messages: List<Message>): String {
        val firstUserMessage = messages.firstOrNull { it.role == "user" }?.content
        return firstUserMessage?.take(20)?.plus("...") ?: "新对话"
    }

    // 加载历史对话
    fun loadConversation(conversationId: String) {
        if (_messages.isNotEmpty()) {
            saveCurrentConversation()
        }
        _conversations.firstOrNull { it.id == conversationId }?.let { conv ->
            _messages.clear()
            _messages.addAll(conv.messages)  // 使用 addAll 保持状态引用
            currentConversationId = conv.id
        }
    }

    fun deleteConversation(conversationId: String) {
        val index = _conversations.indexOfFirst { it.id == conversationId }
        if (index >= 0) {
            _conversations.removeAt(index)
            Log.d("ChatVM", "删除历史对话: ${conversationId}")
            if(conversationId==currentConversationId){
                _messages.clear()
                newConversation()
            }
        }
    }

    fun sendMessage(text: String) {
        // 添加用户消息
        if (_messages.size >= 6) {  // 保留最近3轮（user+assistant各一条）
            _messages.removeRange(0, 2)  // 删除最旧的一对QA
        }
        _messages.add(Message("user", text))

        saveCurrentConversation()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = api.chatCompletion(
                    auth = apiKey,
                    request = ChatRequest(messages = _messages.toList())
                )

                // 修复1：直接访问响应体（非Response包装）
                val aiMessage = response.choices.first().message

                // 修复2：线程安全更新UI
                withContext(Dispatchers.Main) {
                    _messages.add(aiMessage)
                    Log.d("ChatVM", "AI回复: ${aiMessage.content}")
                    saveCurrentConversation()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _messages.add(Message("assistant", "请求失败: ${e.message}"))
                    saveCurrentConversation()
                }
                Log.e("API", "网络错误: ${e.message}")
            }
        }
    }

    private fun generateId(): String = UUID.randomUUID().toString()
}