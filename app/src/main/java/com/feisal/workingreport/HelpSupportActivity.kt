package com.feisal.workingreport

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.feisal.workingreport.ui.components.GlassCard
import com.feisal.workingreport.ui.components.NoiseOverlay
import com.feisal.workingreport.ui.theme.LiquidGlassBackground
import com.feisal.workingreport.ui.theme.p79Colors

class HelpSupportActivity : ComponentActivity() {
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

            val greenColor = Color(0xFF3ECF6E)
            val redColor = Color(0xFFEF5A6F)

            var expandedFaqIndex by remember { mutableStateOf(0) }

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
                            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = colors.text0)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Bantuan & Dukungan",
                            color = colors.text0,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "PERTANYAAN UMUM",
                        color = colors.text1,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )

                    GlassCard(colors = colors, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            FaqItem(
                                question = "Kenapa absen saya gagal?",
                                answer = "Pastikan kamu berada di dalam radius kantor dan izin lokasi GPS sudah aktif.",
                                isExpanded = expandedFaqIndex == 0,
                                onClick = { expandedFaqIndex = if (expandedFaqIndex == 0) -1 else 0 },
                                colors = colors
                            )
                            Divider(color = colors.border)
                            FaqItem(
                                question = "Bagaimana cara mengajukan izin?",
                                answer = "Buka menu Home, tekan tombol Izin, isi form dan kirim ke Admin HC.",
                                isExpanded = expandedFaqIndex == 1,
                                onClick = { expandedFaqIndex = if (expandedFaqIndex == 1) -1 else 1 },
                                colors = colors
                            )
                            Divider(color = colors.border)
                            FaqItem(
                                question = "Lupa kata sandi, harus bagaimana?",
                                answer = "Gunakan tombol Lupa Password di layar login, atau hubungi Admin HC.",
                                isExpanded = expandedFaqIndex == 2,
                                onClick = { expandedFaqIndex = if (expandedFaqIndex == 2) -1 else 2 },
                                colors = colors
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "HUBUNGI KAMI",
                        color = colors.text1,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )

                    GlassCard(colors = colors, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(infoBgColor, RoundedCornerShape(12.dp))
                                    .clickable { Toast.makeText(context, "Membuka WhatsApp...", Toast.LENGTH_SHORT).show() }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(greenColor.copy(alpha = 0.14f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null, tint = greenColor, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("WhatsApp Admin HC", color = colors.text0, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("Respon dalam jam kerja", color = colors.text1, fontSize = 12.sp)
                                }
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = colors.text1)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(infoBgColor, RoundedCornerShape(12.dp))
                                    .clickable { Toast.makeText(context, "Membuka Email...", Toast.LENGTH_SHORT).show() }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(colors.blue.copy(alpha = 0.14f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = colors.blue, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Email Support", color = colors.text0, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("support@padepokan79.app", color = colors.text1, fontSize = 12.sp)
                                }
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = colors.text1)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .background(redColor.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                            .border(1.dp, redColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                            .clickable {
                                context.startActivity(Intent(context, ReportIssueActivity::class.java))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = redColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Laporkan Masalah", color = redColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }

    @Composable
    fun FaqItem(question: String, answer: String, isExpanded: Boolean, onClick: () -> Unit, colors: com.feisal.workingreport.ui.theme.P79Colors) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question,
                    color = colors.text0,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "+",
                    color = if (isExpanded) colors.blue else colors.text1,
                    fontSize = 20.sp,
                    modifier = Modifier.rotate(if (isExpanded) 45f else 0f)
                )
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = answer,
                    color = colors.text1,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}