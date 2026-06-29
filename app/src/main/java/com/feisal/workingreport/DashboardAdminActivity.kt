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

        // 2. Logika Tombol Persetujuan Izin (Pending Approvals)
        setupApprovalActions()

        // 3. Logika Navigasi Bottom Navigation Bar
        setupBottomNavigation()
    }

    /**
     * Mengatur klik pada menu fitur utama admin
     */
    private fun setupAdminFeatures() {
        // Tombol Export Laporan ke Excel
        binding.btnExportLaporan.setOnClickListener {
            Toast.makeText(this, "Mengekspor rekap kehadiran harian ke file .xlsx...", Toast.LENGTH_SHORT).show()
            // Di sini Anda dapat menambahkan library Apache POI atau sejenisnya untuk membuat file Excel
        }

        // Card Menu Data Pegawai
        binding.cardDataKaryawan.setOnClickListener {
            Toast.makeText(this, "Membuka Menu Kelola Data Pegawai", Toast.LENGTH_SHORT).show()
            // Contoh navigasi:
            // startActivity(Intent(this, KelolaPegawaiActivity::class.java))
        }

        // Card Menu Persetujuan (Shortcut)
        binding.cardVerifikasiIzin.setOnClickListener {
            Toast.makeText(this, "Membuka Menu Semua Persetujuan Izin", Toast.LENGTH_SHORT).show()
        }

        // Card Menu Rekap Global
        binding.cardRekapBulanan.setOnClickListener {
            Toast.makeText(this, "Membuka Menu Rekap Kehadiran Bulanan", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Mengatur logika aksi Terima (Approve) atau Tolak (Reject) dokumen izin karyawan
     */
    private fun setupApprovalActions() {
        // --- Item Pending 1: Diky Raihan S. ---
        binding.btnApprove1.setOnClickListener {
            Toast.makeText(this, "Izin Sakit Diky Raihan S. BERHASIL DISETUJUI", Toast.LENGTH_SHORT).show()
            // Logika nyata: Update status ke database/API, lalu hilangkan item atau refresh data
        }

        binding.btnReject1.setOnClickListener {
            Toast.makeText(this, "Izin Sakit Diky Raihan S. DITOLAK", Toast.LENGTH_SHORT).show()
        }

        // --- Item Pending 2: Feisal S. ---
        binding.btnApprove2.setOnClickListener {
            Toast.makeText(this, "Izin Keluarga Feisal S. BERHASIL DISETUJUI", Toast.LENGTH_SHORT).show()
        }

        binding.btnReject2.setOnClickListener {
            Toast.makeText(this, "Izin Keluarga Feisal S. DITOLAK", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Mengatur logika perpindahan halaman melalui Floating Bottom Navigation Bar
     */
    private fun setupBottomNavigation() {
        // Menu Dashboard (Halaman Ini)
        binding.navAdminHome.setOnClickListener {
            // Karena sudah berada di halaman dashboard admin, cukup lakukan scroll ke atas jika diperlukan
            Toast.makeText(this, "Anda berada di Dashboard Admin", Toast.LENGTH_SHORT).show()
        }

        // Menu Pegawai
        binding.navAdminKaryawan.setOnClickListener {
            Toast.makeText(this, "Navigasi ke Daftar Pegawai", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this, DaftarPegawaiActivity::class.java))
        }

        // Menu Approval
        binding.navAdminPersetujuan.setOnClickListener {
            Toast.makeText(this, "Navigasi ke Halaman Utama Approval", Toast.LENGTH_SHORT).show()
        }

        // Menu Profil Admin
        binding.navAdminProfil.setOnClickListener {
            val intent = Intent(this, ProfilActivity::class.java)
            intent.putExtra("EXTRA_ROLE", "admin")
            startActivity(intent)
        }
    }
}