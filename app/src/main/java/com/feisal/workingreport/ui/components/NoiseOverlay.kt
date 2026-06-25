package com.feisal.workingreport.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Shader
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint

@Composable
fun rememberNoiseBrush(
    dotColor: Color = Color.White.copy(alpha = 0.08f),
    tileSize: Dp = 3.dp
): ShaderBrush {
    val density = LocalDensity.current
    return remember(dotColor, tileSize, density) {
        val sizePx = with(density) { tileSize.toPx() }.toInt().coerceAtLeast(2)

        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(bmp)
        val paint = AndroidPaint().apply {
            color = dotColor.toArgb()
            isAntiAlias = true
        }
        canvas.drawCircle(1f, 1f, 1f, paint)

        val shader = BitmapShader(bmp, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        ShaderBrush(shader)
    }
}

@Composable
fun NoiseOverlay(
    modifier: Modifier = Modifier,
    dotColor: Color = Color.White.copy(alpha = 0.08f),
    tileSize: Dp = 3.dp
) {
    val brush = rememberNoiseBrush(dotColor, tileSize)
    Box(
        modifier
            .fillMaxSize()
            .background(brush)
    )
}