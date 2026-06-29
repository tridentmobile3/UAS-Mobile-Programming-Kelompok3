package com.feisal.workingreport

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.feisal.workingreport.ui.components.GlassCard
import com.feisal.workingreport.ui.components.NoiseOverlay
import com.feisal.workingreport.ui.theme.LiquidGlassBackground
import com.feisal.workingreport.ui.theme.p79Colors

class ForgotPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        setContent {
            val context = LocalContext.current
            val sharedPref = remember { context.getSharedPreferences("AppPref", Context.MODE_PRIVATE) }
            val isDarkMode = sharedPref.getBoolean("isDarkMode", true)

            val colors = p79Colors(isDark = isDarkMode)
            var nip by remember { mutableStateOf("") }

            Box(modifier = Modifier.fillMaxSize()) {
                LiquidGlassBackground(colors = colors) { }
                NoiseOverlay()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(80.dp))

                    // Header: Lupa Password
                    Text(
                        text = "Lupa Password",
                        color = colors.text0,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Tujuh Sembilan",
                        style = androidx.compose.ui.text.TextStyle(
                            brush = Brush.horizontalGradient(
                                colors = listOf(colors.blue, colors.green)
                            )
                        ),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Garis aksen 4 warna
                    Row(modifier = Modifier.fillMaxWidth().height(3.dp)) {
                        Box(modifier = Modifier.weight(1f).fillMaxSize().background(colors.red))
                        Box(modifier = Modifier.weight(1f).fillMaxSize().background(colors.amber))
                        Box(modifier = Modifier.weight(1f).fillMaxSize().background(colors.green))
                        Box(modifier = Modifier.weight(1f).fillMaxSize().background(colors.blue))
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Container Utama (GlassCard)
                    GlassCard(
                        colors = colors,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Atur Ulang Kata Sandi",
                            color = colors.text0,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Masukkan NIP Anda untuk menerima tautan pemulihan kata sandi",
                            color = colors.text1,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Input Field: NIP
                        OutlinedTextField(
                            value = nip,
                            onValueChange = { nip = it },
                            label = { Text("NIP (Nomor Induk Pegawai)", color = colors.text1) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = colors.text1) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.blue,
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.text0,
                                unfocusedTextColor = colors.text0,
                                cursorColor = colors.blue,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // Tombol Atur Ulang dengan Logika Validasi
                        Button(
                            onClick = {
                                val nipClean = nip.trim()
                                if (nipClean.isEmpty()) {
                                    Toast.makeText(context, "NIP tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                                } else if (nipClean.length < 6) {
                                    Toast.makeText(context, "NIP minimal terdiri dari 6 digit angka", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Tautan pemulihan dikirim ke email NIP: $nipClean", Toast.LENGTH_LONG).show()

                                    // Kembali ke login setelah sukses
                                    val intent = Intent(context, LoginActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                    context.startActivity(intent)
                                    finish()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(listOf(colors.blue, colors.green)),
                                        RoundedCornerShape(28.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("ATUR ULANG", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Kembali ke Login
                        Text(
                            text = "Kembali ke Login",
                            color = colors.blue,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .clickable {
                                    val intent = Intent(context, LoginActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                    context.startActivity(intent)
                                    finish()
                                }
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    Text(
                        text = "UAS MOBILE PROGRAMMING · KELOMPOK 3",
                        color = colors.text1,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}