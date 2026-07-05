package com.feisal.workingreport

import android.view.LayoutInflater
import android.widget.ImageView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.content.Context
import android.content.Intent
import android.widget.LinearLayout
@Composable
fun ProfilContent(onBackClick: () -> Unit) {
    AndroidView(
        factory = { context ->
            val view = LayoutInflater.from(context).inflate(R.layout.activity_profil, null, false)
            view.setBackgroundColor(android.graphics.Color.TRANSPARENT)

            val btnBackProfil = view.findViewById<ImageView>(R.id.btnBackProfil)
            btnBackProfil?.setOnClickListener {
                onBackClick()
            }
            val layoutLogout = view.findViewById<LinearLayout>(R.id.layoutLogout)

            layoutLogout.setOnClickListener {

                val pref = context.getSharedPreferences("AppPref", Context.MODE_PRIVATE)

                pref.edit().apply {
                    putBoolean("isLoggedIn", false)
                    remove("userRole")
                    remove("userId")
                    apply()
                }

                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            }
            view
        },
        modifier = Modifier.fillMaxSize().padding(top = 48.dp)
    )
}