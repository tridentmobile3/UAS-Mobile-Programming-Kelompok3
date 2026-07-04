package com.feisal.workingreport

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class DashboardAdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DashboardAdminScreen()
        }
    }
}

@Composable
fun DashboardAdminScreen() {
    val context = LocalContext.current

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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mode Admin \uD83D\uDEE0️",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "MONITORING KEHADIRAN Absensi & Laporan Kerja",
                        color = Color(0xFF8B95A5),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFE74C3C), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "A",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161D2F)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2D3548)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF222831), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF3498DB),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text(
                                text = "Total Karyawan: 120 Orang",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Semua divisi · Padepokan 79",
                                color = Color(0xFF8B95A5),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "HADIR", color = Color(0xFF2ECC71), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(text = "102", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                        }
                        Box(modifier = Modifier.width(1.dp).height(50.dp).background(Color(0xFF2D3548)))
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "IZIN/SAKIT", color = Color(0xFFF4D03F), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(text = "5", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                        }
                        Box(modifier = Modifier.width(1.dp).height(50.dp).background(Color(0xFF2D3548)))
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "BELUM ABSEN", color = Color(0xFFE74C3C), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(text = "13", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xFF3498DB), Color(0xFF2980B9))),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                Toast.makeText(context, "Mengekspor rekap kehadiran harian ke file .xlsx...", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "EXPORT LAPORAN (EXCEL)",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AdminActionCard(
                    modifier = Modifier.weight(1f).padding(end = 6.dp),
                    icon = Icons.Default.Place,
                    iconTint = Color(0xFF3498DB),
                    title = "Verifikasi Kehadiran",
                    onClick = { Toast.makeText(context, "Membuka Menu Verifikasi Kehadiran", Toast.LENGTH_SHORT).show() }
                )
                AdminActionCard(
                    modifier = Modifier.weight(1f).padding(horizontal = 3.dp),
                    icon = Icons.Default.Edit,
                    iconTint = Color(0xFF2ECC71),
                    title = "Laporan Rutin",
                    onClick = { Toast.makeText(context, "Membuka Menu Laporan Rutin", Toast.LENGTH_SHORT).show() }
                )
                AdminActionCard(
                    modifier = Modifier.weight(1f).padding(start = 6.dp),
                    icon = Icons.Default.DateRange,
                    iconTint = Color(0xFFF4D03F),
                    title = "Laporan Lembur",
                    onClick = { Toast.makeText(context, "Membuka Menu Laporan Lembur", Toast.LENGTH_SHORT).show() }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF22283A), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "📅 16 Apr 2022",
                        color = Color(0xFF8B95A5),
                        fontSize = 11.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp)
                ) {
                    Text(
                        text = "PENDING KEHADIRAN (APPROVAL)",
                        color = Color(0xFF8B95A5),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    ApprovalCard(
                        title = "Diky Raihan S.\n(Izin: Sakit)",
                        subtitle = "Lihat Detail Absensi",
                        subtitleColor = Color(0xFF3498DB),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    ApprovalCard(
                        title = "Diky Raihan S.\n(Lembur)",
                        subtitle = "Status: Pengajuan Lembur",
                        subtitleColor = Color(0xFFF4D03F),
                        icon = Icons.Default.CheckCircle,
                        iconTint = Color(0xFF2ECC71)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 6.dp)
                ) {
                    Text(
                        text = "IKHTISAR LAPORAN KERJA",
                        color = Color(0xFF8B95A5),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    ReportCard(
                        title = "Budi Santoso",
                        content = "Laporan Harian (Rutin):\n15 Selesai",
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        Row {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2ECC71), modifier = Modifier.size(18.dp).padding(end = 6.dp))
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFE74C3C), modifier = Modifier.size(18.dp))
                        }
                    }

                    ReportCard(
                        title = "Siti Aminah",
                        content = "Laporan Lembur:\n5 Jam (Total)"
                    ) {
                        Icon(Icons.Default.List, contentDescription = null, tint = Color(0xFF3498DB), modifier = Modifier.size(28.dp))
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(35.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xE6161D2F)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
            modifier = Modifier
                .fillMaxWidth()
                .height(94.dp)
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(Modifier.weight(1f), Icons.Default.Place, "Dashboard", true) {}
                BottomNavItem(Modifier.weight(1f), Icons.Default.Edit, "Presensi", false) {
                    Toast.makeText(context, "Menu Presensi Karyawan", Toast.LENGTH_SHORT).show()
                }
                BottomNavItem(Modifier.weight(1f), Icons.Default.List, "Laporan Kerja", false) {
                    Toast.makeText(context, "Menu Rekap Laporan Kerja", Toast.LENGTH_SHORT).show()
                }
                BottomNavItem(Modifier.weight(1f), Icons.Default.Person, "Profil", false) {
                    context.startActivity(Intent(context, DashboardActivity::class.java))
                }
            }
        }
    }
}

@Composable
fun AdminActionCard(modifier: Modifier = Modifier, icon: ImageVector, iconTint: Color, title: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161D2F)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2D3548)),
        modifier = modifier
            .height(95.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(26.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 6.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun ApprovalCard(modifier: Modifier = Modifier, title: String, subtitle: String, subtitleColor: Color, icon: ImageVector? = null, iconTint: Color? = null) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161D2F)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2D3548)),
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Column(modifier = Modifier.align(Alignment.TopStart)) {
                Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = subtitle, color = subtitleColor, fontSize = 11.sp)
            }
            if (icon != null && iconTint != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.align(Alignment.BottomEnd).size(22.dp)
                )
            }
        }
    }
}

@Composable
fun ReportCard(modifier: Modifier = Modifier, title: String, content: String, actionContent: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161D2F)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2D3548)),
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Column(modifier = Modifier.align(Alignment.TopStart)) {
                Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = content, color = Color(0xFF8B95A5), fontSize = 11.sp)
            }
            Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                actionContent()
            }
        }
    }
}

@Composable
fun BottomNavItem(modifier: Modifier = Modifier, icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val color = if (isSelected) Color.White else Color(0xFF8B95A5)
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Text(text = label, color = color, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
    }
}