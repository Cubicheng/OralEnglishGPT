package com.example.oralenglishgpt.viewModel.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import java.util.*

class TTSViewModel(context: Context) : ViewModel() {
    private var tts: TextToSpeech? = null
    var isTTSReady = false

    init {
        tts = TextToSpeech(context) { status ->
            isTTSReady = status == TextToSpeech.SUCCESS
            if (isTTSReady) {
                tts?.language = Locale.US // 默认英语，可动态切换
            }
        }
    }

    fun speak(text: String) {
        if (isTTSReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_utterance_id")
        }
    }

    fun setLanguage(locale: Locale) {
        tts?.language = locale
    }

    override fun onCleared() {
        tts?.stop()
        tts?.shutdown()
        super.onCleared()
    }
}