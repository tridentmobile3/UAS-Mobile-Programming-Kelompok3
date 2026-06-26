package com.feisal.workingreport

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.feisal.workingreport.ui.components.GlassCard
import com.feisal.workingreport.ui.components.NoiseOverlay
import com.feisal.workingreport.ui.theme.LiquidGlassBackground
import com.feisal.workingreport.ui.theme.P79Colors
import com.feisal.workingreport.ui.theme.p79Colors
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {
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
                                onLaporClick = { coroutineScope.launch { pagerState.animateScrollToPage(2) } },
                                onRiwayatClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                                onIzinClick = { showIzinSheet = true },
                                onLemburClick = { coroutineScope.launch { pagerState.animateScrollToPage(4) } },
                                onBellClick = { showNotificationSheet = true }
                            )
                            1 -> RiwayatContent(
                                colors = colors,
                                isDarkMode = isDarkMode,
                                onBackClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
                            )
                            2 -> LaporanContent(
                                colors = colors,
                                isDarkMode = isDarkMode,
                                onBellClick = { showNotificationSheet = true },
                                onAddClick = { showLaporanSheet = true }
                            )
                            3 -> ProfilContent(
                                colors = colors,
                                isDarkMode = isDarkMode,
                                onThemeChange = { isDark ->
                                    isDarkMode = isDark
                                    sharedPref.edit().putBoolean("isDarkMode", isDark).apply()
                                },
                                onBackClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
                            )
                            4 -> LemburContent(
                                colors = colors,
                                isDarkMode = isDarkMode,
                                onBackClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                                onAjukanClick = { showLemburSheet = true }
                            )
                        }
                    }
                }

                DockNavigationBar(
                    colors = colors,
                    isDarkMode = isDarkMode,
                    selectedIndex = if (pagerState.currentPage == 4) 0 else pagerState.currentPage,
                    onItemSelected = { index ->
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
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

            if (showIzinSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showIzinSheet = false },
                    sheetState = sheetState,
                    containerColor = if (isDarkMode) Color(0xFF161D2F) else Color.White,
                    scrimColor = Color.Black.copy(alpha = 0.5f)
                ) {
                    IzinBottomSheetContent(colors = colors, isDarkMode = isDarkMode)
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
                    LaporanBottomSheetContent(colors = colors, isDarkMode = isDarkMode)
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
fun LaporanBottomSheetContent(colors: P79Colors, isDarkMode: Boolean) {
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
        Text("Lampiran (opsional)", color = colors.text1, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f).background(colors.blue, RoundedCornerShape(8.dp)).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                Row {
                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("File", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Box(modifier = Modifier.weight(1f).background(inputBgColor, RoundedCornerShape(8.dp)).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                Row {
                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = colors.text1, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Foto", color = colors.text1, fontSize = 12.sp)
                }
            }
            Box(modifier = Modifier.weight(1f).background(inputBgColor, RoundedCornerShape(8.dp)).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
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
            modifier = Modifier.fillMaxWidth().height(55.dp).background(Brush.horizontalGradient(listOf(colors.blue, colors.green)), RoundedCornerShape(12.dp)).clickable { },
            contentAlignment = Alignment.Center
        ) {
            Text("Kirim Laporan", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IzinBottomSheetContent(colors: P79Colors, isDarkMode: Boolean) {
    val inputBgColor = if (isDarkMode) Color(0xFF222831) else Color(0xFFF3F4F6)

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
        Text("Ajukan Izin", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        Text("Jenis Izin", color = colors.text1, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth().background(inputBgColor, RoundedCornerShape(12.dp)).padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("", color = colors.text0, fontSize = 14.sp)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = colors.text1)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Keterangan", color = colors.text1, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = "",
            onValueChange = {},
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
        Text("Upload Bukti", color = colors.text1, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f).background(colors.blue, RoundedCornerShape(8.dp)).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                Row {
                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("File", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Box(modifier = Modifier.weight(1f).background(inputBgColor, RoundedCornerShape(8.dp)).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                Text("Foto", color = colors.text1, fontSize = 12.sp)
            }
            Box(modifier = Modifier.weight(1f).background(inputBgColor, RoundedCornerShape(8.dp)).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                Text("Link Drive", color = colors.text1, fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(80.dp)
                .background(Color.Transparent, RoundedCornerShape(12.dp))
                .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("", color = colors.text1, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(55.dp).background(Brush.horizontalGradient(listOf(colors.blue, colors.green)), RoundedCornerShape(12.dp)).clickable { },
            contentAlignment = Alignment.Center
        ) {
            Text("Kirim Pengajuan", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatContent(colors: P79Colors, isDarkMode: Boolean, onBackClick: () -> Unit) {
    val cardBgColor = if (isDarkMode) Color(0xFF161D2F) else Color.White
    val innerCardBgColor = if (isDarkMode) Color(0xFF222831) else Color(0xFFF3F4F6)

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(48.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).clickable { onBackClick() }, contentAlignment = Alignment.Center) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.text0, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("", color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Box(modifier = Modifier.size(40.dp).background(colors.green, CircleShape), contentAlignment = Alignment.Center) {
                Text("", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("Riwayat Absensi", color = colors.text0, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Pantau histori kehadiran kamu", color = colors.text1, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(36.dp).background(cardBgColor, RoundedCornerShape(8.dp)).border(1.dp, colors.border, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = colors.text1)
            }
            Text("", color = colors.text0, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Box(modifier = Modifier.size(36.dp).background(cardBgColor, RoundedCornerShape(8.dp)).border(1.dp, colors.border, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = colors.text1)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(modifier = Modifier.weight(1f), bg = cardBgColor, border = colors.border, title = "-", subtitle = "KEHADIRAN", iconTint = colors.green, textColor = colors.text0)
            StatCard(modifier = Modifier.weight(1f), bg = cardBgColor, border = colors.border, title = "-", subtitle = "RATA² MASUK", iconTint = colors.blue, textColor = colors.text0)
            StatCard(modifier = Modifier.weight(1f), bg = cardBgColor, border = colors.border, title = "-", subtitle = "TERLAMBAT", iconTint = colors.amber, textColor = colors.text0)
            StatCard(modifier = Modifier.weight(1f), bg = cardBgColor, border = colors.border, title = "-", subtitle = "GPS GAGAL", iconTint = colors.red, textColor = colors.text0)
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
fun HomeContent(colors: P79Colors, isDarkMode: Boolean, activity: AppCompatActivity, onLaporClick: () -> Unit, onRiwayatClick: () -> Unit, onIzinClick: () -> Unit, onLemburClick: () -> Unit, onBellClick: () -> Unit) {
    val cardBgColor = if (isDarkMode) Color(0xFF161D2F) else Color.White

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(48.dp))
        TopBar(colors = colors, isDarkMode = isDarkMode, onBellClick = onBellClick)
        Spacer(modifier = Modifier.height(24.dp))
        AbsenCard(colors = colors, isDarkMode = isDarkMode, activity = activity)
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
fun ProfilContent(colors: P79Colors, isDarkMode: Boolean, onThemeChange: (Boolean) -> Unit, onBackClick: () -> Unit) {
    val cardBgColor = if (isDarkMode) Color(0xFF161D2F) else Color.White
    val iconBgColor = if (isDarkMode) Color(0xFF222831) else Color(0xFFF3F4F6)

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(48.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).clickable { onBackClick() }, contentAlignment = Alignment.Center) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.text0, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("", color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Box(modifier = Modifier.size(40.dp).background(colors.green, CircleShape), contentAlignment = Alignment.Center) {
                Text("", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.size(90.dp)) {
                Box(modifier = Modifier.size(80.dp).align(Alignment.Center).background(Brush.linearGradient(listOf(colors.blue, colors.green)), RoundedCornerShape(24.dp)), contentAlignment = Alignment.Center) {
                    Text("", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.size(28.dp).align(Alignment.BottomEnd).background(cardBgColor, CircleShape).border(1.dp, colors.border, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = colors.text0, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("", color = colors.text0, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("", color = colors.text1, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) { Text("", color = colors.text1, fontSize = 12.sp) }
                Box(modifier = Modifier.background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) { Text("", color = colors.text1, fontSize = 12.sp) }
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
            SettingsItem(colors, iconBgColor, Icons.Default.Edit, colors.green, "Edit Profil", null)
            Divider(color = colors.border, modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(colors, iconBgColor, Icons.Default.Lock, colors.amber, "Ubah Kata Sandi", null)
            Divider(color = colors.border, modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(colors, iconBgColor, Icons.Default.LocationOn, colors.blue, "Lokasi Kantor Terdaftar", "")
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("LAINNYA", color = colors.text1, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Column(modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp))) {
            SettingsItem(colors, iconBgColor, Icons.Default.Notifications, colors.red, "Notifikasi", null, trailing = { Switch(checked = true, onCheckedChange = {}, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = colors.blue)) })
            Divider(color = colors.border, modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(colors, iconBgColor, Icons.Default.Info, colors.text1, "Bantuan & Dukungan", null)
            Divider(color = colors.border, modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(colors, iconBgColor, Icons.Default.Info, colors.text1, "Tentang Aplikasi", "", showArrow = false)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth().height(55.dp).background(colors.red.copy(alpha = 0.05f), RoundedCornerShape(16.dp)).border(1.dp, colors.red.copy(alpha = 0.5f), RoundedCornerShape(16.dp)).clickable { }, contentAlignment = Alignment.Center) {
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
fun SettingsItem(colors: P79Colors, iconBgColor: Color, icon: ImageVector, iconTint: Color, title: String, subtitle: String?, showArrow: Boolean = true, trailing: @Composable (() -> Unit)? = null) {
    Row(modifier = Modifier.fillMaxWidth().clickable { }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
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
fun LaporanContent(colors: P79Colors, isDarkMode: Boolean, onBellClick: () -> Unit, onAddClick: () -> Unit) {
    val cardBgColor = if (isDarkMode) Color(0xFF161D2F) else Color.White
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(48.dp))
            TopBar(colors = colors, isDarkMode = isDarkMode, onBellClick = onBellClick)
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.padding(horizontal = 24.dp)) { Text("Laporan Kerja", color = colors.text0, fontSize = 24.sp, fontWeight = FontWeight.Bold); Text("Riwayat & pengajuan laporan harian", color = colors.text1, fontSize = 12.sp) }
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LaporanTab(text = "Semua", isSelected = true, colors = colors, cardBgColor = cardBgColor)
                LaporanTab(text = "Disetujui", isSelected = false, colors = colors, cardBgColor = cardBgColor)
                LaporanTab(text = "Menunggu", isSelected = false, colors = colors, cardBgColor = cardBgColor)
                LaporanTab(text = "Revisi", isSelected = false, colors = colors, cardBgColor = cardBgColor)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            }
            Spacer(modifier = Modifier.height(130.dp))
        }
        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 100.dp).size(60.dp).clip(RoundedCornerShape(20.dp)).background(Brush.linearGradient(listOf(colors.blue, colors.green))).clickable { onAddClick() }, contentAlignment = Alignment.Center) { Text("+", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Light, modifier = Modifier.padding(bottom = 6.dp)) }
    }
}

@Composable
fun LaporanTab(text: String, isSelected: Boolean, colors: P79Colors, cardBgColor: Color) {
    Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(if (isSelected) colors.blue.copy(alpha = 0.2f) else cardBgColor).border(1.dp, if (isSelected) colors.blue else colors.border, RoundedCornerShape(20.dp)).clickable { }.padding(horizontal = 20.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
        Text(text, color = if (isSelected) colors.blue else colors.text1, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TopBar(colors: P79Colors, isDarkMode: Boolean, onBellClick: () -> Unit) {
    val iconBgColor = if (isDarkMode) Color(0xFF161D2F) else Color.White
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) { Text(text = "", color = colors.text0, fontSize = 24.sp, fontWeight = FontWeight.Bold); Text(text = "", color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp) }
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(iconBgColor).border(1.dp, colors.border, CircleShape).clickable { onBellClick() }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Notifications, contentDescription = "Notifikasi", tint = colors.text1, modifier = Modifier.size(20.dp)); Box(modifier = Modifier.size(8.dp).background(colors.red, CircleShape).align(Alignment.TopEnd).padding(2.dp)) }
        Spacer(modifier = Modifier.width(12.dp))
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Brush.linearGradient(listOf(colors.blue, colors.green))), contentAlignment = Alignment.Center) { Text("", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
    }
}

@Composable
fun AbsenCard(colors: P79Colors, isDarkMode: Boolean, activity: AppCompatActivity) {
    val iconBgColor = if (isDarkMode) Color(0xFF222831) else Color(0xFFF3F4F6)
    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
        GlassCard(colors = colors, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(40.dp).background(iconBgColor, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Warning, contentDescription = null, tint = colors.amber, modifier = Modifier.size(20.dp)) }; Spacer(modifier = Modifier.width(16.dp)); Column { Text("Belum Absen Masuk", color = colors.text0, fontSize = 16.sp, fontWeight = FontWeight.Bold); Text("Kantor Pusat · radius 100m", color = colors.text1, fontSize = 12.sp) } }
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("MASUK", color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp); Spacer(modifier = Modifier.height(4.dp)); Text("--:--", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold) }; Box(modifier = Modifier.width(1.dp).height(40.dp).background(colors.border)); Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("PULANG", color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp); Spacer(modifier = Modifier.height(4.dp)); Text("--:--", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold) } }
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { activity.startActivity(Intent(activity, CameraAbsenActivity::class.java)) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), contentPadding = PaddingValues(), modifier = Modifier.fillMaxWidth().height(55.dp)) { Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(colors.blue, colors.green)), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Text("ABSEN MASUK", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) } }
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
    Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { }
}

@Composable
fun DockNavigationBar(colors: P79Colors, isDarkMode: Boolean, selectedIndex: Int, onItemSelected: (Int) -> Unit, modifier: Modifier = Modifier) {
    var dragOffset by remember { mutableStateOf(0f) }
    val dockBgColor = if (isDarkMode) Color(0xD9161D2F) else Color(0xD9FFFFFF)
    Row(modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp).pointerInput(Unit) { detectHorizontalDragGestures(onDragStart = { dragOffset = 0f }, onDragEnd = { if (dragOffset < -50f && selectedIndex < 3) { onItemSelected(selectedIndex + 1) } else if (dragOffset > 50f && selectedIndex > 0) { onItemSelected(selectedIndex - 1) }; dragOffset = 0f }) { change, dragAmount -> change.consume(); dragOffset += dragAmount } }, horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        BoxWithConstraints(modifier = Modifier.weight(1f).height(64.dp).clip(RoundedCornerShape(32.dp)).background(dockBgColor).border(1.dp, colors.border, RoundedCornerShape(32.dp))) {
            val itemWidth = maxWidth / 3
            val indicatorOffset by animateDpAsState(targetValue = if (selectedIndex < 3) itemWidth * selectedIndex else itemWidth * 2, animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow), label = "")
            val indicatorAlpha by animateFloatAsState(targetValue = if (selectedIndex < 3) 1f else 0f, label = "")
            Box(modifier = Modifier.offset(x = indicatorOffset).width(itemWidth).fillMaxHeight().padding(6.dp).graphicsLayer(alpha = indicatorAlpha).background(Brush.linearGradient(listOf(colors.blue, colors.green)), RoundedCornerShape(26.dp)))
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) { BottomNavItem(colors = colors, icon = Icons.Default.Home, label = "Home", isSelected = selectedIndex == 0, modifier = Modifier.weight(1f)) { onItemSelected(0) }; BottomNavItem(colors = colors, icon = Icons.Default.DateRange, label = "Riwayat", isSelected = selectedIndex == 1, modifier = Modifier.weight(1f)) { onItemSelected(1) }; BottomNavItem(colors = colors, icon = Icons.Default.Edit, label = "Laporan", isSelected = selectedIndex == 2, modifier = Modifier.weight(1f)) { onItemSelected(2) } }
        }
        Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(if (selectedIndex == 3) Brush.linearGradient(listOf(colors.blue, colors.green)) else SolidColor(dockBgColor)).border(1.dp, if (selectedIndex == 3) Color.Transparent else colors.border, CircleShape).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = { onItemSelected(3) }), contentAlignment = Alignment.Center) { Icon(imageVector = Icons.Default.Person, contentDescription = "Profil", tint = if (selectedIndex == 3) Color.White else colors.text1, modifier = Modifier.size(26.dp)) }
    }
}

@Composable
fun BottomNavItem(colors: P79Colors, icon: ImageVector, label: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)) { Icon(icon, contentDescription = label, tint = if (isSelected) Color.White else colors.text1, modifier = Modifier.size(24.dp)); Spacer(modifier = Modifier.height(4.dp)); Text(label, color = if (isSelected) Color.White else colors.text1, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
}

@Composable
fun NotificationSheetContent(colors: P79Colors, isDarkMode: Boolean) {
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Notifikasi", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold); Text("Tandai semua dibaca", color = colors.blue, fontSize = 12.sp, fontWeight = FontWeight.Medium) }
        Spacer(modifier = Modifier.height(24.dp))
        Spacer(modifier = Modifier.height(24.dp))
    }
}