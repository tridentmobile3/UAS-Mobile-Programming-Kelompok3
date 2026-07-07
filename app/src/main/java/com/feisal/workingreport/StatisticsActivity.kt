package com.feisal.workingreport

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feisal.workingreport.ui.components.DetailItem
import com.feisal.workingreport.ui.components.NoiseOverlay
import com.feisal.workingreport.ui.theme.LiquidGlassBackground
import com.feisal.workingreport.ui.theme.p79Colors
import com.feisal.workingreport.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class StatisticsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val colors = p79Colors(isDark = true)
            var stats by remember { mutableStateOf(mapOf<String, Long>()) }
            var isLoading by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                try {
                    val db = FirebaseFirestore.getInstance()
                    
                    val users = db.collection(Constants.USERS_COLLECTION).get().await()
                    val totalEmployees = users.count { it.getString("role") == "KARYAWAN" }.toLong()
                    val totalHC = users.count { it.getString("role") == "HC" }.toLong()
                    
                    val attendances = db.collection(Constants.ATTENDANCES_COLLECTION).get().await()
                    val totalAttendances = attendances.size().toLong()
                    
                    val permissions = db.collection(Constants.PERMISSIONS_COLLECTION)
                        .whereEqualTo("status", "PENDING").get().await()
                    val totalPendingIzin = permissions.size().toLong()
                    
                    val reports = db.collection(Constants.WORKING_REPORTS_COLLECTION).get().await()
                    val pendingReports = reports.count { it.getString("status") == "SUBMITTED" }.toLong()
                    val approvedReports = reports.count { it.getString("status") == "APPROVED" }.toLong()
                    val rejectedReports = reports.count { it.getString("status") == "REVISION" || it.getString("status") == "REJECTED" }.toLong()

                    stats = mapOf(
                        "Total Karyawan" to totalEmployees,
                        "Total HC" to totalHC,
                        "Total Kehadiran" to totalAttendances,
                        "Total Izin Pending" to totalPendingIzin,
                        "Total Laporan Pending" to pendingReports,
                        "Total Laporan Disetujui" to approvedReports,
                        "Total Laporan Ditolak" to rejectedReports
                    )
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
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Text("Statistik Sistem", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = colors.blue)
                        }
                    } else {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            stats.forEach { (label, value) ->
                                DetailItem(label = label, value = value.toString(), colors = colors)
                            }
                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }
            }
        }
    }
}
