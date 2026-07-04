package com.feisal.workingreport

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.feisal.workingreport.databinding.ActivityDashboardAdminBinding

import com.feisal.workingreport.repository.AttendanceRepository
import com.feisal.workingreport.repository.WorkingReportRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardAdminBinding
    private val attendanceRepository = AttendanceRepository()
    private val workingReportRepository = WorkingReportRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAdminFeatures()
        setupBottomNavigation()
        loadAdminStats()
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
            val intent = Intent(this, AttendanceDetailActivity::class.java).apply {
                putExtra("name", "Diky Raihan S.")
                putExtra("date", "16 Apr 2022")
                putExtra("checkIn", "08:12")
                putExtra("checkOut", "--:--")
                putExtra("status", "IZIN")
            }
            startActivity(intent)
        }

        binding.cardLaporanRutin.setOnClickListener {
            Toast.makeText(this, "Membuka Menu Laporan Rutin", Toast.LENGTH_SHORT).show()
        }

        binding.cardLaporanLembur.setOnClickListener {
            Toast.makeText(this, "Membuka Menu Laporan Lembur", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNavigation() {
        binding.navAdminHome.setOnClickListener { }

        binding.navAdminKaryawan.setOnClickListener {
            Toast.makeText(this, "Menu Presensi Karyawan", Toast.LENGTH_SHORT).show()
        }

        binding.navAdminPersetujuan.setOnClickListener {
            Toast.makeText(this, "Menu Rekap Laporan Kerja", Toast.LENGTH_SHORT).show()
        }

        binding.navAdminProfil.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }
    }
}
