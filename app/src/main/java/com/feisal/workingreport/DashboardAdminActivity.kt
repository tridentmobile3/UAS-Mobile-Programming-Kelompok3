package com.feisal.workingreport

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.feisal.workingreport.databinding.ActivityDashboardAdminBinding

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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.feisal.workingreport.model.User
import com.feisal.workingreport.model.WorkingReport
import com.feisal.workingreport.model.WorkingReportStatus
import com.feisal.workingreport.repository.AttendanceRepository
import com.feisal.workingreport.repository.AuthRepository
import com.feisal.workingreport.repository.WorkingReportRepository
import com.feisal.workingreport.ui.components.NoiseOverlay
import com.feisal.workingreport.ui.theme.LiquidGlassBackground
import com.feisal.workingreport.ui.theme.P79Colors
import com.feisal.workingreport.ui.theme.p79Colors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardAdminBinding
    private val attendanceRepository = AttendanceRepository()
    private val workingReportRepository = WorkingReportRepository()
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAdminFeatures()
        setupBottomNavigation()
        loadAdminStats()

        // Handle navigation for approval page
        if (intent.getBooleanExtra("SHOW_APPROVAL", false)) {
            showAdminApprovalPage()
        }
    }

    private fun showAdminApprovalPage() {
        val composeView = ComposeView(this).apply {
            setContent {
                val colors = p79Colors(isDark = true)
                var currentUser by remember { mutableStateOf<User?>(null) }
                var reports by remember { mutableStateOf<List<WorkingReport>>(emptyList()) }
                val scope = rememberCoroutineScope()

                LaunchedEffect(Unit) {
                    currentUser = authRepository.getCurrentUserProfile()
                    reports = workingReportRepository.getAllReports()
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    LiquidGlassBackground(colors = colors) { }
                    NoiseOverlay()
                    
                    AdminLaporanContent(
                        colors = colors,
                        currentUser = currentUser,
                        reports = reports,
                        onBack = { finish() },
                        onRefresh = {
                            scope.launch {
                                reports = workingReportRepository.getAllReports()
                            }
                        },
                        workingReportRepository = workingReportRepository
                    )
                }
            }
        }
        setContentView(composeView)
    }

    private fun loadAdminStats() {
        CoroutineScope(Dispatchers.IO).launch {
            val pendingAttendances = attendanceRepository.getPendingAttendances()
            val pendingReports = workingReportRepository.getPendingReports()
            
            withContext(Dispatchers.Main) {
                if (pendingAttendances.isNotEmpty()) {
                    val first = pendingAttendances.first()
                    binding.tvLeftName1.text = "${first.employeeName}\n(Izin: ${first.status})"
                } else {
                    binding.tvLeftName1.text = "Tidak ada\npending"
                }

                // Update summary counts (Mock/Dynamic)
                // In real app, calculate from allAttendances
            }
        }
    }

    private fun setupAdminFeatures() {
        binding.btnExportLaporan.setOnClickListener {
            Toast.makeText(this, "Mengekspor rekap kehadiran harian ke file .xlsx...", Toast.LENGTH_SHORT).show()
        }

        binding.cardVerifikasiKehadiran.setOnClickListener {
            val intent = Intent(this, RiwayatActivity::class.java)
            startActivity(intent)
        }

        binding.cardLaporanRutin.setOnClickListener {
            val intent = Intent(this, DashboardAdminActivity::class.java)
            intent.putExtra("SHOW_APPROVAL", true)
            startActivity(intent)
        }

        binding.cardLaporanLembur.setOnClickListener {
            val intent = Intent(this, LemburActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupBottomNavigation() {
        binding.navAdminHome.setOnClickListener { }

        binding.navAdminKaryawan.setOnClickListener {
            val intent = Intent(this, RiwayatActivity::class.java)
            startActivity(intent)
        }

        binding.navAdminPersetujuan.setOnClickListener {
            val intent = Intent(this, DashboardAdminActivity::class.java)
            intent.putExtra("SHOW_APPROVAL", true)
            startActivity(intent)
        }

        binding.navAdminProfil.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra("TARGET_PAGE", 3)
            startActivity(intent)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLaporanContent(
    colors: P79Colors,
    currentUser: User?,
    reports: List<WorkingReport>,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    workingReportRepository: WorkingReportRepository
) {
    val isDarkMode = true
    val cardBgColor = Color(0xFF161D2F)
    var selectedTab by remember { mutableStateOf("Semua") }
    var selectedReport by remember { mutableStateOf<WorkingReport?>(null) }
    var showDetailSheet by remember { mutableStateOf(false) }

    val sortedReports = remember(reports) {
        reports.sortedByDescending { it.createdAt }
    }

    val filteredReports = when (selectedTab) {
        "Menunggu" -> sortedReports.filter { it.status == WorkingReportStatus.SUBMITTED.name }
        "Disetujui" -> sortedReports.filter { it.status == WorkingReportStatus.APPROVED.name }
        "Revisi" -> sortedReports.filter { it.status == WorkingReportStatus.REVISION.name }
        else -> sortedReports
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
                Text(
                    text = "Persetujuan Laporan",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "Tinjau dan kelola laporan kerja karyawan",
                color = Color(0xFF8B95A5),
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = colors.border, thickness = 1.dp)
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("Semua", "Menunggu", "Disetujui", "Revisi").forEach { tab ->
                    LaporanTab(text = tab, isSelected = selectedTab == tab, colors = colors, cardBgColor = cardBgColor, onClick = { selectedTab = tab })
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (filteredReports.isEmpty()) {
                    EmptyState(colors = colors, cardBgColor = cardBgColor, message = "Tidak ada laporan masuk")
                } else {
                    filteredReports.forEach { report ->
                        AdminReportItem(
                            colors = colors,
                            cardBgColor = cardBgColor,
                            report = report,
                            onClick = {
                                selectedReport = report
                                showDetailSheet = true
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(130.dp))
        }
    }

    if (showDetailSheet && selectedReport != null) {
        ModalBottomSheet(
            onDismissRequest = { showDetailSheet = false },
            containerColor = Color(0xFF0F172A),
            dragHandle = { BottomSheetDefaults.DragHandle(color = colors.border) }
        ) {
            AdminReportDetailSheet(
                report = selectedReport!!,
                colors = colors,
                onActionDone = {
                    showDetailSheet = false
                    onRefresh()
                },
                workingReportRepository = workingReportRepository
            )
        }
    }
}

@Composable
fun AdminReportItem(colors: P79Colors, cardBgColor: Color, report: WorkingReport, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(cardBgColor, RoundedCornerShape(16.dp))
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(report.employeeName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("NIP: ${report.employeeNip}", color = Color(0xFF8B95A5), fontSize = 11.sp)
            }
            Text(report.date, color = Color(0xFF8B95A5), fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(report.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, null, tint = colors.blue, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("${report.startTime} - ${report.endTime}", color = Color(0xFF8B95A5), fontSize = 12.sp)
            }
            Box(modifier = Modifier.background(
                when(report.status) {
                    WorkingReportStatus.APPROVED.name -> colors.green.copy(alpha = 0.1f)
                    WorkingReportStatus.REVISION.name -> colors.red.copy(alpha = 0.1f)
                    else -> colors.amber.copy(alpha = 0.1f)
                }, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(
                    text = when(report.status) {
                        WorkingReportStatus.APPROVED.name -> "APPROVED"
                        WorkingReportStatus.REVISION.name -> "REVISION"
                        else -> "SUBMITTED"
                    },
                    color = when(report.status) {
                        WorkingReportStatus.APPROVED.name -> colors.green
                        WorkingReportStatus.REVISION.name -> colors.red
                        else -> colors.amber
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AdminReportDetailSheet(
    report: WorkingReport,
    colors: P79Colors,
    onActionDone: () -> Unit,
    workingReportRepository: WorkingReportRepository
) {
    var showRevisionDialog by remember { mutableStateOf(false) }
    var revisionNote by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Tinjau Laporan", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        DetailItem(label = "Karyawan", value = "${report.employeeName} (${report.employeeNip})", colors = colors)
        DetailItem(label = "Tanggal", value = report.date, colors = colors)
        DetailItem(label = "Jam Kerja", value = "${report.startTime} - ${report.endTime}", colors = colors)
        DetailItem(label = "Lokasi Kerja", value = report.workLocation, colors = colors)
        DetailItem(label = "Judul", value = report.title, colors = colors)
        DetailItem(label = "Deskripsi", value = report.description, colors = colors)
        DetailItem(label = "Progress", value = "${report.progress}%", colors = colors)
        DetailItem(label = "Hambatan", value = report.obstacle.ifEmpty { "-" }, colors = colors)
        DetailItem(label = "Rencana Selanjutnya", value = report.nextPlan.ifEmpty { "-" }, colors = colors)
        DetailItem(label = "Lampiran", value = report.fileName.ifEmpty { "-" }, colors = colors)
        DetailItem(label = "Status", value = report.status, colors = colors)
        if (report.revisionNote.isNotEmpty()) {
            DetailItem(label = "Catatan Revisi", value = report.revisionNote, colors = colors)
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (report.status == WorkingReportStatus.SUBMITTED.name) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { showRevisionDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.red),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("REVISI", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        scope.launch {
                            workingReportRepository.approveReport(report.id).onSuccess {
                                onActionDone()
                            }.onFailure {
                                Toast.makeText(context, "Gagal menyetujui laporan", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.green),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("APPROVE", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showRevisionDialog) {
        AlertDialog(
            onDismissRequest = { showRevisionDialog = false },
            title = { Text("Catatan Revisi", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = revisionNote,
                    onValueChange = { revisionNote = it },
                    placeholder = { Text("Masukkan catatan revisi...", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = colors.blue,
                        unfocusedBorderColor = colors.border
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (revisionNote.isBlank()) {
                        Toast.makeText(context, "Catatan wajib diisi", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }
                    scope.launch {
                        workingReportRepository.revisionReport(report.id, revisionNote).onSuccess {
                            showRevisionDialog = false
                            onActionDone()
                        }.onFailure {
                            Toast.makeText(context, "Gagal mengirim revisi", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Text("Kirim", color = colors.blue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRevisionDialog = false }) {
                    Text("Batal", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF161D2F)
        )
    }
}
