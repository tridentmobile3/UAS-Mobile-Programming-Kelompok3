package com.feisal.workingreport

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val btnAbsenMasuk = findViewById<Button>(R.id.btnAbsenMasuk)

        btnAbsenMasuk.setOnClickListener {
            val pindahKamera = Intent(this@DashboardActivity, CameraAbsenActivity::class.java)
            startActivity(pindahKamera)
        }

        val cardAjukanIzin = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardAjukanIzin)
        cardAjukanIzin.setOnClickListener {
            val bottomSheet = IzinBottomSheetFragment()
            bottomSheet.show(supportFragmentManager, "IzinBottomSheet")
        }
        val cardLaporKerja = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardLaporKerja)
        cardLaporKerja.setOnClickListener {
            val pindahLaporan = Intent(this@DashboardActivity, LaporanKerjaActivity::class.java)
            startActivity(pindahLaporan)
        }
        val cardRiwayat = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardRiwayat)
        cardRiwayat.setOnClickListener {
            val pindahRiwayat = Intent(this@DashboardActivity, RiwayatActivity::class.java)
            startActivity(pindahRiwayat)
        }
        val navHome = findViewById<android.widget.LinearLayout>(R.id.navHome)
        navHome.setOnClickListener {
            android.widget.Toast.makeText(this, "Kamu sudah berada di Home", android.widget.Toast.LENGTH_SHORT).show()
        }
        val navLaporan = findViewById<android.widget.LinearLayout>(R.id.navLaporan)
        navLaporan.setOnClickListener {
            val pindahLaporan = Intent(this@DashboardActivity, LaporanKerjaActivity::class.java)
            startActivity(pindahLaporan)
        }
        val navRiwayat = findViewById<android.widget.LinearLayout>(R.id.navRiwayat)
        navRiwayat.setOnClickListener {
            val pindahRiwayat = Intent(this@DashboardActivity, RiwayatActivity::class.java)
            startActivity(pindahRiwayat)
        }
        val navProfil = findViewById<android.widget.LinearLayout>(R.id.navProfil)
        navProfil.setOnClickListener {
            val pindahProfil = Intent(this@DashboardActivity, ProfilActivity::class.java)
            startActivity(pindahProfil)
        }
    }
}
