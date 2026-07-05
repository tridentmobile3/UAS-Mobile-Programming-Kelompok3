package com.feisal.workingreport

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

class RiwayatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0B101E)) {
                RiwayatContent(onBackClick = { finish() })
            }
        }
    }
}

@Composable
fun RiwayatContent(onBackClick: () -> Unit) {
    AndroidView(
        factory = { context ->
            val view = LayoutInflater.from(context).inflate(R.layout.activity_riwayat, null, false)
            view.setBackgroundColor(android.graphics.Color.TRANSPARENT)

            val btnBackRiwayat = view.findViewById<ImageView>(R.id.btnBackRiwayat)
            btnBackRiwayat?.setOnClickListener {
                onBackClick()
            }
            view
        },
        modifier = Modifier.fillMaxSize().padding(top = 48.dp)
    )
}
