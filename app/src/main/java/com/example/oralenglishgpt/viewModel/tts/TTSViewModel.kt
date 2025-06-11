package com.example.oralenglishgpt.viewModel.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.util.*

class TTSViewModel(context: Context) : ViewModel() {
    private var tts: TextToSpeech? = null
    var isTTSReady = false
    var isPlaying by mutableStateOf(false)

    var currentPlayingIndex: Int? by mutableStateOf(null)

    init {
        tts = TextToSpeech(context) { status ->
            isTTSReady = status == TextToSpeech.SUCCESS
            if (isTTSReady) {
                tts?.language = Locale.US
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        isPlaying = true
                    }

                    override fun onDone(utteranceId: String?) {
                        isPlaying = false
                    }

                    @Deprecated("Deprecated in Java", ReplaceWith("isPlaying = false"))
                    override fun onError(utteranceId: String?) {
                        isPlaying = false
                    }
                })
            }
        }
    }

    fun playText(text: String, index:Int) {
        currentPlayingIndex = index
        isPlaying = true
        speak(text)
    }

    fun stop() {
        currentPlayingIndex = null
        isPlaying = false
        tts?.stop()
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