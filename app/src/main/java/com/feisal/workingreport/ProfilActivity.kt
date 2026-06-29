package com.feisal.workingreport

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import com.feisal.workingreport.ui.components.NoiseOverlay
import com.feisal.workingreport.ui.theme.LiquidGlassBackground
import com.feisal.workingreport.ui.theme.p79Colors
import com.google.android.material.card.MaterialCardView

class ProfilActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        // Mengambil data role yang dikirimkan melalui Intent dari Dashboard (default: karyawan)
        val userRole = intent.getStringExtra("EXTRA_ROLE") ?: "karyawan"

        setContent {
            val context = LocalContext.current
            val sharedPref = remember { context.getSharedPreferences("AppPref", Context.MODE_PRIVATE) }
            val isDarkMode = sharedPref.getBoolean("isDarkMode", true)
            val colors = p79Colors(isDark = isDarkMode)

            Box(modifier = Modifier.fillMaxSize()) {
                LiquidGlassBackground(colors = colors) { }
                NoiseOverlay()

                ProfilContent(
                    role = userRole,
                    onBackClick = {
                        finish() // Menutup halaman profil dan kembali ke dashboard sebelumnya
                    },
                    onLogoutClick = {
                        Toast.makeText(context, "Berhasil Keluar Akun", Toast.LENGTH_SHORT).show()
                        // Mengarahkan kembali ke LoginActivity dan menghapus semua tumpukan halaman
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun ProfilContent(
    role: String,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    AndroidView(
        factory = { context ->
            val view = LayoutInflater.from(context).inflate(R.layout.activity_profil, null, false)
            view.setBackgroundColor(android.graphics.Color.TRANSPARENT)

            // Inisialisasi Komponen UI XML
            val btnBackProfil = view.findViewById<ImageView>(R.id.btnBackProfil)
            val tvNamaUser = view.findViewById<TextView>(R.id.tvNamaUser) // *Pastikan menambahkan ID ini di XML atau sesuaikan
            val tvJabatanUser = view.findViewById<TextView>(R.id.tvJabatanUser) // *Pastikan menambahkan ID ini di XML atau sesuaikan
            val tvInisialAvatar = view.findViewById<TextView>(R.id.tvInisialAvatar) // *Pastikan menambahkan ID ini di XML atau sesuaikan
            val cardKeluar = view.findViewById<MaterialCardView>(R.id.cardKeluar) // *Pastikan menambahkan ID ini di XML atau sesuaikan

            // --- LOGIKA BERDASARKAN ROLE ---
            if (role.lowercase() == "admin") {
                // Konfigurasi Tampilan untuk Admin
                view.findViewById<TextView>(R.id.tvNamaHeader)?.text = "Halo, Admin 🛠️"
                tvNamaUser?.text = "Feisal S. (Admin)"
                tvJabatanUser?.text = "Human Resource · Padepokan 79"
                tvInisialAvatar?.text = "A"
            } else {
                // Konfigurasi Tampilan untuk Karyawan Regular
                view.findViewById<TextView>(R.id.tvNamaHeader)?.text = "Halo, Feisal 👋"
                tvNamaUser?.text = "ah masa"
                tvJabatanUser?.text = "IT Support · Padepokan 79"
                tvInisialAvatar?.text = "F"
            }

            // Logika Aksi Klik Tombol Kembali
            btnBackProfil?.setOnClickListener {
                onBackClick()
            }

            // Logika Aksi Klik Tombol Keluar
            cardKeluar?.setOnClickListener {
                onLogoutClick()
            }

            view
        },
        modifier = Modifier.fillMaxSize().padding(top = 48.dp)
    )
}