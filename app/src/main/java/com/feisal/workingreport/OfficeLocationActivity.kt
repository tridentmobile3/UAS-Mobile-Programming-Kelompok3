package com.feisal.workingreport

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feisal.workingreport.model.OfficeLocation
import com.feisal.workingreport.repository.AttendanceRepository
import com.feisal.workingreport.ui.components.DetailItem
import com.feisal.workingreport.ui.components.NoiseOverlay
import com.feisal.workingreport.ui.theme.LiquidGlassBackground
import com.feisal.workingreport.ui.theme.p79Colors

class OfficeLocationActivity : AppCompatActivity() {
    private val attendanceRepository = AttendanceRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val colors = p79Colors(isDark = true)
            var officeLocation by remember { mutableStateOf<OfficeLocation?>(null) }
            var isLoading by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                try {
                    officeLocation = attendanceRepository.getOfficeLocation()
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isLoading = false
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                LiquidGlassBackground(colors = colors) { }
                NoiseOverlay()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { finish() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                        Text("Lokasi Kantor", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = colors.blue)
                        }
                    } else {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            officeLocation?.let { loc ->
                                DetailItem(label = "Nama Kantor", value = loc.name, colors = colors)
                                DetailItem(label = "Alamat", value = "Jl. Padepokan No. 79, Bandung", colors = colors)
                                DetailItem(label = "Radius Presensi", value = "${loc.radiusMeter} Meter", colors = colors)
                                DetailItem(label = "Latitude", value = loc.latitude.toString(), colors = colors)
                                DetailItem(label = "Longitude", value = loc.longitude.toString(), colors = colors)
                                DetailItem(label = "Status Lokasi", value = if (loc.active) "AKTIF" else "NON-AKTIF", colors = colors)
                            } ?: run {
                                Text("Data lokasi tidak ditemukan.", color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }
            }
        }
    }
}
