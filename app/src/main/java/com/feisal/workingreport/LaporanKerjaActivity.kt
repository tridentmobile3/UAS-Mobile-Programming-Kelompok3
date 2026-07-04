package com.feisal.workingreport

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class LaporanKerjaActivity : AppCompatActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var showBottomSheet by remember { mutableStateOf(false) }
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            Box(modifier = Modifier.fillMaxSize()) {
                LaporanKerjaScreen(
                    onBack = { finish() },
                    onFabClick = { showBottomSheet = true }
                )

                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showBottomSheet = false },
                        sheetState = sheetState,
                        containerColor = Color(0xFF161D2F),
                        scrimColor = Color.Black.copy(alpha = 0.5f)
                    ) {
                        LaporanBottomSheetContent(onDismiss = { showBottomSheet = false })
                    }
                }
            }
        }
    }
}

@Composable
fun LaporanKerjaScreen(onBack: () -> Unit, onFabClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B101E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
                .padding(20.dp)
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
                        .clickable { onBack() }
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
                text = "Laporan Kerja",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp)
            )

            Text(
                text = "Riwayat & pengajuan laporan harian",
                color = Color(0xFF8B95A5),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 24.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                FilterChip(text = "Semua", isActive = true)
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(text = "Disetujui", isActive = false)
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(text = "Menunggu", isActive = false)
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161D2F)),
                border = BorderStroke(1.dp, Color(0xFF2D3548)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "JUM · 19 JUN 2026",
                            color = Color(0xFF8B95A5),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF1A2A22), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Disetujui",
                                color = Color(0xFF2ECC71),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "Maintenance Server Produksi",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Text(
                        text = "Melakukan patching OS, cek backup harian, dan restart service yang error pada server utama.",
                        color = Color(0xFF8B95A5),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        DashedTag(
                            text = "⏱ 6 jam",
                            textColor = Color(0xFF8B95A5),
                            backgroundColor = Color(0xFF222831),
                            strokeColor = Color(0xFF8B95A5)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        DashedTag(
                            text = "🔗 2 lampiran",
                            textColor = Color(0xFF3498DB),
                            backgroundColor = Color(0xFF1E3A8A),
                            strokeColor = Color(0xFF3498DB)
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 100.dp)
                .size(60.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFF3498DB), Color(0xFF2980B9))))
                .clickable { onFabClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun FilterChip(text: String, isActive: Boolean) {
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
fun DashedTag(text: String, textColor: Color, backgroundColor: Color, strokeColor: Color) {
    Box(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = strokeColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(4.dp)
            )
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 10.sp
        )
    }
}