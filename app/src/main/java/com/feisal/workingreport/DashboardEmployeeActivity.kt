package com.feisal.workingreport

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

class DashboardEmployeeActivity : AppCompatActivity() {
    private val attendanceRepository by lazy { AttendanceRepository() }
    private val authRepository by lazy { AuthRepository() }
    private val permissionRepository by lazy { PermissionRepository() }
    private val workingReportRepository by lazy { WorkingReportRepository() }
    private val profileRepository by lazy { ProfileRepository() }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val context = LocalContext.current
            val sharedPref = remember { context.getSharedPreferences("AppPref", Context.MODE_PRIVATE) }
            var isDarkMode by remember { mutableStateOf(sharedPref.getBoolean("isDarkMode", true)) }
            val colors = p79Colors(isDark = isDarkMode)

            val pagerState = rememberPagerState(pageCount = { 4 })
            val coroutineScope = rememberCoroutineScope()

            var currentUser by remember { mutableStateOf<User?>(null) }
            var todayAttendance by remember { mutableStateOf<Attendance?>(null) }
            var attendanceHistory by remember { mutableStateOf<List<Attendance>>(emptyList()) }
            var workingReports by remember { mutableStateOf<List<WorkingReport>>(emptyList()) }
            var permissionHistory by remember { mutableStateOf<List<PermissionRequest>>(emptyList()) }

            var showNotificationSheet by remember { mutableStateOf(false) }
            var showLaporanSheet by remember { mutableStateOf(false) }
            var showIzinSheet by remember { mutableStateOf(false) }

            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            fun refreshData() {
                coroutineScope.launch {
                    try {
                        val user = authRepository.getCurrentUserProfile()
                        currentUser = user
                        user?.let { u ->
                            // Ambil data terbaru langsung dari Firestore (Source.SERVER if possible, but getTodayAttendance is fine)
                            val att = attendanceRepository.getTodayAttendance()
                            todayAttendance = att

                            attendanceHistory = attendanceRepository.getAttendanceHistory()
                            workingReports = workingReportRepository.getMyReports()
                            permissionHistory = permissionRepository.getMyPermissions(u.id)
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

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = false
                ) { page ->
                    when (page) {
                        0 -> HomeContent(
                            colors = colors,
                            isDarkMode = isDarkMode,
                            activity = this@DashboardEmployeeActivity,
                            currentUser = currentUser,
                            todayAttendance = todayAttendance,
                            history = attendanceHistory,
                            hasActivePermission = permissionHistory.any { it.date == SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) },
                            onLaporClick = { coroutineScope.launch { pagerState.animateScrollToPage(2) } },
                            onRiwayatClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                            onIzinClick = { showIzinSheet = true },
                            onLemburClick = {val intent = Intent(this@DashboardEmployeeActivity, LemburActivity::class.java)
                                startActivity(intent)},
                            onBellClick = { showNotificationSheet = true }
                        )
                        1 -> RiwayatContent(
                            colors = colors,
                            isDarkMode = isDarkMode,
                            currentUser = currentUser,
                            history = attendanceHistory,
                            permissionHistory = permissionHistory,
                            onBackClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
                        )
                        2 -> LaporanContent(
                            colors = colors,
                            isDarkMode = isDarkMode,
                            currentUser = currentUser,
                            reports = workingReports,
                            onBellClick = { showNotificationSheet = true },
                            onAddClick = { showLaporanSheet = true }
                        )
                        3 -> ProfilContent(
                            colors = colors,
                            isDarkMode = isDarkMode,
                            currentUser = currentUser,
                            onThemeChange = { isDark ->
                                isDarkMode = isDark
                                sharedPref.edit().putBoolean("isDarkMode", isDark).apply()
                            },
                            onLogoutClick = {
                                authRepository.logout()
                                startActivity(Intent(this@DashboardEmployeeActivity, LoginActivity::class.java))
                                finish()
                            },
                            onBackClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                            onRefresh = { refreshData() },
                            profileRepository = profileRepository
                        )
                    }
                }

                DockNavigationBar(
                    colors = colors,
                    isDarkMode = isDarkMode,
                    selectedIndex = pagerState.currentPage,
                    isHc = currentUser?.role == "HC",
                    onItemSelected = { index ->
                        coroutineScope.launch { pagerState.animateScrollToPage(index) }
                    },
                    onAdminClick = {
                        startActivity(Intent(this@DashboardEmployeeActivity, DashboardHCActivity::class.java))
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            if (showNotificationSheet) {
                ModalBottomSheet(onDismissRequest = { showNotificationSheet = false }, sheetState = sheetState, containerColor = if (isDarkMode) Color(0xFF161D2F) else Color.White) {
                    NotificationSheetContent(colors = colors, isDarkMode = isDarkMode)
                }
            }

            if (showLaporanSheet) {
                ModalBottomSheet(onDismissRequest = { showLaporanSheet = false }, sheetState = sheetState, containerColor = if (isDarkMode) Color(0xFF161D2F) else Color.White) {
                    LaporanBottomSheetContent(colors = colors, isDarkMode = isDarkMode, onSubmit = { tgl, jdl, dsk, mli, sls, uri, nm, mim ->
                        coroutineScope.launch {
                            workingReportRepository.submitReport(mli, sls, "WFO", jdl, dsk, "100%", "-", "-", uri, nm ?: "", mim ?: "").onSuccess {
                                showLaporanSheet = false; Toast.makeText(context, "Laporan terkirim", Toast.LENGTH_SHORT).show(); refreshData()
                            }.onFailure { Toast.makeText(context, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show() }
                        }
                    })
                }
            }

            if (showIzinSheet) {
                LaunchedEffect(Unit) {
                    val fragment = IzinBottomSheetFragment().apply {
                        onSubmitCallback = { type, reason, date, fileName, drive ->
                            coroutineScope.launch {
                                currentUser?.let { user ->
                                    permissionRepository.submitPermission(user.id, user.name, user.nip, type, reason, date, fileName, drive).onSuccess {
                                        Toast.makeText(this@DashboardEmployeeActivity, "Izin dikirim", Toast.LENGTH_SHORT).show(); refreshData()
                                    }.onFailure { Toast.makeText(this@DashboardEmployeeActivity, "Gagal", Toast.LENGTH_SHORT).show() }
                                }
                            }
                        }
                    }
                    fragment.show(supportFragmentManager, "IzinFragment")
                    showIzinSheet = false
                }
            }
        }
    }
}

@Composable
fun DockNavigationBar(
    colors: P79Colors,
    isDarkMode: Boolean,
    selectedIndex: Int,
    isHc: Boolean,
    onItemSelected: (Int) -> Unit,
    onAdminClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dockBgColor = if (isDarkMode) Color(0xD9161D2F) else Color(0xD9FFFFFF)
    Row(modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        BoxWithConstraints(modifier = Modifier.weight(1f).height(64.dp).clip(RoundedCornerShape(32.dp)).background(dockBgColor).border(1.dp, colors.border, RoundedCornerShape(32.dp))) {
            val itemsCount = if (isHc) 4 else 3
            val itemWidth = maxWidth / itemsCount
            val showIndicator = selectedIndex < itemsCount
            val indicatorOffset by animateDpAsState(
                targetValue = if (showIndicator) itemWidth * selectedIndex else 0.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
                label = ""
            )

            if (showIndicator) {
                Box(modifier = Modifier.offset(x = indicatorOffset).width(itemWidth).fillMaxHeight().padding(6.dp).background(Brush.linearGradient(listOf(colors.blue, colors.green)), RoundedCornerShape(26.dp)))
            }

            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                BottomNavItem(icon = Icons.Default.Home, label = "Home", isSelected = selectedIndex == 0) { onItemSelected(0) }
                BottomNavItem(icon = Icons.Default.DateRange, label = "Riwayat", isSelected = selectedIndex == 1) { onItemSelected(1) }
                BottomNavItem(icon = Icons.Default.Edit, label = "Laporan", isSelected = selectedIndex == 2) { onItemSelected(2) }
                if (isHc) { BottomNavItem(icon = Icons.Default.Settings, label = "Admin", isSelected = selectedIndex == 4) { onAdminClick() } }
            }
        }
        Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(if (selectedIndex == 3) Brush.linearGradient(listOf(colors.blue, colors.green)) else SolidColor(dockBgColor)).border(1.dp, if (selectedIndex == 3) Color.Transparent else colors.border, CircleShape).clickable { onItemSelected(3) }, contentAlignment = Alignment.Center) {
            Icon(imageVector = Icons.Default.Person, contentDescription = "Profil", tint = if (selectedIndex == 3) Color.White else Color.Gray, modifier = Modifier.size(26.dp))
        }
    }
}

@Composable
fun BottomNavItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)) {
        Icon(icon, contentDescription = label, tint = if (isSelected) Color.White else Color.Gray, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, color = if (isSelected) Color.White else Color.Gray, fontSize = 9.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}
