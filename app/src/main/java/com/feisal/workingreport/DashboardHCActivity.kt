package com.feisal.workingreport

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.feisal.workingreport.model.Attendance
import com.feisal.workingreport.model.PermissionRequest
import com.feisal.workingreport.model.User
import com.feisal.workingreport.model.WorkingReport
import com.feisal.workingreport.repository.*
import com.feisal.workingreport.ui.components.*
import com.feisal.workingreport.ui.theme.LiquidGlassBackground
import com.feisal.workingreport.ui.theme.P79Colors
import com.feisal.workingreport.ui.theme.p79Colors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardHCActivity : AppCompatActivity() {

    private val authRepository by lazy { AuthRepository() }
    private val attendanceRepository by lazy { AttendanceRepository() }
    private val workingReportRepository by lazy { WorkingReportRepository() }
    private val permissionRepository by lazy { PermissionRepository() }
    private val profileRepository by lazy { ProfileRepository() }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val context = LocalContext.current
            val sharedPref = remember { context.getSharedPreferences("AppPref", Context.MODE_PRIVATE) }
            var isDarkMode by remember { mutableStateOf(sharedPref.getBoolean("isDarkMode", true)) }
            val colors = p79Colors(isDark = isDarkMode)
            
            var selectedIndex by remember { mutableIntStateOf(0) }

            // Handle Back Button: Jika sedang tidak di Dashboard Hub (index 0), tekan back akan balik ke Hub.
            BackHandler(enabled = selectedIndex != 0) {
                selectedIndex = 0
            }

            var showNotificationSheet by remember { mutableStateOf(false) }
            var showLaporanSheet by remember { mutableStateOf(false) }
            var showIzinSheet by remember { mutableStateOf(false) }

            var currentUser by remember { mutableStateOf<User?>(null) }
            var todayAttendance by remember { mutableStateOf<Attendance?>(null) }
            var attendanceHistory by remember { mutableStateOf<List<Attendance>>(emptyList()) }
            var workingReports by remember { mutableStateOf<List<WorkingReport>>(emptyList()) }
            var permissionHistory by remember { mutableStateOf<List<PermissionRequest>>(emptyList()) }
            var unreadNotificationCount by remember { mutableIntStateOf(0) }
            
            val scope = rememberCoroutineScope()
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            fun refreshData() {
                scope.launch {
                    try {
                        val user = authRepository.getCurrentUserProfile()
                        currentUser = user
                        user?.let { u ->
                            val att = attendanceRepository.getTodayAttendance()
                            todayAttendance = att
                            
                            attendanceHistory = attendanceRepository.getAttendanceHistory()
                            workingReports = workingReportRepository.getMyReports()
                            permissionHistory = permissionRepository.getMyPermissions(u.id)
                            
                            unreadNotificationCount = NotificationRepository().getUnreadCount(u.id)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            LaunchedEffect(currentUser) {
                if (currentUser != null) {
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    val today = com.feisal.workingreport.utils.DateHelper.getCurrentDate()
                    val docId = "${currentUser!!.id}_$today"
                    
                    db.collection(com.feisal.workingreport.utils.Constants.ATTENDANCES_COLLECTION)
                        .document(docId)
                        .addSnapshotListener { snapshot, e ->
                            if (e != null) return@addSnapshotListener
                            if (snapshot != null && snapshot.exists()) {
                                todayAttendance = snapshot.toObject(Attendance::class.java)
                            } else {
                                todayAttendance = null
                            }
                        }
                }
            }

            LaunchedEffect(Unit) { refreshData() }

            // Sync data when returning from camera or other activities
            DisposableEffect(Unit) {
                val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                        refreshData()
                    }
                }
                lifecycle.addObserver(observer)
                onDispose {
                    lifecycle.removeObserver(observer)
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                LiquidGlassBackground(colors = colors) { }
                NoiseOverlay()

                // SUSUN SECARA VERTIKAL MENGGUNAKAN COLUMN
                Column(modifier = Modifier.fillMaxSize()) {

                    // Sekarang Box ini berada di dalam Column, sehingga .weight(1f) VALID dan aman digunakan
                    Box(modifier = Modifier.weight(1f)) {
                        when (selectedIndex) {
                            0 -> HubDashboardContent(
                                colors = colors,
                                currentUser = currentUser,
                                onNavigateToPage = { selectedIndex = it }
                            )
                            1 -> {
                                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                                // Kunci mutlak jika hari ini sudah ada izin APPROVED atau PENDING
                                val hasApprovedOrPendingPermission = permissionHistory.any {
                                    it.date == todayStr && (it.status.uppercase() == "APPROVED" || it.status.uppercase() == "PENDING")
                                }

                                HomeContent(
                                    colors = colors,
                                    isDarkMode = isDarkMode,
                                    activity = this@DashboardHCActivity,
                                    currentUser = currentUser,
                                    todayAttendance = todayAttendance,
                                    attendanceHistory = attendanceHistory,
                                    permissionHistory = permissionHistory,
                                    hasActivePermission = hasApprovedOrPendingPermission,
                                    canClickIzin = todayAttendance == null && !hasApprovedOrPendingPermission,
                                    canClickAbsen = !hasApprovedOrPendingPermission,
                                    unreadCount = unreadNotificationCount,
                                    onLaporClick = { selectedIndex = 2 },
                                    onRiwayatClick = { val intent = Intent(this@DashboardHCActivity, RiwayatSayaActivity::class.java)
                                        startActivity(intent) },

                                    // PENCEGAHAN SEBELUM SHEET TERBUKA
                                    onIzinClick = {
                                        if (hasApprovedOrPendingPermission) {
                                            Toast.makeText(this@DashboardHCActivity, "Anda sudah mengajukan izin/cuti hari ini dan tidak bisa mengajukan lagi!", Toast.LENGTH_LONG).show()
                                        } else {
                                            showIzinSheet = true
                                        }
                                    },
                                    onLemburClick = {
                                        startActivity(Intent(this@DashboardHCActivity, LemburActivity::class.java))
                                    },
                                    onBellClick = { 
                                        startActivity(Intent(this@DashboardHCActivity, NotificationActivity::class.java))
                                    }
                                )
                            }
                            2 -> LaporanContent(
                                colors = colors,
                                isDarkMode = isDarkMode,
                                currentUser = currentUser,
                                reports = workingReports,
                                unreadCount = unreadNotificationCount,
                                onBellClick = { 
                                    startActivity(Intent(this@DashboardHCActivity, NotificationActivity::class.java))
                                },
                                onAddClick = { showLaporanSheet = true }
                            )
                            3 -> ProfilContentHC(
                                colors = colors,
                                isDarkMode = isDarkMode,
                                currentUser = currentUser,
                                onThemeChange = { isDark ->
                                    isDarkMode = isDark
                                    sharedPref.edit().putBoolean("isDarkMode", isDark).apply()
                                },
                                onLogoutClick = {
                                    authRepository.logout()
                                    startActivity(Intent(this@DashboardHCActivity, LoginActivity::class.java))
                                    finish()
                                },
                                onBackClick = { selectedIndex = 0 },
                                onRefresh = { refreshData() },
                                profileRepository = profileRepository
                            )
                        }
                    }

                    // Navigasi bawah otomatis terdorong ke area paling bawah layar
                    DockNavigationBarHC(
                        colors = colors,
                        selectedIndex = selectedIndex,
                        onItemSelected = { selectedIndex = it }
                    )
                }
            }

            if (showNotificationSheet) {
                ModalBottomSheet(onDismissRequest = { showNotificationSheet = false }, sheetState = sheetState, containerColor = if (isDarkMode) Color(0xFF161D2F) else Color.White) {
                    NotificationSheetContent(colors = colors, isDarkMode = isDarkMode)
                }
            }

            if (showLaporanSheet) {
                ModalBottomSheet(onDismissRequest = { showLaporanSheet = false }, sheetState = sheetState, containerColor = if (isDarkMode) Color(0xFF161D2F) else Color.White) {
                    LaporanBottomSheetContent(colors = colors, isDarkMode = isDarkMode, onSubmit = { tgl, jdl, dsk, mli, sls, uri, nm, mim ->
                        scope.launch {
                            workingReportRepository.submitReport(mli, sls, "WFO", jdl, dsk, "100%", "-", "-", uri, nm ?: "", mim ?: "").onSuccess {
                                showLaporanSheet = false; Toast.makeText(context, "Laporan terkirim", Toast.LENGTH_SHORT).show(); refreshData()
                            }.onFailure { Toast.makeText(context, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show() }
                        }
                    })
                }
            }

            if (showIzinSheet) {
                // HAPUS LaunchedEffect(Unit) agar dialog fragment tidak langsung menutup sendiri secara paksa
                val fragment = IzinBottomSheetFragment().apply {
                    onSubmitCallback = { type, reason, date, fileName, drive ->
                        scope.launch {
                            currentUser?.let { user ->
                                // Membuat ID unik berdasarkan tanggal untuk mencegah duplikasi di database
                                val docIdDate = date.replace("/", "-")
                                val uniquePermissionId = "${user.id}_$docIdDate"

                                permissionRepository.submitPermission(
                                    userId = user.id,
                                    employeeName = user.name,
                                    employeeNip = user.nip,
                                    type = type,
                                    reason = reason,
                                    date = date,
                                    fileNameText = fileName,
                                    driveLink = drive,
                                    uniqueId = uniquePermissionId // Kirimkan ID Unik ke repositori
                                ).onSuccess {
                                    Toast.makeText(this@DashboardHCActivity, "Izin berhasil dikirim!", Toast.LENGTH_SHORT).show()
                                    showIzinSheet = false
                                    refreshData()
                                }.onFailure {
                                    Toast.makeText(this@DashboardHCActivity, "Gagal mengirim izin", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
                fragment.show(supportFragmentManager, "IzinFragment")
                // Pindahkan showIzinSheet = false ke dalam callback onSuccess / onDismiss, JANGAN taruh di luar sini.
            }
        }
    }
}

@Composable
fun HubDashboardContent(
    colors: P79Colors,
    currentUser: User?,
    onNavigateToPage: (Int) -> Unit
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(24.dp))
        Text("Dashboard HC", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Monitoring & Aktivitas Saya", color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(32.dp))

        Text("AKTIVITAS SAYA", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HubMenuCard(modifier = Modifier.weight(1f), title = "Presensi Saya", icon = Icons.Default.CheckCircle, color = colors.green) {
                onNavigateToPage(1)
            }
            HubMenuCard(modifier = Modifier.weight(1f), title = "Buat Laporan", icon = Icons.Default.Edit, color = colors.blue) {
                onNavigateToPage(2)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        HubMenuCard(modifier = Modifier.fillMaxWidth(), title = "Riwayat Saya", icon = Icons.Default.DateRange, color = colors.amber) {
            context.startActivity(Intent(context, RiwayatSayaActivity::class.java))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- SECTION MONITORING (DIUBAH MENJADI GRID 2x2) ---
        Text("MONITORING", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(16.dp))

        // Baris 1: Monitoring Presensi & Approval Izin
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HubMenuCard(modifier = Modifier.weight(1f), title = "Presensi", icon = Icons.Default.DateRange, color = colors.blue) {
                context.startActivity(Intent(context, HcAttendanceActivity::class.java))
            }
            HubMenuCard(modifier = Modifier.weight(1f), title = "Approval Izin", icon = Icons.Default.Info, color = colors.amber) {
                context.startActivity(Intent(context, ApprovalIzinActivity::class.java))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Baris 2: Approval Laporan & Approval Lembur
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HubMenuCard(modifier = Modifier.weight(1f), title = "Approval Laporan", icon = Icons.Default.Edit, color = colors.green) {
                context.startActivity(Intent(context, ApprovalLaporanActivity::class.java))
            }
            HubMenuCard(modifier = Modifier.weight(1f), title = "Approval Lembur", icon = Icons.Default.Refresh, color = colors.amber) {
                // Pastikan ApprovalLemburActivity sudah terdaftar di AndroidManifest.xml
                context.startActivity(Intent(context, ApprovalLemburActivity::class.java))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("STATISTIK", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(16.dp))
        HubMenuCard(modifier = Modifier.fillMaxWidth(), title = "Statistik Sistem", icon = Icons.Default.ShoppingCart, color = colors.blue) {
            context.startActivity(Intent(context, StatisticsActivity::class.java))
        }

        Spacer(modifier = Modifier.height(130.dp))
    }
}

@Composable
fun HubMenuCard(modifier: Modifier, title: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .background(Color(0xFF161D2F), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF2D3548), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp).background(color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = color)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DockNavigationBarHC(colors: P79Colors, selectedIndex: Int, onItemSelected: (Int) -> Unit) {
    val dockBgColor = Color(0xD9161D2F)
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        BoxWithConstraints(modifier = Modifier.weight(1f).height(64.dp).clip(RoundedCornerShape(32.dp)).background(dockBgColor).border(1.dp, Color(0xFF2D3548), RoundedCornerShape(32.dp))) {
            val itemsCount = 3
            val itemWidth = maxWidth / itemsCount
            val showIndicator = selectedIndex < 3
            val indicatorOffset by animateDpAsState(
                targetValue = if (showIndicator) itemWidth * selectedIndex else 0.dp, 
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow), 
                label = ""
            )
            
            if (showIndicator) {
                Box(modifier = Modifier.offset(x = indicatorOffset).width(itemWidth).fillMaxHeight().padding(6.dp).background(Brush.linearGradient(listOf(colors.blue, colors.green)), RoundedCornerShape(26.dp)))
            }

            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                HubBottomNavItem(label = "Dashboard", isSelected = selectedIndex == 0) { onItemSelected(0) }
                HubBottomNavItem(label = "Presensi", isSelected = selectedIndex == 1) { onItemSelected(1) }
                HubBottomNavItem(label = "Laporan", isSelected = selectedIndex == 2) { onItemSelected(2) }
            }
        }
        Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(if (selectedIndex == 3) Brush.linearGradient(listOf(colors.blue, colors.green)) else SolidColor(dockBgColor)).border(1.dp, Color(0xFF2D3548), CircleShape).clickable { onItemSelected(3) }, contentAlignment = Alignment.Center) {
            Icon(imageVector = Icons.Default.Person, contentDescription = "Profil", tint = if (selectedIndex == 3) Color.White else Color.Gray, modifier = Modifier.size(26.dp))
        }
    }
}

@Composable
fun HubBottomNavItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        color = if (isSelected) Color.White else Color.Gray,
        fontSize = 11.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
    )
}
