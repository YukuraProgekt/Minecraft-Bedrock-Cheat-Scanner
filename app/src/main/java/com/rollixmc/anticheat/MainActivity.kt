package com.rollixmc.anticheat

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rollixmc.anticheat.ui.RollixTheme
import com.rollixmc.anticheat.ui.MainScreen
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize cheats database on first launch
        initializeCheatsDatabases()

        setContent {
            RollixTheme {
                MainScreen(viewModel = vm,
                    onOpenManageAllFiles = { openManageAllFilesSettings() },
                    onOpenFile = { path -> openFilePath(path) })
            }
        }

        // Example of collecting events from ViewModel if needed
        lifecycleScope.launch {
            vm.events.collectLatest { event ->
                when (event) {
                    is MainViewModel.Event.ShowManageAllFilesIntent -> openManageAllFilesSettings()
                }
            }
        }
    }

    private fun initializeCheatsDatabases() {
        try {
            val cheatsFile = File(filesDir, "cheats.txt")
            if (!cheatsFile.exists()) {
                // Create default cheats.txt with sample MD5 hashes
                val sampleHashes = """
5d41402abc4b2a76b9719d911017c592
098f6bcd4621d373cade4e832627b4f6
1b6453892473a467d07372d45eb05abc2
c81e728d9d4c2f636f067f89cc14862c
eccbc87e4b5ce2fe28308fd9f2a7baf3
37693cfc748049645fa1d086239b45b11
1ff1de774005f8da13f42943881c655e0
8f14e45fceea167a5a36dedd4bea2543
c4ca4238a0b923820dcc509a6f75849b
a1d0c6e83f027327d8461063f4ac58a6
17e88db1893aeb9dc0e070b6a7ecb3f1
6512bd43d9caa6e02c990b0a82652dca
c20ad4d76fe97759aa27a0c99bff66710
9b71d224bd62f3785d96f46e3297e394
202cb962ac59075b964b07152d234b70
4e732ced34da4a450588d0d4019e6fbf
5a105e8b9d40e1329780d62ea2265d8a
0bfe935e70e321d1e1da14983b91c7f6
c89f69cbbb3b32a11f0b206f21c01b3b
3c07efa2686b46872ae79c3cda04f00b
                """.trimIndent()
                cheatsFile.writeText(sampleHashes)
            }
        } catch (e: Exception) {
            // Silently fail if we can't initialize the database
        }
    }

    private fun openManageAllFilesSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:$packageName"))
                startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivity(intent)
            }
        }
    }

    private fun openFilePath(path: String) {
        // Open folder containing file
        try {
            val uri = Uri.parse(path)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = uri
            startActivity(intent)
        } catch (e: Exception) {
            // best-effort, silently ignore
        }
    }
}
