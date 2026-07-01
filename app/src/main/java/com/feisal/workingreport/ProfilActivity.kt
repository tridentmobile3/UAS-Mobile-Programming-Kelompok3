package com.feisal.workingreport

import android.view.LayoutInflater
import android.widget.ImageView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

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
            view
        },
        modifier = Modifier.fillMaxSize().padding(top = 48.dp)
    )
}