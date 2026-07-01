package com.feisal.workingreport

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.feisal.workingreport.databinding.ActivityDashboardAdminBinding

class DashboardAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi View Binding
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Logika Aksi Tombol Fitur Utama
        setupAdminFeatures()

        // 2. Logika Navigasi Bottom Navigation Bar
        setupBottomNavigation()
    }

    /**
     * Mengatur klik pada menu fitur utama admin
     */
    private fun setupAdminFeatures() {
        // Tombol Export Laporan ke Excel
        binding.btnExportLaporan.setOnClickListener {
            Toast.makeText(this, "Mengekspor rekap kehadiran harian ke file .xlsx...", Toast.LENGTH_SHORT).show()
        }

        // Card Verifikasi Kehadiran
        binding.cardVerifikasiKehadiran.setOnClickListener {
            Toast.makeText(this, "Membuka Menu Verifikasi Kehadiran", Toast.LENGTH_SHORT).show()
        }

        // Card Laporan Rutin
        binding.cardLaporanRutin.setOnClickListener {
            Toast.makeText(this, "Membuka Menu Laporan Rutin", Toast.LENGTH_SHORT).show()
        }

        // Card Laporan Lembur
        binding.cardLaporanLembur.setOnClickListener {
            Toast.makeText(this, "Membuka Menu Laporan Lembur", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Mengatur navigasi bar bagian bawah
     */
    private fun setupBottomNavigation() {
        binding.navAdminHome.setOnClickListener {
            // Sudah di Dashboard Admin
        }

        binding.navAdminKaryawan.setOnClickListener {
            Toast.makeText(this, "Menu Presensi Karyawan", Toast.LENGTH_SHORT).show()
        }

        binding.navAdminPersetujuan.setOnClickListener {
            Toast.makeText(this, "Menu Rekap Laporan Kerja", Toast.LENGTH_SHORT).show()
        }

        binding.navAdminProfil.setOnClickListener {
            // Untuk sementara arahkan ke DashboardActivity (Compose) -> Profil
            // atau buat ProfilActivity khusus admin jika perlu
            startActivity(Intent(this, DashboardActivity::class.java))
        }
    }
}
