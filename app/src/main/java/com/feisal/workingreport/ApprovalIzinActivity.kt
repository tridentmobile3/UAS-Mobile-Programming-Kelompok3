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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feisal.workingreport.model.PermissionRequest
import com.feisal.workingreport.repository.PermissionRepository
import com.feisal.workingreport.ui.components.DetailItem
import com.feisal.workingreport.ui.components.EmptyState
import com.feisal.workingreport.ui.components.NoiseOverlay
import com.feisal.workingreport.ui.theme.LiquidGlassBackground
import com.feisal.workingreport.ui.theme.P79Colors
import com.feisal.workingreport.ui.theme.p79Colors
import com.feisal.workingreport.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ApprovalIzinActivity : AppCompatActivity() {
    private val permissionRepository = PermissionRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val colors = p79Colors(isDark = true)
            var permissions by remember { mutableStateOf<List<PermissionRequest>>(emptyList()) }
            var isLoading by remember { mutableStateOf(true) }
            val scope = rememberCoroutineScope()

            fun loadPermissions() {
                scope.launch {
                    try {
                        val db = FirebaseFirestore.getInstance()
                        val snapshot = db.collection(Constants.PERMISSIONS_COLLECTION).get().await()
                        permissions = snapshot.toObjects(PermissionRequest::class.java)
                            .sortedByDescending { it.createdAt }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        isLoading = false
                    }
                }
            }

            LaunchedEffect(Unit) { loadPermissions() }

            Box(modifier = Modifier.fillMaxSize()) {
                LiquidGlassBackground(colors = colors) { }
                NoiseOverlay()

                Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { finish() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                        Text("Persetujuan Izin", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = colors.blue)
                        }
                    } else if (permissions.isEmpty()) {
                        Box(modifier = Modifier.padding(24.dp)) {
                            EmptyState(colors, Color(0xFF161D2F), "Tidak ada pengajuan izin")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(permissions) { izin ->
                                PermissionApprovalItem(izin, colors) {
                                    loadPermissions()
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
fun PermissionApprovalItem(
    izin: PermissionRequest,
    colors: P79Colors,
    onActionDone: () -> Unit
) {
    var showDetail by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF161D2F), RoundedCornerShape(16.dp))
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .clickable { showDetail = true }
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(izin.employeeName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("NIP: ${izin.employeeNip}", color = Color.Gray, fontSize = 11.sp)
            }
            Box(
                modifier = Modifier
                    .background(
                        when(izin.status.uppercase()) {
                            "APPROVED" -> colors.green.copy(alpha = 0.1f)
                            "REJECTED" -> colors.red.copy(alpha = 0.1f)
                            else -> colors.amber.copy(alpha = 0.1f)
                        }, RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    izin.status.uppercase(),
                    color = when(izin.status.uppercase()) {
                        "APPROVED" -> colors.green
                        "REJECTED" -> colors.red
                        else -> colors.amber
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(izin.type, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Text(izin.date, color = Color.Gray, fontSize = 12.sp)
    }

    if (showDetail) {
        AlertDialog(
            onDismissRequest = { showDetail = false },
            title = { Text("Tinjau Izin", color = Color.White) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    DetailItem("Karyawan", izin.employeeName, colors)
                    DetailItem("Tipe", izin.type, colors)
                    DetailItem("Tanggal", izin.date, colors)
                    DetailItem("Alasan", izin.reason, colors)
                    DetailItem("Status", izin.status, colors)
                }
            },
            confirmButton = {
                if (izin.status.uppercase() == "PENDING") {
                    Row {
                        TextButton(onClick = {
                            scope.launch {
                                val db = FirebaseFirestore.getInstance()
                                db.collection(Constants.PERMISSIONS_COLLECTION).document(izin.id)
                                    .update("status", "REJECTED").await()
                                Toast.makeText(context, "Izin Ditolak", Toast.LENGTH_SHORT).show()
                                showDetail = false
                                onActionDone()
                            }
                        }) { Text("Tolak", color = colors.red) }
                        TextButton(onClick = {
                            scope.launch {
                                val db = FirebaseFirestore.getInstance()
                                db.collection(Constants.PERMISSIONS_COLLECTION).document(izin.id)
                                    .update("status", "APPROVED").await()
                                Toast.makeText(context, "Izin Disetujui", Toast.LENGTH_SHORT).show()
                                showDetail = false
                                onActionDone()
                            }
                        }) { Text("Setuju", color = colors.green) }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDetail = false }) { Text("Tutup", color = Color.Gray) }
            },
            containerColor = Color(0xFF161D2F)
        )
    }
}
