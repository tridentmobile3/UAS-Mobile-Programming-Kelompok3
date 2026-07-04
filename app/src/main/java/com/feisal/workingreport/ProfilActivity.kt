package com.feisal.workingreport

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feisal.workingreport.ui.theme.p79Colors

@Composable
fun ProfilContent(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("AppPref", Context.MODE_PRIVATE) }
    val isDarkMode = sharedPref.getBoolean("isDarkMode", true)
    val colors = p79Colors(isDark = isDarkMode)

    val bgColor = Color(0xFF0B101E)
    val cardBgColor = Color(0xFF161D2F)
    val strokeColor = Color(0xFF2D3548)
    val textMuted = Color(0xFF8B95A5)
    val textWhite = Color.White

    var isDarkModeEnabled by remember { mutableStateOf(true) }
    var isNotificationEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 48.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = textWhite,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onBackClick() }
                    .padding(4.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Halo, Bro 👋", color = textWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = "SABTU, 20 JUNI 2026", color = textMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF009688), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "F", color = textWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.size(90.dp), contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.Center)
                        .background(Brush.linearGradient(listOf(colors.blue, colors.green)), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "A", color = textWhite, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(cardBgColor, CircleShape)
                        .border(1.dp, strokeColor, CircleShape)
                        .clickable { context.startActivity(Intent(context, EditProfileActivity::class.java)) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = textWhite, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "ah masa", color = textWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "IT Support · Padepokan 79", color = textMuted, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Box(
                    modifier = Modifier
                        .background(cardBgColor, RoundedCornerShape(16.dp))
                        .border(1.dp, strokeColor, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(text = "ID P79-0142", color = textMuted, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(cardBgColor, RoundedCornerShape(16.dp))
                        .border(1.dp, strokeColor, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(text = "Bergabung Jan 2024", color = textMuted, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "TAMPILAN", color = textMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBgColor, RoundedCornerShape(16.dp))
                .border(1.dp, strokeColor, RoundedCornerShape(16.dp))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
                Box(modifier = Modifier.size(36.dp).background(Color(0xFF222831), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Lock, contentDescription = "Secure", tint = Color(0xFF3498DB), modifier = Modifier.size(20.dp))
                }
                Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                    Text(text = "Mode Gelap", color = textWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Tampilan dark / light", color = textMuted, fontSize = 12.sp)
                }
                Switch(checked = isDarkModeEnabled, onCheckedChange = { isDarkModeEnabled = it })
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "AKUN", color = textMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBgColor, RoundedCornerShape(16.dp))
                .border(1.dp, strokeColor, RoundedCornerShape(16.dp))
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { context.startActivity(Intent(context, EditProfileActivity::class.java)) }
                        .padding(16.dp)
                ) {
                    Box(modifier = Modifier.size(36.dp).background(Color(0xFF222831), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF2ECC71), modifier = Modifier.size(20.dp))
                    }
                    Text(text = "Edit Profil", color = textWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(start = 16.dp))
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next", tint = textMuted, modifier = Modifier.size(16.dp))
                }
                Divider(color = strokeColor, modifier = Modifier.padding(horizontal = 16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(16.dp)
                ) {
                    Box(modifier = Modifier.size(36.dp).background(Color(0xFF222831), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Lock, contentDescription = "Password", tint = Color(0xFFF4D03F), modifier = Modifier.size(20.dp))
                    }
                    Text(text = "Ubah Kata Sandi", color = textWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(start = 16.dp))
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next", tint = textMuted, modifier = Modifier.size(16.dp))
                }
                Divider(color = strokeColor, modifier = Modifier.padding(horizontal = 16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(16.dp)
                ) {
                    Box(modifier = Modifier.size(36.dp).background(Color(0xFF222831), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = Color(0xFF3498DB), modifier = Modifier.size(20.dp))
                    }
                    Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                        Text(text = "Lokasi Kantor Terdaftar", color = textWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Kantor Pusat · radius 100m", color = textMuted, fontSize = 12.sp)
                    }
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next", tint = textMuted, modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "LAINNYA", color = textMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBgColor, RoundedCornerShape(16.dp))
                .border(1.dp, strokeColor, RoundedCornerShape(16.dp))
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Box(modifier = Modifier.size(36.dp).background(Color(0xFF222831), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notification", tint = Color(0xFFE74C3C), modifier = Modifier.size(20.dp))
                    }
                    Text(text = "Notifikasi", color = textWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(start = 16.dp))
                    Switch(checked = isNotificationEnabled, onCheckedChange = { isNotificationEnabled = it })
                }
                Divider(color = strokeColor, modifier = Modifier.padding(horizontal = 16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(16.dp)
                ) {
                    Box(modifier = Modifier.size(36.dp).background(Color(0xFF222831), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Info, contentDescription = "Help", tint = textMuted, modifier = Modifier.size(20.dp))
                    }
                    Text(text = "Bantuan & Dukungan", color = textWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(start = 16.dp))
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next", tint = textMuted, modifier = Modifier.size(16.dp))
                }
                Divider(color = strokeColor, modifier = Modifier.padding(horizontal = 16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(16.dp)
                ) {
                    Box(modifier = Modifier.size(36.dp).background(Color(0xFF222831), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Info, contentDescription = "About", tint = textMuted, modifier = Modifier.size(20.dp))
                    }
                    Text(text = "Tentang Aplikasi", color = textWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(start = 16.dp))
                    Text(text = "v1.0.0", color = textMuted, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .background(Color(0xFF1A4A0F0F), RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFE74C3C), RoundedCornerShape(16.dp))
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Logout",
                    tint = Color(0xFFE74C3C),
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer { rotationY = 180f }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Keluar", color = Color(0xFFE74C3C), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "PADEPOKAN 79 · v1.0.0",
            color = Color(0xFF4A5568),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            textAlign = TextAlign.Center
        )
    }
}