package com.feisal.workingreport.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class P79Colors(
    val bg0: Color,
    val bg1: Color,
    val glass: Color,
    val border: Color,
    val text0: Color,
    val text1: Color,
    val red: Color,
    val amber: Color,
    val green: Color,
    val blue: Color
)

val P79Dark = P79Colors(
    bg0    = Color(0xFF0C0C10),
    bg1    = Color(0xFF13131A),
    glass  = Color(0x0FFFFFFF), // ~6% white — matches rgba(255,255,255,0.06)
    border = Color(0x21FFFFFF), // ~13% white
    text0  = Color(0xFFF4F4F7),
    text1  = Color(0xFF9C9CA6),
    red    = Color(0xFFE14545),
    amber  = Color(0xFFF2B705),
    green  = Color(0xFF28A862),
    blue   = Color(0xFF2E72E0)
)

val P79Light = P79Colors(
    bg0    = Color(0xFFEEF0F4),
    bg1    = Color(0xFFFFFFFF),
    glass  = Color(0x8CFFFFFF), // ~55% white
    border = Color(0x190F0F14), // ~9% black
    text0  = Color(0xFF16171C),
    text1  = Color(0xFF6B6B76),
    red    = Color(0xFFE14545),
    amber  = Color(0xFFE0A500),
    green  = Color(0xFF1F9D5C),
    blue   = Color(0xFF2660C9)
)
fun p79Colors(isDark: Boolean): P79Colors = if (isDark) P79Dark else P79Light

@Composable
fun LiquidGlassBackground(
    colors: P79Colors,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg0)
    ) {
        Box(
            Modifier
                .size(420.dp)
                .align(Alignment.TopStart)
                .offset(x = (-140).dp, y = (-140).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(colors.blue.copy(alpha = 0.30f), Color.Transparent)
                    )
                )
        )
        Box(
            Modifier
                .size(360.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 120.dp, y = 120.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(colors.green.copy(alpha = 0.28f), Color.Transparent)
                    )
                )
        )
        content()
    }
}
