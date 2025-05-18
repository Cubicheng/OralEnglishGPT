package com.example.oralenglishgpt.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.oralenglishgpt.R
import com.example.oralenglishgpt.database.AppDatabase

class ChatViewModelFactory(
    private val context: Context,
    private val database: AppDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            // 从secrets.xml读取API Key
            val apiKey = context.getString(R.string.zhipuai_api_key)
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(apiKey,database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}