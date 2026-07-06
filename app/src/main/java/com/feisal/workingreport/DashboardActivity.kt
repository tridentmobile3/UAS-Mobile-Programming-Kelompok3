package com.feisal.workingreport

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowCompat
import coil.compose.AsyncImage
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

            // Sync data when user profile is loaded
            LaunchedEffect(currentUser) {
                if (currentUser != null) {
                    workingReports = workingReportRepository.getMyReports()
                    attendanceHistory = attendanceRepository.getAttendanceHistory()
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
                                history = attendanceHistory,
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
                            onSubmitCallback = { type, reason, dateString, fileNameText, driveLink ->
                                coroutineScope.launch {
                                    currentUser?.let { user ->
                                        val result = permissionRepository.submitPermission(
                                            userId = user.id,
                                            employeeName = user.name,
                                            employeeNip = user.nip,
                                            type = type,
                                            reason = reason,
                                            date = dateString,
                                            fileNameText = fileNameText, // Menerima data teks nama file
                                            driveLink = driveLink
                                        )

                                        result.onSuccess {
                                            Toast.makeText(this@DashboardActivity, "Pengajuan (Teks) berhasil disimpan!", Toast.LENGTH_SHORT).show()
                                            refreshData()
                                        }.onFailure {
                                            Toast.makeText(this@DashboardActivity, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
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
                        onSubmit = { tanggal, judul, deskripsi, mulai, selesai, uri, fileName, mimeType ->
                            coroutineScope.launch {
                                val result = workingReportRepository.submitReport(
                                    startTime = mulai,
                                    endTime = selesai,
                                    workLocation = "WFO",
                                    title = judul,
                                    description = deskripsi,
                                    progress = "100%",
                                    obstacle = "-",
                                    nextPlan = "-",
                                    attachmentUri = uri,
                                    fileName = fileName ?: "",
                                    mimeType = mimeType ?: ""
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
    onSubmit: (String, String, String, String, String, Uri?, String?, String?) -> Unit
) {
    val context = LocalContext.current
    val inputBgColor = if (isDarkMode) Color(0xFF222B3C) else Color(0xFFF3F4F6)

    // Form Input States
    var tanggal by remember { mutableStateOf("") }
    var judul by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    var jamMulai by remember { mutableStateOf("") }
    var jamSelesai by remember { mutableStateOf("") }

    // Validation States
    var tanggalError by remember { mutableStateOf(false) }
    var judulError by remember { mutableStateOf(false) }
    var deskripsiError by remember { mutableStateOf(false) }
    var jamMulaiError by remember { mutableStateOf(false) }
    var jamSelesaiError by remember { mutableStateOf(false) }
    var waktuValidationError by remember { mutableStateOf<String?>(null) }

    // Attachment States
    var currentTab by remember { mutableStateOf("FILE") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedMimeType by remember { mutableStateOf<String?>(null) }
    var linkDriveLaporan by remember { mutableStateOf("") }

    // Dialog State
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Launcher File Picker
    val pickFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            selectedMimeType = context.contentResolver.getType(it)
            context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex != -1) {
                    selectedFileName = cursor.getString(nameIndex)
                }
            }
        }
    }

    // --- DIALOG KONFIRMASI ---
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Konfirmasi Laporan", fontWeight = FontWeight.Bold, color = colors.text0) },
            text = { Text("Apakah data yang Anda masukkan sudah sesuai dan benar?", color = colors.text1) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        val finalDesc = if (linkDriveLaporan.isNotEmpty()) "$deskripsi\nLink: $linkDriveLaporan" else deskripsi
                        onSubmit(tanggal, judul, finalDesc, jamMulai, jamSelesai, selectedFileUri, selectedFileName, selectedMimeType)
                    }
                ) {
                    Text("Sesuai", color = colors.blue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Batal", color = Color.Red)
                }
            },
            containerColor = if (isDarkMode) Color(0xFF1E2738) else Color.White
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text("Buat Laporan Kerja", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        // --- 1. FIELD TANGGAL ---
        Text("Tanggal", color = if (tanggalError) Color.Red else colors.text1, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        DatePickerBox(value = tanggal, onValueChange = { tanggal = it; tanggalError = false }, placeholder = "Pilih Tanggal Laporan", colors = colors, inputBgColor = inputBgColor)
        if (tanggalError) {
            Text("Tanggal tidak boleh kosong", color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 2. FIELD JUDUL AKTIVITAS ---
        Text("Judul Aktivitas", color = if (judulError) Color.Red else colors.text1, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = judul,
            onValueChange = { judul = it; judulError = false },
            textStyle = androidx.compose.ui.text.TextStyle(color = colors.text0),
            placeholder = { Text("Contoh: Pembuatan API Login Karyawan", color = colors.text1.copy(alpha = 0.6f)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            isError = judulError,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = if (judulError) Color.Red else colors.border,
                focusedBorderColor = colors.blue,
                unfocusedContainerColor = inputBgColor,
                focusedContainerColor = inputBgColor
            )
        )
        if (judulError) {
            Text("Judul aktivitas wajib diisi", color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. FIELD DESKRIPSI PEKERJAAN ---
        Text("Deskripsi Pekerjaan", color = if (deskripsiError) Color.Red else colors.text1, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = deskripsi,
            onValueChange = { deskripsi = it; deskripsiError = false },
            textStyle = androidx.compose.ui.text.TextStyle(color = colors.text0),
            placeholder = { Text("Rincian tugas atau fitur yang diselesaikan hari ini...", color = colors.text1.copy(alpha = 0.6f)) },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(12.dp),
            isError = deskripsiError,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = if (deskripsiError) Color.Red else colors.border,
                focusedBorderColor = colors.blue,
                unfocusedContainerColor = inputBgColor,
                focusedContainerColor = inputBgColor
            )
        )
        if (deskripsiError) {
            Text("Deskripsi pekerjaan tidak boleh kosong", color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 4. FIELD DURASI KERJA ---
        Text("Durasi Kerja", color = if (jamMulaiError || jamSelesaiError || waktuValidationError != null) Color.Red else colors.text1, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TimePickerBox(value = jamMulai, onValueChange = { jamMulai = it; jamMulaiError = false; waktuValidationError = null }, placeholder = "Jam Mulai", colors = colors, inputBgColor = inputBgColor, modifier = Modifier.weight(1f))
            TimePickerBox(value = jamSelesai, onValueChange = { jamSelesai = it; jamSelesaiError = false; waktuValidationError = null }, placeholder = "Jam Selesai", colors = colors, inputBgColor = inputBgColor, modifier = Modifier.weight(1f))
        }

        if (jamMulaiError || jamSelesaiError) {
            Text("Jam mulai dan jam selesai wajib diisi", color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
        } else if (waktuValidationError != null) {
            Text(waktuValidationError!!, color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- 5. SEKSI LAMPIRAN ---
        Text("Lampiran (opsional)", color = colors.text1, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val sections = listOf("FILE" to "📄 File", "FOTO" to "📷 Foto", "LINK" to "🔗 Link Drive")
            sections.forEach { (type, label) ->
                val isSelected = currentTab == type
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (isSelected) colors.blue else inputBgColor, RoundedCornerShape(8.dp))
                        .border(1.dp, if (isSelected) colors.blue else colors.border, RoundedCornerShape(8.dp))
                        .clickable {
                            currentTab = type
                            if (type != "LINK") linkDriveLaporan = "" else { selectedFileName = null; selectedFileUri = null }
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, color = if (isSelected) Color.White else colors.text1, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- LOGIKA PRATINJAU (PREVIEW) BERKAS SECARA VISUAL ---
        if (currentTab == "LINK") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .background(inputBgColor, RoundedCornerShape(12.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                OutlinedTextField(
                    value = linkDriveLaporan,
                    onValueChange = { linkDriveLaporan = it },
                    textStyle = androidx.compose.ui.text.TextStyle(color = colors.text0),
                    placeholder = { Text("Paste link berkas Google Drive di sini...", color = colors.text1.copy(alpha = 0.5f), fontSize = 13.sp) },
                    modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent
                    )
                )
            }
        } else {
            // Drop Area yang diperluas tingginya jika ada gambar preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .minimumInteractiveComponentSize()
                    .background(inputBgColor, RoundedCornerShape(12.dp))
                    .border(1.dp, if (selectedFileName != null) colors.green else colors.border, RoundedCornerShape(12.dp))
                    .clickable {
                        if (currentTab == "FILE") pickFileLauncher.launch("*/*")
                        if (currentTab == "FOTO") pickFileLauncher.launch("image/*")
                    }
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (selectedFileName != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // --- LOGIKA MENAMPILKAN PRATINJAU FOTO / ICON FILE ---
                        if (selectedMimeType?.startsWith("image/") == true && selectedFileUri != null) {
                            // Menampilkan Thumbnail Foto asli menggunakan Coil
                            AsyncImage(
                                model = selectedFileUri,
                                contentDescription = "Pratinjau Gambar",
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.LightGray),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Menampilkan Ikon Dokumen Default untuk tipe data non-gambar (PDF, Docx, dll)
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(colors.blue.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "File Dokumen",
                                    tint = colors.blue,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Informasi Nama Berkas
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (selectedMimeType?.startsWith("image/") == true) "📸 Gambar Terpilih:" else "📄 File Terpilih:",
                                color = colors.text1, fontSize = 11.sp
                            )
                            Text(
                                text = selectedFileName!!,
                                color = colors.green, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                maxLines = 2, overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Tombol Hapus Lampiran
                        IconButton(onClick = {
                            selectedFileName = null
                            selectedFileUri = null
                            selectedMimeType = null
                        }) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                                contentDescription = "Hapus Berkas",
                                tint = Color.Red.copy(alpha = 0.8f)
                            )
                        }
                    }
                } else {
                    val placeholderHint = if (currentTab == "FILE") "Ketuk untuk memilih berkas (.pdf/.doc/.docx)" else "Ketuk untuk memilih foto dari galeri"
                    Text(
                        placeholderHint,
                        color = colors.text1.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // --- BUTTON SUBMIT + VALIDATION CHECK ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .background(Brush.horizontalGradient(listOf(colors.blue, colors.green)), RoundedCornerShape(12.dp))
                .clickable {
                    // Validasi Field
                    tanggalError = tanggal.isEmpty()
                    judulError = judul.isEmpty()
                    deskripsiError = deskripsi.isEmpty()
                    jamMulaiError = jamMulai.isEmpty()
                    jamSelesaiError = jamSelesai.isEmpty()

                    if (tanggalError || judulError || deskripsiError || jamMulaiError || jamSelesaiError) {
                        Toast.makeText(context, "Mohon isi semua field yang bertanda merah!", Toast.LENGTH_SHORT).show()
                        return@clickable
                    }

                    // Validasi Logika Waktu
                    try {
                        val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                        val dateMulai = sdf.parse(jamMulai)
                        val dateSelesai = sdf.parse(jamSelesai)

                        if (dateMulai != null && dateSelesai != null) {
                            if (dateMulai.time >= dateSelesai.time) {
                                waktuValidationError = "Jam mulai tidak boleh lebih besar atau sama dengan jam selesai!"
                                return@clickable
                            }

                            val diffMillis = dateSelesai.time - dateMulai.time
                            if (diffMillis > 8 * 60 * 60 * 1000) {
                                waktuValidationError = "Durasi kerja maksimal adalah 8 jam kerja!"
                                return@clickable
                            }
                        }
                    } catch (e: Exception) {
                        waktuValidationError = "Format penulisan waktu tidak valid."
                        return@clickable
                    }

                    showConfirmDialog = true
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
    var selectedAttendance by remember { mutableStateOf<Attendance?>(null) }
    var showDetailSheet by remember { mutableStateOf(false) }

    // Statistics Calculations
    val totalHadir = history.count { it.status.uppercase() == "HADIR" }
    val totalIzin = history.count { it.status.uppercase() == "IZIN" || it.status.uppercase() == "SAKIT" }
    val totalTerlambat = history.count { it.status.uppercase() == "TERLAMBAT" }
    val totalAlpha = history.count { it.status.uppercase() == "ALPHA" }

    // Rata-rata Masuk Calculation
    val averageCheckIn = remember(history) {
        if (history.isEmpty()) "--"
        else {
            val checkInTimes = history.filter { it.checkInTime.isNotEmpty() }
            if (checkInTimes.isEmpty()) "--"
            else {
                val totalMinutes = checkInTimes.sumOf { item ->
                    try {
                        val timeStr = item.checkInTime
                        val parts = timeStr.split(":")
                        if (parts.size >= 2) {
                            parts[0].toInt() * 60 + parts[1].toInt()
                        } else 0
                    } catch (e: Exception) {
                        0
                    }
                }
                val avgMinutes = totalMinutes / checkInTimes.size
                String.format("%02d:%02d", avgMinutes / 60, avgMinutes % 60)
            }
        }
    }

    // Sort history by createdAt DESC
    val sortedHistory = remember(history) {
        history.sortedByDescending { it.createdAt }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(64.dp)) // Added space for status bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(cardBgColor).clickable { onBackClick() }, contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.text0, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(currentUser?.name ?: "Karyawan", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(currentUser?.nip ?: "-", color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Box(modifier = Modifier.size(40.dp).background(Brush.linearGradient(listOf(colors.blue, colors.green)), CircleShape), contentAlignment = Alignment.Center) {
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
            val currentMonth = remember { SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(Date()) }
            Text(currentMonth, color = colors.text0, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Box(modifier = Modifier.size(36.dp).background(cardBgColor, RoundedCornerShape(8.dp)).border(1.dp, colors.border, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = colors.text1)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(modifier = Modifier.weight(1f), bg = cardBgColor, border = colors.border, title = totalHadir.toString(), subtitle = "HADIR", iconTint = colors.green, textColor = colors.text0)
            StatCard(modifier = Modifier.weight(1f), bg = cardBgColor, border = colors.border, title = averageCheckIn, subtitle = "RATA² MASUK", iconTint = colors.blue, textColor = colors.text0)
            StatCard(modifier = Modifier.weight(1f), bg = cardBgColor, border = colors.border, title = totalTerlambat.toString(), subtitle = "TERLAMBAT", iconTint = colors.red, textColor = colors.text0)
            StatCard(modifier = Modifier.weight(1f), bg = cardBgColor, border = colors.border, title = totalIzin.toString(), subtitle = "IZIN/SAKIT", iconTint = colors.amber, textColor = colors.text0)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Column(modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp)).padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf("S", "S", "R", "K", "J", "S", "M").forEach { day ->
                    Text(day, color = colors.text1, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            // Calendar Grid coloring logic
            val attendanceMap = history.associateBy { it.date }
            val calendar = java.util.Calendar.getInstance()
            val currentYear = calendar.get(java.util.Calendar.YEAR)
            val currentMonth = calendar.get(java.util.Calendar.MONTH)
            
            val rows = 5
            val cols = 7
            for (i in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    for (j in 0 until cols) {
                        val dayIndex = i * 7 + j + 1
                        var dayColor = innerCardBgColor
                        
                        if (dayIndex <= 31) {
                            val dateStr = String.format("%04d-%02d-%02d", currentYear, currentMonth + 1, dayIndex)
                            val att = attendanceMap[dateStr]
                            if (att != null) {
                                dayColor = when(att.status.uppercase()) {
                                    "HADIR" -> colors.green
                                    "TERLAMBAT" -> colors.red
                                    "IZIN", "SAKIT" -> colors.amber
                                    "CUTI" -> colors.blue
                                    "ALPHA" -> Color.Gray
                                    else -> innerCardBgColor
                                }
                            }
                        }
                        
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
                LegendItem("Cuti", colors.blue, colors.text1)
                Spacer(modifier = Modifier.width(16.dp))
                LegendItem("Alpha", Color.Gray, colors.text1)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // SEKSI HISTORI PENGAJUAN IZIN
        if (permissionHistory.isNotEmpty()) {
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
                    if (izin.fileName.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("📁 Lampiran: ${izin.fileName}", color = colors.text1, fontSize = 12.sp)
                    }
                    if (izin.driveLink.isNotEmpty()) {
                        Text(
                            text = "🔗 Link Drive",
                            color = colors.green,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(izin.driveLink))
                                    composeContext.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(composeContext, "Gagal membuka link", Toast.LENGTH_SHORT).show()
                                }
                            }.padding(vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Status: ${izin.status}", color = colors.blue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // SEKSI HISTORI KEHADIRAN (ABSEN)
        Text("HISTORI KEHADIRAN", color = colors.text1, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))

        if (sortedHistory.isEmpty()) {
            EmptyState(colors = colors, cardBgColor = cardBgColor, message = "Tidak ada riwayat absensi")
        } else {
            sortedHistory.forEach { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .background(cardBgColor, RoundedCornerShape(16.dp))
                        .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                        .clickable {
                            selectedAttendance = item
                            showDetailSheet = true
                        }
                        .padding(16.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(item.date, color = colors.text0, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Box(modifier = Modifier.background(
                            when(item.status.uppercase()) {
                                "HADIR" -> colors.green.copy(alpha = 0.1f)
                                "TERLAMBAT" -> colors.red.copy(alpha = 0.1f)
                                "IZIN", "SAKIT" -> colors.amber.copy(alpha = 0.1f)
                                else -> colors.blue.copy(alpha = 0.1f)
                            }, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text(item.status.uppercase(), color = when(item.status.uppercase()) {
                                "HADIR" -> colors.green
                                "TERLAMBAT" -> colors.red
                                "IZIN", "SAKIT" -> colors.amber
                                else -> colors.blue
                            }, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Masuk", color = colors.text1, fontSize = 10.sp)
                            Text(item.checkInTime.ifBlank { "--:--" }, color = colors.text0, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Pulang", color = colors.text1, fontSize = 10.sp)
                            Text(item.checkOutTime.ifBlank { "Belum Check Out" }, color = if(item.checkOutTime.isEmpty()) colors.red else colors.text0, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(130.dp))
    }

    if (showDetailSheet && selectedAttendance != null) {
        ModalBottomSheet(
            onDismissRequest = { showDetailSheet = false },
            containerColor = if (isDarkMode) Color(0xFF0F172A) else Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle(color = colors.border) }
        ) {
            AttendanceDetailSheet(attendance = selectedAttendance!!, colors = colors)
        }
    }
}

@Composable
fun AttendanceDetailSheet(attendance: Attendance, colors: P79Colors) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Detail Absensi", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        DetailItem(label = "Tanggal", value = attendance.date, colors = colors)
        DetailItem(label = "Status", value = attendance.status, colors = colors)
        
        Spacer(modifier = Modifier.height(8.dp))
        Text("CHECK IN", color = colors.blue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        DetailItem(label = "Waktu Masuk", value = attendance.checkInTime.ifBlank { "-" }, colors = colors)
        DetailItem(label = "Latitude", value = attendance.checkInLatitude.toString(), colors = colors)
        DetailItem(label = "Longitude", value = attendance.checkInLongitude.toString(), colors = colors)
        DetailItem(label = "Akurasi", value = "${attendance.checkInAccuracy}m", colors = colors)
        DetailItem(label = "Jarak ke Kantor", value = "${attendance.checkInDistance}m", colors = colors)

        Spacer(modifier = Modifier.height(8.dp))
        Text("CHECK OUT", color = colors.red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        DetailItem(label = "Waktu Pulang", value = attendance.checkOutTime.ifBlank { "-" }, colors = colors)
        DetailItem(label = "Latitude", value = attendance.checkOutLatitude.toString(), colors = colors)
        DetailItem(label = "Longitude", value = attendance.checkOutLongitude.toString(), colors = colors)
        DetailItem(label = "Akurasi", value = "${attendance.checkOutAccuracy}m", colors = colors)
        DetailItem(label = "Jarak ke Kantor", value = "${attendance.checkOutDistance}m", colors = colors)

        Spacer(modifier = Modifier.height(8.dp))
        Text("LAINNYA", color = colors.text1, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        DetailItem(label = "Sumber Perangkat", value = attendance.source, colors = colors)
        DetailItem(label = "Verifikasi Wajah", value = if (attendance.faceVerified) "Berhasil" else "Tidak Dilakukan", colors = colors)

        Spacer(modifier = Modifier.height(32.dp))
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
    history: List<Attendance>,
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
        SummaryCard(colors = colors, cardBgColor = cardBgColor, history = history)
        Spacer(modifier = Modifier.height(24.dp))
        Text("RIWAYAT TERBARU", color = colors.text1, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(modifier = Modifier.height(12.dp))
        HistoryList(colors = colors, cardBgColor = cardBgColor, history = history)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanContent(
    colors: P79Colors,
    isDarkMode: Boolean,
    currentUser: User?,
    reports: List<WorkingReport>,
    onBellClick: () -> Unit,
    onAddClick: () -> Unit
) {
    val cardBgColor = if (isDarkMode) Color(0xFF161D2F) else Color.White
    var selectedTab by remember { mutableStateOf("Semua") }
    var selectedReport by remember { mutableStateOf<WorkingReport?>(null) }
    var showDetailSheet by remember { mutableStateOf(false) }

    // Sort reports by createdAt DESC
    val sortedReports = remember(reports) {
        reports.sortedByDescending { it.createdAt }
    }

    val filteredReports = when (selectedTab) {
        "Disetujui" -> sortedReports.filter { it.status.equals("APPROVED", true) || it.status.equals("REVIEWED", true) }
        "Menunggu" -> sortedReports.filter { it.status.equals("PENDING", true) || it.status.equals("SUBMITTED", true) }
        "Revisi" -> sortedReports.filter { it.status.equals("REVISI", true) || it.status.equals("REVISION", true) || it.status.equals("REJECTED", true) }
        else -> sortedReports
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Header Row: Title on Left, Icons on Right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title
                Text(
                    text = "Laporan Kerja",
                    color = colors.text0,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                // Notification Icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isDarkMode) Color(0xFF161D2F) else Color.White)
                        .border(1.dp, colors.border, CircleShape)
                        .clickable { onBellClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = colors.text1,
                        modifier = Modifier.size(20.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(colors.red, CircleShape)
                            .align(Alignment.TopEnd)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Avatar
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(colors.blue, colors.green))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentUser?.name?.firstOrNull()?.toString() ?: "A",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Subtitle
            Text(
                text = "Riwayat & pengajuan laporan kerja",
                color = colors.text1,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Divider
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp),
                color = colors.border,
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("Semua", "Disetujui", "Menunggu", "Revisi").forEach { tab ->
                    LaporanTab(text = tab, isSelected = selectedTab == tab, colors = colors, cardBgColor = cardBgColor, onClick = { selectedTab = tab })
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Reports List
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (filteredReports.isEmpty()) {
                    EmptyState(colors = colors, cardBgColor = cardBgColor, message = "Tidak ada laporan tersedia")
                } else {
                    filteredReports.forEach { report ->
                        WorkingReportItem(
                            colors = colors,
                            cardBgColor = cardBgColor,
                            report = report,
                            onClick = {
                                selectedReport = report
                                showDetailSheet = true
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(130.dp))
        }

        // Floating Action Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 100.dp)
                .size(60.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(colors.blue, colors.green)))
                .clickable { onAddClick() },
            contentAlignment = Alignment.Center
        ) {
            Text("+", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Light, modifier = Modifier.padding(bottom = 6.dp))
        }
    }

    if (showDetailSheet && selectedReport != null) {
        ModalBottomSheet(
            onDismissRequest = { showDetailSheet = false },
            containerColor = if (isDarkMode) Color(0xFF0F172A) else Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle(color = colors.border) }
        ) {
            WorkingReportDetailSheet(
                report = selectedReport!!,
                colors = colors,
                isDarkMode = isDarkMode
            )
        }
    }
}

@Composable
fun WorkingReportItem(colors: P79Colors, cardBgColor: Color, report: WorkingReport, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(cardBgColor, RoundedCornerShape(16.dp))
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${report.progress}%", color = colors.green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.background(colors.blue.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    val statusText = when(report.status.uppercase()) {
                        "SUBMITTED" -> "Menunggu"
                        "PENDING" -> "Menunggu"
                        "APPROVED" -> "Disetujui"
                        "REVIEWED" -> "Disetujui"
                        "REVISI" -> "Revisi"
                        "REVISION" -> "Revisi"
                        "REJECTED" -> "Revisi"
                        else -> report.status
                    }
                    Text(statusText, color = colors.blue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun WorkingReportDetailSheet(report: WorkingReport, colors: P79Colors, isDarkMode: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Detail Laporan Kerja", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        DetailItem(label = "Judul", value = report.title, colors = colors)
        DetailItem(label = "Tanggal", value = report.date, colors = colors)
        DetailItem(label = "Jam Kerja", value = "${report.startTime} - ${report.endTime}", colors = colors)
        DetailItem(label = "Deskripsi", value = report.description, colors = colors)
        DetailItem(label = "Status", value = report.status, colors = colors)
        DetailItem(label = "Lampiran", value = report.fileName.ifEmpty { "-" }, colors = colors)

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun DetailItem(label: String, value: String, colors: P79Colors) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(label.uppercase(), color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = colors.text0, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = colors.border.copy(alpha = 0.5f), thickness = 0.5.dp)
    }
}

@Composable
fun LaporanTab(text: String, isSelected: Boolean, colors: P79Colors, cardBgColor: Color, onClick: () -> Unit) {
    Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(if (isSelected) colors.blue.copy(alpha = 0.2f) else cardBgColor).border(1.dp, if (isSelected) colors.blue else colors.border, RoundedCornerShape(20.dp)).clickable { onClick() }.padding(horizontal = 20.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
        Text(text, color = if (isSelected) colors.blue else colors.text1, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
@Composable
fun SimpleTopBar(
    colors: P79Colors,
    isDarkMode: Boolean,
    currentUser: User?,
    onBellClick: () -> Unit
) {
    val iconBgColor =
        if (isDarkMode) Color(0xFF161D2F)
        else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBgColor)
                .border(
                    1.dp,
                    colors.border,
                    CircleShape
                )
                .clickable {
                    onBellClick()
                },
            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notification",
                tint = colors.text1,
                modifier = Modifier.size(20.dp)
            )

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        colors.red,
                        CircleShape
                    )
                    .align(Alignment.TopEnd)
            )

        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            colors.blue,
                            colors.green
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = currentUser?.name
                    ?.firstOrNull()
                    ?.toString() ?: "U",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

        }

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
fun SummaryCard(colors: P79Colors, cardBgColor: Color, history: List<Attendance>) {
    val totalHadir = history.count { it.status.uppercase() == "HADIR" }
    val totalIzin = history.count { it.status.uppercase() == "IZIN" || it.status.uppercase() == "SAKIT" }
    val totalTerlambat = history.count { it.status.uppercase() == "TERLAMBAT" }
    val totalAlpha = history.count { it.status.uppercase() == "ALPHA" }

    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
        Column(modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp)).padding(20.dp)) {
            Text("RINGKASAN BULAN INI", color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryItem(colors = colors, count = totalHadir.toString(), label = "Hadir", color = colors.green)
                SummaryItem(colors = colors, count = totalIzin.toString(), label = "Izin", color = colors.amber)
                SummaryItem(colors = colors, count = totalTerlambat.toString(), label = "Telat", color = colors.red)
                SummaryItem(colors = colors, count = totalAlpha.toString(), label = "Alpha", color = colors.blue)
            }
        }
    }
}

@Composable
fun SummaryItem(colors: P79Colors, count: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(count, color = color, fontSize = 24.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(4.dp)); Text(label, color = colors.text1, fontSize = 12.sp) }
}

@Composable
fun HistoryList(colors: P79Colors, cardBgColor: Color, history: List<Attendance>) {
    if (history.isEmpty()) {
        EmptyState(colors = colors, cardBgColor = cardBgColor, message = "Tidak ada riwayat terbaru")
    } else {
        Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            history.take(3).forEach { item ->
                Row(modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.date, color = colors.text0, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("${item.checkInTime} - ${item.checkOutTime.ifBlank { "..." }}", color = colors.text1, fontSize = 12.sp)
                    }
                    Box(modifier = Modifier.background(
                        when(item.status.uppercase()) {
                            "HADIR" -> colors.green.copy(alpha = 0.1f)
                            "TERLAMBAT" -> colors.red.copy(alpha = 0.1f)
                            else -> colors.blue.copy(alpha = 0.1f)
                        }, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(item.status.uppercase(), color = when(item.status.uppercase()) {
                            "HADIR" -> colors.green
                            "TERLAMBAT" -> colors.red
                            else -> colors.blue
                        }, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DockNavigationBar(colors: P79Colors, isDarkMode: Boolean, selectedIndex: Int, isHc: Boolean = false, onItemSelected: (Int) -> Unit, onAdminClick: () -> Unit = {}, modifier: Modifier = Modifier) {
    val dockBgColor = if (isDarkMode) Color(0xD9161D2F) else Color(0xD9FFFFFF)
    Row(modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        BoxWithConstraints(modifier = Modifier.weight(1f).height(64.dp).clip(RoundedCornerShape(32.dp)).background(dockBgColor).border(1.dp, colors.border, RoundedCornerShape(32.dp))) {
            val itemsCount = if (isHc) 4 else 3
            val itemWidth = maxWidth / itemsCount
            
            // Perbaikan: Indikator hanya muncul jika salah satu menu di dalam bar ini yang aktif
            val showIndicator = (selectedIndex < itemsCount)
            
            val indicatorOffset by animateDpAsState(
                targetValue = if (showIndicator) itemWidth * selectedIndex else 0.dp, 
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow), 
                label = ""
            )
            
            if (showIndicator) {
                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset)
                        .width(itemWidth)
                        .fillMaxHeight()
                        .padding(6.dp)
                        .background(Brush.linearGradient(listOf(colors.blue, colors.green)), RoundedCornerShape(26.dp))
                )
            }

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
