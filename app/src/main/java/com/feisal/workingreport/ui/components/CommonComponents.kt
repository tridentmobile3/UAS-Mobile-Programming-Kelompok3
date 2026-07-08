package com.feisal.workingreport.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.feisal.workingreport.*
import com.feisal.workingreport.model.Attendance
import com.feisal.workingreport.model.PermissionRequest
import com.feisal.workingreport.model.User
import com.feisal.workingreport.model.WorkingReport
import com.feisal.workingreport.model.WorkingReportStatus
import com.feisal.workingreport.repository.ProfileRepository
import com.feisal.workingreport.repository.WorkingReportRepository
import com.feisal.workingreport.ui.theme.P79Colors
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EmptyState(colors: P79Colors, cardBgColor: Color, message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(cardBgColor, RoundedCornerShape(16.dp))
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun FilterTab(text: String, isSelected: Boolean, colors: P79Colors, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) colors.blue.copy(alpha = 0.2f) else Color(0xFF161D2F))
            .border(1.dp, if (isSelected) colors.blue else colors.border, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) colors.blue else Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DetailItem(label: String, value: String, colors: P79Colors) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = label.uppercase(),
            color = Color.Gray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = Color.White,
            fontSize = 15.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = colors.border.copy(alpha = 0.5f), thickness = 0.5.dp)
    }
}

@Composable
fun SettingsItem(
    colors: P79Colors, 
    iconBgColor: Color, 
    icon: ImageVector, 
    iconTint: Color, 
    title: String, 
    subtitle: String?, 
    showArrow: Boolean = true, 
    trailing: @Composable (() -> Unit)? = null, 
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp), 
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconBgColor, RoundedCornerShape(10.dp)), 
            contentAlignment = Alignment.Center
        ) { 
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp)) 
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle.ifBlank { "Tidak tersedia" }, color = Color.Gray, fontSize = 12.sp)
            }
        }
        if (trailing != null) { 
            trailing() 
        } else if (showArrow) { 
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp)) 
        }
    }
}

@Composable
fun ProfilContentHC(
    colors: P79Colors,
    isDarkMode: Boolean,
    currentUser: User?,
    onThemeChange: (Boolean) -> Unit,
    onLogoutClick: () -> Unit,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    profileRepository: ProfileRepository
) {
    val cardBgColor = Color(0xFF161D2F)
    val iconBgColor = Color(0xFF222831)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    
    var newName by remember { mutableStateOf("") }
    var isUpdating by remember { mutableStateOf(false) }

    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var isChangingPassword by remember { mutableStateOf(false) }
    
    var totalEmployees by remember { mutableLongStateOf(0L) }
    var pendingReports by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        try {
            val db = FirebaseFirestore.getInstance()
            val employees = db.collection("users").whereEqualTo("role", "KARYAWAN").get().await()
            totalEmployees = employees.size().toLong()
            val reports = db.collection("working_reports").whereEqualTo("status", "SUBMITTED").get().await()
            pendingReports = reports.size().toLong()
        } catch (e: Exception) { e.printStackTrace() }
    }

    LaunchedEffect(showEditProfileDialog) {
        if (showEditProfileDialog) { newName = currentUser?.name ?: "" }
    }

    if (showEditProfileDialog) {
        Dialog(onDismissRequest = { if (!isUpdating) showEditProfileDialog = false }) {
            Surface(shape = RoundedCornerShape(24.dp), color = cardBgColor, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Edit Profil", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newName, onValueChange = { newName = it }, label = { Text("Nama Lengkap", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), enabled = !isUpdating, singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = colors.blue, unfocusedBorderColor = colors.border)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { 
                            val trimmedName = newName.trim()
                            if (trimmedName.isEmpty() || trimmedName.length < 3) { Toast.makeText(context, "Nama tidak valid", Toast.LENGTH_SHORT).show(); return@Button }
                            if (trimmedName == currentUser?.name) { Toast.makeText(context, "Tidak ada perubahan.", Toast.LENGTH_SHORT).show(); return@Button }
                            isUpdating = true
                            scope.launch {
                                profileRepository.updateProfileName(trimmedName).onSuccess {
                                    isUpdating = false; showEditProfileDialog = false; Toast.makeText(context, "Profil berhasil diperbarui.", Toast.LENGTH_SHORT).show(); onRefresh()
                                }.onFailure { e -> isUpdating = false; Toast.makeText(context, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show() }
                            }
                        }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.blue), enabled = !isUpdating
                    ) { 
                        if (isUpdating) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        else Text("Simpan Perubahan", color = Color.White) 
                    }
                }
            }
        }
    }

    if (showChangePasswordDialog) {
        Dialog(onDismissRequest = { if (!isChangingPassword) showChangePasswordDialog = false }) {
            Surface(shape = RoundedCornerShape(24.dp), color = cardBgColor, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Ubah Kata Sandi", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = oldPassword, onValueChange = { oldPassword = it }, label = { Text("Kata Sandi Lama", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), enabled = !isChangingPassword,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(), singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = colors.blue, unfocusedBorderColor = colors.border)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPassword, onValueChange = { newPassword = it }, label = { Text("Kata Sandi Baru", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), enabled = !isChangingPassword,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(), singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = colors.blue, unfocusedBorderColor = colors.border)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { 
                            if (oldPassword.isBlank() || newPassword.length < 6) { Toast.makeText(context, "Password tidak valid", Toast.LENGTH_SHORT).show(); return@Button }
                            isChangingPassword = true
                            scope.launch {
                                profileRepository.changePassword(oldPassword, newPassword).onSuccess {
                                    isChangingPassword = false; showChangePasswordDialog = false; oldPassword = ""; newPassword = ""; Toast.makeText(context, "Kata sandi berhasil diperbarui.", Toast.LENGTH_SHORT).show()
                                }.onFailure { e -> isChangingPassword = false; Toast.makeText(context, "Gagal mengubah kata sandi", Toast.LENGTH_SHORT).show() }
                            }
                        }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.blue), enabled = !isChangingPassword
                    ) { 
                        if (isChangingPassword) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        else Text("Ubah Sandi", color = Color.White) 
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(64.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(cardBgColor).clickable { onBackClick() }, contentAlignment = Alignment.Center) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(currentUser?.name ?: "User HC", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(currentUser?.nip ?: "-", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.size(90.dp).background(Brush.linearGradient(listOf(colors.blue, colors.green)), RoundedCornerShape(24.dp)), contentAlignment = Alignment.Center) {
                Text(currentUser?.name?.firstOrNull()?.toString() ?: "A", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(currentUser?.name ?: "Admin HC", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(currentUser?.authEmail ?: "-", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) { Text(currentUser?.department ?: "IT", color = Color.Gray, fontSize = 12.sp) }
                Box(modifier = Modifier.background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) { Text(currentUser?.position ?: "Human Capital", color = Color.Gray, fontSize = 12.sp) }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("TAMPILAN", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Column(modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp))) {
            SettingsItem(colors, iconBgColor, Icons.Default.Lock, colors.blue, "Mode Gelap", "Tampilan dark / light", trailing = {
                Switch(checked = isDarkMode, onCheckedChange = onThemeChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = colors.blue))
            })
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("AKUN", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Column(modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp))) {
            SettingsItem(colors, iconBgColor, Icons.Default.Edit, colors.green, "Edit Profil", null, onClick = { showEditProfileDialog = true })
            HorizontalDivider(color = colors.border, modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(colors, iconBgColor, Icons.Default.Lock, colors.amber, "Ubah Kata Sandi", null, onClick = { showChangePasswordDialog = true })
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("ADMIN", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Column(modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp))) {
            SettingsItem(colors, iconBgColor, Icons.Default.ShoppingCart, colors.blue, "Statistik Sistem", if (pendingReports > 0) "$pendingReports Laporan Pending" else "Semua laporan terproses", onClick = { context.startActivity(Intent(context, StatisticsActivity::class.java)) })
            HorizontalDivider(color = colors.border, modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(colors, iconBgColor, Icons.Default.Person, colors.green, "Total Karyawan", "$totalEmployees Karyawan Terdaftar", onClick = { context.startActivity(Intent(context, EmployeeListActivity::class.java)) })
            HorizontalDivider(color = colors.border, modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(colors, iconBgColor, Icons.Default.LocationOn, colors.amber, "Lokasi Kantor", "Padepokan 79 (Bandung)", onClick = { context.startActivity(Intent(context, OfficeLocationActivity::class.java)) })
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("LAINNYA", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Column(modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp))) {
            SettingsItem(colors, iconBgColor, Icons.Default.Info, colors.red, "Bantuan & Dukungan", null, onClick = { Toast.makeText(context, "Membuka Bantuan & Dukungan...", Toast.LENGTH_SHORT).show() })
            HorizontalDivider(color = colors.border, modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(colors, iconBgColor, Icons.Default.Info, colors.text1, "Tentang Aplikasi", "Sapta Work v1.0", onClick = { Toast.makeText(context, "Sapta Work v1.0 (Kelompok 3)", Toast.LENGTH_SHORT).show() })
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onLogoutClick, modifier = Modifier.fillMaxWidth().height(55.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f)), border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)), shape = RoundedCornerShape(16.dp)) {
            Text("KELUAR", color = Color.Red, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(130.dp))
    }
}

@Composable
fun HomeContent(
    colors: P79Colors,
    isDarkMode: Boolean,
    activity: AppCompatActivity,
    currentUser: User?,
    todayAttendance: Attendance?,
    attendanceHistory: List<Attendance>,
    permissionHistory: List<PermissionRequest>,
    hasActivePermission: Boolean,
    canClickIzin: Boolean,               // Sudah aman, tidak bertabrakan lagi
    canClickAbsen: Boolean,              // Sudah aman, tidak bertabrakan lagi
    unreadCount: Int = 0,
    onLaporClick: () -> Unit,
    onRiwayatClick: () -> Unit,
    onIzinClick: () -> Unit,
    onLemburClick: () -> Unit,
    onBellClick: () -> Unit
) {
    val cardBgColor = if (isDarkMode) Color(0xFF161D2F) else Color.White
    val calendar = java.util.Calendar.getInstance()
    val currentMonth = calendar.get(java.util.Calendar.MONTH)
    val currentYear = calendar.get(java.util.Calendar.YEAR)

    // Perhitungan Ringkasan Bulanan
    val attendanceThisMonth = attendanceHistory.filter { att ->
        try {
            val dateStr = att.date
            val attCal = java.util.Calendar.getInstance()

            val parsedDate = when {
                dateStr.contains("/") -> {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateStr)
                }
                dateStr.contains("-") -> {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
                }
                else -> null
            }

            if (parsedDate != null) {
                attCal.time = parsedDate
                attCal.get(java.util.Calendar.MONTH) == currentMonth && attCal.get(java.util.Calendar.YEAR) == currentYear
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    val countHadir = attendanceThisMonth.count { it.status.uppercase() == "HADIR" || it.status.uppercase() == "TERLAMBAT" }
    val countIzin = attendanceThisMonth.count { it.status.uppercase() == "IZIN" }
    val countSakit = attendanceThisMonth.count { it.status.uppercase() == "SAKIT" }
    val countCuti = attendanceThisMonth.count { it.status.uppercase() == "CUTI" }

    // --- LOGIKA PENYELARASAN FORMAT TANGGAL PADA LIST HISTORY ---
    // Memetakan isi list agar semua format tanggal izin/cuti/sakit menjadi yyyy-MM-dd
    val formattedAttendanceHistory = remember(attendanceHistory) {
        attendanceHistory.map { att ->
            if (att.date.contains("/")) {
                try {
                    val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val parsedDate = inputFormat.parse(att.date)
                    if (parsedDate != null) {
                        att.copy(date = outputFormat.format(parsedDate))
                    } else att
                } catch (e: Exception) {
                    att
                }
            } else {
                att
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(48.dp))
        TopBar(colors = colors, isDarkMode = isDarkMode, currentUser = currentUser, unreadCount = unreadCount, onBellClick = onBellClick)
        Spacer(modifier = Modifier.height(24.dp))
        AbsenCard(colors = colors, isDarkMode = isDarkMode, todayAttendance = todayAttendance, hasActivePermission = hasActivePermission, activity = activity)
        Spacer(modifier = Modifier.height(16.dp))

        // Jika MenuRow Anda bisa menerima parameter status aktif tombol, masukkan variabel canClickIzin ke sini
        MenuRow(colors = colors, cardBgColor = cardBgColor, onIzinClick = onIzinClick, onLaporClick = onLaporClick, onRiwayatClick = onRiwayatClick, onLemburClick = onLemburClick)
        Spacer(modifier = Modifier.height(16.dp))

        SummaryCard(
            colors = colors,
            cardBgColor = cardBgColor,
            countHadir = countHadir,
            countIzin = countIzin,
            countSakit = countSakit,
            countCuti = countCuti
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text("RIWAYAT TERBARU", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(modifier = Modifier.height(12.dp))

        // Menggunakan data list yang format tanggalnya sudah disamakan/diselaraskan
        HistoryList(colors = colors, cardBgColor = cardBgColor, history = formattedAttendanceHistory)
        Spacer(modifier = Modifier.height(130.dp))
    }
}

@Composable
fun TopBar(
    colors: P79Colors, 
    isDarkMode: Boolean, 
    currentUser: User?, 
    unreadCount: Int = 0,
    onBellClick: () -> Unit
) {
    val iconBgColor = if (isDarkMode) Color(0xFF161D2F) else Color.White
    val isHc = currentUser?.role == "HC"
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Halo, ${if (isHc) "Admin" else currentUser?.name?.split(" ")?.firstOrNull() ?: "Sobat"}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(text = "Padepokan Tujuh Sembilan", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBgColor)
                .border(1.dp, colors.border, CircleShape)
                .clickable { onBellClick() }, 
            contentAlignment = Alignment.Center
        ) { 
            Icon(Icons.Default.Notifications, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                        .background(Color.Red, CircleShape)
                        .border(1.5.dp, iconBgColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Brush.linearGradient(listOf(colors.blue, colors.green))), contentAlignment = Alignment.Center) {
            Text(currentUser?.name?.firstOrNull()?.toString() ?: "U", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun AbsenCard(colors: P79Colors, isDarkMode: Boolean, todayAttendance: Attendance?, hasActivePermission: Boolean, activity: AppCompatActivity) {
    val iconBgColor = if (isDarkMode) Color(0xFF222831) else Color(0xFFF3F4F6)
    val hasCheckedIn = todayAttendance != null
    val hasCheckedOut = todayAttendance?.checkOutTime?.isNotEmpty() == true
    val buttonEnabled = !hasCheckedOut && !hasActivePermission
    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
        Column(modifier = Modifier.fillMaxWidth().background(if (isDarkMode) Color(0xFF161D2F) else Color.White, RoundedCornerShape(24.dp)).border(1.dp, colors.border, RoundedCornerShape(24.dp)).padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(iconBgColor, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    Icon(if (hasActivePermission) Icons.Default.Info else if (hasCheckedIn) Icons.Default.CheckCircle else Icons.Default.Warning, null, tint = if (hasActivePermission) colors.amber else if (hasCheckedIn) colors.green else colors.amber, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(if (hasActivePermission) "Status: Sedang Izin Kerja" else if (hasCheckedOut) "Sudah Selesai Kerja" else if (hasCheckedIn) "Sedang Bekerja" else "Belum Absen Masuk", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Kantor Pusat · radius 100m", color = Color.Gray, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("MASUK", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold);
                    Spacer(modifier = Modifier.height(4.dp));
                    Text(if (todayAttendance?.checkInTime.isNullOrEmpty()) "--:--" else todayAttendance!!.checkInTime, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(colors.border))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PULANG", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold);
                    Spacer(modifier = Modifier.height(4.dp));
                    Text(if (todayAttendance?.checkOutTime.isNullOrEmpty()) "--:--" else todayAttendance!!.checkOutTime, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            val buttonText = if (hasActivePermission) "TIDAK DAPAT ABSEN" else if (hasCheckedOut) "SUDAH ABSEN PULANG" else if (hasCheckedIn) "ABSEN PULANG" else "ABSEN MASUK"
            Button(onClick = { val intent = Intent(activity, CameraAbsenActivity::class.java); intent.putExtra("type", if (!hasCheckedIn) "CHECK_IN" else "CHECK_OUT"); activity.startActivity(intent) }, enabled = buttonEnabled, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Gray.copy(alpha = 0.2f)), contentPadding = PaddingValues(), modifier = Modifier.fillMaxWidth().height(55.dp)) {
                Box(modifier = Modifier.fillMaxSize().background(if (buttonEnabled) Brush.horizontalGradient(listOf(colors.blue, colors.green)) else SolidColor(Color.Transparent), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Text(buttonText, color = if (buttonEnabled) Color.White else Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
fun MenuRow(colors: P79Colors, cardBgColor: Color, onIzinClick: () -> Unit, onLaporClick: () -> Unit, onRiwayatClick: () -> Unit, onLemburClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MenuCard(colors, cardBgColor, "Ajukan Izin", colors.amber, Modifier.weight(1f)) { onIzinClick() }
        MenuCard(colors, cardBgColor, "Lapor Kerja", colors.green, Modifier.weight(1f)) { onLaporClick() }
        MenuCard(colors, cardBgColor, "Riwayat", colors.blue, Modifier.weight(1f)) { onRiwayatClick() }
        MenuCard(colors, cardBgColor, "Lembur", colors.red, Modifier.weight(1f)) { onLemburClick() }
    }
}

@Composable
fun MenuCard(colors: P79Colors, cardBgColor: Color, title: String, iconColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(modifier = modifier.height(90.dp).background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp)).clickable { onClick() }, contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Info, null, tint = iconColor, modifier = Modifier.size(24.dp)); Spacer(modifier = Modifier.height(8.dp)); Text(title, color = Color.White, fontSize = 12.sp) } }
}

@Composable
fun SummaryCard(
    colors: P79Colors,
    cardBgColor: Color,
    countHadir: Int,  // Terima data kalkulasi baru dari HomeContent
    countIzin: Int,
    countSakit: Int,
    countCuti: Int
) {
    // ... Bagian pembungkus modifier background, border, dll tetap sama ...

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "RINGKASAN BULAN INI",
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. HADIR
            SummaryItem(label = "Hadir", count = countHadir, color = colors.green)

            // 2. IZIN
            SummaryItem(label = "Izin", count = countIzin, color = colors.amber)

            // 3. SAKIT (Menggantikan Telat)
            SummaryItem(label = "Sakit", count = countSakit, color = colors.red)

            // 4. CUTI (Menggantikan Alpha)
            SummaryItem(label = "Cuti", count = countCuti, color = colors.blue)
        }
    }
}

@Composable
fun SummaryItem(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            color = color,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

@Composable
fun StatCard(modifier: Modifier, bg: Color, border: Color, title: String, subtitle: String, iconTint: Color, textColor: Color) {
    Column(modifier = modifier.background(bg, RoundedCornerShape(12.dp)).border(1.dp, border, RoundedCornerShape(12.dp)).padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(8.dp).background(iconTint, CircleShape))
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AttendanceDetailSheet(attendance: Attendance, colors: P79Colors) {
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp).verticalScroll(rememberScrollState())) {
        Text("Detail Absensi", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        DetailItem(label = "Tanggal", value = attendance.date, colors = colors)
        DetailItem(label = "Status", value = attendance.status, colors = colors)
        DetailItem(label = "Waktu Masuk", value = attendance.checkInTime.ifBlank { "-" }, colors = colors)
        DetailItem(label = "Waktu Pulang", value = attendance.checkOutTime.ifBlank { "-" }, colors = colors)
        DetailItem(label = "Jarak Masuk", value = "${attendance.checkInDistance}m", colors = colors)
        DetailItem(label = "Jarak Pulang", value = "${attendance.checkOutDistance}m", colors = colors)
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun WorkingReportItem(colors: P79Colors, cardBgColor: Color, report: WorkingReport, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp)).clickable { onClick() }.padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(report.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(report.date, color = Color.Gray, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(report.description, color = Color.Gray, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${report.startTime} - ${report.endTime}", color = Color.Gray, fontSize = 12.sp)
            Box(modifier = Modifier.background(colors.blue.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(report.status, color = colors.blue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun WorkingReportDetailSheet(report: WorkingReport, colors: P79Colors) {
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp).verticalScroll(rememberScrollState())) {
        Text("Detail Laporan Kerja", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        DetailItem(label = "Judul", value = report.title, colors = colors)
        DetailItem(label = "Tanggal", value = report.date, colors = colors)
        DetailItem(label = "Deskripsi", value = report.description, colors = colors)
        DetailItem(label = "Status", value = report.status, colors = colors)
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatContent(
    colors: P79Colors,
    isDarkMode: Boolean,
    currentUser: User?,
    history: List<Attendance>,
    permissionHistory: List<PermissionRequest>,
    onBackClick: () -> Unit
) {
    val cardBgColor = Color(0xFF161D2F)
    val innerCardBgColor = Color(0xFF222831)
    var selectedAttendance by remember { mutableStateOf<Attendance?>(null) }
    var showDetailSheet by remember { mutableStateOf(false) }

    // Hitung ulang statistik berdasarkan kategori baru
    val totalHadir = history.count { it.status.uppercase() == "HADIR" || it.status.uppercase() == "TERLAMBAT" }
    val totalIzin = history.count { it.status.uppercase() == "IZIN" || it.status.uppercase() == "SAKIT" }
    val totalCuti = history.count { it.status.uppercase() == "CUTI" }

    val averageCheckIn = remember(history) {
        if (history.isEmpty()) "--"
        else {
            val checkInTimes = history.filter { it.checkInTime.isNotEmpty() }
            if (checkInTimes.isEmpty()) "--"
            else {
                val totalMinutes = checkInTimes.sumOf { item ->
                    try {
                        val timeStr = item.checkInTime
                        val parts = timeStr.split(":")
                        if (parts.size >= 2) parts[0].toInt() * 60 + parts[1].toInt() else 0
                    } catch (e: Exception) { 0 }
                }
                val avgMinutes = totalMinutes / checkInTimes.size
                String.format("%02d:%02d", avgMinutes / 60, avgMinutes % 60)
            }
        }
    }

    // --- LOGIKA PENYELARASAN FORMAT TANGGAL DAN SORTING ---
    val sortedHistory = remember(history) {
        history.map { att ->
            if (att.date.contains("/")) {
                try {
                    val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val parsedDate = inputFormat.parse(att.date)
                    if (parsedDate != null) {
                        att.copy(date = outputFormat.format(parsedDate))
                    } else att
                } catch (e: Exception) {
                    att
                }
            } else {
                att
            }
        }.sortedByDescending { it.createdAt }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(64.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(cardBgColor).clickable { onBackClick() }, contentAlignment = Alignment.Center) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(currentUser?.name ?: "Karyawan", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(currentUser?.nip ?: "-", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("Riwayat Absensi & Izin", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Pantau histori kehadiran dan pengajuan izin kamu", color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(modifier = Modifier.weight(1f), bg = cardBgColor, border = colors.border, title = totalHadir.toString(), subtitle = "HADIR", iconTint = colors.green, textColor = Color.White)
            StatCard(modifier = Modifier.weight(1f), bg = cardBgColor, border = colors.border, title = averageCheckIn, subtitle = "RATA² MASUK", iconTint = colors.blue, textColor = Color.White)
            StatCard(modifier = Modifier.weight(1f), bg = cardBgColor, border = colors.border, title = totalCuti.toString(), subtitle = "CUTI", iconTint = colors.red, textColor = Color.White)
            StatCard(modifier = Modifier.weight(1f), bg = cardBgColor, border = colors.border, title = totalIzin.toString(), subtitle = "IZIN/SAKIT", iconTint = colors.amber, textColor = Color.White)
        }
        Spacer(modifier = Modifier.height(24.dp))

        Column(modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp)).padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf("S", "S", "R", "K", "J", "S", "M").forEach { day ->
                    Text(day, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            val attendanceMap = history.associateBy { it.date }
            val calendar = java.util.Calendar.getInstance()
            val currentYear = calendar.get(java.util.Calendar.YEAR)
            val currentMonth = calendar.get(java.util.Calendar.MONTH)
            for (i in 0 until 5) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    for (j in 0 until 7) {
                        val dayIndex = i * 7 + j + 1
                        var dayColor = innerCardBgColor

                        if (dayIndex <= 31) {
                            val dateWithSlash = String.format("%02d/%02d/%04d", dayIndex, currentMonth + 1, currentYear)
                            val dateWithDash = String.format("%04d-%02d-%02d", currentYear, currentMonth + 1, dayIndex)

                            val att = attendanceMap[dateWithSlash] ?: attendanceMap[dateWithDash]

                            if (att != null) {
                                dayColor = when(att.status.uppercase()) {
                                    "HADIR", "TERLAMBAT" -> colors.green
                                    "IZIN", "SAKIT"      -> colors.amber
                                    "CUTI"               -> colors.red
                                    else                 -> innerCardBgColor
                                }
                            }
                        }
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp).background(dayColor, RoundedCornerShape(8.dp)))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("HISTORI KEHADIRAN", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(12.dp))
        if (sortedHistory.isEmpty()) {
            EmptyState(colors = colors, cardBgColor = cardBgColor, message = "Tidak ada riwayat absensi")
        } else {
            sortedHistory.forEach { item ->
                val statusUpper = item.status.uppercase()

                val currentStatusColor = when (statusUpper) {
                    "HADIR", "TERLAMBAT" -> colors.green
                    "IZIN", "SAKIT"      -> colors.amber
                    "CUTI"               -> colors.red
                    else                 -> colors.blue
                }

                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp))
                        .clickable { selectedAttendance = item; showDetailSheet = true }.padding(16.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        // Di sini otomatis menampilkan format yyyy-MM-dd hasil convert
                        Text(item.date, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                        Box(modifier = Modifier.background(
                            currentStatusColor.copy(alpha = 0.15f),
                            RoundedCornerShape(8.dp)
                        ).padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text(
                                text = statusUpper,
                                color = currentStatusColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Masuk", color = Color.Gray, fontSize = 10.sp)
                            Text(item.checkInTime.ifBlank { "--:--" }, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Pulang", color = Color.Gray, fontSize = 10.sp)
                            // Mengubah format fallback kosong dari "..." menjadi "--:--" agar lebih rapi untuk data non-hadir
                            Text(item.checkOutTime.ifBlank { "--:--" }, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(130.dp))
    }

    if (showDetailSheet && selectedAttendance != null) {
        ModalBottomSheet(onDismissRequest = { showDetailSheet = false }, containerColor = Color(0xFF0F172A)) {
            AttendanceDetailSheet(attendance = selectedAttendance!!, colors = colors)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanContent(
    colors: P79Colors,
    isDarkMode: Boolean,
    currentUser: User?,
    reports: List<WorkingReport>,
    unreadCount: Int = 0,
    onBellClick: () -> Unit,
    onAddClick: () -> Unit
) {
    val cardBgColor = Color(0xFF161D2F)
    var selectedTab by remember { mutableStateOf("Semua") }
    var selectedReport by remember { mutableStateOf<WorkingReport?>(null) }
    var showDetailSheet by remember { mutableStateOf(false) }

    val filteredReports = reports.filter { report ->
        when (selectedTab) {
            "Disetujui" -> report.status == "APPROVED"
            "Menunggu" -> report.status == "SUBMITTED"
            "Revisi" -> report.status == "REVISION"
            else -> true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(24.dp))
            TopBar(colors = colors, isDarkMode = isDarkMode, currentUser = currentUser, unreadCount = unreadCount, onBellClick = onBellClick)
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("Semua", "Disetujui", "Menunggu", "Revisi").forEach { tab ->
                    FilterTab(text = tab, isSelected = selectedTab == tab, colors = colors, onClick = { selectedTab = tab })
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (filteredReports.isEmpty()) {
                    EmptyState(colors = colors, cardBgColor = cardBgColor, message = "Tidak ada laporan")
                } else {
                    filteredReports.forEach { report ->
                        WorkingReportItem(colors = colors, cardBgColor = cardBgColor, report = report, onClick = { selectedReport = report; showDetailSheet = true })
                    }
                }
            }
            Spacer(modifier = Modifier.height(130.dp))
        }
        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 100.dp).size(60.dp).clip(RoundedCornerShape(20.dp)).background(Brush.linearGradient(listOf(colors.blue, colors.green))).clickable { onAddClick() }, contentAlignment = Alignment.Center) { Text("+", color = Color.White, fontSize = 36.sp) }
    }

    if (showDetailSheet && selectedReport != null) {
        ModalBottomSheet(onDismissRequest = { showDetailSheet = false }, containerColor = Color(0xFF0F172A)) {
            WorkingReportDetailSheet(report = selectedReport!!, colors = colors)
        }
    }
}

@Composable
fun HistoryList(colors: P79Colors, cardBgColor: Color, history: List<Attendance>) {
    if (history.isEmpty()) EmptyState(colors, cardBgColor, "Tidak ada riwayat")
    else {
        Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            history.take(3).forEach { item ->
                Row(modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) { Text(item.date, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold); Text("${item.checkInTime} - ${item.checkOutTime}", color = Color.Gray, fontSize = 12.sp) }
                    Box(modifier = Modifier.background(if (item.status.uppercase()=="HADIR") colors.green.copy(alpha=0.1f) else colors.red.copy(alpha=0.1f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) { Text(item.status.uppercase(), color = if (item.status.uppercase()=="HADIR") colors.green else colors.red, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLaporanContent(colors: P79Colors, reports: List<WorkingReport>, onRefresh: () -> Unit, workingReportRepository: WorkingReportRepository, onBackClick: () -> Unit) {
    val cardBgColor = Color(0xFF161D2F)
    var selectedTab by remember { mutableStateOf("Semua") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedReport by remember { mutableStateOf<WorkingReport?>(null) }
    var showDetailSheet by remember { mutableStateOf(false) }
    val filteredReports = reports.filter { report ->
        val matchesTab = when (selectedTab) { "Menunggu" -> report.status == "SUBMITTED"; "Disetujui" -> report.status == "APPROVED"; "Revisi" -> report.status == "REVISION"; else -> true }
        val matchesSearch = report.employeeName.contains(searchQuery, ignoreCase = true) || report.title.contains(searchQuery, ignoreCase = true)
        matchesTab && matchesSearch
    }
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 24.dp)) { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }; Text("Persetujuan Laporan", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), placeholder = { Text("Cari Nama atau Judul...", color = Color.Gray) }, leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) }, shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = colors.blue, unfocusedBorderColor = colors.border), singleLine = true)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("Semua", "Menunggu", "Disetujui", "Revisi").forEach { tab -> FilterTab(tab, selectedTab == tab, colors) { selectedTab = tab } } }
        Spacer(modifier = Modifier.height(16.dp))
        Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (filteredReports.isEmpty()) EmptyState(colors, cardBgColor, "Tidak ada laporan masuk")
            else filteredReports.forEach { report -> AdminReportItem(colors, cardBgColor, report) { selectedReport = report; showDetailSheet = true } }
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
    if (showDetailSheet && selectedReport != null) { ModalBottomSheet(onDismissRequest = { showDetailSheet = false }, containerColor = Color(0xFF0F172A)) { AdminReportDetailSheet(selectedReport!!, colors, { showDetailSheet = false; onRefresh() }, workingReportRepository) } }
}

@Composable
fun AdminReportItem(colors: P79Colors, cardBgColor: Color, report: WorkingReport, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFF2D3548), RoundedCornerShape(16.dp)).clickable { onClick() }.padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) { Text(report.employeeName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold); Text("NIP: ${report.employeeNip}", color = Color.Gray, fontSize = 11.sp) }
            Box(modifier = Modifier.background(if (report.status=="APPROVED") colors.green.copy(alpha=0.1f) else colors.amber.copy(alpha=0.1f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) { Text(report.status, color = if (report.status=="APPROVED") colors.green else colors.amber, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
        }
        Spacer(modifier = Modifier.height(12.dp)); Text(report.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp)); Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.DateRange, null, tint = colors.blue, modifier = Modifier.size(14.dp)); Spacer(modifier = Modifier.width(4.dp)); Text(report.date, color = Color.Gray, fontSize = 12.sp) }; Text("${report.startTime} - ${report.endTime}", color = Color.Gray, fontSize = 12.sp) }
    }
}

@Composable
fun AdminReportDetailSheet(report: WorkingReport, colors: P79Colors, onActionDone: () -> Unit, workingReportRepository: WorkingReportRepository) {
    var showRevisionDialog by remember { mutableStateOf(false) }
    var revisionNote by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp).verticalScroll(rememberScrollState())) {
        Text("Tinjau Laporan", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        DetailItem("Karyawan", "${report.employeeName} (${report.employeeNip})", colors)
        DetailItem("Tanggal", report.date, colors)
        DetailItem("Judul", report.title, colors)
        DetailItem("Deskripsi", report.description, colors)
        DetailItem("Status", report.status, colors)
        Spacer(modifier = Modifier.height(24.dp))
        if (report.status == "SUBMITTED") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { showRevisionDialog = true }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Red), shape = RoundedCornerShape(12.dp)) { Text("REVISI", color = Color.White, fontWeight = FontWeight.Bold) }
                Button(onClick = { scope.launch { workingReportRepository.approveReport(report.id).onSuccess { onActionDone() }.onFailure { Toast.makeText(context, "Gagal", Toast.LENGTH_SHORT).show() } } }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = colors.green), shape = RoundedCornerShape(12.dp)) { Text("APPROVE", color = Color.White, fontWeight = FontWeight.Bold) }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
    if (showRevisionDialog) {
        AlertDialog(onDismissRequest = { showRevisionDialog = false }, title = { Text("Catatan Revisi", color = Color.White) }, text = { OutlinedTextField(value = revisionNote, onValueChange = { revisionNote = it }, placeholder = { Text("Ketik catatan...", color = Color.Gray) }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = colors.blue, unfocusedBorderColor = colors.border)) }, confirmButton = { TextButton(onClick = { if (revisionNote.isBlank()) return@TextButton; scope.launch { workingReportRepository.revisionReport(report.id, revisionNote).onSuccess { showRevisionDialog = false; onActionDone() }.onFailure { Toast.makeText(context, "Gagal", Toast.LENGTH_SHORT).show() } } }) { Text("Kirim", color = colors.blue) } }, dismissButton = { TextButton(onClick = { showRevisionDialog = false }) { Text("Batal", color = Color.Gray) } }, containerColor = Color(0xFF161D2F))
    }
}

@Composable
fun NotificationSheetContent(colors: P79Colors, isDarkMode: Boolean) {
    val cardBgColor = if (isDarkMode) Color(0xFF161D2F) else Color.White
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Notifikasi", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold); Text("Tandai semua dibaca", color = colors.blue, fontSize = 12.sp, fontWeight = FontWeight.Medium) }
        Spacer(modifier = Modifier.height(24.dp))
        EmptyState(colors = colors, cardBgColor = cardBgColor, message = "Tidak ada notifikasi baru")
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun DatePickerBox(value: String, onValueChange: (String) -> Unit, placeholder: String, colors: P79Colors, inputBgColor: Color, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val calendar = java.util.Calendar.getInstance()
    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
            onValueChange(formattedDate)
        },
        calendar.get(java.util.Calendar.YEAR),
        calendar.get(java.util.Calendar.MONTH),
        calendar.get(java.util.Calendar.DAY_OF_MONTH)
    )

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            placeholder = { Text(placeholder, color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = colors.blue,
                unfocusedContainerColor = inputBgColor,
                focusedContainerColor = inputBgColor
            )
        )
        Box(modifier = Modifier.matchParentSize().background(Color.Transparent).clickable { datePickerDialog.show() })
    }
}

@Composable
fun TimePickerBox(value: String, onValueChange: (String) -> Unit, placeholder: String, colors: P79Colors, inputBgColor: Color, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val calendar = java.util.Calendar.getInstance()
    val timePickerDialog = android.app.TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
            onValueChange(formattedTime)
        },
        calendar.get(java.util.Calendar.HOUR_OF_DAY),
        calendar.get(java.util.Calendar.MINUTE),
        true
    )

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            placeholder = { Text(placeholder, color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = colors.blue,
                unfocusedContainerColor = inputBgColor,
                focusedContainerColor = inputBgColor
            )
        )
        Box(modifier = Modifier.matchParentSize().background(Color.Transparent).clickable { timePickerDialog.show() })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanBottomSheetContent(
    colors: P79Colors,
    isDarkMode: Boolean,
    onSubmit: (String, String, String, String, String, Uri?, String?, String?) -> Unit
) {
    val context = LocalContext.current
    val inputBgColor = if (isDarkMode) Color(0xFF222B3C) else Color(0xFFF3F4F6)

    var tanggal by remember { mutableStateOf("") }
    var judul by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    var jamMulai by remember { mutableStateOf("") }
    var jamSelesai by remember { mutableStateOf("") }

    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedMimeType by remember { mutableStateOf<String?>(null) }

    val pickFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            selectedMimeType = context.contentResolver.getType(it)
            context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex != -1) {
                    selectedFileName = cursor.getString(nameIndex)
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp)) {
        Text("Buat Laporan Kerja", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        Text("Tanggal", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        DatePickerBox(value = tanggal, onValueChange = { tanggal = it }, placeholder = "Pilih Tanggal", colors = colors, inputBgColor = inputBgColor)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Judul Aktivitas", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = judul, onValueChange = { judul = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, unfocusedBorderColor = colors.border, focusedBorderColor = colors.blue, unfocusedContainerColor = inputBgColor, focusedContainerColor = inputBgColor))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Deskripsi", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = deskripsi, onValueChange = { deskripsi = it }, modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, unfocusedBorderColor = colors.border, focusedBorderColor = colors.blue, unfocusedContainerColor = inputBgColor, focusedContainerColor = inputBgColor))
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Mulai", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                TimePickerBox(value = jamMulai, onValueChange = { jamMulai = it }, placeholder = "Jam", colors = colors, inputBgColor = inputBgColor)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Selesai", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                TimePickerBox(value = jamSelesai, onValueChange = { jamSelesai = it }, placeholder = "Jam", colors = colors, inputBgColor = inputBgColor)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { if (tanggal.isBlank() || judul.isBlank()) Toast.makeText(context, "Isi field wajib", Toast.LENGTH_SHORT).show() else onSubmit(tanggal, judul, deskripsi, jamMulai, jamSelesai, selectedFileUri, selectedFileName, selectedMimeType) }, modifier = Modifier.fillMaxWidth().height(55.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.blue)) {
            Text("Kirim Laporan", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ProfilContent(
    colors: P79Colors,
    isDarkMode: Boolean,
    currentUser: User?,
    onThemeChange: (Boolean) -> Unit,
    onLogoutClick: () -> Unit,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    profileRepository: ProfileRepository
) {
    if (currentUser?.role == "HC") {
        ProfilContentHC(
            colors = colors,
            isDarkMode = isDarkMode,
            currentUser = currentUser,
            onThemeChange = onThemeChange,
            onLogoutClick = onLogoutClick,
            onBackClick = onBackClick,
            onRefresh = onRefresh,
            profileRepository = profileRepository
        )
    } else {
        ProfilContentEmployee(
            colors = colors,
            isDarkMode = isDarkMode,
            currentUser = currentUser,
            onThemeChange = onThemeChange,
            onLogoutClick = onLogoutClick,
            onBackClick = onBackClick,
            onRefresh = onRefresh,
            profileRepository = profileRepository
        )
    }
}

@Composable
fun ProfilContentEmployee(
    colors: P79Colors,
    isDarkMode: Boolean,
    currentUser: User?,
    onThemeChange: (Boolean) -> Unit,
    onLogoutClick: () -> Unit,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    profileRepository: ProfileRepository
) {
    val cardBgColor = Color(0xFF161D2F)
    val iconBgColor = Color(0xFF222831)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }

    var oldPasswordInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }

    LaunchedEffect(currentUser) {
        currentUser?.let { nameInput = it.name }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(64.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(cardBgColor).clickable { onBackClick() }, contentAlignment = Alignment.Center) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(currentUser?.name ?: "User", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(currentUser?.nip ?: "-", color = Color.Gray, fontSize = 10.sp, letterSpacing = 1.sp)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.size(90.dp).background(Brush.linearGradient(listOf(colors.blue, colors.green)), RoundedCornerShape(24.dp)), contentAlignment = Alignment.Center) {
                Text(currentUser?.name?.firstOrNull()?.toString() ?: "U", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(currentUser?.name ?: "User Name", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(currentUser?.authEmail ?: "email@example.com", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) { Text(currentUser?.department ?: "Dept", color = Color.Gray, fontSize = 12.sp) }
                Box(modifier = Modifier.background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) { Text(currentUser?.position ?: "Position", color = Color.Gray, fontSize = 12.sp) }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("TAMPILAN", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Column(modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp))) {
            SettingsItem(colors, iconBgColor, Icons.Default.Lock, colors.blue, "Mode Gelap", "Tampilan dark / light", trailing = {
                Switch(checked = isDarkMode, onCheckedChange = onThemeChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = colors.blue))
            })
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("AKUN", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Column(modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp))) {
            SettingsItem(colors, iconBgColor, Icons.Default.Edit, colors.green, "Edit Profil", null, onClick = { showEditProfileDialog = true })
            HorizontalDivider(color = colors.border, modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(colors, iconBgColor, Icons.Default.Lock, colors.amber, "Ubah Kata Sandi", null, onClick = { showChangePasswordDialog = true })
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("LAINNYA", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Column(modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp))) {
            SettingsItem(colors, iconBgColor, Icons.Default.Info, colors.red, "Bantuan & Dukungan", null, onClick = { Toast.makeText(context, "Membuka Bantuan & Dukungan...", Toast.LENGTH_SHORT).show() })
            HorizontalDivider(color = colors.border, modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(colors, iconBgColor, Icons.Default.Info, colors.text1, "Tentang Aplikasi", "Sapta Work v1.0", onClick = { Toast.makeText(context, "Sapta Work v1.0 (Kelompok 3)", Toast.LENGTH_SHORT).show() })
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onLogoutClick, modifier = Modifier.fillMaxWidth().height(55.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f)), border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)), shape = RoundedCornerShape(16.dp)) {
            Text("KELUAR", color = Color.Red, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(130.dp))
    }

    if (showEditProfileDialog) {
        Dialog(onDismissRequest = { showEditProfileDialog = false }) {
            Surface(shape = RoundedCornerShape(24.dp), color = cardBgColor, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Edit Profil", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = nameInput, onValueChange = { nameInput = it }, label = { Text("Nama Lengkap", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = colors.blue, unfocusedBorderColor = colors.border)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (nameInput.trim().isEmpty()) return@Button
                            scope.launch {
                                profileRepository.updateProfileName(nameInput.trim()).onSuccess {
                                    Toast.makeText(context, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                                    showEditProfileDialog = false
                                    onRefresh()
                                }
                            }
                        }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.blue)
                    ) { Text("Simpan", color = Color.White) }
                }
            }
        }
    }

    if (showChangePasswordDialog) {
        Dialog(onDismissRequest = { showChangePasswordDialog = false }) {
            Surface(shape = RoundedCornerShape(24.dp), color = cardBgColor, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Ubah Kata Sandi", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = oldPasswordInput, onValueChange = { oldPasswordInput = it }, label = { Text("Kata Sandi Lama", color = Color.Gray) },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = colors.blue, unfocusedBorderColor = colors.border)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPasswordInput, onValueChange = { newPasswordInput = it }, label = { Text("Kata Sandi Baru", color = Color.Gray) },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = colors.blue, unfocusedBorderColor = colors.border)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (oldPasswordInput.isEmpty() || newPasswordInput.isEmpty()) return@Button
                            scope.launch {
                                profileRepository.changePassword(oldPasswordInput, newPasswordInput).onSuccess {
                                    Toast.makeText(context, "Berhasil diubah", Toast.LENGTH_SHORT).show()
                                    showChangePasswordDialog = false
                                }
                            }
                        }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.blue)
                    ) { Text("Ubah Sandi", color = Color.White) }
                }
            }
        }
    }
}
