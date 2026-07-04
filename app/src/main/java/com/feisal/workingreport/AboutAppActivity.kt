package com.feisal.workingreport

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.feisal.workingreport.ui.components.GlassCard
import com.feisal.workingreport.ui.components.NoiseOverlay
import com.feisal.workingreport.ui.theme.LiquidGlassBackground
import com.feisal.workingreport.ui.theme.p79Colors

class AboutAppActivity : ComponentActivity() {
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

            val cardBgColor = if (isDarkMode) Color(0xFF161D2F) else Color.White
            val infoBgColor = if (isDarkMode) Color(0xFF222831) else Color(0xFFF3F4F6)

            Box(modifier = Modifier.fillMaxSize()) {
                LiquidGlassBackground(colors = colors) { }
                NoiseOverlay()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(56.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(cardBgColor, RoundedCornerShape(12.dp))
                                .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { finish() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.text0)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Tentang Aplikasi",
                            color = colors.text0,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(74.dp)
                                .background(Brush.linearGradient(listOf(colors.blue, colors.green)), RoundedCornerShape(22.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("P79", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("SaptaWork", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Versi 1.0.0 (build 1)", color = colors.text1, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Aplikasi absensi & laporan kerja internal untuk karyawan Padepokan 79. Absen, ajukan izin, dan kirim laporan kerja langsung dari satu tempat.",
                            color = colors.text1,
                            fontSize = 13.sp,
                            lineHeight = 22.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "DIKEMBANGKAN OLEH",
                        color = colors.text1,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )

                    GlassCard(colors = colors, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            TeamItem("DI", "Dafa Irsyad Nasrullah", "Backend", colors, infoBgColor)
                            Divider(color = colors.border)
                            TeamItem("DR", "Diky Raihan Subagja", "UI / Frontend", colors, infoBgColor)
                            Divider(color = colors.border)
                            TeamItem("FR", "Feisal Ramdhani Riyadi", "UI / Frontend", colors, infoBgColor)
                            Divider(color = colors.border)
                            TeamItem("AR", "Muhamad Arga Reksapati", "Backend", colors, infoBgColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "LAINNYA",
                        color = colors.text1,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )

                    GlassCard(colors = colors, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            LinkItem("Kebijakan Privasi", colors) {
                                val intent = Intent(context, LegalActivity::class.java).apply { putExtra("TYPE", "PRIVACY") }
                                context.startActivity(intent)
                            }
                            Divider(color = colors.border)
                            LinkItem("Syarat & Ketentuan", colors) {
                                val intent = Intent(context, LegalActivity::class.java).apply { putExtra("TYPE", "TERMS") }
                                context.startActivity(intent)
                            }
                            Divider(color = colors.border)
                            LinkItem("Lisensi Open Source", colors) {
                                val intent = Intent(context, LegalActivity::class.java).apply { putExtra("TYPE", "LICENSE") }
                                context.startActivity(intent)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "© 2026 Padepokan 79. Tugas Besar Mobile Programming\n— TIF RP 24C CNS.",
                        color = colors.text1,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }

    @Composable
    fun TeamItem(initials: String, name: String, role: String, colors: com.feisal.workingreport.ui.theme.P79Colors, bgColor: Color) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(bgColor, CircleShape)
                    .border(1.dp, colors.border, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, color = colors.text1, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(name, color = colors.text0, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(role, color = colors.text1, fontSize = 11.sp)
        }
    }

    @Composable
    fun LinkItem(title: String, colors: com.feisal.workingreport.ui.theme.P79Colors, onClick: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = colors.text0, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = colors.text1)
        }
    }
}