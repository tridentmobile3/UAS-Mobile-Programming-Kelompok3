package com.feisal.workingreport

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.feisal.workingreport.ui.components.NoiseOverlay
import com.feisal.workingreport.ui.theme.LiquidGlassBackground
import com.feisal.workingreport.ui.theme.p79Colors
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        setContent {
            val sharedPref = getSharedPreferences("AppPref", Context.MODE_PRIVATE)
            val isDarkMode = sharedPref.getBoolean("isDarkMode", true)
            val colors = p79Colors(isDark = isDarkMode)

            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkMode
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                LiquidGlassBackground(colors = colors) { }
                NoiseOverlay()

                SplashScreenContent(
                    onFinish = {
                        startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SplashScreenContent(onFinish: () -> Unit) {
    val greetings = listOf("Hello", "Bonjour", "こんにちは", "안녕하세요", "你好", "Padepokan 79")
    val padepokanColors = listOf(Color(0xFFE53935), Color(0xFF4CAF50), Color(0xFF1E88E5), Color(0xFFFBC02D))

    var currentIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        for (i in greetings.indices) {
            currentIndex = i
            delay(400)
        }
        delay(200)
        onFinish()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AnimatedContent(
            targetState = currentIndex,
            transitionSpec = {
                (slideInVertically(animationSpec = tween(300)) { height -> height } + fadeIn(animationSpec = tween(300))).togetherWith(
                    slideOutVertically(animationSpec = tween(300)) { height -> -height } + fadeOut(animationSpec = tween(300)))
            },
            contentAlignment = Alignment.Center,
            label = "splash_anim"
        ) { targetIndex ->
            val text = greetings[targetIndex]
            val annotatedString = buildAnnotatedString {
                var colorIndex = 0
                for (char in text) {
                    if (char != ' ') {
                        withStyle(style = SpanStyle(color = padepokanColors[colorIndex % padepokanColors.size])) {
                            append(char)
                        }
                        colorIndex++
                    } else {
                        append(char)
                    }
                }
            }
            Text(
                text = annotatedString,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}