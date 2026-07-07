package com.feisal.workingreport

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feisal.workingreport.model.Lembur
import com.feisal.workingreport.model.User
import com.feisal.workingreport.repository.AuthRepository
import com.feisal.workingreport.repository.LemburRepository
import com.feisal.workingreport.ui.theme.P79Colors
import com.feisal.workingreport.ui.theme.p79Colors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class LemburActivity : ComponentActivity() {
    private val lemburRepository by lazy { LemburRepository() }
    private val authRepository by lazy { AuthRepository() } // Untuk mengambil profil user aktif

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val colors = p79Colors(isDark = true)
            var showBottomSheet by remember { mutableStateOf(false) }
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val scope = rememberCoroutineScope()

            var currentUser by remember { mutableStateOf<User?>(null) }
            val riwayatLemburList = remember { mutableStateListOf<Lembur>() }

            // Fungsi mengambil data ter-update dari database
            fun refreshData(userId: String) {
                scope.launch {
                    val list = lemburRepository.getMyLemburHistory(userId)
                    riwayatLemburList.clear()
                    riwayatLemburList.addAll(list)
                }
            }

            LaunchedEffect(Unit) {
                try {
                    val user = authRepository.getCurrentUserProfile()
                    currentUser = user
                    user?.let { refreshData(it.id) }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0B101E)) {
                LemburContent(
                    colors = colors,
                    isDarkMode = true,
                    riwayatList = riwayatLemburList,
                    onBackClick = { finish() },
                    onAjukanClick = { showBottomSheet = true }
                )
                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showBottomSheet = false },
                        sheetState = sheetState,
                        containerColor = Color(0xFF161D2F),
                        scrimColor = Color.Black.copy(alpha = 0.5f)
                    ) {
                        LemburBottomSheetContent(
                            colors = colors,
                            isDarkMode = true,
                            onSuccessSubmit = { tanggal, jamMulai, jamSelesai, alasan ->
                                scope.launch {
                                    currentUser?.let { user ->
                                        val dataLemburNew = Lembur(
                                            userId = user.id,
                                            namaKaryawan = user.name,
                                            nip = user.nip,
                                            tanggal = tanggal,
                                            jamMulai = jamMulai,
                                            jamSelesai = jamSelesai,
                                            alasanLembur = alasan,
                                            status = "PENDING"
                                        )
                                        lemburRepository.submitLembur(dataLemburNew).onSuccess {
                                            Toast.makeText(this@LemburActivity, "Pengajuan lembur berhasil dikirim!", Toast.LENGTH_SHORT).show()
                                            refreshData(user.id) // Reload list riwayat langsung dari DB
                                            showBottomSheet = false
                                        }.onFailure {
                                            Toast.makeText(this@LemburActivity, "Gagal mengirim data", Toast.LENGTH_SHORT).show()
                                        }
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

@Composable
fun LemburContent(
    colors: P79Colors,
    isDarkMode: Boolean,
    riwayatList: List<Lembur>, // Berubah tipe ke objek model Lembur resmi
    onBackClick: () -> Unit,
    onAjukanClick: () -> Unit
) {
    val cardBgColor = if (isDarkMode) Color(0xFF161D2F) else Color.White

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(48.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ArrowBack, "Back", tint = colors.text0, modifier = Modifier.size(24.dp).clickable { onBackClick() })
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Lembur", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Pengajuan & riwayat jam lembur kamu", color = colors.text1, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Statistik Dinamis Berdasarkan Data Firestore
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LemburStatCard(modifier = Modifier.weight(1f), bg = cardBgColor, border = colors.border, title = "${riwayatList.size}", subtitle = "TOTAL AJUAN", iconTint = colors.red, textColor = colors.text0)
            LemburStatCard(modifier = Modifier.weight(1f), bg = cardBgColor, border = colors.border, title = "${riwayatList.count { it.status.uppercase() == "APPROVED" }}", subtitle = "DISETUJUI", iconTint = colors.green, textColor = colors.text0)
            LemburStatCard(modifier = Modifier.weight(1f), bg = cardBgColor, border = colors.border, title = "${riwayatList.count { it.status.uppercase() == "PENDING" }}", subtitle = "PENDING", iconTint = colors.amber, textColor = colors.text0)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onAjukanClick,
            modifier = Modifier.fillMaxWidth().height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.blue)
        ) { Text("Ajukan Lembur", fontWeight = FontWeight.Bold) }

        Spacer(modifier = Modifier.height(24.dp))

        Text("RIWAYAT PENGAJUAN", color = colors.text1, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(12.dp))

        if (riwayatList.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().background(cardBgColor, RoundedCornerShape(16.dp)).border(1.dp, colors.border, RoundedCornerShape(16.dp)).padding(24.dp), contentAlignment = Alignment.Center) {
                Text(text = "Tidak ada riwayat lembur", color = colors.text1, fontSize = 14.sp, textAlign = TextAlign.Center)
            }
        } else {
            riwayatList.forEach { lembur ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .background(cardBgColor, RoundedCornerShape(16.dp))
                        .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    val badgeColor = when (lembur.status.uppercase()) {
                        "APPROVED" -> colors.green
                        "REJECTED" -> Color.Red
                        else -> colors.amber
                    }

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(lembur.tanggal, color = colors.text0, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Box(modifier = Modifier.background(badgeColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text(lembur.status, color = badgeColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Waktu: ${lembur.jamMulai} - ${lembur.jamSelesai}", color = colors.text1, fontSize = 13.sp)
                    Text("Alasan: ${lembur.alasanLembur}", color = colors.text0, fontSize = 13.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LemburBottomSheetContent(
    colors: P79Colors,
    isDarkMode: Boolean,
    onSuccessSubmit: (String, String, String, String) -> Unit // Ganti callback menerima String murni
) {
    val context = LocalContext.current
    val inputBgColor = if (isDarkMode) Color(0xFF222831) else Color.White

    var tanggal by remember { mutableStateOf("") }
    var jamMulai by remember { mutableStateOf("") }
    var jamSelesai by remember { mutableStateOf("") }
    var alasan by remember { mutableStateOf("") }

    var tanggalError by remember { mutableStateOf(false) }
    var jamMulaiError by remember { mutableStateOf(false) }
    var jamSelesaiError by remember { mutableStateOf(false) }
    var alasanError by remember { mutableStateOf(false) }

    var showConfirmDialog by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            tanggal = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
            tanggalError = false
        },
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerMulai = TimePickerDialog(context, { _, hour, minute ->
        jamMulai = String.format("%02d:%02d", hour, minute)
        jamMulaiError = false
    }, 18, 0, true)

    val timePickerSelesai = TimePickerDialog(context, { _, hour, minute ->
        jamSelesai = String.format("%02d:%02d", hour, minute)
        jamSelesaiError = false
    }, 21, 0, true)

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Konfirmasi Pengajuan", fontWeight = FontWeight.Bold, color = colors.text0) },
            text = { Text("Apakah data lembur yang Anda masukkan sudah benar?", color = colors.text1) },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    onSuccessSubmit(tanggal, jamMulai, jamSelesai, alasan)
                }) {
                    Text("Ya, Benar", color = colors.blue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { showConfirmDialog = false }) { Text("Batal", color = Color.Red) } },
            containerColor = if (isDarkMode) Color(0xFF1E2738) else Color.White
        )
    }

    // --- SISA TAMPILAN UI TETAP SAMA SEPERTI KODE LAMA KAMU ---
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp).verticalScroll(rememberScrollState())) {
        Text("Ajukan Lembur", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        Text("Tanggal", color = if (tanggalError) Color.Red else colors.text1, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth().background(inputBgColor, RoundedCornerShape(12.dp)).border(1.dp, if (tanggalError) Color.Red else colors.border, RoundedCornerShape(12.dp)).clickable { datePickerDialog.show() }.padding(16.dp)) {
            Text(if (tanggal.isEmpty()) "Pilih tanggal lembur" else tanggal, color = if (tanggal.isEmpty()) colors.text1 else colors.text0)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Jam Mulai", color = if (jamMulaiError) Color.Red else colors.text1, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth().background(inputBgColor, RoundedCornerShape(12.dp)).border(1.dp, if (jamMulaiError) Color.Red else colors.border, RoundedCornerShape(12.dp)).clickable { timePickerMulai.show() }.padding(16.dp)) { Text(if (jamMulai.isEmpty()) "18:00" else jamMulai, color = colors.text0) }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Jam Selesai", color = if (jamSelesaiError) Color.Red else colors.text1, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth().background(inputBgColor, RoundedCornerShape(12.dp)).border(1.dp, if (jamSelesaiError) Color.Red else colors.border, RoundedCornerShape(12.dp)).clickable { timePickerSelesai.show() }.padding(16.dp)) { Text(if (jamSelesai.isEmpty()) "21:00" else jamSelesai, color = colors.text0) }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Alasan Lembur", color = if (alasanError) Color.Red else colors.text1, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = alasan,
            onValueChange = { alasan = it; alasanError = false },
            placeholder = { Text("Jelaskan alasan...", color = colors.text1) },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            textStyle = androidx.compose.ui.text.TextStyle(color = colors.text0),
            shape = RoundedCornerShape(12.dp),
            isError = alasanError,
            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = inputBgColor, focusedContainerColor = inputBgColor, unfocusedBorderColor = if (alasanError) Color.Red else colors.border, focusedBorderColor = colors.blue)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // Reset flag error di awal
                tanggalError = tanggal.isEmpty()
                jamMulaiError = jamMulai.isEmpty()
                jamSelesaiError = jamSelesai.isEmpty()
                alasanError = alasan.trim().isEmpty()

                // A. Validasi semua field wajib diisi
                if (tanggalError || jamMulaiError || jamSelesaiError || alasanError) {
                    Toast.makeText(context, "Semua field wajib diisi!", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                // B. Validasi logika perhitungan jam
                try {
                    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val timeMulai = sdf.parse(jamMulai)
                    val timeSelesai = sdf.parse(jamSelesai)

                    if (timeMulai != null && timeSelesai != null) {
                        // 1. Validasi jam mulai tidak boleh lebih besar atau sama dengan jam selesai
                        if (timeMulai.time >= timeSelesai.time) {
                            Toast.makeText(context, "Jam Mulai tidak boleh melebihi atau sama dengan Jam Selesai!", Toast.LENGTH_LONG).show()
                            jamMulaiError = true
                            return@Button
                        }

                        // 2. Validasi durasi jam tidak boleh lebih dari 4 jam
                        // (4 jam = 4 * 60 * 60 * 1000 milidetik = 14.400.000 ms)
                        val durationMillis = timeSelesai.time - timeMulai.time
                        val maxDurationMillis = 4 * 60 * 60 * 1000

                        if (durationMillis > maxDurationMillis) {
                            Toast.makeText(context, "Durasi lembur maksimal adalah 4 jam!", Toast.LENGTH_LONG).show()
                            jamSelesaiError = true
                            return@Button
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Format penulisan waktu salah.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                // Jika lolos semua validasi, munculkan dialog konfirmasi
                showConfirmDialog = true
            },
            modifier = Modifier.fillMaxWidth().height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.blue)
        ) {
            Text("Kirim Pengajuan", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LemburStatCard(
    modifier: Modifier,
    bg: Color,
    border: Color,
    title: String,
    subtitle: String,
    iconTint: Color,
    textColor: Color
) {
    Column(
        modifier = modifier
            .background(bg, RoundedCornerShape(12.dp))
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(8.dp).background(iconTint, CircleShape))
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = textColor.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}