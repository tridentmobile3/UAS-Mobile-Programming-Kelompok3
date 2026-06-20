package com.feisal.workingreport

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class RiwayatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat)

        val btnBackRiwayat = findViewById<ImageView>(R.id.btnBackRiwayat)
        btnBackRiwayat.setOnClickListener {
            finish()
        }
    }
}