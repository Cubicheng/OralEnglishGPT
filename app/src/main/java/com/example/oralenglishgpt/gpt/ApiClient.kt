package com.example.oralenglishgpt.gpt

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private val BASE_URL = "https://open.bigmodel.cn/"
    val instance: ZhipuAIApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS) // 连接超时
                    .readTimeout(30, TimeUnit.SECONDS)    // 读取超时
                    .writeTimeout(30, TimeUnit.SECONDS)   // 写入超时
                    .addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ZhipuAIApi::class.java)
    }
}