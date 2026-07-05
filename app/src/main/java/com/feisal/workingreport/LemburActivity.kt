package com.feisal.workingreport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feisal.workingreport.ui.theme.P79Colors
import com.feisal.workingreport.ui.theme.p79Colors

class LemburActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val colors = p79Colors(isDark = true)
            Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0B101E)) {
                LemburContent(
                    colors = colors,
                    isDarkMode = true,
                    onBackClick = { finish() },
                    onAjukanClick = { }
                )
            }
        }
    }
}

@Composable
fun LemburContent(colors: P79Colors, isDarkMode: Boolean, onBackClick: () -> Unit, onAjukanClick: () -> Unit) {
    val cardBgColor = if (isDarkMode) Color(0xFF161D2F) else Color.White

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(48.dp))

        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ArrowBack, "Back", tint = colors.text0, modifier = Modifier.size(24.dp).clickable { onBackClick() })
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Lembur", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Pengajuan & riwayat jam lembur kamu", color = colors.text1, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Stats
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(modifier = Modifier.weight(1f), bg = cardBgColor, border = colors.border, title = "9.5j", subtitle = "BULAN INI", iconTint = colors.red, textColor = colors.text0)
            StatCard(modifier = Modifier.weight(1f), bg = cardBgColor, border = colors.border, title = "2", subtitle = "DISETUJUI", iconTint = colors.green, textColor = colors.text0)
            StatCard(modifier = Modifier.weight(1f), bg = cardBgColor, border = colors.border, title = "1", subtitle = "MENUNGGU", iconTint = colors.amber, textColor = colors.text0)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onAjukanClick,
            modifier = Modifier.fillMaxWidth().height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.blue)
        ) { Text("Ajukan Lembur") }

        Spacer(modifier = Modifier.height(24.dp))
        EmptyState(colors = colors, cardBgColor = cardBgColor, message = "Tidak ada riwayat lembur")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LemburBottomSheetContent(colors: P79Colors, isDarkMode: Boolean) {
    val inputBgColor = if (isDarkMode) Color(0xFF222831) else Color(0xFFF3F4F6)

    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Text("Ajukan Lembur", color = colors.text0, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        Text("Tanggal", color = colors.text1, fontSize = 12.sp)
        Box(modifier = Modifier.fillMaxWidth().background(inputBgColor, RoundedCornerShape(12.dp)).padding(16.dp)) {
            Text("Pilih tanggal lembur", color = colors.text1)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Jam Mulai", color = colors.text1, fontSize = 12.sp)
                Box(modifier = Modifier.fillMaxWidth().background(inputBgColor, RoundedCornerShape(12.dp)).padding(16.dp)) { Text("18:00", color = colors.text0) }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Jam Selesai", color = colors.text1, fontSize = 12.sp)
                Box(modifier = Modifier.fillMaxWidth().background(inputBgColor, RoundedCornerShape(12.dp)).padding(16.dp)) { Text("21:00", color = colors.text0) }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Alasan Lembur", color = colors.text1, fontSize = 12.sp)
        OutlinedTextField(
            value = "", onValueChange = {},
            placeholder = { Text("Jelaskan alasan...", color = colors.text1) },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            textStyle = androidx.compose.ui.text.TextStyle(color = colors.text0),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = inputBgColor, focusedContainerColor = inputBgColor)
        )

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {}, modifier = Modifier.fillMaxWidth().height(55.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.blue)) {
            Text("Kirim Pengajuan", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
