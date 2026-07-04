package com.feisal.workingreport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import /* ... other needed imports ... */ androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

class ChatBantuanActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            ChatBantuanScreen(onBackClick = { finish() })
        }
    }
}

@Composable
fun ChatBantuanScreen(onBackClick: () -> Unit) {
    var inputText by remember { mutableStateOf("") }
    val transition = rememberInfiniteTransition(label = "")

    val alpha1 by transition.animateFloat(initialValue = 0.25f, targetValue = 1f, animationSpec = infiniteRepeatable(animation = tween(600, delayMillis = 0, easing = LinearEasing), repeatMode = RepeatMode.Reverse), label = "")
    val alpha2 by transition.animateFloat(initialValue = 0.25f, targetValue = 1f, animationSpec = infiniteRepeatable(animation = tween(600, delayMillis = 200, easing = LinearEasing), repeatMode = RepeatMode.Reverse), label = "")
    val alpha3 by transition.animateFloat(initialValue = 0.25f, targetValue = 1f, animationSpec = infiniteRepeatable(animation = tween(600, delayMillis = 400, easing = LinearEasing), repeatMode = RepeatMode.Reverse), label = "")

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
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Brush.linearGradient(listOf(Color(0xFF3D7BFF), Color(0xFF2FD3A8))), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("HC", color = Color(0xFF04121E), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .size(11.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp)
                        .background(Color(0xFF3ECF6E), CircleShape)
                        .border(2.dp, Color(0xFF0C1120), CircleShape)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Admin HC", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("Online · biasanya balas cepat", color = Color(0xFF3ECF6E), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .background(Color(0x0AFFFFFF), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Hari ini", color = Color(0xFF6D7690), fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.78f)
                    .background(Color(0xFF182035), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp))
                    .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        buildAnnotatedString {
                            append("Halo! Kamu memilih kategori ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Absensi Gagal") }
                            append(". Boleh cerita lebih detail masalahnya?")
                        },
                        color = Color.White, fontSize = 13.sp, lineHeight = 18.sp
                    )
                    Text("02.20", color = Color(0xFF6D7690), fontSize = 9.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.78f)
                    .align(Alignment.End)
                    .background(Brush.linearGradient(listOf(Color(0xFF3D7BFF), Color(0xFF2FD3A8))), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 4.dp, bottomStart = 16.dp))
                    .padding(14.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("Absen saya kepentok \"di luar radius\" padahal saya di kantor", color = Color(0xFF04121E), fontSize = 13.sp, fontWeight = FontWeight.Bold, lineHeight = 18.sp)
                    Text("02.21", color = Color(0x9904121E), fontSize = 9.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.78f)
                    .background(Color(0xFF182035), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp))
                    .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text("Baik, laporanmu sudah diteruskan ke tim HC ✅. Sambil menunggu, coba cek GPS akurasi HP kamu di Pengaturan > Lokasi ya.", color = Color.White, fontSize = 13.sp, lineHeight = 18.sp)
                    Text("02.21", color = Color(0xFF6D7690), fontSize = 9.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .background(Color(0xFF182035), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp))
                    .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(6.dp).background(Color(0xFF6D7690).copy(alpha = alpha1), CircleShape))
                    Box(modifier = Modifier.size(6.dp).background(Color(0xFF6D7690).copy(alpha = alpha2), CircleShape))
                    Box(modifier = Modifier.size(6.dp).background(Color(0xFF6D7690).copy(alpha = alpha3), CircleShape))
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.background(Color(0x143D7BFF), RoundedCornerShape(16.dp)).border(1.dp, Color(0x663D7BFF), RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 7.dp)) {
                Text("Sudah saya cek, masih gagal", color = Color(0xFF3D7BFF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Box(modifier = Modifier.background(Color(0x143D7BFF), RoundedCornerShape(16.dp)).border(1.dp, Color(0x663D7BFF), RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 7.dp)) {
                Text("Kirim screenshot", color = Color(0xFF3D7BFF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = inputText,
                onValueChange = { inputText = it },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF182035), RoundedCornerShape(22.dp))
                            .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(22.dp))
                            .padding(horizontal = 16.dp, vertical = 11.dp)
                    ) {
                        if (inputText.isEmpty()) Text("Tulis pesan...", color = Color(0xFF6D7690), fontSize = 13.sp)
                        innerTextField()
                    }
                }
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Brush.linearGradient(listOf(Color(0xFF3D7BFF), Color(0xFF2FD3A8))), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF04121E), modifier = Modifier.size(18.dp))
            }
        }
    }
}