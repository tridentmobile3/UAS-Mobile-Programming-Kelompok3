package com.feisal.workingreport

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.feisal.workingreport.model.Attendance
import com.feisal.workingreport.model.PermissionRequest
import com.feisal.workingreport.model.User
import com.feisal.workingreport.repository.AttendanceRepository
import com.feisal.workingreport.repository.AuthRepository
import com.feisal.workingreport.repository.PermissionRepository
import com.feisal.workingreport.ui.components.NoiseOverlay
import com.feisal.workingreport.ui.theme.LiquidGlassBackground
import com.feisal.workingreport.ui.theme.p79Colors
import kotlinx.coroutines.launch

class RiwayatActivity : AppCompatActivity() {
    private val authRepository = AuthRepository()
    private val attendanceRepository = AttendanceRepository()
    private val permissionRepository = PermissionRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val colors = p79Colors(isDark = true)
            var currentUser by remember { mutableStateOf<User?>(null) }
            var attendanceHistory by remember { mutableStateOf<List<Attendance>>(emptyList()) }
            var permissionHistory by remember { mutableStateOf<List<PermissionRequest>>(emptyList()) }
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                currentUser = authRepository.getCurrentUserProfile()
                currentUser?.let { user ->
                    attendanceHistory = attendanceRepository.getAttendanceHistory()
                    permissionHistory = permissionRepository.getMyPermissions(user.id)
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                LiquidGlassBackground(colors = colors) { }
                NoiseOverlay()
                
                RiwayatContent(
                    colors = colors,
                    isDarkMode = true,
                    currentUser = currentUser,
                    history = attendanceHistory,
                    permissionHistory = permissionHistory,
                    onBackClick = { finish() }
                )
            }
        }
    }
}
