package com.example.oralenglishgpt.viewModel.stt

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oralenglishgpt.JsonParser
import com.iflytek.cloud.*
import com.iflytek.cloud.ui.RecognizerDialog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class STTViewModel : ViewModel() {
    private val _recognitionResult = MutableStateFlow("")
    val recognitionResult: StateFlow<String> = _recognitionResult.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    private val _isRecognizing = MutableStateFlow(false)
    val isRecognizing: StateFlow<Boolean> = _isRecognizing.asStateFlow()

    private var mIat: SpeechRecognizer? = null
    private var mIatDialog: RecognizerDialog? = null
    private var mSharedPreferences: SharedPreferences? = null

    // 用HashMap存储听写结果
    private val mIatResults = LinkedHashMap<String, String>()

    private val mEngineType = SpeechConstant.TYPE_CLOUD // 引擎类型
    private val language = "zh_cn" // 识别语言
    private val resultType = "json" // 结果内容数据格式

    // 新增回调，用于在识别完成后发送消息
    var onSendMessage: ((String) -> Unit)? = null

    // 初始化监听器
    private val mInitListener = InitListener { code ->
        Log.d("SpeechRecognition", "SpeechRecognizer init() code = $code")
        if (code != ErrorCode.SUCCESS) {
            viewModelScope.launch {
                _errorMessage.value = "初始化失败，错误码：$code"
            }
        }
    }

    private val mRecognizerListener = object : RecognizerListener {
        override fun onVolumeChanged(volume: Int, data: ByteArray?) {
            // 可以处理音量变化
        }

        override fun onBeginOfSpeech() {
            // 开始说话
        }

        override fun onEndOfSpeech() {
            // 结束说话
        }

        override fun onResult(results: RecognizerResult?, isLast: Boolean) {
            results?.let { printResult(it) }
            if (isLast) {
                viewModelScope.launch {
                    _isRecognizing.value = false
                    // 直接调用发送逻辑
                    if (_recognitionResult.value.isNotBlank()) {
                        onSendMessage?.invoke(_recognitionResult.value)
                        _recognitionResult.value = "" // 清空结果，避免重复发送
                    }
                }
            }
        }

        override fun onError(error: SpeechError?) {
            error?.let {
                viewModelScope.launch {
                    _errorMessage.value = it.getPlainDescription(true)
                    _isRecognizing.value = false
                }
            }
        }

        override fun onEvent(p0: Int, p1: Int, p2: Int, p3: Bundle?) {

        }
    }

    fun initialize(context: Context) {
        mSharedPreferences = context.getSharedPreferences("ASR", Context.MODE_PRIVATE)

        // 使用SpeechRecognizer对象
        mIat = SpeechRecognizer.createRecognizer(context, mInitListener)
    }

    fun startRecognition() {
        viewModelScope.launch {
            _isRecognizing.value = true
        }

        mIat?.let { recognizer ->
            if (recognizer.isListening) {
                recognizer.cancel()
            }

            mIatResults.clear() // 清除数据
            setParam() // 设置参数
            recognizer.startListening(mRecognizerListener)
        } ?: run {
            viewModelScope.launch {
                _errorMessage.value = "创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化"
                _isRecognizing.value = false
            }
        }
    }

    private fun setParam() {
        mIat?.let { recognizer ->
            // 清空参数
            recognizer.setParameter(SpeechConstant.PARAMS, null)
            // 设置听写引擎
            recognizer.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType)
            // 设置返回结果格式
            recognizer.setParameter(SpeechConstant.RESULT_TYPE, resultType)

            if (language == "zh_cn") {
                val lag = mSharedPreferences?.getString("iat_language_preference", "mandarin") ?: "mandarin"
                Log.d("SpeechRecognition", "language:$language")
                recognizer.setParameter(SpeechConstant.LANGUAGE, "zh_cn")
                // 设置语言区域
                recognizer.setParameter(SpeechConstant.ACCENT, lag)
            } else {
                recognizer.setParameter(SpeechConstant.LANGUAGE, language)
            }

            // 设置语音前端点:静音超时时间
            recognizer.setParameter(
                SpeechConstant.VAD_BOS,
                mSharedPreferences?.getString("iat_vadbos_preference", "4000") ?: "4000"
            )

            // 设置语音后端点:后端点静音检测时间
            recognizer.setParameter(
                SpeechConstant.VAD_EOS,
                mSharedPreferences?.getString("iat_vadeos_preference", "1000") ?: "1000"
            )

            // 设置标点符号
            recognizer.setParameter(
                SpeechConstant.ASR_PTT,
                mSharedPreferences?.getString("iat_punc_preference", "1") ?: "1"
            )

            // 设置音频保存路径
            recognizer.setParameter(SpeechConstant.AUDIO_FORMAT, "wav")
            recognizer.setParameter(
                SpeechConstant.ASR_AUDIO_PATH,
                Environment.getExternalStorageDirectory().toString() + "/msc/iat.wav"
            )
        }
    }

    private fun printResult(results: RecognizerResult) {
        val text = JsonParser.parseIatResult(results.resultString)

        val sn = try {
            val resultJson = JSONObject(results.resultString)
            resultJson.optString("sn")
        } catch (e: JSONException) {
            e.printStackTrace()
            null
        }

        sn?.let { mIatResults[it] = text }

        val resultBuffer = StringBuilder()
        for (key in mIatResults.keys) {
            resultBuffer.append(mIatResults[key])
        }

        viewModelScope.launch {
            _recognitionResult.value = resultBuffer.toString()
        }
    }

    fun clearErrorMessage() {
        viewModelScope.launch {
            _errorMessage.value = ""
        }
    }

    override fun onCleared() {
        super.onCleared()
        mIat?.let {
            it.cancel()
            it.destroy()
        }
        _isRecognizing.value = false
    }

    fun clearRecognitionResult() {
        _recognitionResult.value = ""
    }
}