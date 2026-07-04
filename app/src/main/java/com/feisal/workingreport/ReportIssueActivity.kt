package com.feisal.workingreport

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

class ReportIssueActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            LaporkanMasalahScreen(
                onBackClick = { finish() },
                onKirimClick = {
                    startActivity(Intent(this, ChatBantuanActivity::class.java))
                    finish()
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LaporkanMasalahScreen(onBackClick: () -> Unit, onKirimClick: () -> Unit) {
    val categories = listOf("Absensi Gagal", "Akun & Login", "Laporan Kerja", "Bug Tampilan", "Lainnya")
    val priorities = listOf("Rendah", "Sedang", "Tinggi")

    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var selectedPriority by remember { mutableStateOf(priorities[1]) }
    var judul by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B101E))
            .padding(top = 48.dp, bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(Color(0xFF161D2F), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF2D3548), RoundedCornerShape(10.dp))
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Laporkan Masalah", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Text("KATEGORI MASALAH", color = Color(0xFF6D7690), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp)
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { cat ->
                    val isActive = selectedCategory == cat
                    val activeBg = if (cat == "Absensi Gagal") Color(0x26EF5A6F) else Color(0x263D7BFF)
                    val activeBorder = if (cat == "Absensi Gagal") Color(0xFFEF5A6F) else Color(0xFF3D7BFF)
                    val activeText = if (cat == "Absensi Gagal") Color(0xFFEF5A6F) else Color(0xFF3D7BFF)

                    Box(
                        modifier = Modifier
                            .background(if (isActive) activeBg else Color(0x05FFFFFF), RoundedCornerShape(20.dp))
                            .border(1.dp, if (isActive) activeBorder else Color(0x1FFFFFFF), RoundedCornerShape(20.dp))
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(cat, color = if (isActive) activeText else Color(0xFFAAB3C8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("DETAIL LAPORAN", color = Color(0xFF6D7690), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141B2C), RoundedCornerShape(26.dp))
                    .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(26.dp))
                    .padding(20.dp)
            ) {
                Text("Judul Masalah", color = Color(0xFFAAB3C8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                BasicTextField(
                    value = judul,
                    onValueChange = { judul = it },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x08FFFFFF), RoundedCornerShape(12.dp))
                                .border(1.dp, if (judul.isEmpty()) Color(0x1FFFFFFF) else Color(0xFF3D7BFF), RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            if (judul.isEmpty()) Text("Contoh: Gagal absen di lokasi kantor", color = Color(0xFF6D7690), fontSize = 14.sp)
                            innerTextField()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Deskripsi", color = Color(0xFFAAB3C8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                BasicTextField(
                    value = deskripsi,
                    onValueChange = { deskripsi = it },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                    modifier = Modifier.height(100.dp),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0x08FFFFFF), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            if (deskripsi.isEmpty()) Text("Ceritakan detail masalah yang kamu alami...", color = Color(0xFF6D7690), fontSize = 14.sp)
                            innerTextField()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("LAMPIRAN (OPSIONAL)", color = Color(0xFF6D7690), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141B2C), RoundedCornerShape(26.dp))
                    .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(26.dp))
                    .padding(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .drawBehind {
                            drawRoundRect(
                                color = Color(0x1FFFFFFF),
                                style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)),
                                cornerRadius = CornerRadius(12.dp.toPx())
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF6D7690))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Maks. 3 gambar, format JPG/PNG, 5MB per file", color = Color(0xFF6D7690), fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("TINGKAT PRIORITAS", color = Color(0xFF6D7690), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                priorities.forEach { prio ->
                    val isActive = selectedPriority == prio
                    Box(
                        modifier = Modifier
                            .background(if (isActive) Color(0x263D7BFF) else Color(0x05FFFFFF), RoundedCornerShape(20.dp))
                            .border(1.dp, if (isActive) Color(0xFF3D7BFF) else Color(0x1FFFFFFF), RoundedCornerShape(20.dp))
                            .clickable { selectedPriority = prio }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(prio, color = if (isActive) Color(0xFF3D7BFF) else Color(0xFFAAB3C8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .background(Brush.horizontalGradient(listOf(Color(0xFF3D7BFF), Color(0xFF2FD3A8))), RoundedCornerShape(16.dp))
                    .clickable { onKirimClick() },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF04121E), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kirim Laporan", color = Color(0xFF04121E), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}