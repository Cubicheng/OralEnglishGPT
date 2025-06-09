package com.example.oralenglishgpt.utils

import org.json.JSONException
import org.json.JSONObject

object JsonParser {
    /**
     * 解析语音听写结果
     */
    fun parseIatResult(json: String?): String {
        val result = StringBuilder()
        try {
            val jsonObject = JSONObject(json)
            val wordsArray = jsonObject.getJSONArray("ws")
            for (i in 0 until wordsArray.length()) {
                val wsItem = wordsArray.getJSONObject(i)
                val items = wsItem.getJSONArray("cw")
                for (j in 0 until items.length()) {
                    val obj = items.getJSONObject(j)
                    result.append(obj.getString("w"))
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return result.toString()
    }
}