package com.example.oralenglishgpt.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    scope: CoroutineScope,
    drawerState: DrawerState,
    autoPlay: Boolean,
    onAutoPlayChange: (Boolean) -> Unit
) {
    TopAppBar(
        title = { Text("OralEnglishGPT") },
        navigationIcon = {
            IconButton(
                onClick = { scope.launch { drawerState.open() } }
            ) {
                Icon(Icons.Default.Menu, contentDescription = "菜单")
            }
        },
        actions = {
            // AutoPlay 开关 - 调整大小和间距
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 16.dp, end = 8.dp) // 左侧16dp间距，右侧8dp
                    .widthIn(min = 100.dp) // 最小宽度
            ) {
                Text(
                    text = "AutoPlay",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(end = 8.dp) // 文本和开关间距
                )
                Switch(
                    checked = autoPlay,
                    onCheckedChange = onAutoPlayChange,
                    modifier = Modifier
                        .size(32.dp) // 缩小开关整体大小
                        .scale(0.8f)  // 进一步缩小开关内部元素
                )
            }
        }
    )
}