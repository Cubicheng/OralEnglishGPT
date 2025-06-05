package com.example.oralenglishgpt.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.oralenglishgpt.R
import com.example.oralenglishgpt.viewModel.stt.SpeechRecognitionViewModel

@Composable
fun InputArea(
    sttViewModel: SpeechRecognitionViewModel,
    onSendMessage: (String) -> Unit
) {
    var inputMode by remember { mutableStateOf(InputMode.KEYBOARD) }
    var inputText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(inputMode) {
        if (inputMode == InputMode.KEYBOARD) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // 输入模式切换按钮
        IconButton(
            onClick = {
                inputMode = if (inputMode == InputMode.KEYBOARD) InputMode.VOICE else InputMode.KEYBOARD
            }
        ) {
            Icon(
                painter = painterResource(
                    id = if (inputMode == InputMode.KEYBOARD)
                        R.drawable.mic_circle
                    else
                        R.drawable.keyboard
                ),
                contentDescription = if (inputMode == InputMode.KEYBOARD)
                    "切换到语音输入"
                else
                    "切换到键盘输入",
                modifier = Modifier.size(32.dp) // 设置图标大小
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        when (inputMode) {
            InputMode.KEYBOARD -> {
                // 键盘输入模式
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    placeholder = { Text("Input text...") },
                    shape = MaterialTheme.shapes.medium, // 使用主题中的中等圆角
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(
                            R.drawable.send_circle_o
                        ),
                        contentDescription = "发送消息",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            InputMode.VOICE -> {
                // 语音输入模式
                SpeechRecognitionButton(viewModel = sttViewModel)
            }
        }
    }
}

enum class InputMode {
    KEYBOARD, VOICE
}