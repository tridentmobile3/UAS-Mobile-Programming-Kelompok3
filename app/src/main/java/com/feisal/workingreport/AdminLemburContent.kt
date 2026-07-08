package com.feisal.workingreport

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feisal.workingreport.model.Lembur
import com.feisal.workingreport.repository.LemburRepository
import com.feisal.workingreport.ui.theme.P79Colors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLemburContent(
    colors: P79Colors,
    lemburList: List<Lembur>,
    onRefresh: () -> Unit,
    lemburRepository: LemburRepository,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedFilter by remember { mutableStateOf("ALL") }
    val filters = listOf("ALL", "PENDING", "APPROVED", "REJECTED")

    val filteredList = remember(selectedFilter, lemburList) {
        if (selectedFilter == "ALL") {
            lemburList
        } else {
            lemburList.filter { it.status.uppercase() == selectedFilter }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Approval Lembur", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // --- TAB FILTER BUTTONS (SCROLLABLE CHIPS) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { status ->
                    val isSelected = selectedFilter == status

                    // Tentukan warna border/bg chip berdasarkan statusnya agar estetik
                    val activeColor = when (status) {
                        "APPROVED" -> colors.green
                        "REJECTED" -> Color.Red
                        "PENDING" -> colors.amber
                        else -> colors.blue
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) activeColor.copy(alpha = 0.2f) else Color(0xFF161D2F).copy(alpha = 0.6f),
                                RoundedCornerShape(20.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) activeColor else Color(0xFF2D3548),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedFilter = status }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = status,
                            color = if (isSelected) activeColor else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- LIST HASIL FILTER ---
            Box(modifier = Modifier.weight(1f)) {
                if (filteredList.isEmpty()) {
                    Text(
                        text = "Tidak ada data lembur dengan status $selectedFilter",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 14.sp
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredList) { lembur ->
                            CardLemburItem(
                                colors = colors,
                                lembur = lembur,
                                onApprove = {
                                    scope.launch {
                                        lemburRepository.updateStatusLembur(lembur.id, "APPROVED").onSuccess {
                                            Toast.makeText(context, "Lembur disetujui", Toast.LENGTH_SHORT).show()
                                            onRefresh()
                                        }.onFailure {
                                            Toast.makeText(context, "Gagal memperbarui status", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onReject = {
                                    scope.launch {
                                        lemburRepository.updateStatusLembur(lembur.id, "REJECTED").onSuccess {
                                            Toast.makeText(context, "Lembur ditolak", Toast.LENGTH_SHORT).show()
                                            onRefresh()
                                        }.onFailure {
                                            Toast.makeText(context, "Gagal memperbarui status", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CardLemburItem(
    colors: P79Colors,
    lembur: Lembur,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF161D2F).copy(alpha = 0.8f), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF2D3548), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = lembur.namaKaryawan, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "${lembur.tanggal} (${lembur.jamMulai} - ${lembur.jamSelesai})", color = Color.Gray, fontSize = 12.sp)
            }

            val badgeColor = when (lembur.status.uppercase()) {
                "APPROVED" -> colors.green
                "REJECTED" -> Color.Red
                else -> colors.amber
            }
            Box(
                modifier = Modifier
                    .background(badgeColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(text = lembur.status, color = badgeColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // FOKUS UTAMA: ALASAN LEMBUR (Menggantikan judul/deskripsi laporan)
        Text(text = "Alasan Lembur:", color = colors.blue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = lembur.alasanLembur, color = Color.White, fontSize = 14.sp)

        if (lembur.status.uppercase() == "PENDING") {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.Red.copy(alpha = 0.1f), CircleShape)
                        .border(1.dp, Color.Red.copy(alpha = 0.3f), CircleShape)
                        .clickable { onReject() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Reject", tint = Color.Red, modifier = Modifier.size(18.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(colors.green.copy(alpha = 0.1f), CircleShape)
                        .border(1.dp, colors.green.copy(alpha = 0.3f), CircleShape)
                        .clickable { onApprove() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Approve", tint = colors.green, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}