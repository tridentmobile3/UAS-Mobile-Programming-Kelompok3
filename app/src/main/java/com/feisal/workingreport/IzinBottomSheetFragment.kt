package com.feisal.workingreport

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IzinBottomSheetContent(onDismiss: () -> Unit) {
    val context = LocalContext.current

    val jenisIzinList = listOf("Cuti", "Sakit", "Keperluan Pribadi", "Lainnya")
    var expanded by remember { mutableStateOf(false) }
    var selectedJenisIzin by remember { mutableStateOf(jenisIzinList[0]) }

    var keterangan by remember { mutableStateOf("") }

    var currentTab by remember { mutableStateOf("FILE") }
    var linkDrive by remember { mutableStateOf("") }

    var uploadHintText by remember { mutableStateOf("Pilih dokumen PDF/DOC ") }
    var isUploadSuccess by remember { mutableStateOf(false) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }

    val pickFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            uploadHintText = "File berhasil dilampirkan! ✅"
            isUploadSuccess = true
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            uploadHintText = "Foto berhasil dilampirkan! ✅"
            isUploadSuccess = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF161D2F))
            .padding(24.dp)
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(Color(0xFF4A5568), RoundedCornerShape(2.dp))
                .align(Alignment.CenterHorizontally)
        )

        Text(
            text = "Ajukan Izin",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 24.dp, bottom = 24.dp)
        )

        Text(
            text = "Jenis Izin",
            color = Color(0xFF8B95A5),
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF222831)),
            border = BorderStroke(1.dp, Color(0xFF2D3548)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(bottom = 16.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    value = selectedJenisIzin,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color(0xFF222831))
                ) {
                    jenisIzinList.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption, color = Color.White) },
                            onClick = {
                                selectedJenisIzin = selectionOption
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Text(
            text = "Keterangan",
            color = Color(0xFF8B95A5),
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        TextField(
            value = keterangan,
            onValueChange = { keterangan = it },
            placeholder = { Text("Tulis alasan izin...", color = Color(0xFF4A5568)) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF222831),
                unfocusedContainerColor = Color(0xFF222831),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(bottom = 16.dp)
                .drawBehind {
                    drawRoundRect(
                        color = Color(0xFF4A5568),
                        style = Stroke(
                            width = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        ),
                        cornerRadius = CornerRadius(8.dp.toPx())
                    )
                }
        )

        Text(
            text = "Upload Bukti",
            color = Color(0xFF8B95A5),
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TabCard(
                text = "📄 File",
                isActive = currentTab == "FILE",
                modifier = Modifier.weight(1f).padding(end = 4.dp)
            ) {
                currentTab = "FILE"
                uploadHintText = "Pilih dokumen PDF/DOC "
                isUploadSuccess = false
                selectedFileUri = null
            }
            TabCard(
                text = "📷 Foto",
                isActive = currentTab == "FOTO",
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
            ) {
                currentTab = "FOTO"
                uploadHintText = "Pilih foto bukti dari Galeri "
                isUploadSuccess = false
                selectedFileUri = null
            }
            TabCard(
                text = "🔗 Link Drive",
                isActive = currentTab == "LINK",
                modifier = Modifier.weight(1f).padding(start = 4.dp)
            ) {
                currentTab = "LINK"
                isUploadSuccess = false
                selectedFileUri = null
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(bottom = 24.dp)
                .drawBehind {
                    drawRoundRect(
                        color = Color(0xFF4A5568),
                        style = Stroke(
                            width = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        ),
                        cornerRadius = CornerRadius(8.dp.toPx())
                    )
                }
                .clickable {
                    when (currentTab) {
                        "FILE" -> pickFileLauncher.launch("application/*")
                        "FOTO" -> pickImageLauncher.launch("image/*")
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (currentTab == "LINK") {
                TextField(
                    value = linkDrive,
                    onValueChange = { linkDrive = it },
                    placeholder = { Text("Tempel link Google Drive di sini...", color = Color(0xFF4A5568)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = buildAnnotatedString {
                        if (isUploadSuccess) {
                            withStyle(style = SpanStyle(color = Color(0xFF2ECC71))) {
                                append(uploadHintText)
                            }
                        } else {
                            withStyle(style = SpanStyle(color = Color(0xFF8B95A5))) {
                                append(uploadHintText)
                            }
                            withStyle(style = SpanStyle(color = Color(0xFF3498DB))) {
                                append("di sini")
                            }
                        }
                    },
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF3498DB), Color(0xFF2980B9))))
                .clickable {
                    if (keterangan.isEmpty()) {
                        Toast.makeText(context, "Keterangan alasan izin tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Pengajuan Izin Berhasil Dikirim!", Toast.LENGTH_LONG).show()
                        onDismiss()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Kirim Pengajuan",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TabCard(text: String, isActive: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val bgColor = if (isActive) Color(0xFF1E3A8A) else Color(0xFF222831)
    val textColor = if (isActive) Color.White else Color(0xFF8B95A5)

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, Color(0xFF2D3548)),
        modifier = modifier
            .height(40.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = 12.sp
            )
        }
    }
}