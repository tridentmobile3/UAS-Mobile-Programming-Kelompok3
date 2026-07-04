package com.feisal.workingreport
import androidx.compose.foundation.interaction.MutableInteractionSource
import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.feisal.workingreport.model.Attendance
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
import java.util.Calendar
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
            val pagerState = rememberPagerState(pageCount = { 5 })
            val coroutineScope = rememberCoroutineScope()

            var currentUser by remember { mutableStateOf<User?>(null) }
            var todayAttendance by remember { mutableStateOf<Attendance?>(null) }
            var attendanceHistory by remember { mutableStateOf<List<Attendance>>(emptyList()) }
            var workingReports by remember { mutableStateOf<List<WorkingReport>>(emptyList()) }

            LaunchedEffect(Unit) {
                try {
                    currentUser = authRepository.getCurrentUserProfile()
                    if (currentUser != null) {
                        todayAttendance = attendanceRepository.getTodayAttendance()
                        attendanceHistory = attendanceRepository.getAttendanceHistory()
                        workingReports = workingReportRepository.getMyReports()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
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
                            0 -> DashHomeContent(
                                colors = colors,
                                isDarkMode = isDarkMode,
                                activity = this@DashboardActivity,
                                currentUser = currentUser,
                                todayAttendance = todayAttendance,
                                onLaporClick = { coroutineScope.launch { pagerState.animateScrollToPage(2) } },
                                onRiwayatClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                                onIzinClick = { showIzinSheet = true },
                                onLemburClick = { coroutineScope.launch { pagerState.animateScrollToPage(4) } },
                                onBellClick = { showNotificationSheet = true }
                            )
                            1 -> DashRiwayatContent(
                                colors = colors,
                                isDarkMode = isDarkMode,
                                currentUser = currentUser,
                                history = attendanceHistory,
                                onBackClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
                            )
                            2 -> DashLaporanContent(
                                colors = colors,
                                isDarkMode = isDarkMode,
                                currentUser = currentUser,
                                reports = workingReports,
                                onBellClick = { showNotificationSheet = true },
                                onAddClick = { showLaporanSheet = true }
                            )
                            3 -> DashProfilContent(
                                colors = colors,
                                isDarkMode = isDarkMode,
                                currentUser = currentUser,
                                onThemeChange = { isDark ->
                                    isDarkMode = isDark
                                    sharedPref.edit().putBoolean("isDarkMode", isDark).apply()
                                },
                                onLogoutClick = {
                                    try { authRepository.logout() } catch (e: Exception) { e.printStackTrace() }
                                    startActivity(Intent(this@DashboardActivity, LoginActivity::class.java))
                                    finish()
                                },
                                onBackClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
                            )
                            // Jika kamu punya LemburContent bawaan, biarkan memanggil yang asli
                            4 -> LemburContent(
                                colors = colors,
                                isDarkMode = isDarkMode,
                                onBackClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                                onAjukanClick = { showLemburSheet = true }
                            )
                        }
                    }
                }

                DashDockNavigationBar(
                    colors = colors,
                    isDarkMode = isDarkMode,
                    selectedIndex = if (pagerState.currentPage == 4) 0 else pagerState.currentPage,
                    onItemSelected = { index -> coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            if (showNotificationSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showNotificationSheet = false },
                    sheetState = sheetState,
                    containerColor = if (isDarkMode) Color(0xFF161D2F) else Color.White,
                    scrimColor = Color.Black.copy(alpha = 0.5f)
                ) { DashNotificationSheetContent(colors = colors) }
            }

            if (showIzinSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showIzinSheet = false },
                    sheetState = sheetState,
                    containerColor = Color(0xFF161D2F),
                    scrimColor = Color.Black.copy(alpha = 0.5f)
                ) { DashIzinBottomSheet(colors = colors, isDarkMode = isDarkMode, onDismiss = { showIzinSheet = false }) }
            }

            if (showLaporanSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showLaporanSheet = false },
                    sheetState = sheetState,
                    containerColor = Color(0xFF161D2F),
                    scrimColor = Color.Black.copy(alpha = 0.5f)
                ) { DashLaporanBottomSheet(colors = colors, isDarkMode = isDarkMode, onDismiss = { showLaporanSheet = false }) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashIzinBottomSheet(colors: P79Colors, isDarkMode: Boolean, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val inputBgColor = if (isDarkMode) Color(0xFF222831) else Color(0xFFF3F4F6)
    val jenisIzinList = listOf("Cuti", "Sakit", "Keperluan Pribadi", "Lainnya")

    var expanded by remember { mutableStateOf(false) }
    var selectedJenisIzin by remember { mutableStateOf(jenisIzinList[0]) }
    var keterangan by remember { mutableStateOf("") }
    var currentTab by remember { mutableStateOf("FILE") }
    var linkDrive by remember { mutableStateOf("") }
    var uploadHintText by remember { mutableStateOf("Pilih dokumen PDF/DOC ") }
    var isUploadSuccess by remember { mutableStateOf(false) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }

    val pickFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) { selectedFileUri = uri; uploadHintText = "File berhasil dilampirkan! ✅"; isUploadSuccess = true }
    }
    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) { selectedFileUri = uri; uploadHintText = "Foto berhasil dilampirkan! ✅"; isUploadSuccess = true }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Box(modifier = Modifier.width(40.dp).height(4.dp).background(Color(0xFF4A5568), RoundedCornerShape(2.dp)).align(Alignment.CenterHorizontally))
        Text("Ajukan Izin", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 24.dp, bottom = 24.dp))
        Text("Jenis Izin", color = Color(0xFF8B95A5), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = selectedJenisIzin,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.blue,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = inputBgColor,
                    unfocusedContainerColor = inputBgColor,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.menuAnchor().fillMaxWidth().padding(bottom = 16.dp)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(inputBgColor)
            ) {
                jenisIzinList.forEach { selectionOption ->
                    DropdownMenuItem(text = { Text(selectionOption, color = Color.White) }, onClick = { selectedJenisIzin = selectionOption; expanded = false })
                }
            }
        }

        Text("Keterangan", color = Color(0xFF8B95A5), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = keterangan,
            onValueChange = { keterangan = it },
            placeholder = { Text("Tulis alasan izin...", color = Color(0xFF4A5568)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = inputBgColor,
                unfocusedContainerColor = inputBgColor,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth().height(100.dp).padding(bottom = 16.dp).drawBehind {
                drawRoundRect(color = Color(0xFF4A5568), style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)), cornerRadius = CornerRadius(8.dp.toPx()))
            }
        )

        Text("Upload Bukti", color = Color(0xFF8B95A5), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            DashTabCard("📄 File", currentTab == "FILE", Modifier.weight(1f).padding(end = 4.dp)) { currentTab = "FILE"; uploadHintText = "Pilih dokumen PDF/DOC "; isUploadSuccess = false }
            DashTabCard("📷 Foto", currentTab == "FOTO", Modifier.weight(1f).padding(horizontal = 4.dp)) { currentTab = "FOTO"; uploadHintText = "Pilih foto bukti dari Galeri "; isUploadSuccess = false }
            DashTabCard("🔗 Link", currentTab == "LINK", Modifier.weight(1f).padding(start = 4.dp)) { currentTab = "LINK"; isUploadSuccess = false }
        }

        Box(
            modifier = Modifier.fillMaxWidth().height(80.dp).padding(bottom = 24.dp)
                .drawBehind { drawRoundRect(color = Color(0xFF4A5568), style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)), cornerRadius = CornerRadius(8.dp.toPx())) }
                .clickable { when (currentTab) { "FILE" -> pickFileLauncher.launch("application/*"); "FOTO" -> pickImageLauncher.launch("image/*") } },
            contentAlignment = Alignment.Center
        ) {
            if (currentTab == "LINK") {
                TextField(
                    value = linkDrive, onValueChange = { linkDrive = it },
                    placeholder = { Text("Tempel link Drive...", color = Color(0xFF4A5568)) },
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(text = buildAnnotatedString {
                    if (isUploadSuccess) { withStyle(SpanStyle(color = Color(0xFF2ECC71))) { append(uploadHintText) } }
                    else { withStyle(SpanStyle(color = Color(0xFF8B95A5))) { append(uploadHintText) }; withStyle(SpanStyle(color = Color(0xFF3498DB))) { append("di sini") } }
                }, fontSize = 12.sp, textAlign = TextAlign.Center)
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth().height(55.dp).clip(RoundedCornerShape(8.dp)).background(Brush.horizontalGradient(listOf(Color(0xFF3498DB), Color(0xFF2980B9))))
                .clickable {
                    if (keterangan.isEmpty()) Toast.makeText(context, "Keterangan tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                    else { Toast.makeText(context, "Pengajuan Berhasil Dikirim!", Toast.LENGTH_LONG).show(); onDismiss() }
                },
            contentAlignment = Alignment.Center
        ) { Text("Kirim Pengajuan", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun DashLaporanBottomSheet(colors: P79Colors, isDarkMode: Boolean, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    var tanggal by remember { mutableStateOf("") }
    var judul by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    var jamMulai by remember { mutableStateOf("") }
    var jamSelesai by remember { mutableStateOf("") }
    var currentTab by remember { mutableStateOf("FILE") }
    var linkDrive by remember { mutableStateOf("") }
    var uploadHintPrefix by remember { mutableStateOf("Tarik file/screenshot di sini atau ") }
    var uploadHintAction by remember { mutableStateOf("pilih file") }
    var isUploadSuccess by remember { mutableStateOf(false) }

    val datePicker = DatePickerDialog(context, { _, y, m, d -> calendar.set(y, m, d); tanggal = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(calendar.time) }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
    val timeStart = TimePickerDialog(context, { _, h, m -> jamMulai = String.format(Locale.getDefault(), "%02d:%02d", h, m) }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
    val timeEnd = TimePickerDialog(context, { _, h, m -> jamSelesai = String.format(Locale.getDefault(), "%02d:%02d", h, m) }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Box(modifier = Modifier.width(40.dp).height(4.dp).background(Color(0xFF4A5568), RoundedCornerShape(2.dp)).align(Alignment.CenterHorizontally))
        Text("Buat Laporan Kerja", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 24.dp, bottom = 24.dp))

        Text("Tanggal", color = Color(0xFF8B95A5), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        DashOutlinedTextField(value = tanggal, onValueChange = {}, hint = "Pilih tanggal", readOnly = true, onClick = { datePicker.show() }, modifier = Modifier.fillMaxWidth().height(50.dp).padding(bottom = 16.dp))

        Text("Judul Aktivitas", color = Color(0xFF8B95A5), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        DashOutlinedTextField(value = judul, onValueChange = { judul = it }, hint = "cth. Maintenance Server", modifier = Modifier.fillMaxWidth().height(50.dp).padding(bottom = 16.dp))

        Text("Deskripsi Pekerjaan", color = Color(0xFF8B95A5), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        DashOutlinedTextField(value = deskripsi, onValueChange = { deskripsi = it }, hint = "Ceritakan aktivitasmu...", modifier = Modifier.fillMaxWidth().height(100.dp).padding(bottom = 16.dp))

        Text("Durasi Kerja", color = Color(0xFF8B95A5), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            DashOutlinedTextField(value = jamMulai, onValueChange = {}, hint = "Mulai", readOnly = true, textAlign = TextAlign.Center, onClick = { timeStart.show() }, modifier = Modifier.weight(1f).height(50.dp).padding(end = 8.dp))
            DashOutlinedTextField(value = jamSelesai, onValueChange = {}, hint = "Selesai", readOnly = true, textAlign = TextAlign.Center, onClick = { timeEnd.show() }, modifier = Modifier.weight(1f).height(50.dp).padding(start = 8.dp))
        }

        Text("Lampiran (opsional)", color = Color(0xFF8B95A5), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            DashTabCard("📄 File", currentTab == "FILE", Modifier.weight(1f).padding(end = 4.dp)) { currentTab = "FILE"; isUploadSuccess = false }
            DashTabCard("📷 Foto", currentTab == "FOTO", Modifier.weight(1f).padding(horizontal = 4.dp)) { currentTab = "FOTO"; isUploadSuccess = false }
            DashTabCard("🔗 Link", currentTab == "LINK", Modifier.weight(1f).padding(start = 4.dp)) { currentTab = "LINK"; isUploadSuccess = false }
        }

        Box(
            modifier = Modifier.fillMaxWidth().height(80.dp).padding(bottom = 24.dp).drawBehind { drawRoundRect(color = Color(0xFF4A5568), style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)), cornerRadius = CornerRadius(8.dp.toPx())) },
            contentAlignment = Alignment.Center
        ) {
            if (currentTab == "LINK") {
                TextField(value = linkDrive, onValueChange = { linkDrive = it }, placeholder = { Text("Tempel link...", color = Color(0xFF4A5568)) }, colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent), modifier = Modifier.fillMaxWidth())
            } else {
                Text(text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color(0xFF8B95A5))) { append("Upload bukti ") }
                    withStyle(SpanStyle(color = Color(0xFF3498DB))) { append("di sini") }
                }, fontSize = 12.sp)
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth().height(55.dp).clip(RoundedCornerShape(8.dp)).background(Brush.horizontalGradient(listOf(Color(0xFF3498DB), Color(0xFF2980B9))))
                .clickable {
                    if (judul.isEmpty() || deskripsi.isEmpty()) Toast.makeText(context, "Judul & Deskripsi wajib diisi!", Toast.LENGTH_SHORT).show()
                    else { Toast.makeText(context, "Laporan Terkirim!", Toast.LENGTH_LONG).show(); onDismiss() }
                },
            contentAlignment = Alignment.Center
        ) { Text("Kirim Laporan", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun DashOutlinedTextField(value: String, onValueChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier, readOnly: Boolean = false, textAlign: TextAlign = TextAlign.Start, onClick: (() -> Unit)? = null) {
    Box(
        modifier = modifier.background(Color.Transparent, RoundedCornerShape(8.dp)).border(1.dp, Color(0xFF4A5568), RoundedCornerShape(8.dp)).then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = if (textAlign == TextAlign.Center) Alignment.Center else Alignment.TopStart
    ) {
        TextField(
            value = value, onValueChange = onValueChange, readOnly = readOnly, enabled = onClick == null,
            placeholder = { Text(hint, color = Color(0xFF4A5568), modifier = Modifier.fillMaxWidth(), textAlign = textAlign) },
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, disabledContainerColor = Color.Transparent, focusedTextColor = Color.White, unfocusedTextColor = Color.White, disabledTextColor = Color.White, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, disabledIndicatorColor = Color.Transparent),
            textStyle = androidx.compose.ui.text.TextStyle(textAlign = textAlign, color = Color.White),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun DashTabCard(text: String, isActive: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = if (isActive) Color(0xFF1E3A8A) else Color(0xFF222831)),
        border = BorderStroke(1.dp, Color(0xFF2D3548)),
        modifier = modifier.height(40.dp).clickable { onClick() }
    ) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text, color = if (isActive) Color.White else Color(0xFF8B95A5), fontSize = 12.sp) } }
}

@Composable
fun DashProfilContent(colors: P79Colors, isDarkMode: Boolean, currentUser: User?, onThemeChange: (Boolean) -> Unit, onLogoutClick: () -> Unit, onBackClick: () -> Unit) {
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("AppPref", Context.MODE_PRIVATE) }
    var isNotifEnabled by remember { mutableStateOf(sharedPref.getBoolean("isNotifEnabled", true)) }
    val cardBgColor = if (isDarkMode) Color(0xFF161D2F) else Color.White
    val iconBgColor = if (isDarkMode) Color(0xFF222831) else Color(0xFFF3F4F6)

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(48.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).clickable { onBackClick() }, contentAlignment = Alignment.Center) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.text0, modifier = Modifier.size(24.dp)) }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(currentUser?.name ?: "User", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(currentUser?.nip ?: "-", color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Box(modifier = Modifier.size(40.dp).background(colors.green, CircleShape), contentAlignment = Alignment.Center) { Text(currentUser?.name?.firstOrNull()?.toString() ?: "U", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.size(90.dp)) {
                Box(modifier = Modifier.size(80.dp).align(Alignment.Center).background(Brush.linearGradient(listOf(colors.blue, colors.green)), RoundedCornerShape(24.dp)), contentAlignment = Alignment.Center) { Text(currentUser?.name?.firstOrNull()?.toString() ?: "U", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold) }
                Box(modifier = Modifier.size(28.dp).align(Alignment.BottomEnd).background(cardBgColor, CircleShape).border(1.dp, colors.border, CircleShape).clickable { context.startActivity(Intent(context, EditProfileActivity::class.java)) }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = colors.text0, modifier = Modifier.size(14.dp)) }
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
            DashSettingsItem(colors = colors, iconBgColor = iconBgColor, icon = Icons.Default.Lock, iconTint = colors.blue, title = "Mode Gelap", subtitle = "Tampilan dark / light", showArrow = false, trailing = { Switch(checked = isDarkMode, onCheckedChange = onThemeChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = colors.blue, uncheckedThumbColor = colors.text1, uncheckedTrackColor = iconBgColor, uncheckedBorderColor = colors.border)) })
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("AKUN", color = colors.text1, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Column(modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp))) {
            DashSettingsItem(colors = colors, iconBgColor = iconBgColor, icon = Icons.Default.Edit, iconTint = colors.green, title = "Edit Profil", subtitle = null, onClick = { context.startActivity(Intent(context, EditProfileActivity::class.java)) })
            Divider(color = colors.border, modifier = Modifier.padding(horizontal = 16.dp))
            DashSettingsItem(colors = colors, iconBgColor = iconBgColor, icon = Icons.Default.Lock, iconTint = colors.amber, title = "Ubah Kata Sandi", subtitle = null, onClick = { context.startActivity(Intent(context, ChangePasswordActivity::class.java)) })
            Divider(color = colors.border, modifier = Modifier.padding(horizontal = 16.dp))
            DashSettingsItem(colors = colors, iconBgColor = iconBgColor, icon = Icons.Default.LocationOn, iconTint = colors.blue, title = "Lokasi Kantor Terdaftar", subtitle = "", onClick = { context.startActivity(Intent(context, OfficeLocationActivity::class.java)) })
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("LAINNYA", color = colors.text1, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Column(modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp))) {
            DashSettingsItem(colors = colors, iconBgColor = iconBgColor, icon = Icons.Default.Notifications, iconTint = colors.red, title = "Notifikasi", subtitle = null, showArrow = false, trailing = { Switch(checked = isNotifEnabled, onCheckedChange = { isNotifEnabled = it; sharedPref.edit().putBoolean("isNotifEnabled", it).apply() }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = colors.blue, uncheckedThumbColor = colors.text1, uncheckedTrackColor = iconBgColor, uncheckedBorderColor = colors.border)) })
            Divider(color = colors.border, modifier = Modifier.padding(horizontal = 16.dp))
            DashSettingsItem(colors = colors, iconBgColor = iconBgColor, icon = Icons.Default.Info, iconTint = colors.text1, title = "Bantuan & Dukungan", subtitle = null, onClick = { context.startActivity(Intent(context, HelpSupportActivity::class.java)) })
            Divider(color = colors.border, modifier = Modifier.padding(horizontal = 16.dp))
            DashSettingsItem(colors = colors, iconBgColor = iconBgColor, icon = Icons.Default.Info, iconTint = colors.text1, title = "Tentang Aplikasi", subtitle = "", showArrow = true, onClick = { context.startActivity(Intent(context, AboutAppActivity::class.java)) })
        }
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth().height(55.dp).background(colors.red.copy(alpha = 0.05f), RoundedCornerShape(16.dp)).border(1.dp, colors.red.copy(alpha = 0.5f), RoundedCornerShape(16.dp)).clickable { onLogoutClick() }, contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Keluar", tint = colors.red, modifier = Modifier.size(20.dp))
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
fun DashSettingsItem(colors: P79Colors, iconBgColor: Color, icon: ImageVector, iconTint: Color, title: String, subtitle: String?, showArrow: Boolean = true, onClick: () -> Unit = {}, trailing: @Composable (() -> Unit)? = null) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(36.dp).background(iconBgColor, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp)) }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = colors.text0, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            if (subtitle != null) { Spacer(modifier = Modifier.height(2.dp)); Text(subtitle, color = colors.text1, fontSize = 12.sp) }
        }
        if (trailing != null) trailing() else if (showArrow) Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = colors.text1, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun DashRiwayatContent(colors: P79Colors, isDarkMode: Boolean, currentUser: User?, history: List<Attendance>, onBackClick: () -> Unit) {
    val cardBgColor = if (isDarkMode) Color(0xFF161D2F) else Color.White
    val innerCardBgColor = if (isDarkMode) Color(0xFF222831) else Color(0xFFF3F4F6)

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(48.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).clickable { onBackClick() }, contentAlignment = Alignment.Center) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.text0, modifier = Modifier.size(24.dp)) }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(currentUser?.name ?: "User", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(currentUser?.nip ?: "-", color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("Riwayat Absensi", color = colors.text0, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Pantau histori kehadiran kamu", color = colors.text1, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(36.dp).background(cardBgColor, RoundedCornerShape(8.dp)).border(1.dp, colors.border, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = colors.text1) }
            Text("Juli 2026", color = colors.text0, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Box(modifier = Modifier.size(36.dp).background(cardBgColor, RoundedCornerShape(8.dp)).border(1.dp, colors.border, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = colors.text1) }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DashStatCard(modifier = Modifier.weight(1f), bg = cardBgColor, border = colors.border, title = history.size.toString(), subtitle = "KEHADIRAN", iconTint = colors.green, textColor = colors.text0)
            DashStatCard(modifier = Modifier.weight(1f), bg = cardBgColor, border = colors.border, title = "08:00", subtitle = "RATA² MASUK", iconTint = colors.blue, textColor = colors.text0)
            DashStatCard(modifier = Modifier.weight(1f), bg = cardBgColor, border = colors.border, title = "0", subtitle = "TERLAMBAT", iconTint = colors.amber, textColor = colors.text0)
            DashStatCard(modifier = Modifier.weight(1f), bg = cardBgColor, border = colors.border, title = "0", subtitle = "GPS GAGAL", iconTint = colors.red, textColor = colors.text0)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Column(modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp)).padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf("S", "S", "R", "K", "J", "S", "M").forEach { day -> Text(day, color = colors.text1, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center) }
            }
            Spacer(modifier = Modifier.height(12.dp))
            val calendarData = List(5) { List(7) { innerCardBgColor } }
            calendarData.forEach { week ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    week.forEach { dayColor -> Box(modifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp).background(dayColor, RoundedCornerShape(8.dp))) }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                DashLegendItem("Hadir", colors.green, colors.text1)
                Spacer(modifier = Modifier.width(16.dp))
                DashLegendItem("Izin", colors.amber, colors.text1)
                Spacer(modifier = Modifier.width(16.dp))
                DashLegendItem("Telat", colors.red, colors.text1)
                Spacer(modifier = Modifier.width(16.dp))
                DashLegendItem("Libur", innerCardBgColor, colors.text1)
            }
        }
        Spacer(modifier = Modifier.height(130.dp))
    }
}

@Composable
fun DashStatCard(modifier: Modifier, bg: Color, border: Color, title: String, subtitle: String, iconTint: Color, textColor: Color) {
    Column(modifier = modifier.background(bg, RoundedCornerShape(12.dp)).border(1.dp, border, RoundedCornerShape(12.dp)).padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(8.dp).background(iconTint, CircleShape))
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = textColor.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DashLegendItem(label: String, dotColor: Color, textColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(dotColor, CircleShape))
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, color = textColor, fontSize = 10.sp)
    }
}

@Composable
fun DashLaporanContent(colors: P79Colors, isDarkMode: Boolean, currentUser: User?, reports: List<WorkingReport>, onBellClick: () -> Unit, onAddClick: () -> Unit) {
    val cardBgColor = if (isDarkMode) Color(0xFF161D2F) else Color.White
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(48.dp))
            DashTopBar(colors = colors, isDarkMode = isDarkMode, currentUser = currentUser, onBellClick = onBellClick)
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.padding(horizontal = 24.dp)) { Text("Laporan Kerja", color = colors.text0, fontSize = 24.sp, fontWeight = FontWeight.Bold); Text("Riwayat & pengajuan laporan harian", color = colors.text1, fontSize = 12.sp) }
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashLaporanTab(text = "Semua", isSelected = true, colors = colors, cardBgColor = cardBgColor)
                DashLaporanTab(text = "Disetujui", isSelected = false, colors = colors, cardBgColor = cardBgColor)
                DashLaporanTab(text = "Menunggu", isSelected = false, colors = colors, cardBgColor = cardBgColor)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                reports.forEach { report -> DashWorkingReportItem(colors = colors, cardBgColor = cardBgColor, report = report) }
            }
            Spacer(modifier = Modifier.height(130.dp))
        }
        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 100.dp).size(60.dp).clip(RoundedCornerShape(20.dp)).background(Brush.linearGradient(listOf(colors.blue, colors.green))).clickable { onAddClick() }, contentAlignment = Alignment.Center) { Text("+", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Light, modifier = Modifier.padding(bottom = 6.dp)) }
    }
}

@Composable
fun DashWorkingReportItem(colors: P79Colors, cardBgColor: Color, report: WorkingReport) {
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
fun DashLaporanTab(text: String, isSelected: Boolean, colors: P79Colors, cardBgColor: Color) {
    Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(if (isSelected) colors.blue.copy(alpha = 0.2f) else cardBgColor).border(1.dp, if (isSelected) colors.blue else colors.border, RoundedCornerShape(20.dp)).clickable { }.padding(horizontal = 20.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
        Text(text, color = if (isSelected) colors.blue else colors.text1, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DashHomeContent(colors: P79Colors, isDarkMode: Boolean, activity: AppCompatActivity, currentUser: User?, todayAttendance: Attendance?, onLaporClick: () -> Unit, onRiwayatClick: () -> Unit, onIzinClick: () -> Unit, onLemburClick: () -> Unit, onBellClick: () -> Unit) {
    val cardBgColor = if (isDarkMode) Color(0xFF161D2F) else Color.White

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(48.dp))
        DashTopBar(colors = colors, isDarkMode = isDarkMode, currentUser = currentUser, onBellClick = onBellClick)
        Spacer(modifier = Modifier.height(24.dp))
        DashAbsenCard(colors = colors, isDarkMode = isDarkMode, todayAttendance = todayAttendance, activity = activity)
        Spacer(modifier = Modifier.height(16.dp))
        DashMenuRow(colors = colors, cardBgColor = cardBgColor, onIzinClick = onIzinClick, onLaporClick = onLaporClick, onRiwayatClick = onRiwayatClick, onLemburClick = onLemburClick)
        Spacer(modifier = Modifier.height(16.dp))
        DashSummaryCard(colors = colors, cardBgColor = cardBgColor)
        Spacer(modifier = Modifier.height(24.dp))
        Text("RIWAYAT TERBARU", color = colors.text1, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Spacer(modifier = Modifier.height(130.dp))
    }
}

@Composable
fun DashTopBar(colors: P79Colors, isDarkMode: Boolean, currentUser: User?, onBellClick: () -> Unit) {
    val iconBgColor = if (isDarkMode) Color(0xFF161D2F) else Color.White
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Halo, ${currentUser?.name?.split(" ")?.firstOrNull() ?: "Sobat"}", color = colors.text0, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(text = "Padepokan Tujuh Sembilan", color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(iconBgColor).border(1.dp, colors.border, CircleShape).clickable { onBellClick() }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Notifications, contentDescription = "Notifikasi", tint = colors.text1, modifier = Modifier.size(20.dp)); Box(modifier = Modifier.size(8.dp).background(colors.red, CircleShape).align(Alignment.TopEnd).padding(2.dp)) }
        Spacer(modifier = Modifier.width(12.dp))
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Brush.linearGradient(listOf(colors.blue, colors.green))), contentAlignment = Alignment.Center) { Text(currentUser?.name?.firstOrNull()?.toString() ?: "U", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
    }
}

@Composable
fun DashAbsenCard(colors: P79Colors, isDarkMode: Boolean, todayAttendance: Attendance?, activity: AppCompatActivity) {
    val iconBgColor = if (isDarkMode) Color(0xFF222831) else Color(0xFFF3F4F6)
    val hasCheckedIn = todayAttendance != null
    val hasCheckedOut = todayAttendance?.checkOutTime?.isNotEmpty() == true

    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
        GlassCard(colors = colors, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(iconBgColor, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Icon(if (hasCheckedIn) Icons.Default.CheckCircle else Icons.Default.Warning, contentDescription = null, tint = if (hasCheckedIn) colors.green else colors.amber, modifier = Modifier.size(20.dp)) }
                Spacer(modifier = Modifier.width(16.dp))
                Column { Text(if (hasCheckedOut) "Sudah Selesai Kerja" else if (hasCheckedIn) "Sedang Bekerja" else "Belum Absen Masuk", color = colors.text0, fontSize = 16.sp, fontWeight = FontWeight.Bold); Text("Kantor Pusat · radius 100m", color = colors.text1, fontSize = 12.sp) }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("MASUK", color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp); Spacer(modifier = Modifier.height(4.dp)); Text(todayAttendance?.checkInTime ?: "--:--", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(colors.border))
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("PULANG", color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp); Spacer(modifier = Modifier.height(4.dp)); Text(todayAttendance?.checkOutTime ?: "--:--", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            }
            Spacer(modifier = Modifier.height(24.dp))
            val buttonText = if (hasCheckedOut) "SUDAH ABSEN PULANG" else if (hasCheckedIn) "ABSEN PULANG" else "ABSEN MASUK"
            val buttonEnabled = !hasCheckedOut
            Button(onClick = { val intent = Intent(activity, CameraAbsenActivity::class.java); if (!hasCheckedIn) intent.putExtra("type", "CHECK_IN") else intent.putExtra("type", "CHECK_OUT"); activity.startActivity(intent) }, enabled = buttonEnabled, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Gray.copy(alpha = 0.2f)), contentPadding = PaddingValues(), modifier = Modifier.fillMaxWidth().height(55.dp)) {
                Box(modifier = Modifier.fillMaxSize().background(if (buttonEnabled) Brush.horizontalGradient(listOf(colors.blue, colors.green)) else SolidColor(Color.Transparent), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Text(buttonText, color = if (buttonEnabled) Color.White else colors.text1, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
fun DashMenuRow(colors: P79Colors, cardBgColor: Color, onIzinClick: () -> Unit, onLaporClick: () -> Unit, onRiwayatClick: () -> Unit, onLemburClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        DashMenuCard(colors = colors, cardBgColor = cardBgColor, title = "Ajukan Izin", iconColor = colors.amber, modifier = Modifier.weight(1f)) { onIzinClick() }
        DashMenuCard(colors = colors, cardBgColor = cardBgColor, title = "Lapor Kerja", iconColor = colors.green, modifier = Modifier.weight(1f)) { onLaporClick() }
        DashMenuCard(colors = colors, cardBgColor = cardBgColor, title = "Riwayat", iconColor = colors.blue, modifier = Modifier.weight(1f)) { onRiwayatClick() }
        DashMenuCard(colors = colors, cardBgColor = cardBgColor, title = "Extra Lembur", iconColor = colors.red, modifier = Modifier.weight(1f)) { onLemburClick() }
    }
}

@Composable
fun DashMenuCard(colors: P79Colors, cardBgColor: Color, title: String, iconColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(modifier = modifier.height(90.dp).background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp)).clickable { onClick() }, contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Info, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp)); Spacer(modifier = Modifier.height(8.dp)); Text(title, color = colors.text0, fontSize = 12.sp) } }
}

@Composable
fun DashSummaryCard(colors: P79Colors, cardBgColor: Color) {
    Box(modifier = Modifier.padding(horizontal = 24.dp)) { Column(modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp)).padding(20.dp)) { Text("RINGKASAN BULAN INI", color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp); Spacer(modifier = Modifier.height(16.dp)); Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { DashSummaryItem(colors = colors, count = "0", label = "Hadir", color = colors.green); DashSummaryItem(colors = colors, count = "0", label = "Izin", color = colors.amber); DashSummaryItem(colors = colors, count = "0", label = "Telat", color = colors.red); DashSummaryItem(colors = colors, count = "0", label = "Alpha", color = colors.blue) } } }
}

@Composable
fun DashSummaryItem(colors: P79Colors, count: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(count, color = color, fontSize = 24.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(4.dp)); Text(label, color = colors.text1, fontSize = 12.sp) }
}

@Composable
fun DashDockNavigationBar(colors: P79Colors, isDarkMode: Boolean, selectedIndex: Int, onItemSelected: (Int) -> Unit, modifier: Modifier = Modifier) {
    var dragOffset by remember { mutableStateOf(0f) }
    val dockBgColor = if (isDarkMode) Color(0xD9161D2F) else Color(0xD9FFFFFF)
    Row(modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp).pointerInput(Unit) { detectHorizontalDragGestures(onDragStart = { dragOffset = 0f }, onDragEnd = { if (dragOffset < -50f && selectedIndex < 3) { onItemSelected(selectedIndex + 1) } else if (dragOffset > 50f && selectedIndex > 0) { onItemSelected(selectedIndex - 1) }; dragOffset = 0f }) { change, dragAmount -> change.consume(); dragOffset += dragAmount } }, horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        BoxWithConstraints(modifier = Modifier.weight(1f).height(64.dp).clip(RoundedCornerShape(32.dp)).background(dockBgColor).border(1.dp, colors.border, RoundedCornerShape(32.dp))) {
            val itemWidth = maxWidth / 3
            val indicatorOffset by animateDpAsState(targetValue = if (selectedIndex < 3) itemWidth * selectedIndex else itemWidth * 2, animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow), label = "")
            val indicatorAlpha by animateFloatAsState(targetValue = if (selectedIndex < 3) 1f else 0f, label = "")
            Box(modifier = Modifier.offset(x = indicatorOffset).width(itemWidth).fillMaxHeight().padding(6.dp).graphicsLayer(alpha = indicatorAlpha).background(Brush.linearGradient(listOf(colors.blue, colors.green)), RoundedCornerShape(26.dp)))
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) { DashBottomNavItem(colors = colors, icon = Icons.Default.Home, label = "Home", isSelected = selectedIndex == 0, modifier = Modifier.weight(1f)) { onItemSelected(0) }; DashBottomNavItem(colors = colors, icon = Icons.Default.DateRange, label = "Riwayat", isSelected = selectedIndex == 1, modifier = Modifier.weight(1f)) { onItemSelected(1) }; DashBottomNavItem(colors = colors, icon = Icons.Default.Edit, label = "Laporan", isSelected = selectedIndex == 2, modifier = Modifier.weight(1f)) { onItemSelected(2) } }
        }
        Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(if (selectedIndex == 3) Brush.linearGradient(listOf(colors.blue, colors.green)) else SolidColor(dockBgColor)).border(1.dp, if (selectedIndex == 3) Color.Transparent else colors.border, CircleShape).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = { onItemSelected(3) }), contentAlignment = Alignment.Center) { Icon(imageVector = Icons.Default.Person, contentDescription = "Profil", tint = if (selectedIndex == 3) Color.White else colors.text1, modifier = Modifier.size(26.dp)) }
    }
}

@Composable
fun DashBottomNavItem(colors: P79Colors, icon: ImageVector, label: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)) { Icon(icon, contentDescription = label, tint = if (isSelected) Color.White else colors.text1, modifier = Modifier.size(24.dp)); Spacer(modifier = Modifier.height(4.dp)); Text(label, color = if (isSelected) Color.White else colors.text1, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
}

@Composable
fun DashNotificationSheetContent(colors: P79Colors) {
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Notifikasi", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold); Text("Tandai semua dibaca", color = colors.blue, fontSize = 12.sp, fontWeight = FontWeight.Medium) }
        Spacer(modifier = Modifier.height(24.dp))
    }
}