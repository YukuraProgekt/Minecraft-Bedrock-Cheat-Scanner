package com.rollixmc.anticheat.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawRect
import androidx.compose.ui.text.font.FontFamily

private val MinecraftGreen = Color(0xFF00FF00)
private val BackgroundBlack = Color(0xFF0F0F0F)
private val SurfaceDark = Color(0xFF121212)
private val Accent = MinecraftGreen

private val DarkColors = darkColorScheme(
    primary = Accent,
    onPrimary = Color.Black,
    background = BackgroundBlack,
    surface = SurfaceDark,
    onSurface = Color(0xFFBFD7B6)
)

@Composable
fun RollixTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = DarkColors

    // Basic 'pixel' background implemented in Compose using Canvas: subtle tiled blocks
    MaterialTheme(
        colorScheme = colors,
        typography = Typography(defaultFontFamily = FontFamily.Monospace)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            PixelBackground(color = Color(0xFF0B0B0B), tile = Color(0xFF111111))
            content()
        }
    }
}

@Composable
private fun PixelBackground(color: Color, tile: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(color)
        val tileSize = 12f * density
        var y = 0f
        while (y < size.height) {
            var x = 0f
            while (x < size.width) {
                // draw subtle squares to simulate blocky texture
                drawRect(tile, topLeft = Offset(x, y), size = androidx.compose.ui.geometry.Size(tileSize * 0.9f, tileSize * 0.9f))
                x += tileSize
            }
            y += tileSize
        }
    }
}

