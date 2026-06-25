package com.feisal.workingreport

import androidx.core.view.WindowCompat
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            val colors = p79Colors(isDark = true)
            var showNotificationSheet by remember { mutableStateOf(false) }
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
                                activity = this@DashboardActivity,
                                onLaporClick = { coroutineScope.launch { pagerState.animateScrollToPage(2) } },
                                onRiwayatClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                                onBellClick = { showNotificationSheet = true }
                            )
                            1 -> RiwayatContent(
                                onBackClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
                            )
                            2 -> LaporanContent(colors) { showNotificationSheet = true }
                            3 -> ProfilContent(
                                onBackClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
                            )
                        }
                    }
                }

                DockNavigationBar(
                    colors = colors,
                    selectedIndex = pagerState.currentPage,
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
                    containerColor = Color(0xFF161D2F),
                    scrimColor = Color.Black.copy(alpha = 0.5f)
                ) {
                    NotificationSheetContent(colors = colors)
                }
            }
        }
    }
}

@Composable
fun LaporanContent(colors: P79Colors, onBellClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            TopBar(colors = colors, onBellClick = onBellClick)
            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text("Laporan Kerja", color = colors.text0, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Riwayat & pengajuan laporan harian", color = colors.text1, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LaporanTab(text = "Semua", isSelected = true, colors = colors)
                LaporanTab(text = "Disetujui", isSelected = false, colors = colors)
                LaporanTab(text = "Menunggu", isSelected = false, colors = colors)
                LaporanTab(text = "Revisi", isSelected = false, colors = colors)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LaporanCard(date = "JUM · 19 JUN 2026", status = "Disetujui", statusColor = colors.green, title = "Maintenance Server Produksi", desc = "Melakukan patching OS, cek backup harian, dan restart service yang error pada server utama.", time = "6 jam", attachments = "2 lampiran", colors = colors)
                LaporanCard(date = "KAM · 18 JUN 2026", status = "Menunggu", statusColor = colors.amber, title = "Develop Fitur Absensi GPS", desc = "Implementasi validasi radius lokasi kantor dan integrasi CameraX untuk foto absen masuk.", time = "7.5 jam", attachments = "1 lampiran", colors = colors)
                LaporanCard(date = "RAB · 17 JUN 2026", status = "Revisi", statusColor = colors.red, title = "Meeting Klien & Revisi UI", desc = "Diskusi kebutuhan tambahan dari klien, catatan revisi warna dan layout dashboard.", time = "4 jam", attachments = null, colors = colors)
            }

            Spacer(modifier = Modifier.height(130.dp))
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 100.dp)
                .size(60.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(colors.blue, colors.green)))
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Text("+", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Light, modifier = Modifier.padding(bottom = 6.dp))
        }
    }
}

@Composable
fun LaporanTab(text: String, isSelected: Boolean, colors: P79Colors) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) colors.blue.copy(alpha = 0.2f) else Color(0xFF161D2F))
            .border(1.dp, if (isSelected) colors.blue else colors.border, RoundedCornerShape(20.dp))
            .clickable { }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (isSelected) colors.blue else colors.text1, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LaporanCard(date: String, status: String, statusColor: Color, title: String, desc: String, time: String, attachments: String?, colors: P79Colors) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF161D2F), RoundedCornerShape(16.dp))
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(date, color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Box(
                modifier = Modifier
                    .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(status, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, color = colors.text0, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(desc, color = colors.text1, fontSize = 12.sp, lineHeight = 18.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(Color(0xFF222831), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 6.dp)) {
                Icon(Icons.Default.Info, contentDescription = null, tint = colors.text1, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(time, color = colors.text1, fontSize = 10.sp)
            }
            if (attachments != null) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(colors.blue.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).border(1.dp, colors.blue.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = colors.blue, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(attachments, color = colors.blue, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun HomeContent(colors: P79Colors, activity: AppCompatActivity, onLaporClick: () -> Unit, onRiwayatClick: () -> Unit, onBellClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        TopBar(colors = colors, onBellClick = onBellClick)
        Spacer(modifier = Modifier.height(24.dp))
        AbsenCard(colors = colors, activity = activity)
        Spacer(modifier = Modifier.height(16.dp))
        MenuRow(colors = colors, activity = activity, onLaporClick = onLaporClick, onRiwayatClick = onRiwayatClick)
        Spacer(modifier = Modifier.height(16.dp))
        SummaryCard(colors = colors)
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "RIWAYAT TERBARU",
            color = colors.text1,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        HistoryList(colors = colors)
        Spacer(modifier = Modifier.height(130.dp))
    }
}

@Composable
fun TopBar(colors: P79Colors, onBellClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Halo, Feisal \uD83D\uDC4B",
                color = colors.text0,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "SABTU, 20 JUNI 2026",
                color = colors.text1,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF161D2F))
                .border(1.dp, colors.border, CircleShape)
                .clickable { onBellClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Notifications, contentDescription = "Notifikasi", tint = colors.text1, modifier = Modifier.size(20.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(colors.red, CircleShape)
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(colors.blue, colors.green))),
            contentAlignment = Alignment.Center
        ) {
            Text("F", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun AbsenCard(colors: P79Colors, activity: AppCompatActivity) {
    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
        GlassCard(colors = colors, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF222831), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = colors.amber, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Belum Absen Masuk", color = colors.text0, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Kantor Pusat · radius 100m", color = colors.text1, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("MASUK", color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("--:--", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(colors.border))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PULANG", color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("--:--", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    activity.startActivity(Intent(activity, CameraAbsenActivity::class.java))
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(listOf(colors.blue, colors.green)),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("ABSEN MASUK", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MenuRow(colors: P79Colors, activity: AppCompatActivity, onLaporClick: () -> Unit, onRiwayatClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MenuCard(colors = colors, title = "Ajukan Izin", iconColor = colors.amber, modifier = Modifier.weight(1f)) {
            val bottomSheet = IzinBottomSheetFragment()
            bottomSheet.show(activity.supportFragmentManager, "IzinBottomSheet")
        }
        MenuCard(colors = colors, title = "Lapor Kerja", iconColor = colors.green, modifier = Modifier.weight(1f)) {
            onLaporClick()
        }
        MenuCard(colors = colors, title = "Riwayat", iconColor = colors.blue, modifier = Modifier.weight(1f)) {
            onRiwayatClick()
        }
    }
}

@Composable
fun MenuCard(colors: P79Colors, title: String, iconColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(90.dp)
            .background(Color(0xFF161D2F), RoundedCornerShape(16.dp))
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Info, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, color = colors.text0, fontSize = 12.sp)
        }
    }
}

@Composable
fun SummaryCard(colors: P79Colors) {
    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF161D2F), RoundedCornerShape(16.dp))
                .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Text("RINGKASAN BULAN INI", color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryItem(colors = colors, count = "18", label = "Hadir", color = colors.green)
                SummaryItem(colors = colors, count = "2", label = "Izin", color = colors.amber)
                SummaryItem(colors = colors, count = "1", label = "Telat", color = colors.red)
                SummaryItem(colors = colors, count = "0", label = "Alpha", color = colors.blue)
            }
        }
    }
}

@Composable
fun SummaryItem(colors: P79Colors, count: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count, color = color, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = colors.text1, fontSize = 12.sp)
    }
}

@Composable
fun HistoryList(colors: P79Colors) {
    Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        HistoryItem(colors = colors, date = "JUN\n19", title = "Hadir Lengkap", desc = "08:01 - 17:05 · GPS terverifikasi", iconColor = colors.green)
        HistoryItem(colors = colors, date = "JUN\n18", title = "Izin Sakit", desc = "Bukti: surat_dokter.pdf", iconColor = colors.amber)
    }
}

@Composable
fun HistoryItem(colors: P79Colors, date: String, title: String, desc: String, iconColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF161D2F), RoundedCornerShape(16.dp))
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(date, color = colors.text1, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(30.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = colors.text0, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(desc, color = colors.text1, fontSize = 12.sp)
        }
        Box(modifier = Modifier.size(24.dp).background(iconColor, RoundedCornerShape(6.dp)))
    }
}

@Composable
fun DockNavigationBar(colors: P79Colors, selectedIndex: Int, onItemSelected: (Int) -> Unit, modifier: Modifier = Modifier) {
    var dragOffset by remember { mutableStateOf(0f) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { dragOffset = 0f },
                    onDragEnd = {
                        if (dragOffset < -50f && selectedIndex < 3) {
                            onItemSelected(selectedIndex + 1)
                        } else if (dragOffset > 50f && selectedIndex > 0) {
                            onItemSelected(selectedIndex - 1)
                        }
                        dragOffset = 0f
                    }
                ) { change, dragAmount ->
                    change.consume()
                    dragOffset += dragAmount
                }
            },
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .height(64.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xD9161D2F))
                .border(1.dp, colors.border, RoundedCornerShape(32.dp))
        ) {
            val itemWidth = maxWidth / 3

            val targetOffset = if (selectedIndex < 3) itemWidth * selectedIndex else itemWidth * 2
            val indicatorOffset by animateDpAsState(
                targetValue = targetOffset,
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
                label = "indicatorOffset"
            )

            val indicatorAlpha by animateFloatAsState(
                targetValue = if (selectedIndex < 3) 1f else 0f,
                label = "indicatorAlpha"
            )

            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(itemWidth)
                    .fillMaxHeight()
                    .padding(6.dp)
                    .graphicsLayer(alpha = indicatorAlpha)
                    .background(
                        Brush.linearGradient(listOf(colors.blue, colors.green)),
                        RoundedCornerShape(26.dp)
                    )
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(colors = colors, icon = Icons.Default.Home, label = "Home", isSelected = selectedIndex == 0, modifier = Modifier.weight(1f)) { onItemSelected(0) }
                BottomNavItem(colors = colors, icon = Icons.Default.DateRange, label = "Riwayat", isSelected = selectedIndex == 1, modifier = Modifier.weight(1f)) { onItemSelected(1) }
                BottomNavItem(colors = colors, icon = Icons.Default.Edit, label = "Laporan", isSelected = selectedIndex == 2, modifier = Modifier.weight(1f)) { onItemSelected(2) }
            }
        }

        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                // PERBAIKAN: Menggunakan SolidColor agar Kotlin tidak bingung
                .background(if (selectedIndex == 3) Brush.linearGradient(listOf(colors.blue, colors.green)) else SolidColor(Color(0xD9161D2F)))
                .border(1.dp, if (selectedIndex == 3) Color.Transparent else colors.border, CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onItemSelected(3) }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profil",
                tint = if (selectedIndex == 3) Color.White else colors.text1,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
fun BottomNavItem(
    colors: P79Colors,
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Icon(icon, contentDescription = label, tint = if (isSelected) Color.White else colors.text1, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = if (isSelected) Color.White else colors.text1, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun NotificationSheetContent(colors: P79Colors) {
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Notifikasi", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Tandai semua dibaca", color = colors.blue, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(24.dp))
        NotificationItem(colors = colors, icon = Icons.Default.Check, iconColor = colors.green, title = "Pengajuan izin disetujui", desc = "Izin sakit tanggal 18 Jun kamu sudah disetujui atasan.", time = "Hari ini · 09:14", isUnread = true)
        NotificationItem(colors = colors, icon = Icons.Default.Warning, iconColor = colors.amber, title = "Jangan lupa absen pulang", desc = "Kamu belum melakukan absen pulang hari ini.", time = "Hari ini · 17:30", isUnread = true)
        NotificationItem(colors = colors, icon = Icons.Default.Info, iconColor = colors.blue, title = "Laporan kerja perlu revisi", desc = "\"Meeting Klien & Revisi UI\" diminta direvisi oleh atasan.", time = "Kemarin · 16:02", isUnread = false)
        NotificationItem(colors = colors, icon = Icons.Default.Warning, iconColor = colors.red, title = "Terlambat absen masuk", desc = "Absen masuk tanggal 17 Jun tercatat jam 08:21.", time = "3 hari lalu", isUnread = false)
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun NotificationItem(
    colors: P79Colors,
    icon: ImageVector,
    iconColor: Color,
    title: String,
    desc: String,
    time: String,
    isUnread: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = colors.text0, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(desc, color = colors.text1, fontSize = 12.sp, lineHeight = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(time, color = colors.text1.copy(alpha = 0.6f), fontSize = 10.sp)
        }

        if (isUnread) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(colors.blue, CircleShape)
            )
        }
    }
}