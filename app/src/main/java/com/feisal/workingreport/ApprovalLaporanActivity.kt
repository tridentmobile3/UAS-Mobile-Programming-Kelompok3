package com.feisal.workingreport

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.feisal.workingreport.model.WorkingReport
import com.feisal.workingreport.repository.WorkingReportRepository
import com.feisal.workingreport.ui.components.AdminLaporanContent
import com.feisal.workingreport.ui.components.NoiseOverlay
import com.feisal.workingreport.ui.theme.LiquidGlassBackground
import com.feisal.workingreport.ui.theme.p79Colors
import kotlinx.coroutines.launch

class ApprovalLaporanActivity : AppCompatActivity() {
    private val workingReportRepository = WorkingReportRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val colors = p79Colors(isDark = true)
            var allReports by remember { mutableStateOf<List<WorkingReport>>(emptyList()) }
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                allReports = workingReportRepository.getAllReports()
            }

            Box(modifier = Modifier.fillMaxSize()) {
                LiquidGlassBackground(colors = colors) { }
                NoiseOverlay()

                AdminLaporanContent(
                    colors = colors,
                    reports = allReports,
                    onRefresh = { 
                        scope.launch { allReports = workingReportRepository.getAllReports() }
                    },
                    workingReportRepository = workingReportRepository,
                    onBackClick = { finish() }
                )
            }
        }
    }
}
