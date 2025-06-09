package com.example.oralenglishgpt.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.oralenglishgpt.utils.NetworkUtils
import com.example.oralenglishgpt.viewModel.stt.STTViewModel
import kotlinx.coroutines.launch
import android.provider.Settings
import android.net.Uri

@Composable
fun SpeechRecognitionButton(
    viewModel: STTViewModel,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    onStartListening: () -> Unit = {}
) {
    val context = LocalContext.current
    val recognitionResult by viewModel.recognitionResult.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isRecognizing by viewModel.isRecognizing.collectAsState()
    val scope = rememberCoroutineScope()

    // 添加控制自定义对话框的状态
    var showPermissionRationaleDialog by remember { mutableStateOf(false) }

    // 权限请求相关
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startRecognition()
        } else {
            val activity = context as? Activity
            if (activity != null &&
                !ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    android.Manifest.permission.RECORD_AUDIO
                )
            ) {
                showPermissionRationaleDialog = true
            }
        }
    }

    // 初始化一次
    LaunchedEffect(Unit) {
        viewModel.initialize(context)
    }

    if (showPermissionRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionRationaleDialog = false },
            title = { Text("Recording Permission Required") },
            text = {
                Text("Speech recognition requires recording permission. Please manually enable the permission in settings.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionRationaleDialog = false
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text("Go to Settings")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPermissionRationaleDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
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
            scope.launch {
                if (!NetworkUtils.isNetworkAvailable(context)) {
                    NetworkUtils.showNetworkErrorSnackbar(context, snackbarHostState)
                    return@launch
                }

                when {
                    ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        onStartListening()
                        viewModel.startRecognition()
                    }
                    else -> {
                        Log.d("STT", "录音权限被拒绝")
                        permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                    }
                }
            }
        },
        enabled = !isRecognizing,
        modifier = modifier
            .fillMaxWidth(),
        border = BorderStroke(
            width = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )
    ) {
        if (isRecognizing) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Speaking...",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            Text(
                "Press to talk",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}