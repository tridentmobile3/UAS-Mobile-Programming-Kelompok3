package com.feisal.workingreport

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class LaporanKerjaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_laporan_kerja)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        val fabBuatLaporan = findViewById<MaterialCardView>(R.id.fabBuatLaporan)
        fabBuatLaporan.setOnClickListener {
            val bottomSheet = LaporanBottomSheetFragment()
            bottomSheet.show(supportFragmentManager, "LaporanBottomSheet")
        }
    }
}