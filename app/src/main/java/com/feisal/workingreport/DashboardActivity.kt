package com.feisal.workingreport

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowCompat
import com.feisal.workingreport.model.Attendance
import com.feisal.workingreport.model.PermissionRequest
import com.feisal.workingreport.model.User
import com.feisal.workingreport.model.WorkingReport
import com.feisal.workingreport.repository.AttendanceRepository
import com.feisal.workingreport.repository.AuthRepository
import com.feisal.workingreport.repository.PermissionRepository
import com.feisal.workingreport.repository.WorkingReportRepository
import com.feisal.workingreport.ui.components.GlassCard
import com.feisal.workingreport.ui.components.NoiseOverlay
import com.feisal.workingreport.ui.theme.LiquidGlassBackground
import com.feisal.workingreport.ui.theme.P79Colors
import com.feisal.workingreport.ui.theme.p79Colors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.net.Uri
import androidx.compose.ui.platform.LocalContext

class DashboardActivity : AppCompatActivity() {
    private val attendanceRepository by lazy { AttendanceRepository() }
    private val authRepository by lazy { AuthRepository() }
    private val permissionRepository by lazy { PermissionRepository() }
    private val workingReportRepository by lazy { WorkingReportRepository() }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        setContent {
            val context = LocalContext.current
            val sharedPref = remember { context.getSharedPreferences("AppPref", Context.MODE_PRIVATE) }
            var isDarkMode by remember { mutableStateOf(sharedPref.getBoolean("isDarkMode", true)) }

            val colors = p79Colors(isDark = isDarkMode)
            val view = androidx.compose.ui.platform.LocalView.current
            if (!view.isInEditMode) {
                LaunchedEffect(isDarkMode) {
                    val window = (view.context as android.app.Activity).window
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkMode
                }
            }

            var showNotificationSheet by remember { mutableStateOf(false) }
            var showIzinSheet by remember { mutableStateOf(false) }
            var showLemburSheet by remember { mutableStateOf(false) }
            var showLaporanSheet by remember { mutableStateOf(false) }

            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val pagerState = rememberPagerState(pageCount = { 4 })
            val coroutineScope = rememberCoroutineScope()

            // State for Data
            var currentUser by remember { mutableStateOf<User?>(null) }
            var todayAttendance by remember { mutableStateOf<Attendance?>(null) }
            var todayPermission by remember { mutableStateOf<PermissionRequest?>(null) }
            var permissionHistory by remember { mutableStateOf<List<PermissionRequest>>(emptyList()) }
            var attendanceHistory by remember { mutableStateOf<List<Attendance>>(emptyList()) }
            var workingReports by remember { mutableStateOf<List<WorkingReport>>(emptyList()) }

            val refreshData = {
                coroutineScope.launch {
                    try {
                        currentUser = authRepository.getCurrentUserProfile()
                        currentUser?.let { user ->
                            todayAttendance = attendanceRepository.getTodayAttendance()
                            attendanceHistory = attendanceRepository.getAttendanceHistory()
                            workingReports = workingReportRepository.getMyReports()

                            // Sinkronisasi data izin dari Firebase
                            permissionHistory = permissionRepository.getMyPermissions(user.id)
                            val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                            todayPermission = permissionHistory.find { it.date == todayStr }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            // Load Initial Data
            LaunchedEffect(Unit) {
                refreshData()
                val targetPage = intent.getIntExtra("TARGET_PAGE", -1)
                if (targetPage != -1) {
                    coroutineScope.launch {
                        pagerState.scrollToPage(targetPage)
                    }
                }
            }

            // Sync attendance when returning from camera
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
                    pageSpacing = 16.dp,
                    userScrollEnabled = false
                ) { page ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (page) {
                            0 -> HomeContent(
                                colors = colors,
                                isDarkMode = isDarkMode,
                                activity = this@DashboardActivity,
                                currentUser = currentUser,
                                todayAttendance = todayAttendance,
                                hasActivePermission = (todayPermission != null),
                                onLaporClick = { coroutineScope.launch { pagerState.animateScrollToPage(2) } },
                                onRiwayatClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                                onIzinClick = { showIzinSheet = true },
                                onLemburClick = {
                                    val intent = Intent(this@DashboardActivity, LemburActivity::class.java)
                                    startActivity(intent)
                                },
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
                                    try {
                                        authRepository.logout()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                    startActivity(Intent(this@DashboardActivity, LoginActivity::class.java))
                                    finish()
                                },
                                onBackClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
                            )
                        }
                    }
                }

                DockNavigationBar(
                    colors = colors,
                    isDarkMode = isDarkMode,
                    selectedIndex = pagerState.currentPage,
                    isHc = currentUser?.role == "HC",
                    onItemSelected = { index ->
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    onAdminClick = {
                        startActivity(Intent(this@DashboardActivity, DashboardAdminActivity::class.java))
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            if (showNotificationSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showNotificationSheet = false },
                    sheetState = sheetState,
                    containerColor = if (isDarkMode) Color(0xFF161D2F) else Color.White,
                    scrimColor = Color.Black.copy(alpha = 0.5f)
                ) {
                    NotificationSheetContent(colors = colors, isDarkMode = isDarkMode)
                }
            }

            // Logika Bisnis Interaktif Pengajuan Izin via DialogFragment XML
            if (showIzinSheet) {
                val sudahAbsenMasuk = todayAttendance != null && todayAttendance?.checkInTime?.isNotEmpty() == true

                if (sudahAbsenMasuk) {
                    LaunchedEffect(Unit) {
                        Toast.makeText(context, "Anda sudah melakukan absen masuk hari ini. Tidak dapat mengajukan izin!", Toast.LENGTH_LONG).show()
                        showIzinSheet = false
                    }
                } else {
                    LaunchedEffect(Unit) {
                        val izinFragment = IzinBottomSheetFragment().apply {
                            // Menerima parameter byte data mentah dari fragment
                            onSubmitCallback = { type, reason, dateString, bytes, driveLink, ext ->
                                coroutineScope.launch {
                                    currentUser?.let { user ->
                                        val result = permissionRepository.submitPermission(
                                            userId = user.id,
                                            employeeName = user.name,
                                            employeeNip = user.nip,
                                            type = type,
                                            reason = reason,
                                            date = dateString,
                                            fileBytes = bytes,
                                            driveLink = driveLink,
                                            extension = ext
                                        )

                                        result.onSuccess {
                                            Toast.makeText(this@DashboardActivity, "Pengajuan izin berhasil disimpan ke database!", Toast.LENGTH_SHORT).show()
                                            refreshData()
                                        }.onFailure {
                                            Toast.makeText(this@DashboardActivity, "Gagal menyimpan: ${it.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        }
                        izinFragment.show(supportFragmentManager, "IzinBottomSheetFragment")
                        showIzinSheet = false
                    }
                }
            }

            if (showLemburSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showLemburSheet = false },
                    sheetState = sheetState,
                    containerColor = if (isDarkMode) Color(0xFF161D2F) else Color.White,
                    scrimColor = Color.Black.copy(alpha = 0.5f)
                ) {
                    LemburBottomSheetContent(colors = colors, isDarkMode = isDarkMode)
                }
            }

            if (showLaporanSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showLaporanSheet = false },
                    sheetState = sheetState,
                    containerColor = if (isDarkMode) Color(0xFF161D2F) else Color.White,
                    scrimColor = Color.Black.copy(alpha = 0.5f)
                ) {
                    LaporanBottomSheetContent(
                        colors = colors,
                        isDarkMode = isDarkMode,
                        onSubmit = { tanggal, judul, deskripsi, mulai, selesai ->
                            coroutineScope.launch {
                                val result = workingReportRepository.submitReport(
                                    startTime = mulai,
                                    endTime = selesai,
                                    workLocation = "WFH/WFO",
                                    title = judul,
                                    description = deskripsi,
                                    progress = "100%",
                                    obstacle = "-",
                                    nextPlan = "-"
                                )
                                result.onSuccess {
                                    showLaporanSheet = false
                                    Toast.makeText(context, "Laporan kerja berhasil terkirim", Toast.LENGTH_SHORT).show()
                                    workingReports = workingReportRepository.getMyReports()
                                }.onFailure {
                                    Toast.makeText(context, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DatePickerBox(value: String, onValueChange: (String) -> Unit, placeholder: String, colors: P79Colors, inputBgColor: Color, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val calendar = java.util.Calendar.getInstance()
    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
            onValueChange(formattedDate)
        },
        calendar.get(java.util.Calendar.YEAR),
        calendar.get(java.util.Calendar.MONTH),
        calendar.get(java.util.Calendar.DAY_OF_MONTH)
    )

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            textStyle = androidx.compose.ui.text.TextStyle(color = colors.text0),
            placeholder = { Text(placeholder, color = colors.text1) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = colors.blue,
                unfocusedContainerColor = inputBgColor,
                focusedContainerColor = inputBgColor
            )
        )
        Box(modifier = Modifier.matchParentSize().background(Color.Transparent).clickable { datePickerDialog.show() })
    }
}

@Composable
fun TimePickerBox(value: String, onValueChange: (String) -> Unit, placeholder: String, colors: P79Colors, inputBgColor: Color, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val calendar = java.util.Calendar.getInstance()
    val timePickerDialog = android.app.TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
            onValueChange(formattedTime)
        },
        calendar.get(java.util.Calendar.HOUR_OF_DAY),
        calendar.get(java.util.Calendar.MINUTE),
        true
    )

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            textStyle = androidx.compose.ui.text.TextStyle(color = colors.text0),
            placeholder = { Text(placeholder, color = colors.text1) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = colors.blue,
                unfocusedContainerColor = inputBgColor,
                focusedContainerColor = inputBgColor
            )
        )
        Box(modifier = Modifier.matchParentSize().background(Color.Transparent).clickable { timePickerDialog.show() })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanBottomSheetContent(
    colors: P79Colors,
    isDarkMode: Boolean,
    onSubmit: (String, String, String, String, String) -> Unit
) {
    val inputBgColor = if (isDarkMode) Color(0xFF222831) else Color(0xFFF3F4F6)
    var tanggal by remember { mutableStateOf("") }
    var judul by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    var jamMulai by remember { mutableStateOf("") }
    var jamSelesai by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
        Text("Buat Laporan Kerja", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        Text("Tanggal", color = colors.text1, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        DatePickerBox(value = tanggal, onValueChange = { tanggal = it }, placeholder = "", colors = colors, inputBgColor = inputBgColor)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Judul Aktivitas", color = colors.text1, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = judul,
            onValueChange = { judul = it },
            textStyle = androidx.compose.ui.text.TextStyle(color = colors.text0),
            placeholder = { Text("", color = colors.text1) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = colors.blue,
                unfocusedContainerColor = inputBgColor,
                focusedContainerColor = inputBgColor
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Deskripsi Pekerjaan", color = colors.text1, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = deskripsi,
            onValueChange = { deskripsi = it },
            textStyle = androidx.compose.ui.text.TextStyle(color = colors.text0),
            placeholder = { Text("", color = colors.text1) },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = colors.blue,
                unfocusedContainerColor = inputBgColor,
                focusedContainerColor = inputBgColor
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Durasi Kerja", color = colors.text1, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TimePickerBox(value = jamMulai, onValueChange = { jamMulai = it }, placeholder = "", colors = colors, inputBgColor = inputBgColor, modifier = Modifier.weight(1f))
            TimePickerBox(value = jamSelesai, onValueChange = { jamSelesai = it }, placeholder = "", colors = colors, inputBgColor = inputBgColor, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        val context = LocalContext.current
        Text("Lampiran (opsional)", color = colors.text1, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(colors.blue, RoundedCornerShape(8.dp))
                    .clickable {
                        Toast.makeText(context, "Upload file belum tersedia", Toast.LENGTH_SHORT).show()
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row {
                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("File", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(inputBgColor, RoundedCornerShape(8.dp))
                    .clickable {
                        Toast.makeText(context, "Fitur belum tersedia", Toast.LENGTH_SHORT).show()
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row {
                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = colors.text1, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Foto", color = colors.text1, fontSize = 12.sp)
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(inputBgColor, RoundedCornerShape(8.dp))
                    .clickable {
                        Toast.makeText(context, "Fitur belum tersedia", Toast.LENGTH_SHORT).show()
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row {
                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = colors.text1, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Link Drive", color = colors.text1, fontSize = 12.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(80.dp)
                .background(Color.Transparent, RoundedCornerShape(12.dp))
                .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Tarik file/screenshot di sini atau pilih file", color = colors.text1, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(55.dp).background(Brush.horizontalGradient(listOf(colors.blue, colors.green)), RoundedCornerShape(12.dp)).clickable {
                onSubmit(tanggal, judul, deskripsi, jamMulai, jamSelesai)
            },
            contentAlignment = Alignment.Center
        ) {
            Text("Kirim Laporan", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatContent(
    colors: P79Colors,
    isDarkMode: Boolean,
    currentUser: User?,
    history: List<Attendance>,
    permissionHistory: List<PermissionRequest>,
    onBackClick: () -> Unit
) {
    val cardBgColor = if (isDarkMode) Color(0xFF161D2F) else Color.White
    val innerCardBgColor = if (isDarkMode) Color(0xFF222831) else Color(0xFFF3F4F6)

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(48.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).clickable { onBackClick() }, contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.text0, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(currentUser?.name ?: "User", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(currentUser?.nip ?: "-", color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Box(modifier = Modifier.size(40.dp).background(colors.green, CircleShape), contentAlignment = Alignment.Center) {
                Text(currentUser?.name?.firstOrNull()?.toString() ?: "U", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("Riwayat Absensi & Izin", color = colors.text0, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Pantau histori kehadiran dan pengajuan izin kamu", color = colors.text1, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(36.dp).background(cardBgColor, RoundedCornerShape(8.dp)).border(1.dp, colors.border, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = colors.text1)
            }
            Text("Juli 2026", color = colors.text0, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Box(modifier = Modifier.size(36.dp).background(cardBgColor, RoundedCornerShape(8.dp)).border(1.dp, colors.border, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = colors.text1)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(modifier = Modifier.weight(1f), bg = cardBgColor, border = colors.border, title = history.size.toString(), subtitle = "KEHADIRAN", iconTint = colors.green, textColor = colors.text0)
            StatCard(modifier = Modifier.weight(1f), bg = cardBgColor, border = colors.border, title = "08:00", subtitle = "RATA² MASUK", iconTint = colors.blue, textColor = colors.text0)
            StatCard(modifier = Modifier.weight(1f), bg = cardBgColor, border = colors.border, title = "0", subtitle = "TERLAMBAT", iconTint = colors.amber, textColor = colors.text0)
            StatCard(modifier = Modifier.weight(1f), bg = cardBgColor, border = colors.border, title = "0", subtitle = "GPS GAGAL", iconTint = colors.red, textColor = colors.text0)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Column(modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp)).padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf("S", "S", "R", "K", "J", "S", "M").forEach { day ->
                    Text(day, color = colors.text1, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            val calendarData = List(5) { List(7) { innerCardBgColor } }
            calendarData.forEach { week ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    week.forEach { dayColor ->
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp).background(dayColor, RoundedCornerShape(8.dp)))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                LegendItem("Hadir", colors.green, colors.text1)
                Spacer(modifier = Modifier.width(16.dp))
                LegendItem("Izin", colors.amber, colors.text1)
                Spacer(modifier = Modifier.width(16.dp))
                LegendItem("Telat", colors.red, colors.text1)
                Spacer(modifier = Modifier.width(16.dp))
                LegendItem("Libur", innerCardBgColor, colors.text1)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // SEKSI HISTORI PENGAJUAN IZIN
        if (permissionHistory.isNotEmpty()) {
            // Ambil context lokal yang valid di dalam Jetpack Compose
            val composeContext = LocalContext.current

            Text("HISTORI IZIN", color = colors.text1, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            permissionHistory.forEach { izin ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .background(cardBgColor, RoundedCornerShape(16.dp))
                        .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(izin.type, color = colors.amber, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(izin.date, color = colors.text1, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Alasan: ${izin.reason}", color = colors.text0, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    // LOGIKA MENAMPILKAN LINK BUKTI FISIK (FOTO/FILE) DARI STORAGE
                    if (izin.proofUrl.isNotEmpty()) {
                        Text(
                            text = "📄 Lihat Lampiran Berkas Bukti",
                            color = colors.blue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(izin.proofUrl))
                                        composeContext.startActivity(intent) // Perbaikan di sini
                                    } catch (e: Exception) {
                                        Toast.makeText(composeContext, "Gagal membuka tautan bukti", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(vertical = 4.dp)
                        )
                    }

                    // LOGIKA MENAMPILKAN TAUTAN GOOGLE DRIVE
                    if (izin.driveLink.isNotEmpty()) {
                        Text(
                            text = "🔗 Tautan Google Drive Karyawan",
                            color = colors.green,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(izin.driveLink))
                                        composeContext.startActivity(intent) // Perbaikan di sini
                                    } catch (e: Exception) {
                                        Toast.makeText(composeContext, "Gagal membuka link Drive", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Status Persetujuan: ${izin.status}", color = colors.blue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // SEKSI HISTORI KEHADIRAN (ABSEN)
        Text("HISTORI KEHADIRAN", color = colors.text1, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))

        if (history.isEmpty()) {
            EmptyState(colors = colors, cardBgColor = cardBgColor, message = "Tidak ada riwayat absensi")
        } else {
            history.forEach { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .background(cardBgColor, RoundedCornerShape(16.dp))
                        .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(item.date, color = colors.text0, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Masuk: ${item.checkInTime.ifBlank { "--:--" }}", color = colors.text1, fontSize = 12.sp)
                    Text("Pulang: ${item.checkOutTime.ifBlank { "--:--" }}", color = colors.text1, fontSize = 12.sp)
                    Text("Status: ${item.status.ifBlank { "Tidak tersedia" }}", color = colors.text1, fontSize = 12.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(130.dp))
    }
}

@Composable
fun StatCard(modifier: Modifier, bg: Color, border: Color, title: String, subtitle: String, iconTint: Color, textColor: Color) {
    Column(modifier = modifier.background(bg, RoundedCornerShape(12.dp)).border(1.dp, border, RoundedCornerShape(12.dp)).padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(8.dp).background(iconTint, CircleShape))
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = textColor.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LegendItem(label: String, dotColor: Color, textColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(dotColor, CircleShape))
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, color = textColor, fontSize = 10.sp)
    }
}

@Composable
fun HomeContent(
    colors: P79Colors,
    isDarkMode: Boolean,
    activity: AppCompatActivity,
    currentUser: User?,
    todayAttendance: Attendance?,
    hasActivePermission: Boolean,
    onLaporClick: () -> Unit,
    onRiwayatClick: () -> Unit,
    onIzinClick: () -> Unit,
    onLemburClick: () -> Unit,
    onBellClick: () -> Unit
) {
    val cardBgColor = if (isDarkMode) Color(0xFF161D2F) else Color.White

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(48.dp))
        TopBar(colors = colors, isDarkMode = isDarkMode, currentUser = currentUser, onBellClick = onBellClick)
        Spacer(modifier = Modifier.height(24.dp))
        AbsenCard(colors = colors, isDarkMode = isDarkMode, todayAttendance = todayAttendance, hasActivePermission = hasActivePermission, activity = activity)
        Spacer(modifier = Modifier.height(16.dp))
        MenuRow(colors = colors, cardBgColor = cardBgColor, onIzinClick = onIzinClick, onLaporClick = onLaporClick, onRiwayatClick = onRiwayatClick, onLemburClick = onLemburClick)
        Spacer(modifier = Modifier.height(16.dp))
        SummaryCard(colors = colors, cardBgColor = cardBgColor)
        Spacer(modifier = Modifier.height(24.dp))
        Text("RIWAYAT TERBARU", color = colors.text1, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(modifier = Modifier.height(12.dp))
        HistoryList(colors = colors, cardBgColor = cardBgColor)
        Spacer(modifier = Modifier.height(130.dp))
    }
}

@Composable
fun MenuRow(colors: P79Colors, cardBgColor: Color, onIzinClick: () -> Unit, onLaporClick: () -> Unit, onRiwayatClick: () -> Unit, onLemburClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MenuCard(colors = colors, cardBgColor = cardBgColor, title = "Ajukan Izin", iconColor = colors.amber, modifier = Modifier.weight(1f)) { onIzinClick() }
        MenuCard(colors = colors, cardBgColor = cardBgColor, title = "Lapor Kerja", iconColor = colors.green, modifier = Modifier.weight(1f)) { onLaporClick() }
        MenuCard(colors = colors, cardBgColor = cardBgColor, title = "Riwayat", iconColor = colors.blue, modifier = Modifier.weight(1f)) { onRiwayatClick() }
        MenuCard(colors = colors, cardBgColor = cardBgColor, title = "Extra Lembur", iconColor = colors.red, modifier = Modifier.weight(1f)) { onLemburClick() }
    }
}

@Composable
fun ProfilContent(
    colors: P79Colors,
    isDarkMode: Boolean,
    currentUser: User?,
    onThemeChange: (Boolean) -> Unit,
    onLogoutClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val cardBgColor = if (isDarkMode) Color(0xFF161D2F) else Color.White
    val iconBgColor = if (isDarkMode) Color(0xFF222831) else Color(0xFFF3F4F6)
    val context = LocalContext.current

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showOfficeLocationDialog by remember { mutableStateOf(false) }

    if (showEditProfileDialog) {
        Dialog(onDismissRequest = { showEditProfileDialog = false }) {
            Surface(shape = RoundedCornerShape(24.dp), color = cardBgColor, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Edit Profil", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = currentUser?.name ?: "", onValueChange = { }, label = { Text("Nama Lengkap", color = colors.text1) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { showEditProfileDialog = false; Toast.makeText(context, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.blue)) { Text("Simpan Perubahan", color = Color.White) }
                }
            }
        }
    }

    if (showChangePasswordDialog) {
        Dialog(onDismissRequest = { showChangePasswordDialog = false }) {
            Surface(shape = RoundedCornerShape(24.dp), color = cardBgColor, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Ubah Kata Sandi", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = "", onValueChange = { }, label = { Text("Kata Sandi Lama", color = colors.text1) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = "", onValueChange = { }, label = { Text("Kata Sandi Baru", color = colors.text1) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { showChangePasswordDialog = false; Toast.makeText(context, "Kata sandi berhasil diubah", Toast.LENGTH_SHORT).show() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.blue)) { Text("Ubah Sandi", color = Color.White) }
                }
            }
        }
    }

    if (showOfficeLocationDialog) {
        Dialog(onDismissRequest = { showOfficeLocationDialog = false }) {
            Surface(shape = RoundedCornerShape(24.dp), color = cardBgColor, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Lokasi Kantor Terdaftar", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(iconBgColor, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = colors.blue, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Padepokan 79 Main Office", color = colors.text0, fontWeight = FontWeight.Bold)
                            Text("-6.9174639, 107.6191228", color = colors.text1, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { showOfficeLocationDialog = false }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.blue)) { Text("Tutup", color = Color.White) }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(48.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).clickable { onBackClick() }, contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.text0, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(currentUser?.name ?: "User", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(currentUser?.nip ?: "-", color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Box(modifier = Modifier.size(40.dp).background(colors.green, CircleShape), contentAlignment = Alignment.Center) {
                Text(currentUser?.name?.firstOrNull()?.toString() ?: "U", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.size(90.dp)) {
                Box(modifier = Modifier.size(80.dp).align(Alignment.Center).background(Brush.linearGradient(listOf(colors.blue, colors.green)), RoundedCornerShape(24.dp)), contentAlignment = Alignment.Center) {
                    Text(currentUser?.name?.firstOrNull()?.toString() ?: "U", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.size(28.dp).align(Alignment.BottomEnd).background(cardBgColor, CircleShape).border(1.dp, colors.border, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = colors.text0, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(currentUser?.name ?: "User Name", color = colors.text0, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(currentUser?.authEmail ?: "email@example.com", color = colors.text1, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) { Text(currentUser?.department ?: "Dept", color = colors.text1, fontSize = 12.sp) }
                Box(modifier = Modifier.background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) { Text(currentUser?.position ?: "Position", color = colors.text1, fontSize = 12.sp) }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("TAMPILAN", color = colors.text1, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Column(modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp))) {
            SettingsItem(colors, iconBgColor, Icons.Default.Lock, colors.blue, "Mode Gelap", "Tampilan dark / light", trailing = {
                Switch(checked = isDarkMode, onCheckedChange = onThemeChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = colors.blue, uncheckedThumbColor = colors.text1, uncheckedTrackColor = iconBgColor, uncheckedBorderColor = colors.border))
            })
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("AKUN", color = colors.text1, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Column(modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp))) {
            SettingsItem(colors, iconBgColor, Icons.Default.Edit, colors.green, "Edit Profil", null, onClick = { showEditProfileDialog = true })
            Divider(color = colors.border, modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(colors, iconBgColor, Icons.Default.Lock, colors.amber, "Ubah Kata Sandi", null, onClick = { showChangePasswordDialog = true })
            Divider(color = colors.border, modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(colors, iconBgColor, Icons.Default.LocationOn, colors.blue, "Lokasi Kantor Terdaftar", currentUser?.department ?: "Tidak tersedia", onClick = { showOfficeLocationDialog = true })
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("LAINNYA", color = colors.text1, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Column(modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp))) {
            SettingsItem(colors, iconBgColor, Icons.Default.Notifications, colors.red, "Notifikasi", null, trailing = { Switch(checked = true, onCheckedChange = { Toast.makeText(context, "Pengaturan notifikasi diperbarui", Toast.LENGTH_SHORT).show() }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = colors.blue)) }, onClick = { Toast.makeText(context, "Membuka notifikasi", Toast.LENGTH_SHORT).show() })
            Divider(color = colors.border, modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(colors, iconBgColor, Icons.Default.Info, colors.text1, "Bantuan & Dukungan", null, onClick = { Toast.makeText(context, "Menghubungi bantuan...", Toast.LENGTH_SHORT).show() })
            Divider(color = colors.border, modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(colors, iconBgColor, Icons.Default.Info, colors.text1, "Tentang Aplikasi", "Sapta Work v1.0", showArrow = true, onClick = { Toast.makeText(context, "Sapta Work v1.0 (Final Project)", Toast.LENGTH_LONG).show() })
        }
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth().height(55.dp).background(colors.red.copy(alpha = 0.05f), RoundedCornerShape(16.dp)).border(1.dp, colors.red.copy(alpha = 0.5f), RoundedCornerShape(16.dp)).clickable { onLogoutClick() }, contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Keluar", tint = colors.red, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Keluar", color = colors.red, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("PADEPOKAN 79", color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(130.dp))
    }
}

@Composable
fun SettingsItem(colors: P79Colors, iconBgColor: Color, icon: ImageVector, iconTint: Color, title: String, subtitle: String?, showArrow: Boolean = true, trailing: @Composable (() -> Unit)? = null, onClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(36.dp).background(iconBgColor, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp)) }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = colors.text0, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle.ifBlank { "Tidak tersedia" }, color = colors.text1, fontSize = 12.sp)
            }
        }
        if (trailing != null) { trailing() } else if (showArrow) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = colors.text1, modifier = Modifier.size(20.dp)) }
    }
}

@Composable
fun LaporanContent(colors: P79Colors, isDarkMode: Boolean, currentUser: User?, reports: List<WorkingReport>, onBellClick: () -> Unit, onAddClick: () -> Unit) {
    val cardBgColor = if (isDarkMode) Color(0xFF161D2F) else Color.White
    var selectedTab by remember { mutableStateOf("Semua") }
    val filteredReports = when (selectedTab) {
        "Disetujui" -> reports.filter { it.status.equals("APPROVED", true) || it.status.equals("REVIEWED", true) }
        "Menunggu" -> reports.filter { it.status.equals("PENDING", true) || it.status.equals("SUBMITTED", true) }
        "Revisi" -> reports.filter { it.status.equals("REVISI", true) || it.status.equals("REVISION", true) }
        else -> reports
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(48.dp))
            TopBar(colors = colors, isDarkMode = isDarkMode, currentUser = currentUser, onBellClick = onBellClick)
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.padding(horizontal = 24.dp)) { Text("Laporan Kerja", color = colors.text0, fontSize = 24.sp, fontWeight = FontWeight.Bold); Text("Riwayat & pengajuan laporan harian", color = colors.text1, fontSize = 12.sp) }
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("Semua", "Disetujui", "Menunggu", "Revisi").forEach { tab ->
                    LaporanTab(text = tab, isSelected = selectedTab == tab, colors = colors, cardBgColor = cardBgColor, onClick = { selectedTab = tab })
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (filteredReports.isEmpty()) { EmptyState(colors = colors, cardBgColor = cardBgColor, message = "Tidak ada laporan tersedia") } else { filteredReports.forEach { report -> WorkingReportItem(colors = colors, cardBgColor = cardBgColor, report = report) } }
            }
            Spacer(modifier = Modifier.height(130.dp))
        }
        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 100.dp).size(60.dp).clip(RoundedCornerShape(20.dp)).background(Brush.linearGradient(listOf(colors.blue, colors.green))).clickable { onAddClick() }, contentAlignment = Alignment.Center) { Text("+", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Light, modifier = Modifier.padding(bottom = 6.dp)) }
    }
}

@Composable
fun WorkingReportItem(colors: P79Colors, cardBgColor: Color, report: WorkingReport) {
    Column(modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp)).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(report.title, color = colors.text0, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(report.date, color = colors.text1, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(report.description, color = colors.text1, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, null, tint = colors.blue, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("${report.startTime} - ${report.endTime}", color = colors.text1, fontSize = 12.sp)
            }
            Box(modifier = Modifier.background(colors.blue.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(report.status, color = colors.blue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun LaporanTab(text: String, isSelected: Boolean, colors: P79Colors, cardBgColor: Color, onClick: () -> Unit) {
    Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(if (isSelected) colors.blue.copy(alpha = 0.2f) else cardBgColor).border(1.dp, if (isSelected) colors.blue else colors.border, RoundedCornerShape(20.dp)).clickable { onClick() }.padding(horizontal = 20.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
        Text(text, color = if (isSelected) colors.blue else colors.text1, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TopBar(colors: P79Colors, isDarkMode: Boolean, currentUser: User?, onBellClick: () -> Unit) {
    val iconBgColor = if (isDarkMode) Color(0xFF161D2F) else Color.White
    val isHc = currentUser?.role == "HC"
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Halo, ${if (isHc) "Admin" else currentUser?.name?.split(" ")?.firstOrNull() ?: "Sobat"}", color = colors.text0, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(text = "Padepokan Tujuh Sembilan", color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(iconBgColor).border(1.dp, colors.border, CircleShape).clickable { onBellClick() }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Notifications, contentDescription = "Notifikasi", tint = colors.text1, modifier = Modifier.size(20.dp)); Box(modifier = Modifier.size(8.dp).background(colors.red, CircleShape).align(Alignment.TopEnd).padding(2.dp)) }
        Spacer(modifier = Modifier.width(12.dp))
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Brush.linearGradient(listOf(colors.blue, colors.green))), contentAlignment = Alignment.Center) {
            Text(currentUser?.name?.firstOrNull()?.toString() ?: "U", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun AbsenCard(colors: P79Colors, isDarkMode: Boolean, todayAttendance: Attendance?, hasActivePermission: Boolean, activity: AppCompatActivity) {
    val iconBgColor = if (isDarkMode) Color(0xFF222831) else Color(0xFFF3F4F6)
    val hasCheckedIn = todayAttendance != null
    val hasCheckedOut = todayAttendance?.checkOutTime?.isNotEmpty() == true

    // Proteksi: Tombol Terkunci Mati apabila user sedang izin hari ini
    val buttonEnabled = !hasCheckedOut && !hasActivePermission

    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
        GlassCard(colors = colors, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(iconBgColor, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    Icon(
                        if (hasActivePermission) Icons.Default.Info
                        else if (hasCheckedIn) Icons.Default.CheckCircle
                        else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (hasActivePermission) colors.amber else if (hasCheckedIn) colors.green else colors.amber,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        if (hasActivePermission) "Status: Sedang Izin Kerja"
                        else if (hasCheckedOut) "Sudah Selesai Kerja"
                        else if (hasCheckedIn) "Sedang Bekerja"
                        else "Belum Absen Masuk",
                        color = colors.text0, fontSize = 16.sp, fontWeight = FontWeight.Bold
                    )
                    Text("Kantor Pusat · radius 100m", color = colors.text1, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("MASUK", color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(todayAttendance?.checkInTime ?: "--:--", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(colors.border))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PULANG", color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(todayAttendance?.checkOutTime ?: "--:--", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            val buttonText = if (hasActivePermission) "TIDAK DAPAT ABSEN (SEDANG IZIN)" else if (hasCheckedOut) "SUDAH ABSEN PULANG" else if (hasCheckedIn) "ABSEN PULANG" else "ABSEN MASUK"

            Button(
                onClick = {
                    val intent = Intent(activity, CameraAbsenActivity::class.java)
                    intent.putExtra("type", if (!hasCheckedIn) "CHECK_IN" else "CHECK_OUT")
                    activity.startActivity(intent)
                },
                enabled = buttonEnabled,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Gray.copy(alpha = 0.2f)),
                contentPadding = PaddingValues(),
                modifier = Modifier.fillMaxWidth().height(55.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize().background(if (buttonEnabled) Brush.horizontalGradient(listOf(colors.blue, colors.green)) else SolidColor(Color.Transparent), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Text(buttonText, color = if (buttonEnabled) Color.White else colors.text1, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MenuCard(colors: P79Colors, cardBgColor: Color, title: String, iconColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(modifier = modifier.height(90.dp).background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp)).clickable { onClick() }, contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Info, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp)); Spacer(modifier = Modifier.height(8.dp)); Text(title, color = colors.text0, fontSize = 12.sp) } }
}

@Composable
fun SummaryCard(colors: P79Colors, cardBgColor: Color) {
    Box(modifier = Modifier.padding(horizontal = 24.dp)) { Column(modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp)).padding(20.dp)) { Text("RINGKASAN BULAN INI", color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp); Spacer(modifier = Modifier.height(16.dp)); Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { SummaryItem(colors = colors, count = "0", label = "Hadir", color = colors.green); SummaryItem(colors = colors, count = "0", label = "Izin", color = colors.amber); SummaryItem(colors = colors, count = "0", label = "Telat", color = colors.red); SummaryItem(colors = colors, count = "0", label = "Alpha", color = colors.blue) } } }
}

@Composable
fun SummaryItem(colors: P79Colors, count: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(count, color = color, fontSize = 24.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(4.dp)); Text(label, color = colors.text1, fontSize = 12.sp) }
}

@Composable
fun HistoryList(colors: P79Colors, cardBgColor: Color) {
    EmptyState(colors = colors, cardBgColor = cardBgColor, message = "Tidak ada riwayat terbaru")
}

@Composable
fun DockNavigationBar(colors: P79Colors, isDarkMode: Boolean, selectedIndex: Int, isHc: Boolean = false, onItemSelected: (Int) -> Unit, onAdminClick: () -> Unit = {}, modifier: Modifier = Modifier) {
    val dockBgColor = if (isDarkMode) Color(0xD9161D2F) else Color(0xD9FFFFFF)
    Row(modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        BoxWithConstraints(modifier = Modifier.weight(1f).height(64.dp).clip(RoundedCornerShape(32.dp)).background(dockBgColor).border(1.dp, colors.border, RoundedCornerShape(32.dp))) {
            val itemsCount = if (isHc) 4 else 3
            val itemWidth = maxWidth / itemsCount
            val indicatorOffset by animateDpAsState(targetValue = if (selectedIndex < itemsCount) itemWidth * selectedIndex else itemWidth * (itemsCount - 1), animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow), label = "")
            Box(modifier = Modifier.offset(x = indicatorOffset).width(itemWidth).fillMaxHeight().padding(6.dp).background(Brush.linearGradient(listOf(colors.blue, colors.green)), RoundedCornerShape(26.dp)))
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                BottomNavItem(colors = colors, icon = Icons.Default.Home, label = "Home", isSelected = selectedIndex == 0) { onItemSelected(0) }
                BottomNavItem(colors = colors, icon = Icons.Default.DateRange, label = "Riwayat", isSelected = selectedIndex == 1) { onItemSelected(1) }
                BottomNavItem(colors = colors, icon = Icons.Default.Edit, label = "Laporan", isSelected = selectedIndex == 2) { onItemSelected(2) }
                if (isHc) { BottomNavItem(colors = colors, icon = Icons.Default.Settings, label = "Admin", isSelected = selectedIndex == 4) { onAdminClick() } }
            }
        }
        Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(if (selectedIndex == 3) Brush.linearGradient(listOf(colors.blue, colors.green)) else SolidColor(dockBgColor)).border(1.dp, if (selectedIndex == 3) Color.Transparent else colors.border, CircleShape).clickable { onItemSelected(3) }, contentAlignment = Alignment.Center) { Icon(imageVector = Icons.Default.Person, contentDescription = "Profil", tint = if (selectedIndex == 3) Color.White else colors.text1, modifier = Modifier.size(26.dp)) }
    }
}

@Composable
fun BottomNavItem(colors: P79Colors, icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)) { Icon(icon, contentDescription = label, tint = if (isSelected) Color.White else colors.text1, modifier = Modifier.size(24.dp)); Spacer(modifier = Modifier.height(2.dp)); Text(label, color = if (isSelected) Color.White else colors.text1, fontSize = 9.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
}

@Composable
fun NotificationSheetContent(colors: P79Colors, isDarkMode: Boolean) {
    val cardBgColor = if (isDarkMode) Color(0xFF161D2F) else Color.White
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Notifikasi", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold); Text("Tandai semua dibaca", color = colors.blue, fontSize = 12.sp, fontWeight = FontWeight.Medium) }
        Spacer(modifier = Modifier.height(24.dp))
        EmptyState(colors = colors, cardBgColor = cardBgColor, message = "Tidak ada notifikasi baru")
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun EmptyState(colors: P79Colors, cardBgColor: Color, message: String) {
    Box(modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp)).padding(24.dp), contentAlignment = Alignment.Center) { Text(text = message, color = colors.text1, fontSize = 14.sp, textAlign = TextAlign.Center) }
}