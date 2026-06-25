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
            var showIzinSheet by remember { mutableStateOf(false) } // State baru untuk Bottom Sheet Izin

            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val pagerState = rememberPagerState(pageCount = { 4 })
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
                                onBellClick = { showNotificationSheet = true }
                            )
                            1 -> RiwayatContent(
                                colors = colors,
                                isDarkMode = isDarkMode,
                                onBackClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
                            )
                            2 -> LaporanContent(colors, isDarkMode) { showNotificationSheet = true }
                            3 -> ProfilContent(
                                colors = colors,
                                isDarkMode = isDarkMode,
                                onThemeChange = { isDark ->
                                    isDarkMode = isDark
                                    sharedPref.edit().putBoolean("isDarkMode", isDark).apply()
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
                    onItemSelected = { index ->
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            // MODAL BOTTOM SHEET NOTIFIKASI
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

            // MODAL BOTTOM SHEET PENGAJUAN IZIN (PURE COMPOSE)
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
        }
    }
}

// ---------------------------------------------
// 1. KONTEN PENGAJUAN IZIN BOTTOM SHEET
// ---------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IzinBottomSheetContent(colors: P79Colors, isDarkMode: Boolean) {
    val inputBgColor = if (isDarkMode) Color(0xFF222831) else Color(0xFFF3F4F6)

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
        Text("Ajukan Izin", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        // Jenis Izin
        Text("Jenis Izin", color = colors.text1, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth().background(inputBgColor, RoundedCornerShape(12.dp)).padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Cuti", color = colors.text0, fontSize = 14.sp)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = colors.text1)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Keterangan
        Text("Keterangan", color = colors.text1, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Tulis alasan izin...", color = colors.text1) },
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

        // Upload Bukti
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

        // Area Drop File
        Box(
            modifier = Modifier.fillMaxWidth().height(80.dp)
                .background(Color.Transparent, RoundedCornerShape(12.dp))
                .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Pilih dokumen PDF/DOC di sini", color = colors.text1, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tombol Kirim
        Box(
            modifier = Modifier.fillMaxWidth().height(55.dp).background(Brush.horizontalGradient(listOf(colors.blue, colors.green)), RoundedCornerShape(12.dp)).clickable { },
            contentAlignment = Alignment.Center
        ) {
            Text("Kirim Pengajuan", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ---------------------------------------------
// 2. KONTEN RIWAYAT (PURE COMPOSE REWRITE)
// ---------------------------------------------
@Composable
fun RiwayatContent(colors: P79Colors, isDarkMode: Boolean, onBackClick: () -> Unit) {
    val cardBgColor = if (isDarkMode) Color(0xFF161D2F) else Color.White
    val innerCardBgColor = if (isDarkMode) Color(0xFF222831) else Color(0xFFF3F4F6)

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(48.dp))

        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).clickable { onBackClick() }, contentAlignment = Alignment.Center) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.text0, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Halo, Feisal \uD83D\uDC4B", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("SABTU, 20 JUNI 2026", color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Box(modifier = Modifier.size(40.dp).background(colors.green, CircleShape), contentAlignment = Alignment.Center) {
                Text("F", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("Riwayat Absensi", color = colors.text0, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Pantau histori kehadiran kamu", color = colors.text1, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(24.dp))

        // Selector Bulan
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(36.dp).background(cardBgColor, RoundedCornerShape(8.dp)).border(1.dp, colors.border, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = colors.text1)
            }
            Text("Juni 2026", color = colors.text0, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Box(modifier = Modifier.size(36.dp).background(cardBgColor, RoundedCornerShape(8.dp)).border(1.dp, colors.border, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = colors.text1)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4 Kartu Statistik
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(modifier = Modifier.weight(1f), bg = cardBgColor, border = colors.border, title = "92%", subtitle = "KEHADIRAN", iconTint = colors.green, textColor = colors.text0)
            StatCard(modifier = Modifier.weight(1f), bg = cardBgColor, border = colors.border, title = "08:06", subtitle = "RATA² MASUK", iconTint = colors.blue, textColor = colors.text0)
            StatCard(modifier = Modifier.weight(1f), bg = cardBgColor, border = colors.border, title = "1x", subtitle = "TERLAMBAT", iconTint = colors.amber, textColor = colors.text0)
            StatCard(modifier = Modifier.weight(1f), bg = cardBgColor, border = colors.border, title = "0x", subtitle = "GPS GAGAL", iconTint = colors.red, textColor = colors.text0)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Kalender Container
        Column(modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp)).padding(16.dp)) {
            // Header Hari
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf("S", "S", "R", "K", "J", "S", "M").forEach { day ->
                    Text(day, color = colors.text1, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Grid Kalender (Dummy Data untuk visual yang sama dengan XML lamamu)
            val calendarData = listOf(
                listOf(colors.green, colors.green, colors.green, colors.green, colors.green, colors.green, innerCardBgColor),
                listOf(colors.green, colors.green, colors.green, colors.green, colors.amber, colors.green, innerCardBgColor),
                listOf(colors.green, colors.green, colors.red, colors.amber, colors.green, colors.blue, innerCardBgColor),
                listOf(innerCardBgColor, innerCardBgColor, innerCardBgColor, innerCardBgColor, innerCardBgColor, innerCardBgColor, innerCardBgColor),
                listOf(innerCardBgColor, innerCardBgColor, innerCardBgColor, innerCardBgColor, innerCardBgColor, innerCardBgColor, innerCardBgColor)
            )

            calendarData.forEach { week ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    week.forEach { dayColor ->
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp).background(dayColor, RoundedCornerShape(8.dp)))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend Kalender
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

// ---------------------------------------------
// 3. KONTEN DASHBOARD LAINNYA
// ---------------------------------------------

@Composable
fun HomeContent(colors: P79Colors, isDarkMode: Boolean, activity: AppCompatActivity, onLaporClick: () -> Unit, onRiwayatClick: () -> Unit, onIzinClick: () -> Unit, onBellClick: () -> Unit) {
    val cardBgColor = if (isDarkMode) Color(0xFF161D2F) else Color.White

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(48.dp))
        TopBar(colors = colors, isDarkMode = isDarkMode, onBellClick = onBellClick)
        Spacer(modifier = Modifier.height(24.dp))
        AbsenCard(colors = colors, isDarkMode = isDarkMode, activity = activity)
        Spacer(modifier = Modifier.height(16.dp))
        MenuRow(colors = colors, cardBgColor = cardBgColor, onIzinClick = onIzinClick, onLaporClick = onLaporClick, onRiwayatClick = onRiwayatClick)
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
fun MenuRow(colors: P79Colors, cardBgColor: Color, onIzinClick: () -> Unit, onLaporClick: () -> Unit, onRiwayatClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MenuCard(colors = colors, cardBgColor = cardBgColor, title = "Ajukan Izin", iconColor = colors.amber, modifier = Modifier.weight(1f)) { onIzinClick() }
        MenuCard(colors = colors, cardBgColor = cardBgColor, title = "Lapor Kerja", iconColor = colors.green, modifier = Modifier.weight(1f)) { onLaporClick() }
        MenuCard(colors = colors, cardBgColor = cardBgColor, title = "Riwayat", iconColor = colors.blue, modifier = Modifier.weight(1f)) { onRiwayatClick() }
    }
}

// ... KODE PROFIL, LAPORAN, TOPBAR, ABSENCARD, DOCK NAVBAR, DLL SAMA SEPERTI SEBELUMNYA ...
// (Untuk menghemat ruang pesan, letakkan ulang semua kode fungsi pendukung yang tidak berubah di bawah sini:
// ProfilContent, SettingsItem, LaporanContent, LaporanTab, LaporanCard, TopBar, AbsenCard, MenuCard, SummaryCard, SummaryItem, HistoryList, HistoryItem, DockNavigationBar, BottomNavItem, NotificationSheetContent, NotificationItem)

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
                Text("Halo, Feisal \uD83D\uDC4B", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("SABTU, 20 JUNI 2026", color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Box(modifier = Modifier.size(40.dp).background(colors.green, CircleShape), contentAlignment = Alignment.Center) {
                Text("F", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.size(90.dp)) {
                Box(modifier = Modifier.size(80.dp).align(Alignment.Center).background(Brush.linearGradient(listOf(colors.blue, colors.green)), RoundedCornerShape(24.dp)), contentAlignment = Alignment.Center) {
                    Text("A", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.size(28.dp).align(Alignment.BottomEnd).background(cardBgColor, CircleShape).border(1.dp, colors.border, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = colors.text0, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("ah masa", color = colors.text0, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("IT Support · Padepokan 79", color = colors.text1, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) { Text("ID P79-0142", color = colors.text1, fontSize = 12.sp) }
                Box(modifier = Modifier.background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) { Text("Bergabung Jan 2024", color = colors.text1, fontSize = 12.sp) }
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
            SettingsItem(colors, iconBgColor, Icons.Default.LocationOn, colors.blue, "Lokasi Kantor Terdaftar", "Kantor Pusat · radius 100m")
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("LAINNYA", color = colors.text1, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Column(modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp))) {
            SettingsItem(colors, iconBgColor, Icons.Default.Notifications, colors.red, "Notifikasi", null, trailing = { Switch(checked = true, onCheckedChange = {}, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = colors.blue)) })
            Divider(color = colors.border, modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(colors, iconBgColor, Icons.Default.Info, colors.text1, "Bantuan & Dukungan", null)
            Divider(color = colors.border, modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(colors, iconBgColor, Icons.Default.Info, colors.text1, "Tentang Aplikasi", "v1.0.0", showArrow = false)
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
        Text("PADEPOKAN 79 · v1.0.0", color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
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
fun LaporanContent(colors: P79Colors, isDarkMode: Boolean, onBellClick: () -> Unit) {
    val cardBgColor = if (isDarkMode) Color(0xFF161D2F) else Color.White
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(48.dp)); TopBar(colors = colors, isDarkMode = isDarkMode, onBellClick = onBellClick); Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.padding(horizontal = 24.dp)) { Text("Laporan Kerja", color = colors.text0, fontSize = 24.sp, fontWeight = FontWeight.Bold); Text("Riwayat & pengajuan laporan harian", color = colors.text1, fontSize = 12.sp) }
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LaporanTab(text = "Semua", isSelected = true, colors = colors, cardBgColor = cardBgColor); LaporanTab(text = "Disetujui", isSelected = false, colors = colors, cardBgColor = cardBgColor); LaporanTab(text = "Menunggu", isSelected = false, colors = colors, cardBgColor = cardBgColor); LaporanTab(text = "Revisi", isSelected = false, colors = colors, cardBgColor = cardBgColor)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                LaporanCard(date = "JUM · 19 JUN 2026", status = "Disetujui", statusColor = colors.green, title = "Maintenance Server Produksi", desc = "Melakukan patching OS, cek backup harian, dan restart service yang error pada server utama.", time = "6 jam", attachments = "2 lampiran", colors = colors, cardBgColor = cardBgColor, isDarkMode = isDarkMode)
                LaporanCard(date = "KAM · 18 JUN 2026", status = "Menunggu", statusColor = colors.amber, title = "Develop Fitur Absensi GPS", desc = "Implementasi validasi radius lokasi kantor dan integrasi CameraX untuk foto absen masuk.", time = "7.5 jam", attachments = "1 lampiran", colors = colors, cardBgColor = cardBgColor, isDarkMode = isDarkMode)
                LaporanCard(date = "RAB · 17 JUN 2026", status = "Revisi", statusColor = colors.red, title = "Meeting Klien & Revisi UI", desc = "Diskusi kebutuhan tambahan dari klien, catatan revisi warna dan layout dashboard.", time = "4 jam", attachments = null, colors = colors, cardBgColor = cardBgColor, isDarkMode = isDarkMode)
            }
            Spacer(modifier = Modifier.height(130.dp))
        }
        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 100.dp).size(60.dp).clip(RoundedCornerShape(20.dp)).background(Brush.linearGradient(listOf(colors.blue, colors.green))).clickable { }, contentAlignment = Alignment.Center) { Text("+", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Light, modifier = Modifier.padding(bottom = 6.dp)) }
    }
}

@Composable
fun LaporanTab(text: String, isSelected: Boolean, colors: P79Colors, cardBgColor: Color) { Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(if (isSelected) colors.blue.copy(alpha = 0.2f) else cardBgColor).border(1.dp, if (isSelected) colors.blue else colors.border, RoundedCornerShape(20.dp)).clickable { }.padding(horizontal = 20.dp, vertical = 10.dp), contentAlignment = Alignment.Center) { Text(text, color = if (isSelected) colors.blue else colors.text1, fontSize = 12.sp, fontWeight = FontWeight.Bold) } }

@Composable
fun LaporanCard(date: String, status: String, statusColor: Color, title: String, desc: String, time: String, attachments: String?, colors: P79Colors, cardBgColor: Color, isDarkMode: Boolean) {
    val iconBgColor = if (isDarkMode) Color(0xFF222831) else Color(0xFFF3F4F6)
    Column(modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp)).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(date, color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp); Box(modifier = Modifier.background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) { Text(status, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold) } }
        Spacer(modifier = Modifier.height(12.dp)); Text(title, color = colors.text0, fontSize = 16.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(8.dp)); Text(desc, color = colors.text1, fontSize = 12.sp, lineHeight = 18.sp); Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(iconBgColor, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 6.dp)) { Icon(Icons.Default.Info, contentDescription = null, tint = colors.text1, modifier = Modifier.size(12.dp)); Spacer(modifier = Modifier.width(6.dp)); Text(time, color = colors.text1, fontSize = 10.sp) }
            if (attachments != null) { Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(colors.blue.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).border(1.dp, colors.blue.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 6.dp)) { Icon(Icons.Default.Info, contentDescription = null, tint = colors.blue, modifier = Modifier.size(12.dp)); Spacer(modifier = Modifier.width(6.dp)); Text(attachments, color = colors.blue, fontSize = 10.sp) } }
        }
    }
}

@Composable
fun TopBar(colors: P79Colors, isDarkMode: Boolean, onBellClick: () -> Unit) {
    val iconBgColor = if (isDarkMode) Color(0xFF161D2F) else Color.White
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) { Text(text = "Halo, Feisal \uD83D\uDC4B", color = colors.text0, fontSize = 24.sp, fontWeight = FontWeight.Bold); Text(text = "SABTU, 20 JUNI 2026", color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp) }
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(iconBgColor).border(1.dp, colors.border, CircleShape).clickable { onBellClick() }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Notifications, contentDescription = "Notifikasi", tint = colors.text1, modifier = Modifier.size(20.dp)); Box(modifier = Modifier.size(8.dp).background(colors.red, CircleShape).align(Alignment.TopEnd).padding(2.dp)) }
        Spacer(modifier = Modifier.width(12.dp))
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Brush.linearGradient(listOf(colors.blue, colors.green))), contentAlignment = Alignment.Center) { Text("F", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
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
fun MenuCard(colors: P79Colors, cardBgColor: Color, title: String, iconColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit) { Box(modifier = modifier.height(90.dp).background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp)).clickable { onClick() }, contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Info, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp)); Spacer(modifier = Modifier.height(8.dp)); Text(title, color = colors.text0, fontSize = 12.sp) } } }

@Composable
fun SummaryCard(colors: P79Colors, cardBgColor: Color) { Box(modifier = Modifier.padding(horizontal = 24.dp)) { Column(modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp)).padding(20.dp)) { Text("RINGKASAN BULAN INI", color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp); Spacer(modifier = Modifier.height(16.dp)); Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { SummaryItem(colors = colors, count = "18", label = "Hadir", color = colors.green); SummaryItem(colors = colors, count = "2", label = "Izin", color = colors.amber); SummaryItem(colors = colors, count = "1", label = "Telat", color = colors.red); SummaryItem(colors = colors, count = "0", label = "Alpha", color = colors.blue) } } } }

@Composable
fun SummaryItem(colors: P79Colors, count: String, label: String, color: Color) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(count, color = color, fontSize = 24.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(4.dp)); Text(label, color = colors.text1, fontSize = 12.sp) } }

@Composable
fun HistoryList(colors: P79Colors, cardBgColor: Color) { Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { HistoryItem(colors = colors, cardBgColor = cardBgColor, date = "JUN\n19", title = "Hadir Lengkap", desc = "08:01 - 17:05 · GPS terverifikasi", iconColor = colors.green); HistoryItem(colors = colors, cardBgColor = cardBgColor, date = "JUN\n18", title = "Izin Sakit", desc = "Bukti: surat_dokter.pdf", iconColor = colors.amber) } }

@Composable
fun HistoryItem(colors: P79Colors, cardBgColor: Color, date: String, title: String, desc: String, iconColor: Color) { Row(modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Text(date, color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(30.dp)); Spacer(modifier = Modifier.width(16.dp)); Column(modifier = Modifier.weight(1f)) { Text(title, color = colors.text0, fontSize = 14.sp, fontWeight = FontWeight.Bold); Text(desc, color = colors.text1, fontSize = 12.sp) }; Box(modifier = Modifier.size(24.dp).background(iconColor, RoundedCornerShape(6.dp))) } }

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
fun BottomNavItem(colors: P79Colors, icon: ImageVector, label: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) { Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)) { Icon(icon, contentDescription = label, tint = if (isSelected) Color.White else colors.text1, modifier = Modifier.size(24.dp)); Spacer(modifier = Modifier.height(4.dp)); Text(label, color = if (isSelected) Color.White else colors.text1, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) } }

@Composable
fun NotificationSheetContent(colors: P79Colors, isDarkMode: Boolean) {
    val iconBgColor = if (isDarkMode) Color(0xFF222831) else Color(0xFFF3F4F6)
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Notifikasi", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold); Text("Tandai semua dibaca", color = colors.blue, fontSize = 12.sp, fontWeight = FontWeight.Medium) }
        Spacer(modifier = Modifier.height(24.dp))
        NotificationItem(colors = colors, iconBgColor = iconBgColor, icon = Icons.Default.Check, iconTint = colors.green, title = "Pengajuan izin disetujui", desc = "Izin sakit tanggal 18 Jun kamu sudah disetujui atasan.", time = "Hari ini · 09:14", isUnread = true)
        NotificationItem(colors = colors, iconBgColor = iconBgColor, icon = Icons.Default.Warning, iconTint = colors.amber, title = "Jangan lupa absen pulang", desc = "Kamu belum melakukan absen pulang hari ini.", time = "Hari ini · 17:30", isUnread = true)
        NotificationItem(colors = colors, iconBgColor = iconBgColor, icon = Icons.Default.Info, iconTint = colors.blue, title = "Laporan kerja perlu revisi", desc = "\"Meeting Klien & Revisi UI\" diminta direvisi oleh atasan.", time = "Kemarin · 16:02", isUnread = false)
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun NotificationItem(colors: P79Colors, iconBgColor: Color, icon: ImageVector, iconTint: Color, title: String, desc: String, time: String, isUnread: Boolean) { Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(40.dp).background(iconBgColor, CircleShape), contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp)) }; Spacer(modifier = Modifier.width(16.dp)); Column(modifier = Modifier.weight(1f)) { Text(title, color = colors.text0, fontSize = 14.sp, fontWeight = FontWeight.Bold); Text(desc, color = colors.text1, fontSize = 12.sp, lineHeight = 18.sp); Spacer(modifier = Modifier.height(4.dp)); Text(time, color = colors.text1.copy(alpha = 0.6f), fontSize = 10.sp) }; if (isUnread) { Box(modifier = Modifier.size(8.dp).background(colors.blue, CircleShape)) } } }