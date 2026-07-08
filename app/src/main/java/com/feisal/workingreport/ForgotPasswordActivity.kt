package com.feisal.workingreport

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.textfield.TextInputEditText

class ForgotPasswordActivity : AppCompatActivity() {

    // Deklarasi View sesuai dengan ID di XML
    private lateinit var etNipReset: TextInputEditText
    private lateinit var btnResetPassword: AppCompatButton
    private lateinit var tvBackToLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Menghubungkan dengan layout XML
        setContentView(R.layout.activity_forgot_password)

        // Inisialisasi View berdasarkan ID
        initViews()

        // Pasang Event Listener (Logika Klik)
        setupListeners()
    }

    private fun initViews() {
        etNipReset = findViewById(R.id.etNipReset)
        btnResetPassword = findViewById(R.id.btnResetPassword)
        tvBackToLogin = findViewById(R.id.tvBackToLogin)
    }

    private fun setupListeners() {
        // Logika ketika tombol "ATUR ULANG" diklik
        btnResetPassword.setOnClickListener {
            handleResetPassword()
        }

        // Logika ketika teks "Kembali ke Login" diklik
        tvBackToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Menutup activity ini agar tidak menumpuk di backstack
        }
    }

    private fun handleResetPassword() {
        val nipInput = etNipReset.text.toString().trim()

        // Validasi 1: Memastikan input NIP tidak kosong
        if (nipInput.isEmpty()) {
            etNipReset.error = "NIP tidak boleh kosong"
            etNipReset.requestFocus()
            return
        }

        // Validasi 2: Contoh validasi panjang NIP minimal (misal: minimal 9 digit)
        if (nipInput.length < 4) {
            etNipReset.error = "NIP tidak valid (minimal 9 digit)"
            etNipReset.requestFocus()
            return
        }

        // Logika Integrasi Backend / API
        sendResetLinkToServer(nipInput)
    }

    private fun sendResetLinkToServer(nip: String) {
        Toast.makeText(
            this,
            "Tautan pemulihan untuk NIP $nip telah dikirim!",
            Toast.LENGTH_LONG
        ).show()
    }
}