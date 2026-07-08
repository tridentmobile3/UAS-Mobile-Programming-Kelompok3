package com.feisal.workingreport

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feisal.workingreport.model.Attendance
import com.feisal.workingreport.repository.HcAttendanceRepository
import com.feisal.workingreport.ui.components.FilterTab
import com.feisal.workingreport.ui.components.NoiseOverlay
import com.feisal.workingreport.ui.theme.LiquidGlassBackground
import com.feisal.workingreport.ui.theme.P79Colors
import com.feisal.workingreport.ui.theme.p79Colors

class HcAttendanceActivity : AppCompatActivity() {
    private val hcAttendanceRepository = HcAttendanceRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val colors = p79Colors(isDark = true)
            var allAttendances by remember { mutableStateOf<List<Attendance>>(emptyList()) }
            var searchQuery by remember { mutableStateOf("") }
            var selectedFilter by remember { mutableStateOf("Semua") }

            LaunchedEffect(Unit) {
                allAttendances = hcAttendanceRepository.getAllAttendances()
            }

            val filteredAttendances = remember(allAttendances, searchQuery, selectedFilter) {
                allAttendances.filter { att ->
                    val matchesSearch = att.employeeName.contains(searchQuery, ignoreCase = true) || 
                                      att.employeeNip.contains(searchQuery, ignoreCase = true)
                    val matchesFilter = if (selectedFilter == "Semua") true else att.status.equals(selectedFilter, ignoreCase = true)
                    matchesSearch && matchesFilter
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                LiquidGlassBackground(colors = colors) { }
                NoiseOverlay()
                
                HcAttendanceContent(
                    colors = colors,
                    attendances = filteredAttendances,
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    selectedFilter = selectedFilter,
                    onFilterChange = { selectedFilter = it },
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HcAttendanceContent(
    colors: P79Colors,
    attendances: List<Attendance>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }
            Text(
                "Monitoring Presensi",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            placeholder = { Text("Cari Nama atau NIP...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = colors.blue,
                unfocusedBorderColor = colors.border
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Tabs
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Semua", "HADIR", "IZIN", "TERLAMBAT", "ALPHA").forEach { filter ->
                FilterTab(
                    text = filter,
                    isSelected = selectedFilter == filter,
                    colors = colors,
                    onClick = { onFilterChange(filter) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (attendances.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Tidak ada data presensi", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(attendances) { att ->
                    EmployeeAttendanceItem(
                        attendance = att,
                        colors = colors,
                        onClick = {
                            val intent = Intent(context, EmployeeAttendanceDetailActivity::class.java)
                            intent.putExtra("USER_ID", att.userId)
                            intent.putExtra("USER_NAME", att.employeeName)
                            intent.putExtra("USER_NIP", att.employeeNip)
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmployeeAttendanceItem(attendance: Attendance, colors: P79Colors, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF161D2F), RoundedCornerShape(16.dp))
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(attendance.employeeName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("NIP: ${attendance.employeeNip}", color = Color.Gray, fontSize = 11.sp)
            }
            Box(
                modifier = Modifier
                    .background(
                        when(attendance.status.uppercase()) {
                            "HADIR" -> colors.green.copy(alpha = 0.1f)
                            "TERLAMBAT" -> colors.red.copy(alpha = 0.1f)
                            else -> colors.amber.copy(alpha = 0.1f)
                        }, RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    attendance.status.uppercase(),
                    color = when(attendance.status.uppercase()) {
                        "HADIR" -> colors.green
                        "TERLAMBAT" -> colors.red
                        else -> colors.amber
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, null, tint = colors.blue, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(attendance.date, color = Color.Gray, fontSize = 12.sp)
            }
            Text(
                "${attendance.checkInTime} - ${attendance.checkOutTime.ifBlank { "--:--" }}",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
