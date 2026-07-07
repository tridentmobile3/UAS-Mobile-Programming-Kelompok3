package com.feisal.workingreport

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.feisal.workingreport.model.Lembur // Pastikan model data Lembur sudah dibuat
import com.feisal.workingreport.repository.LemburRepository // Pastikan repository Lembur sudah dibuat
import com.feisal.workingreport.AdminLemburContent
import com.feisal.workingreport.ui.components.NoiseOverlay
import com.feisal.workingreport.ui.theme.LiquidGlassBackground
import com.feisal.workingreport.ui.theme.p79Colors
import kotlinx.coroutines.launch

class ApprovalLemburActivity : AppCompatActivity() {
    private val lemburRepository = LemburRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val colors = p79Colors(isDark = true)
            var allLembur by remember { mutableStateOf<List<Lembur>>(emptyList()) }
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                allLembur = lemburRepository.getAllLembur()
            }

            Box(modifier = Modifier.fillMaxSize()) {
                LiquidGlassBackground(colors = colors) { }
                NoiseOverlay()

                AdminLemburContent(
                    colors = colors,
                    lemburList = allLembur,
                    onRefresh = {
                        scope.launch { allLembur = lemburRepository.getAllLembur() }
                    },
                    lemburRepository = lemburRepository,
                    onBackClick = { finish() }
                )
            }
        }
    }
}