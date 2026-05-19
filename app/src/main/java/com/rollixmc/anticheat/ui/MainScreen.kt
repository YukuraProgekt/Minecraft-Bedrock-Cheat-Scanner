package com.rollixmc.anticheat.ui

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rollixmc.anticheat.MainViewModel
import com.rollixmc.anticheat.ScanResult
import com.rollixmc.anticheat.UiState

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onRequestAllFilesPermission: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showPermissionDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rollix AntiCheat") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        onRequestAllFilesPermission()
                    }
                    viewModel.scan()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🚀 НАЧАТЬ СКАНИРОВАНИЕ")
            }

            when (state) {
                is UiState.Idle -> {
                    Text(
                        "Нажмите кнопку для поиска читов в Minecraft Bedrock",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                is UiState.Scanning -> {
                    val scanning = state as UiState.Scanning
                    val progress = if (scanning.total > 0) scanning.scanned.toFloat() / scanning.total else 0f

                    Text("Сканирование... \( {scanning.scanned}/ \){scanning.total}")
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                is UiState.Results -> {
                    val results = (state as UiState.Results).items

                    Text(
                        "Найдено подозрительных файлов: ${results.size}",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    if (results.isEmpty()) {
                        Text("Читы не обнаружены ✅", color = MaterialTheme.colorScheme.primary)
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(results) { result ->
                                ResultCard(result)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultCard(result: ScanResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = result.reason,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = result.path,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3
            )
            if (result.md5Hash.isNotEmpty()) {
                Text(
                    "MD5: ${result.md5Hash}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}