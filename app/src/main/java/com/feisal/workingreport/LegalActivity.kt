package com.feisal.workingreport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

class LegalActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val type = intent.getStringExtra("TYPE") ?: "PRIVACY"

        setContent {
            when (type) {
                "PRIVACY" -> KebijakanPrivasiScreen(onBackClick = { finish() })
                "TERMS" -> SyaratKetentuanScreen(onBackClick = { finish() })
                "LICENSE" -> LisensiOpenSourceScreen(onBackClick = { finish() })
            }
        }
    }
}

@Composable
fun LegalScreenTemplate(title: String, onBackClick: () -> Unit, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B101E))
            .padding(top = 48.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(Color(0xFF161D2F), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF2D3548), RoundedCornerShape(10.dp))
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            content()
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun KebijakanPrivasiScreen(onBackClick: () -> Unit) {
    LegalScreenTemplate(title = "Kebijakan Privasi", onBackClick = onBackClick) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Berlaku sejak 1 Jul 2026", color = Color(0xFF6D7690), fontSize = 11.sp)
            Text("v1.0", color = Color(0xFF6D7690), fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.height(14.dp))

        val sections = listOf(
            "1. Data yang Kami Kumpulkan" to "Aplikasi SaptaNawa mengumpulkan data berikut untuk keperluan operasional absensi & laporan kerja karyawan Padepokan 79:\n• Data akun: NIP, nama, email, departemen, posisi (Firebase Authentication)\n• Data absensi: foto selfie, waktu, dan titik lokasi GPS (Firestore & Cloud Storage)\n• Data laporan kerja dan pengajuan izin",
            "2. Penggunaan Lokasi" to "Izin lokasi digunakan khusus saat proses absensi berlangsung, untuk memverifikasi kamu berada dalam radius kantor terdaftar. Lokasi tidak dilacak di luar sesi absensi.",
            "3. Penyimpanan & Keamanan" to "Seluruh data disimpan pada Firebase (Google Cloud) dengan aturan akses (Firestore/Storage Rules) yang membatasi data hanya bisa dibaca oleh pemilik akun dan Admin HC.",
            "4. Berbagi Data" to "Data tidak dibagikan ke pihak ketiga mana pun di luar keperluan internal Padepokan 79.",
            "5. Hak Pengguna" to "Kamu dapat meminta koreksi atau penghapusan data pribadi dengan menghubungi Admin HC melalui menu Bantuan & Dukungan."
        )

        sections.forEach { (header, body) ->
            Text(header, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(body, color = Color(0xFFAAB3C8), fontSize = 12.sp, lineHeight = 20.sp)
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
fun SyaratKetentuanScreen(onBackClick: () -> Unit) {
    LegalScreenTemplate(title = "Syarat & Ketentuan", onBackClick = onBackClick) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Berlaku sejak 1 Jul 2026", color = Color(0xFF6D7690), fontSize = 11.sp)
            Text("v1.0", color = Color(0xFF6D7690), fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.height(14.dp))

        val sections = listOf(
            "1. Penerimaan Ketentuan" to "Dengan masuk menggunakan NIP terdaftar, kamu setuju menggunakan SaptaNawa sesuai ketentuan internal Padepokan 79.",
            "2. Akun Pengguna" to "• Satu akun hanya untuk satu karyawan aktif (status ACTIVE)\n• Dilarang meminjamkan akun untuk absen orang lain (titip absen)\n• Password wajib dijaga kerahasiaannya",
            "3. Ketentuan Absensi" to "Absensi sah hanya bila dilakukan dalam radius lokasi kantor terdaftar dan menggunakan foto real-time (bukan dari galeri).",
            "4. Pengajuan Izin & Laporan" to "Pengajuan izin dan laporan kerja yang dikirim melalui aplikasi akan diverifikasi oleh Admin HC dan menjadi dasar rekap kehadiran resmi.",
            "5. Perubahan Ketentuan" to "Padepokan 79 berhak memperbarui ketentuan ini sewaktu-waktu, pemberitahuan akan disampaikan melalui aplikasi."
        )

        sections.forEach { (header, body) ->
            Text(header, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(body, color = Color(0xFFAAB3C8), fontSize = 12.sp, lineHeight = 20.sp)
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
fun LisensiOpenSourceScreen(onBackClick: () -> Unit) {
    LegalScreenTemplate(title = "Lisensi Open Source", onBackClick = onBackClick) {
        Text("SaptaNawa dibangun menggunakan pustaka open source berikut:", color = Color(0xFF6D7690), fontSize = 11.sp)
        Spacer(modifier = Modifier.height(16.dp))

        val deps = mapOf(
            "GOOGLE / FIREBASE" to listOf(
                "Firebase Authentication" to "com.google.firebase:firebase-auth",
                "Cloud Firestore" to "com.google.firebase:firebase-firestore",
                "Firebase Storage" to "com.google.firebase:firebase-storage"
            ),
            "ANDROIDX" to listOf(
                "Core KTX" to "androidx.core:core-ktx",
                "AppCompat" to "androidx.appcompat",
                "Material Components" to "com.google.android.material",
                "ConstraintLayout" to "androidx.constraintlayout",
                "CameraX (core, camera2, view)" to "androidx.camera:1.3.0-rc01",
                "Compose Foundation / UI / Material3" to "androidx.compose · 2024.06 BOM"
            ),
            "KOTLIN" to listOf(
                "Kotlinx Coroutines Play Services" to "org.jetbrains.kotlinx:1.10.2"
            )
        )

        deps.forEach { (category, libraries) ->
            Text(category, color = Color(0xFF6D7690), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141B2C), RoundedCornerShape(18.dp))
                    .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(18.dp))
            ) {
                libraries.forEachIndexed { index, (name, version) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(version, color = Color(0xFF6D7690), fontSize = 10.sp)
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0x243ECF6E), RoundedCornerShape(20.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Apache-2.0", color = Color(0xFF3ECF6E), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (index < libraries.size - 1) {
                        HorizontalDivider(color = Color(0x1FFFFFFF))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        Text("Ketuk salah satu item untuk melihat teks lisensi lengkap.", color = Color(0xFF6D7690), fontSize = 11.sp)
    }
}