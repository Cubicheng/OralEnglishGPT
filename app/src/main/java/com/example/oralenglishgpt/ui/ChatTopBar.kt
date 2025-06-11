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
            // AutoPlay 开关
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 16.dp, end = 8.dp)
                    .widthIn(min = 100.dp) // 最小宽度
            ) {
                Text(
                    text = "AutoPlay",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Switch(
                    checked = autoPlay,
                    onCheckedChange = onAutoPlayChange,
                    modifier = Modifier
                        .size(32.dp)
                        .scale(0.8f)
                )
            }
        }
    )
}