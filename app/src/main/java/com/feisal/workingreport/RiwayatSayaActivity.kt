package com.feisal.workingreport

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.feisal.workingreport.model.Attendance
import com.feisal.workingreport.model.PermissionRequest
import com.feisal.workingreport.repository.AttendanceRepository
import com.feisal.workingreport.repository.AuthRepository
import com.feisal.workingreport.repository.PermissionRepository
import com.feisal.workingreport.ui.components.NoiseOverlay
import com.feisal.workingreport.ui.components.RiwayatContent
import com.feisal.workingreport.ui.theme.LiquidGlassBackground
import com.feisal.workingreport.ui.theme.p79Colors
import kotlinx.coroutines.launch

class RiwayatSayaActivity : AppCompatActivity() {
    private val attendanceRepository = AttendanceRepository()
    private val authRepository = AuthRepository()
    private val permissionRepository = PermissionRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val colors = p79Colors(isDark = true)
            var attendanceHistory by remember { mutableStateOf<List<Attendance>>(emptyList()) }
            var permissionHistory by remember { mutableStateOf<List<PermissionRequest>>(emptyList()) }
            var currentUser by remember { mutableStateOf(authRepository.getCurrentUserId()?.let { com.feisal.workingreport.model.User(id = it) }) }
            
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                scope.launch {
                    val user = authRepository.getCurrentUserProfile()
                    currentUser = user
                    user?.let {
                        attendanceHistory = attendanceRepository.getAttendanceHistory()
                        permissionHistory = permissionRepository.getMyPermissions(it.id)
                    }
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