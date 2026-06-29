package com.feisal.workingreport

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.feisal.workingreport.ui.components.GlassCard
import com.feisal.workingreport.ui.components.NoiseOverlay
import com.feisal.workingreport.ui.theme.LiquidGlassBackground
import com.feisal.workingreport.ui.theme.p79Colors

// Data Model Dummy untuk Pegawai
data class Pegawai(val id: String, val nama: String, val divisi: String, val inisial: String, val avatarBg: Color, val statusColor: Color?)

class DaftarPegawaiActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        setContent {
            val context = LocalContext.current
            val sharedPref = remember { context.getSharedPreferences("AppPref", Context.MODE_PRIVATE) }
            val isDarkMode = sharedPref.getBoolean("isDarkMode", true)
            val colors = p79Colors(isDark = isDarkMode)

            var searchQuery by remember { mutableStateOf("") }
            var selectedMenuIndex by remember { mutableStateOf<Int?>(0) } // Simulasi item pertama (Diky) membuka pop-up menu

            // List Data Pegawai sesuai gambar Anda
            val listPegawai = remember {
                listOf(
                    Pegawai("10011222", "Diky Raihan S.", "Dev Ops", "DR", Color(0xFF2ECC71), null),
                    Pegawai("10011353", "Feisal S.", "Design", "FS", Color(0xFF9B59B6), null),
                    Pegawai("10019968", "Rina Nastiti", "QA", "RN", Color(0xFFF4D03F), Color(0xFF2ECC71)),
                    Pegawai("10019227", "Aditya Ardi", "Sales", "AA", Color(0xFFE67E22), Color(0xFF2ECC71)),
                    Pegawai("10019035", "Siti Aminah", "Design", "SIA", Color(0xFFE74C3C), Color(0xFFF4D03F)),
                    Pegawai("10019172", "Budiman S.", "Finance", "BU", Color(0xFF1ABC9C), Color(0xFFE74C3C))
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                LiquidGlassBackground(colors = colors) { }
                NoiseOverlay()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(64.dp))

                    // HEADER: Judul & Tombol Tambah Pegawai
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Daftar Data", color = colors.text0, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            Text("Pegawai", color = colors.text0, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        }

                        // Tombol Tambah Pegawai Gradien Biru
                        Box(
                            modifier = Modifier
                                .height(46.dp)
                                .background(
                                    Brush.horizontalGradient(listOf(colors.blue, Color(0xFF1D83E4))),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { Toast.makeText(context, "Tambah Pegawai", Toast.LENGTH_SHORT).show() }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tambah Pegawai", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // SEARCH BAR & FILTER ICON
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Cari nama atau NIP...", color = colors.text1, fontSize = 14.sp) },
                            trailingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colors.text1) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color(0xFF161D2F).copy(alpha = 0.6f),
                                unfocusedContainerColor = Color(0xFF161D2F).copy(alpha = 0.6f),
                                focusedTextColor = colors.text0,
                                unfocusedTextColor = colors.text0
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .background(Color(0xFF161D2F).copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                                .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                                .clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_sort_by_size),
                                contentDescription = "Filter",
                                tint = colors.text1,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ROW FILTER CAPSULES
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterCapsule(text = "Hadir", textColor = Color(0xFF2ECC71), modifier = Modifier.weight(1f))
                        FilterCapsule(text = "Izin/Sakit", textColor = Color(0xFFF4D03F), modifier = Modifier.weight(1.2f))
                        FilterCapsule(text = "Belum Absen", textColor = Color(0xFFE74C3C), modifier = Modifier.weight(1.3f))
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // LIST PEGAWAI RENDER
                    listPegawai.forEachIndexed { index, pegawai ->
                        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                            // Card Utama Pegawai
                            GlassCard(
                                colors = colors,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // Toggle Pop-up Menu saat card diklik
                                        selectedMenuIndex = if (selectedMenuIndex == index) null else index
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Avatar Lingkaran Bulat
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(pegawai.avatarBg, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(pegawai.inisial, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    // Data Teks Pegawai
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(pegawai.nama, color = colors.text0, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        Text("ID: ${pegawai.id}", color = colors.text1, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                                        Text(pegawai.divisi, color = colors.text1, fontSize = 12.sp)
                                    }

                                    // Dot Penanda Status Kehadiran di Sisi Kanan (Jika ada)
                                    pegawai.statusColor?.let { color ->
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(color, CircleShape)
                                        )
                                    }
                                }
                            }

                            // SIMULASI POP-UP CONTEXT MENU (Detail, Edit, Hapus)
                            if (selectedMenuIndex == index) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(end = 16.dp)
                                        .width(110.dp)
                                        .background(Color(0xFF121622), RoundedCornerShape(14.dp))
                                        .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Text("Detail", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { selectedMenuIndex = null })
                                        Text("Edit", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { selectedMenuIndex = null })
                                        Text("Hapus", color = Color(0xFFE74C3C), fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().clickable { selectedMenuIndex = null })
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(120.dp)) // Jarak agar tidak tertutup bottom bar
                }
            }
        }
    }
}

@Composable
fun FilterCapsule(text: String, textColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(38.dp)
            .background(textColor.copy(alpha = 0.1f), RoundedCornerShape(18.dp))
            .border(1.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}