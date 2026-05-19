package com.rollixmc.anticheat

import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import kotlinx.coroutines.delay
import java.io.File
import java.security.MessageDigest

/**
 * Утилита для поиска возможных читов на основе MD5 хешей.
 * Сравнивает хеши файлов с базой данных в cheats.txt.
 * Использует best-effort подход, не гарантирует полного покрытия.
 */
class CheatScanner {

    private val keywords = listOf(
        "horion", "protohax", "luma", "toolbox", "gameguardian", "luckypatcher",
        "modmenu", "xray", "fly", "killaura", "nuker", "reach", "aimbot",
        "cheat", "hack", "inject", "bettergui", "exploit", "bedrock", "ghostclient",
        "addon", "mcpack", "mcaddon"
    )

    private val extensions = listOf(".mcpack", ".mcaddon", ".apk", ".lua", ".js")

    private fun loadCheatHashes(context: Context): Set<String> {
        return try {
            // Try to load from app's data directory
            val cheatsFile = File(context.filesDir, "cheats.txt")
            if (cheatsFile.exists()) {
                cheatsFile.bufferedReader().use { reader ->
                    reader.readLines().map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
                }
            } else {
                // Fallback: return empty set if file doesn't exist
                emptySet()
            }
        } catch (e: Exception) {
            emptySet()
        }
    }

    private fun calculateMD5(file: File): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            file.inputStream().use { fis ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    md.update(buffer, 0, bytesRead)
                }
            }
            md.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun scan(context: Context): List<ScanResult> {
        val results = mutableListOf<ScanResult>()
        val cheatHashes = loadCheatHashes(context)

        try {
            // 1) Scan some public folders
            val publicDirs = listOf(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                Environment.getExternalStorageDirectory()
            ).filterNotNull().distinct()

            var checked = 0
            for (dir in publicDirs) {
                if (dir.exists() && dir.isDirectory) {
                    dir.walkTopDown().forEach { f ->
                        if (checked > 20000) return@forEach
                        try {
                            if (f.isFile) {
                                val lower = f.name.lowercase()
                                val match = extensions.firstOrNull { lower.endsWith(it) }
                                
                                // Check by MD5 hash if file has relevant extension
                                if (match != null || lower.contains("/minecraft/") || lower.contains("/cheat/") || lower.contains("/mod/")) {
                                    val md5 = calculateMD5(f)
                                    if (md5.isNotEmpty() && md5 in cheatHashes) {
                                        results.add(ScanResult(f.absolutePath, "MD5 hash matched cheat database", md5, ResultType.FILE))
                                    }
                                }
                                checked++
                            }
                        } catch (_: Exception) {
                        }
                    }
                }
            }

            // 2) Scan for folders anywhere with minecraft/mod/cheat in name (shallow search in external root)
            try {
                val root = Environment.getExternalStorageDirectory()
                root.listFiles()?.forEach { f ->
                    try {
                        if (f.isDirectory && (f.name.lowercase().contains("minecraft") || f.name.lowercase().contains("cheat") || f.name.lowercase().contains("mod"))) {
                            f.walkTopDown().limit(2000).forEach { ff ->
                                if (ff.isFile) {
                                    val lower = ff.name.lowercase()
                                    val match = extensions.firstOrNull { lower.endsWith(it) }
                                    if (match != null) {
                                        val md5 = calculateMD5(ff)
                                        if (md5.isNotEmpty() && md5 in cheatHashes) {
                                            results.add(ScanResult(ff.absolutePath, "MD5 hash matched cheat database", md5, ResultType.FILE))
                                        }
                                    }
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {}

            // 3) Scan installed packages (best-effort: package name and label)
            val pm = context.packageManager
            val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            for (app in apps) {
                try {
                    val name = (pm.getApplicationLabel(app) ?: "").toString().lowercase()
                    val pkg = (app.packageName ?: "").lowercase()
                    val kw = keywords.firstOrNull { name.contains(it) || pkg.contains(it) }
                    if (kw != null) {
                        results.add(ScanResult(pkg, "installed app matches keyword=$kw", kw, ResultType.PACKAGE))
                    }
                } catch (_: Exception) {}
            }

            // Small delay so coroutine-friendly
            delay(50)
        } catch (e: Exception) {
            // swallow errors — scanner is best-effort
        }

        // de-duplicate by path
        return results.distinctBy { it.path }
    }
}

// Helper extension to limit sequences when walking
private fun Sequence<File>.limit(max: Int) = this.take(max)
