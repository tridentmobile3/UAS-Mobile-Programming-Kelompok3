package com.feisal.workingreport

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class ProfilActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil)

        val btnBackProfil = findViewById<ImageView>(R.id.btnBackProfil)
        btnBackProfil.setOnClickListener {
            finish()
        }
    }
}