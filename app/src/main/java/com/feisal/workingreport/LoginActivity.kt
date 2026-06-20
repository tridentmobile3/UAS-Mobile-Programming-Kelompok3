package com.feisal.workingreport

import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Animasi (dari kode aslimu)
        val mainContainer = findViewById<LinearLayout>(R.id.mainContainer)
        val animSlideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade)
        mainContainer?.startAnimation(animSlideUp)

        // Gradient Teks (dari kode aslimu)
        applyGradientToTujuhSembilan()

        // --- MULAI LOGIKA LOGIN DISINI ---

        // Kenalkan ID dari layout XML ke variabel Kotlin
        val etEmail = findViewById<EditText>(R.id.etEmail)       // Sesuaikan ID dengan XML kamu
        val etPassword = findViewById<EditText>(R.id.etPassword) // Sesuaikan ID dengan XML kamu
        val btnLogin = findViewById<Button>(R.id.btnLogin)       // Sesuaikan ID dengan XML kamu

        btnLogin.setOnClickListener {
            // Ambil teks yang diketik user
            val emailKetik = etEmail.text.toString().trim()
            val passKetik = etPassword.text.toString().trim()

            // Cek kecocokan
            if (emailKetik == "admin@gmail.com" && passKetik == "admin123") {
                // Jika cocok, buat Intent untuk pindah ke DashboardActivity
                val pindahHalaman = Intent(this@LoginActivity, DashboardActivity::class.java)
                startActivity(pindahHalaman)

                // Gunakan finish() agar user tidak kembali ke halaman login saat menekan tombol "Back"
                finish()
            } else {
                // Jika salah, tampilkan pesan error
                Toast.makeText(this, "Email atau password salah, Mas!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun applyGradientToTujuhSembilan() {
        val tvTujuhSembilan = findViewById<TextView>(R.id.tvTujuhSembilan)
        tvTujuhSembilan.post {
            val width = tvTujuhSembilan.width.toFloat()
            if (width <= 0f) return@post

            val shader = LinearGradient(
                0f, 0f, width, 0f,
                intArrayOf(
                    Color.parseColor("#1E88E5"),
                    Color.parseColor("#26C281")
                ),
                null,
                Shader.TileMode.CLAMP
            )

            tvTujuhSembilan.paint.shader = shader
            tvTujuhSembilan.invalidate()
        }
    }
}