package com.feisal.workingreport

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.feisal.workingreport.ui.components.GlassCard
import com.feisal.workingreport.ui.components.NoiseOverlay
import com.feisal.workingreport.ui.theme.LiquidGlassBackground
import com.feisal.workingreport.ui.theme.p79Colors

class ChangePasswordActivity : ComponentActivity() {
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

            val redColor = Color(0xFFEF5A6F)
            val amberColor = Color(0xFFF0B429)
            val greenColor = Color(0xFF3ECF6E)
            val barEmptyColor = Color.White.copy(alpha = 0.08f)

            var oldPassword by remember { mutableStateOf("") }
            var newPassword by remember { mutableStateOf("") }
            var confirmPassword by remember { mutableStateOf("") }

            var isOldPasswordVisible by remember { mutableStateOf(false) }
            var isNewPasswordVisible by remember { mutableStateOf(false) }
            var isConfirmPasswordVisible by remember { mutableStateOf(false) }

            var strengthScore by remember { mutableStateOf(0) }
            var strengthText by remember { mutableStateOf("") }
            var strengthColor by remember { mutableStateOf(barEmptyColor) }

            fun calculateStrength(password: String) {
                if (password.isEmpty()) {
                    strengthScore = 0
                    strengthText = ""
                    strengthColor = barEmptyColor
                    return
                }

                if (password.length < 8) {
                    strengthScore = 1
                    strengthText = "Lemah — terlalu pendek"
                    strengthColor = redColor
                    return
                }

                val hasUpper = password.any { it.isUpperCase() }
                val hasLower = password.any { it.isLowerCase() }
                val hasDigit = password.any { it.isDigit() }
                val hasSpecial = password.any { !it.isLetterOrDigit() }

                val conditionsMet = listOf(hasUpper, hasLower, hasDigit, hasSpecial).count { it }

                when {
                    conditionsMet >= 4 -> {
                        strengthScore = 3
                        strengthText = "Kuat — aman digunakan"
                        strengthColor = greenColor
                    }
                    conditionsMet >= 2 -> {
                        strengthScore = 2
                        strengthText = "Sedang — tambahkan simbol/huruf besar"
                        strengthColor = amberColor
                    }
                    else -> {
                        strengthScore = 1
                        strengthText = "Lemah — gunakan kombinasi karakter"
                        strengthColor = redColor
                    }
                }
            }

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
                            text = "Ubah Kata Sandi",
                            color = colors.text0,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    GlassCard(colors = colors, modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(amberColor.copy(alpha = 0.14f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = amberColor, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Gunakan kombinasi huruf besar, kecil, dan angka agar akun kamu lebih aman.",
                                color = colors.text1,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "PASSWORD SAAT INI",
                        color = colors.text1,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )

                    GlassCard(colors = colors, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedTextField(
                                value = oldPassword,
                                onValueChange = { oldPassword = it },
                                label = { Text("Password Lama", color = colors.text1) },
                                singleLine = true,
                                visualTransformation = if (isOldPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { isOldPasswordVisible = !isOldPasswordVisible }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isOldPasswordVisible) "Tutup" else "Lihat",
                                            color = colors.blue,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.blue,
                                    unfocusedBorderColor = colors.border,
                                    focusedTextColor = colors.text0,
                                    unfocusedTextColor = colors.text0,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "PASSWORD BARU",
                        color = colors.text1,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )

                    GlassCard(colors = colors, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = {
                                    newPassword = it
                                    calculateStrength(it)
                                },
                                label = { Text("Password Baru", color = colors.text1) },
                                singleLine = true,
                                visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { isNewPasswordVisible = !isNewPasswordVisible }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isNewPasswordVisible) "Tutup" else "Lihat",
                                            color = colors.blue,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.blue,
                                    unfocusedBorderColor = colors.border,
                                    focusedTextColor = colors.text0,
                                    unfocusedTextColor = colors.text0,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val bar1Color = if (strengthScore >= 1) strengthColor else barEmptyColor
                                val bar2Color = if (strengthScore >= 2) strengthColor else barEmptyColor
                                val bar3Color = if (strengthScore >= 3) strengthColor else barEmptyColor

                                Box(modifier = Modifier.weight(1f).height(4.dp).background(bar1Color, RoundedCornerShape(2.dp)))
                                Box(modifier = Modifier.weight(1f).height(4.dp).background(bar2Color, RoundedCornerShape(2.dp)))
                                Box(modifier = Modifier.weight(1f).height(4.dp).background(bar3Color, RoundedCornerShape(2.dp)))
                            }

                            if (strengthText.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = strengthText,
                                    color = strengthColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Konfirmasi Password Baru", color = colors.text1) },
                                singleLine = true,
                                visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { isConfirmPasswordVisible = !isConfirmPasswordVisible }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isConfirmPasswordVisible) "Tutup" else "Lihat",
                                            color = colors.blue,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.blue,
                                    unfocusedBorderColor = colors.border,
                                    focusedTextColor = colors.text0,
                                    unfocusedTextColor = colors.text0,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                                Toast.makeText(context, "Harap lengkapi semua kolom password", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (newPassword != confirmPassword) {
                                Toast.makeText(context, "Konfirmasi password baru tidak sama", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (strengthScore < 2) {
                                Toast.makeText(context, "Password baru masih terlalu lemah", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            Toast.makeText(context, "Password berhasil diperbarui", Toast.LENGTH_SHORT).show()
                            (context as android.app.Activity).finish()
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
                                    RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Perbarui Password", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}