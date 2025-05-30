package com.example.oralenglishgpt.ui

import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.oralenglishgpt.viewModel.stt.SpeechRecognitionViewModel

@Composable
fun SpeechRecognitionButton(
    viewModel: SpeechRecognitionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val recognitionResult by viewModel.recognitionResult.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isRecognizing by viewModel.isRecognizing.collectAsState()

    // 权限请求相关
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startRecognition()
        } else {

        }
    }

    // 初始化一次
    LaunchedEffect(Unit) {
        viewModel.initialize(context)
    }


    // 显示识别结果
//        Text(
//            text = recognitionResult.ifEmpty { "点击下方按钮开始语音识别" },
//            style = MaterialTheme.typography.bodyLarge,
//            textAlign = TextAlign.Center,
//            modifier = Modifier
//                .fillMaxWidth()
//                .weight(1f)
//                .padding(16.dp)
//        )

    // 识别按钮
    Button(
        onClick = {
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED -> {
                    viewModel.startRecognition()
                }

                else -> {
                    Log.d("STT", "录音权限被拒绝")
                    permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                }
            }
        },
        enabled = !isRecognizing,
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