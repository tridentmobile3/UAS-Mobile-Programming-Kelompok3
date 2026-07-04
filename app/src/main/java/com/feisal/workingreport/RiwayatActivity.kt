package com.feisal.workingreport

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RiwayatContent(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B101E))
            .padding(top = 48.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 16.dp)
                        .clickable { onBackClick() }
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Halo, Feisal 👋",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "SABTU, 20 JUNI 2026",
                        color = Color(0xFF8B95A5),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.1.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF009688), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "F",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "Riwayat Absensi",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp)
            )

            Text(
                text = "Pantau histori kehadiran kamu",
                color = Color(0xFF8B95A5),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161D2F)),
                    border = BorderStroke(1.dp, Color(0xFF2D3548)),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = Color(0xFF8B95A5))
                    }
                }

                Text(
                    text = "Juni 2026",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161D2F)),
                    border = BorderStroke(1.dp, Color(0xFF2D3548)),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF8B95A5))
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatsCard(
                    modifier = Modifier.weight(1f),
                    value = "92%",
                    label = "KEHADIRAN"
                ) {
                    Box(modifier = Modifier.size(12.dp).background(Color(0xFF2ECC71), CircleShape))
                }
                StatsCard(
                    modifier = Modifier.weight(1f),
                    value = "08:06",
                    label = "RATA² MASUK"
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF3498DB), modifier = Modifier.size(16.dp))
                }
                StatsCard(
                    modifier = Modifier.weight(1f),
                    value = "1×",
                    label = "TERLAMBAT"
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF4D03F), modifier = Modifier.size(16.dp))
                }
                StatsCard(
                    modifier = Modifier.weight(1f),
                    value = "0×",
                    label = "GPS GAGAL"
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFE74C3C), modifier = Modifier.size(16.dp))
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161D2F)),
                border = BorderStroke(1.dp, Color(0xFF2D3548)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        val days = listOf("S", "S", "R", "K", "J", "S", "M")
                        days.forEach { day ->
                            Text(
                                text = day,
                                color = Color(0xFF8B95A5),
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Column {
                        val row1 = listOf(Color(0xFF2ECC71), Color(0xFF2ECC71), Color(0xFF2ECC71), Color(0xFF2ECC71), Color(0xFF2ECC71), Color(0xFF2ECC71), Color(0xFF222831))
                        val row2 = listOf(Color(0xFF2ECC71), Color(0xFF2ECC71), Color(0xFF2ECC71), Color(0xFF2ECC71), Color(0xFFF4D03F), Color(0xFF2ECC71), Color(0xFF222831))
                        val row3 = listOf(Color(0xFF2ECC71), Color(0xFF2ECC71), Color(0xFFE74C3C), Color(0xFFF4D03F), Color(0xFF2ECC71), Color.Transparent, Color(0xFF222831))
                        val row4 = listOf(Color(0xFF222831), Color(0xFF222831), Color(0xFF222831), Color(0xFF222831), Color(0xFF222831), Color(0xFF222831), Color(0xFF222831))

                        HeatmapRow(row1)
                        Spacer(modifier = Modifier.height(8.dp))
                        HeatmapRow(row2)
                        Spacer(modifier = Modifier.height(8.dp))
                        HeatmapRowWithBorderItem(row3)
                        Spacer(modifier = Modifier.height(8.dp))
                        HeatmapRow(row4)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendItem(color = Color(0xFF2ECC71), text = "Hadir")
                        Spacer(modifier = Modifier.width(16.dp))
                        LegendItem(color = Color(0xFFF4D03F), text = "Izin")
                        Spacer(modifier = Modifier.width(16.dp))
                        LegendItem(color = Color(0xFFE74C3C), text = "Telat")
                        Spacer(modifier = Modifier.width(16.dp))
                        LegendItem(color = Color(0xFF222831), text = "Libur")
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 16.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                HistoryFilterChip(text = "Semua", isActive = true)
                Spacer(modifier = Modifier.width(8.dp))
                HistoryFilterChip(text = "Hadir", isActive = false)
                Spacer(modifier = Modifier.width(8.dp))
                HistoryFilterChip(text = "Izin", isActive = false)
                Spacer(modifier = Modifier.width(8.dp))
                HistoryFilterChip(text = "Telat", isActive = false)
            }

            RecordCard(
                dateNum = "19",
                title = "Hadir Lengkap",
                subtitle = "08:01 - 17:05 · GPS terverifikasi",
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color(0xFF2ECC71), CircleShape)
                )
            }

            RecordCard(
                dateNum = "18",
                title = "Izin Sakit",
                subtitle = "Bukti: surat_dokter.pdf",
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFFF4D03F),
                    modifier = Modifier.size(24.dp)
                )
            }

            RecordCard(
                dateNum = "17",
                title = "Hadir, Terlambat",
                subtitle = "08:21 - 17:00"
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFE74C3C),
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = "Lihat Riwayat Lengkap →",
                color = Color(0xFF3498DB),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StatsCard(modifier: Modifier = Modifier, value: String, label: String, icon: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161D2F)),
        border = BorderStroke(1.dp, Color(0xFF2D3548)),
        modifier = modifier.height(90.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon()
            Text(
                text = value,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = label,
                color = Color(0xFF8B95A5),
                fontSize = 8.sp
            )
        }
    }
}

@Composable
fun HeatmapRow(colors: List<Color>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        colors.forEach { color ->
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = color),
                    modifier = Modifier.size(34.dp)
                ) {}
            }
        }
    }
}

@Composable
fun HeatmapRowWithBorderItem(colors: List<Color>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        colors.forEachIndexed { index, color ->
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (index == 5) {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A8A)),
                        border = BorderStroke(2.dp, Color(0xFF3498DB)),
                        modifier = Modifier.size(34.dp)
                    ) {}
                } else {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = color),
                        modifier = Modifier.size(34.dp)
                    ) {}
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color)
        )
        Text(
            text = text,
            color = Color(0xFF8B95A5),
            fontSize = 10.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
fun HistoryFilterChip(text: String, isActive: Boolean) {
    val bgColor = if (isActive) Color(0xFF1E3A8A) else Color(0xFF161D2F)
    val strokeColor = if (isActive) Color(0xFF3498DB) else Color(0xFF2D3548)
    val textColor = if (isActive) Color.White else Color(0xFF8B95A5)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, strokeColor),
        modifier = Modifier.height(36.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}

@Composable
fun RecordCard(modifier: Modifier = Modifier, dateNum: String, title: String, subtitle: String, trailingIcon: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161D2F)),
        border = BorderStroke(1.dp, Color(0xFF2D3548)),
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "JUN", color = Color(0xFF8B95A5), fontSize = 10.sp)
                Text(text = dateNum, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = Color(0xFF8B95A5), fontSize = 12.sp)
            }
            trailingIcon()
        }
    }
}