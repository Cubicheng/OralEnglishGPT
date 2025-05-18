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
import com.example.oralenglishgpt.database.AppDatabase
import com.example.oralenglishgpt.database.entity.ConversationEntity
import com.example.oralenglishgpt.database.entity.MessageEntity
import kotlinx.coroutines.flow.first

class ChatViewModel(
    private val apiKey: String,
    private val database: AppDatabase
) : ViewModel() {
    private val chatDao = database.chatDao()
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

    init{
        loadAllConversations()
    }

    private fun loadAllConversations() {
        viewModelScope.launch {
            try {
                val conversationEntities = chatDao.getAllConversations().first()

                _conversations.clear()
                conversationEntities.forEach { entity ->
                    val messages = chatDao.getMessagesByConversation(entity.id)
                        .first()
                        .map { Message(it.role, it.content) }

                    _conversations.add(
                        Conversation(
                            id = entity.id,
                            title = entity.title,
                            messages = messages,
                            lastModified = entity.lastModified
                        )
                    )
                }
                // 按时间降序排序
                _conversations.sortByDescending { it.lastModified }
                Log.d("ChatVM", "初始加载完成，对话数: ${_conversations.size}")
            } catch (e: Exception) {
                Log.e("ChatVM", "加载对话失败", e)
            }
        }
    }

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
    suspend fun confirmDelete() {
        _conversationToDelete.value?.let { conversationId ->
            deleteConversation(conversationId)
        }
        _showDeleteDialog.value = false
        _conversationToDelete.value = null
    }

    // 创建新对话
    suspend fun newConversation() {
        if (messages.isNotEmpty()) {
            saveCurrentConversation()
        }

        // 重置当前对话
        currentConversationId = generateId()
        _messages.clear()
    }

    // 保存当前对话到历史记录
    private suspend fun saveCurrentConversation() {
        if (_messages.isEmpty()) {
            Log.d("ChatVM", "当前对话无消息，跳过保存")
            return
        }

        val existingIndex = _conversations.indexOfFirst { it.id == currentConversationId }

        val title = generateConversationTitle(_messages)
        val lastModified = System.currentTimeMillis()

        // 数据库操作
        chatDao.insertConversation(
            ConversationEntity(
                id = currentConversationId,
                title = title,
                lastModified = lastModified
            )
        )

        // 先删除旧消息再插入新消息，避免重复
        chatDao.deleteMessagesByConversation(currentConversationId)
        _messages.forEach { message ->
            chatDao.insertMessage(
                MessageEntity(
                    conversationId = currentConversationId,
                    role = message.role,
                    content = message.content
                )
            )
        }

        val currentConversation = Conversation(
            id = currentConversationId,
            title = title,
            messages = _messages.toList(),
            lastModified = lastModified
        )

        Log.d("ChatVM", _conversations.size.toString())

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
    suspend fun loadConversation(conversationId: String) {
        if (_messages.isNotEmpty()) {
            saveCurrentConversation()
        }
        _conversations.firstOrNull { it.id == conversationId }?.let { conv ->
            _messages.clear()
            _messages.addAll(conv.messages)  // 使用 addAll 保持状态引用
            currentConversationId = conv.id
        }
    }

    suspend fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            chatDao.deleteMessagesByConversation(conversationId)
            chatDao.deleteConversation(conversationId)

            _conversations.removeAll { it.id == conversationId }

            if (conversationId == currentConversationId) {
                _messages.clear()
                newConversation()
            }
        }
    }

    suspend fun sendMessage(text: String) {
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