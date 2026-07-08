package com.feisal.workingreport

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.feisal.workingreport.model.Attendance
import com.feisal.workingreport.model.PermissionRequest
import com.feisal.workingreport.model.User
import com.feisal.workingreport.repository.HcAttendanceRepository
import com.feisal.workingreport.repository.PermissionRepository
import com.feisal.workingreport.ui.components.NoiseOverlay
import com.feisal.workingreport.ui.components.RiwayatContent
import com.feisal.workingreport.ui.theme.LiquidGlassBackground
import com.feisal.workingreport.ui.theme.p79Colors

class EmployeeAttendanceDetailActivity : AppCompatActivity() {
    private val hcAttendanceRepository = HcAttendanceRepository()
    private val permissionRepository = PermissionRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val userId = intent.getStringExtra("USER_ID") ?: ""
        val userName = intent.getStringExtra("USER_NAME") ?: "Karyawan"
        val userNip = intent.getStringExtra("USER_NIP") ?: "-"

        setContent {
            val colors = p79Colors(isDark = true)
            var attendanceHistory by remember { mutableStateOf<List<Attendance>>(emptyList()) }
            var permissionHistory by remember { mutableStateOf<List<PermissionRequest>>(emptyList()) }

            LaunchedEffect(userId) {
                if (userId.isNotEmpty()) {
                    // Force refresh using real Firestore query
                    attendanceHistory = hcAttendanceRepository.getEmployeeAttendanceHistory(userId)
                    permissionHistory = permissionRepository.getMyPermissions(userId)
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                LiquidGlassBackground(colors = colors) { }
                NoiseOverlay()
                
                RiwayatContent(
                    colors = colors,
                    isDarkMode = true,
                    currentUser = User(id = userId, name = userName, nip = userNip),
                    history = attendanceHistory,
                    permissionHistory = permissionHistory,
                    onBackClick = { finish() }
                )
            }
        }
    }
}
