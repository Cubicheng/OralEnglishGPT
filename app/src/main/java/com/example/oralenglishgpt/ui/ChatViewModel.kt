package com.example.oralenglishgpt.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oralenglishgpt.api.ApiClient
import com.example.oralenglishgpt.api.ChatRequest
import com.example.oralenglishgpt.api.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel(private val apiKey: String) : ViewModel() {
    private val _messages = mutableStateListOf<Message>()
    val messages: List<Message> = _messages

    private val api = ApiClient.instance

    fun sendMessage(text: String) {
        // 添加用户消息
        if (_messages.size >= 6) {  // 保留最近3轮（user+assistant各一条）
            _messages.removeRange(0, 2)  // 删除最旧的一对QA
        }
        _messages.add(Message("user", text))

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
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _messages.add(Message("assistant", "请求失败: ${e.message}"))
                }
                Log.e("API", "网络错误: ${e.message}")
            }
        }
    }
}