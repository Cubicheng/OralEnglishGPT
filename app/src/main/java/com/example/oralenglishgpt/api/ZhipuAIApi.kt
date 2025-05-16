package com.example.oralenglishgpt.api

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import java.time.Instant

interface ZhipuAIApi {
    @Headers("Content-Type: application/json")
    @POST("api/paas/v4/chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") auth: String,
        @Body request: ChatRequest
    ): ChatResponse
}

data class Conversation(
    val id: String,
    val title: String,
    val messages: List<Message>,
    var lastModified: Long = System.currentTimeMillis()
)

data class ChatRequest(
    val model: String = "glm-4",
    val messages: List<Message>
)

data class Message(
    val role: String,
    val content: String
)

data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)