package com.rollixmc.anticheat.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.rollixmc.anticheat.MainViewModel
import com.rollixmc.anticheat.ScanResult

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onOpenManageAllFiles: () -> Unit,
    onOpenFile: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val storagePermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        // если не разрешил — показать снекбар
        if (!granted) {
            LaunchedEffect(Unit) {
                snackbarHostState.showSnackbar("Разрешение доступа к файлам не предоставлено")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)) {

            Text("Minecraft Bedrock Cheat Scanner", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = {
                    // Request standard storage permission, then call manage if needed
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        onOpenManageAllFiles()
                    } else {
                        storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                    // Start scan (best-effort)
                    viewModel.scan()
                }) {
                    Text("Начать сканирование")
                }
                Spacer(modifier = Modifier.width(12.dp))
                when (state) {
                    is MainViewModel.UiState.Idle -> Text("Готов к сканированию")
                    is MainViewModel.UiState.Scanning -> Text("Сканирование...")
                    is MainViewModel.UiState.Results -> Text("Результаты: ${(state as MainViewModel.UiState.Results).items.size}")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (state) {
                is MainViewModel.UiState.Idle -> {
                    // подсказка
                    Text("Нажмите «Начать сканирование», чтобы проверить устройство на известные чит‑аддоны.")
                }
                is MainViewModel.UiState.Scanning -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                is MainViewModel.UiState.Results -> {
                    val items = (state as MainViewModel.UiState.Results).items
                    ResultsList(items = items, onOpen = onOpenFile)
                }
            }
        }
    }
}

@Composable
fun ResultsList(items: List<ScanResult>, onOpen: (String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(items) { item ->
            ResultCard(item = item, onOpen = onOpen)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ResultCard(item: ScanResult, onOpen: (String) -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.995f else 1f)

    Card(modifier = Modifier
        .fillMaxWidth()
        .animateContentSize()
        .scale(scale)
        .clickable(onClick = { onOpen(item.path) }, onClickLabel = "Открыть")
    , colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = item.reason, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Путь: ${item.path}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "MD5: ${item.md5Hash}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = { onOpen(item.path) }) {
                    Text("Открыть")
                }
            }
        }
    }
}
