package com.feisal.workingreport

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.feisal.workingreport.repository.AuthRepository
import com.feisal.workingreport.ui.components.GlassCard
import com.feisal.workingreport.ui.components.NoiseOverlay
import com.feisal.workingreport.ui.theme.LiquidGlassBackground
import com.feisal.workingreport.ui.theme.p79Colors
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {
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

            var nip by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }

            val authRepository = remember { try { AuthRepository() } catch (e: Exception) { null } }

            Box(modifier = Modifier.fillMaxSize()) {
                LiquidGlassBackground(colors = colors) { }
                NoiseOverlay()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(64.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(colors.blue, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "WELCOME BACK",
                            color = colors.text1,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Padepokan",
                        color = colors.text0,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Tujuh Sembilan",
                        style = TextStyle(
                            brush = Brush.horizontalGradient(
                                colors = listOf(colors.blue, colors.green)
                            )
                        ),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth().height(3.dp)) {
                        Box(modifier = Modifier.weight(1f).fillMaxSize().background(colors.red))
                        Box(modifier = Modifier.weight(1f).fillMaxSize().background(colors.amber))
                        Box(modifier = Modifier.weight(1f).fillMaxSize().background(colors.green))
                        Box(modifier = Modifier.weight(1f).fillMaxSize().background(colors.blue))
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    GlassCard(
                        colors = colors,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(cardBgColor, RoundedCornerShape(12.dp))
                                    .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo_padepokan79_icon),
                                    contentDescription = "Logo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Always Improving You", color = colors.text1, fontSize = 10.sp)
                                Text(text = "Padepokan 79", color = colors.text0, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Sign In",
                            color = colors.text0,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = nip,
                            onValueChange = { nip = it },
                            label = { Text("NIP (Nomor Induk Pegawai)", color = colors.text1) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = colors.text1) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = androidx.compose.ui.text.input.ImeAction.Next
                            ),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.blue,
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.text0,
                                unfocusedTextColor = colors.text0,
                                cursorColor = colors.blue,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password", color = colors.text1) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = colors.text1) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = androidx.compose.ui.text.input.ImeAction.Done
                            ),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.blue,
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.text0,
                                unfocusedTextColor = colors.text0,
                                cursorColor = colors.blue,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Forgot Password?",
                            color = colors.blue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.End)
                                .clickable { }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (nip == "12345678" && password == "12345678") {
                                    Toast.makeText(this@LoginActivity, "Login Karyawan (Dummy) Berhasil", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                                    finish()
                                    return@Button
                                } else if (nip == "1234" && password == "hc1234") {
                                    Toast.makeText(this@LoginActivity, "Login HC (Dummy) Berhasil", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this@LoginActivity, DashboardAdminActivity::class.java))
                                    finish()
                                    return@Button
                                }

                                lifecycleScope.launch {
                                    if (authRepository == null) {
                                        Toast.makeText(this@LoginActivity, "Firebase tidak terinisialisasi", Toast.LENGTH_SHORT).show()
                                        return@launch
                                    }
                                    val result = authRepository.loginWithNip(nip, password)

                                    result.onSuccess { user ->
                                        if (user.role == "HC") {
                                            startActivity(Intent(this@LoginActivity, DashboardAdminActivity::class.java))
                                        } else {
                                            startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                                        }
                                        finish()
                                    }.onFailure {
                                        Toast.makeText(
                                            this@LoginActivity,
                                            it.message ?: "Login gagal",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
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
                                        RoundedCornerShape(28.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("LOGIN", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Divider(color = colors.border, modifier = Modifier.weight(1f).padding(horizontal = 16.dp))
                            Text("OR CONTINUE WITH", color = colors.text1, fontSize = 10.sp)
                            Divider(color = colors.border, modifier = Modifier.weight(1f).padding(horizontal = 16.dp))
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedButton(
                            onClick = { },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent),
                            border = BorderStroke(1.dp, colors.border),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_google),
                                    contentDescription = "Google",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Continue with Google", color = colors.text0, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    Text(
                        text = "UAS MOBILE PROGRAMMING · KELOMPOK 3",
                        color = colors.text1,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}