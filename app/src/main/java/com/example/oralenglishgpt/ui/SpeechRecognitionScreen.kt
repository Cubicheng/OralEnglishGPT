package com.example.oralenglishgpt.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oralenglishgpt.viewModel.stt.SpeechRecognitionViewModel
import com.iflytek.cloud.RecognizerResult
import com.iflytek.cloud.ui.RecognizerDialogListener

@Composable
fun SpeechRecognitionScreen(
    modifier: Modifier = Modifier,
    viewModel: SpeechRecognitionViewModel = viewModel()
) {
    val context = LocalContext.current
    val recognitionResult by viewModel.recognitionResult.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isRecognizing by viewModel.isRecognizing.collectAsState()

    // 初始化一次
    LaunchedEffect(Unit) {
        viewModel.initialize(context)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 显示识别结果
        Text(
            text = recognitionResult.ifEmpty { "点击下方按钮开始语音识别" },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
        )

        // 错误消息
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp)
            )
            Button(
                onClick = { viewModel.clearErrorMessage() },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("清除错误")
            }
        }

        // 识别按钮
        Button(
            onClick = { viewModel.startRecognition() },
            enabled = !isRecognizing,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            if (isRecognizing) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("识别中...")
            } else {
                Text("开始语音识别")
            }
        }
    }
}