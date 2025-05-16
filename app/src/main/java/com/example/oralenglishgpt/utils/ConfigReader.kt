// ConfigReader.kt
package com.example.oralenglishgpt.utils

import android.content.Context
import java.io.IOException

object ConfigReader {
    fun getApiKey(context: Context): String {
        return try {
            context.assets.open("config.txt")
                .bufferedReader()
                .useLines { lines ->
                    lines.firstOrNull { it.startsWith("API_KEY=") }
                        ?.substringAfter("API_KEY=")
                        ?: throw IllegalArgumentException("API key not found in config file")
                }
        } catch (e: IOException) {
            throw RuntimeException("Failed to read config file", e)
        }
    }
}