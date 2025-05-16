package com.example.oralenglishgpt.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.oralenglishgpt.R

class ChatViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            // 从secrets.xml读取API Key
            val apiKey = context.getString(R.string.zhipuai_api_key)
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(apiKey) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}