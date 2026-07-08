package com.feisal.workingreport

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feisal.workingreport.model.Notification
import com.feisal.workingreport.repository.AuthRepository
import com.feisal.workingreport.repository.NotificationRepository
import com.feisal.workingreport.ui.components.EmptyState
import com.feisal.workingreport.ui.components.NoiseOverlay
import com.feisal.workingreport.ui.theme.LiquidGlassBackground
import com.feisal.workingreport.ui.theme.P79Colors
import com.feisal.workingreport.ui.theme.p79Colors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NotificationActivity : AppCompatActivity() {
    private val notificationRepository = NotificationRepository()
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val colors = p79Colors(isDark = true)
            var notifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
            var isLoading by remember { mutableStateOf(true) }
            val scope = rememberCoroutineScope()
            val userId = authRepository.getCurrentUserId() ?: ""

            fun loadNotifications() {
                scope.launch {
                    if (userId.isNotEmpty()) {
                        notifications = notificationRepository.getMyNotifications(userId)
                    }
                    isLoading = false
                }
            }

            LaunchedEffect(Unit) { loadNotifications() }

            Box(modifier = Modifier.fillMaxSize()) {
                LiquidGlassBackground(colors = colors) { }
                NoiseOverlay()

                Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { finish() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                            }
                            Text("Notifikasi", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        if (notifications.any { !it.read }) {
                            TextButton(onClick = {
                                scope.launch {
                                    notificationRepository.markAllAsRead(userId)
                                    loadNotifications()
                                }
                            }) {
                                Text("Tandai semua dibaca", color = colors.blue, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = colors.blue)
                        }
                    } else if (notifications.isEmpty()) {
                        Box(modifier = Modifier.padding(24.dp)) {
                            EmptyState(colors, Color(0xFF161D2F), "Belum ada notifikasi")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(notifications) { notification ->
                                NotificationItem(notification, colors) {
                                    scope.launch {
                                        notificationRepository.markAsRead(notification.id)
                                        loadNotifications()
                                    }
                                }
                            }
                            item { Spacer(modifier = Modifier.height(100.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification, colors: P79Colors, onClick: () -> Unit) {
    val sdf = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val dateStr = sdf.format(Date(notification.createdAt))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (notification.read) Color(0xFF161D2F).copy(alpha = 0.5f) else Color(0xFF161D2F),
                RoundedCornerShape(16.dp)
            )
            .border(
                1.dp,
                if (notification.read) colors.border.copy(alpha = 0.5f) else colors.border,
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = notification.title,
                color = if (notification.read) Color.Gray else Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            if (!notification.read) {
                Box(modifier = Modifier.size(8.dp).background(Color.Red, CircleShape))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = notification.message,
            color = if (notification.read) Color.Gray.copy(alpha = 0.7f) else Color.Gray,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = dateStr,
            color = colors.blue.copy(alpha = 0.7f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
