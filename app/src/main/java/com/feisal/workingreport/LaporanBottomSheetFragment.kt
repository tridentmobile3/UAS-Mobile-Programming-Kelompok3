package com.feisal.workingreport

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun LaporanBottomSheetContent(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    var tanggal by remember { mutableStateOf("") }
    var judul by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    var jamMulai by remember { mutableStateOf("") }
    var jamSelesai by remember { mutableStateOf("") }
    var currentTab by remember { mutableStateOf("FILE") }
    var linkDrive by remember { mutableStateOf("") }
    var uploadHintPrefix by remember { mutableStateOf("Tarik file/screenshot di sini atau ") }
    var uploadHintAction by remember { mutableStateOf("pilih file") }
    var isUploadSuccess by remember { mutableStateOf(false) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }

    val pickFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            uploadHintPrefix = "Lampiran berhasil ditambahkan! ✅"
            isUploadSuccess = true
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            uploadHintPrefix = "Screenshot berhasil ditambahkan! ✅"
            isUploadSuccess = true
        }
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val formatTanggal = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
            tanggal = formatTanggal.format(calendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerMulai = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            jamMulai = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    val timePickerSelesai = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            jamSelesai = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

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
            text = "Buat Laporan Kerja",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 24.dp, bottom = 24.dp)
        )

        Text(text = "Tanggal", color = Color(0xFF8B95A5), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        CustomOutlinedTextField(
            value = tanggal,
            onValueChange = {},
            hint = "Pilih tanggal",
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(bottom = 16.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { datePickerDialog.show() }
        )

        Text(text = "Judul Aktivitas", color = Color(0xFF8B95A5), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        CustomOutlinedTextField(
            value = judul,
            onValueChange = { judul = it },
            hint = "cth. Maintenance Server Produksi",
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(bottom = 16.dp)
        )

        Text(text = "Deskripsi Pekerjaan", color = Color(0xFF8B95A5), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        CustomOutlinedTextField(
            value = deskripsi,
            onValueChange = { deskripsi = it },
            hint = "Ceritakan apa yang dikerjakan hari ini...",
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(bottom = 16.dp)
        )

        Text(text = "Durasi Kerja", color = Color(0xFF8B95A5), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CustomOutlinedTextField(
                value = jamMulai,
                onValueChange = {},
                hint = "Jam mulai",
                readOnly = true,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .padding(end = 8.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { timePickerMulai.show() }
            )
            CustomOutlinedTextField(
                value = jamSelesai,
                onValueChange = {},
                hint = "Jam selesai",
                readOnly = true,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .padding(start = 8.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { timePickerSelesai.show() }
            )
        }

        Text(text = "Lampiran (opsional)", color = Color(0xFF8B95A5), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TabCardLaporan(text = "📄 File", isActive = currentTab == "FILE", modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                currentTab = "FILE"
                uploadHintPrefix = "Tarik file/screenshot di sini atau "
                uploadHintAction = "pilih file"
                isUploadSuccess = false
                selectedFileUri = null
            }
            TabCardLaporan(text = "📷 Foto", isActive = currentTab == "FOTO", modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                currentTab = "FOTO"
                uploadHintPrefix = "Pilih foto dari Galeri "
                uploadHintAction = "di sini"
                isUploadSuccess = false
                selectedFileUri = null
            }
            TabCardLaporan(text = "🔗 Link Drive", isActive = currentTab == "LINK", modifier = Modifier.weight(1f).padding(start = 4.dp)) {
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
                        style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)),
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
                            withStyle(style = SpanStyle(color = Color(0xFF2ECC71))) { append(uploadHintPrefix) }
                        } else {
                            withStyle(style = SpanStyle(color = Color(0xFF8B95A5))) { append(uploadHintPrefix) }
                            withStyle(style = SpanStyle(color = Color(0xFF3498DB))) { append(uploadHintAction) }
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
                    if (judul.isEmpty() || deskripsi.isEmpty()) {
                        Toast.makeText(context, "Judul dan Deskripsi harus diisi!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Laporan Kerja Terkirim!", Toast.LENGTH_LONG).show()
                        onDismiss()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Kirim Laporan", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    textAlign: TextAlign = TextAlign.Start
) {
    Box(
        modifier = modifier
            .background(Color.Transparent, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF4A5568), RoundedCornerShape(8.dp)),
        contentAlignment = if (textAlign == TextAlign.Center) Alignment.Center else Alignment.TopStart
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = readOnly,
            placeholder = {
                Text(
                    text = hint,
                    color = Color(0xFF4A5568),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = textAlign
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            enabled = !readOnly,
            textStyle = androidx.compose.ui.text.TextStyle(textAlign = textAlign, color = Color.White),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun TabCardLaporan(text: String, isActive: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
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
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = text, color = textColor, fontSize = 12.sp)
        }
    }
}